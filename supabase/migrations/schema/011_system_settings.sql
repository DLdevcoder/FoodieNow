CREATE TABLE IF NOT EXISTS public.system_settings (
    key text PRIMARY KEY,
    value numeric NOT NULL
);

INSERT INTO public.system_settings (key, value) VALUES
  ('admin_commission_rate', 0.10),
  ('base_delivery_fee', 15000),
  ('free_delivery_threshold', 100000)
ON CONFLICT (key) DO NOTHING;

CREATE OR REPLACE VIEW public.admin_detailed_financial_stats AS
SELECT
  (SELECT coalesce(sum(balance), 0)::bigint FROM public.profiles) AS total_system_balance,
  (SELECT coalesce(sum(p.amount), 0)::bigint 
   FROM public.payments p 
   JOIN public.orders o ON o.id = p.order_id 
   WHERE p.status = 'SUCCESS' 
     AND p.method IN ('FOODIE_PAY', 'WALLET', 'CARD') 
     AND o.status IN ('PENDING', 'PREPARING', 'DRIVER_ASSIGNED', 'DELIVERING')
  ) AS pending_escrow_balance,
  (SELECT coalesce(sum(admin_commission), 0)::bigint FROM public.financial_monitoring_logs) AS total_commissions,
  (SELECT coalesce(sum(balance), 0)::bigint FROM public.profiles WHERE role = 'SHIPPER') AS total_shipper_balance,
  (SELECT coalesce(sum(balance), 0)::bigint FROM public.profiles WHERE role = 'MERCHANT') AS total_merchant_balance;

CREATE OR REPLACE FUNCTION public.trigger_order_status_notification()
RETURNS TRIGGER AS $$
DECLARE
    v_payment RECORD;
    v_is_refunded BOOLEAN := FALSE;
    v_desc text;
    v_subtotal numeric;
    v_delivery_fee numeric;
    v_admin_commission numeric;
    v_merchant_payout numeric;
    v_shipper_payout numeric;
    v_admin_commission_rate numeric;
    v_base_delivery_fee numeric;
    v_free_delivery_threshold numeric;
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        IF NEW.status = 'CANCELLED' THEN
            SELECT id, amount, method, status, provider
            INTO v_payment
            FROM public.payments
            WHERE order_id = NEW.id AND status = 'SUCCESS'
            LIMIT 1;

            IF v_payment.id IS NOT NULL AND v_payment.method IN ('FOODIE_PAY', 'WALLET', 'CARD') THEN
                UPDATE public.profiles
                SET balance = balance - v_payment.amount,
                    updated_at = now()
                WHERE id = 'd83d47d4-0994-4d8e-be25-1e0fcfd9b000';

                v_desc := CASE 
                    WHEN v_payment.method = 'CARD' THEN 'Hoan tra (The) don ' || substring(NEW.id::text from 1 for 8)
                    WHEN v_payment.method = 'WALLET' THEN 'Hoan tra (' || COALESCE(v_payment.provider, 'Vi') || ') don ' || substring(NEW.id::text from 1 for 8)
                    ELSE 'Hoan tra don hang ' || substring(NEW.id::text from 1 for 8)
                END;

                INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                VALUES (
                    'ESCROW-REF-' || replace(gen_random_uuid()::text, '-', ''),
                    'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
                    'WITHDRAW',
                    v_payment.amount,
                    v_desc
                );

                UPDATE public.profiles
                SET balance = balance + v_payment.amount,
                    updated_at = now()
                WHERE id = NEW.customer_id;

                INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                VALUES (
                    'REFUND-' || NEW.id::text,
                    NEW.customer_id,
                    'REFUND',
                    v_payment.amount,
                    'Hoan tien don hang ' || substring(NEW.id::text from 1 for 8)
                );

                UPDATE public.payments
                SET status = 'REFUNDED'
                WHERE id = v_payment.id;

                v_is_refunded := TRUE;
            END IF;

            INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
            VALUES (
                NEW.customer_id, 
                'TXT_ORDER_CANCELLED', 
                '{"type":"ORDER_CANCELLED", "order_id":"' || NEW.id::text || '", "is_refunded":"' || v_is_refunded::text || '"}',
                '{"type":"ORDER_CANCELLED", "order_id":"' || NEW.id::text || '", "is_refunded":"' || v_is_refunded::text || '"}',
                'push_and_tab',
                false
            );

            IF NEW.shipper_id IS NOT NULL THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.shipper_id, 
                    'TXT_ORDER_CANCELLED_SHIPPER', 
                    '{"type":"ORDER_CANCELLED_SHIPPER", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_CANCELLED_SHIPPER", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
            END IF;
        ELSE
            IF NEW.status = 'PREPARING' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_PREPARING', 
                    '{"type":"ORDER_PREPARING", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_PREPARING", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
            ELSIF NEW.status = 'DRIVER_ASSIGNED' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_DRIVER_ASSIGNED', 
                    '{"type":"ORDER_DRIVER_ASSIGNED", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_DRIVER_ASSIGNED", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
            ELSIF NEW.status = 'DELIVERING' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_DELIVERING', 
                    '{"type":"ORDER_DELIVERING", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_DELIVERING", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
            ELSIF NEW.status = 'COMPLETED' THEN
                SELECT COALESCE(SUM(quantity * price_at_time), 0)
                INTO v_subtotal
                FROM public.order_items
                WHERE order_id = NEW.id;

                SELECT value INTO v_admin_commission_rate FROM public.system_settings WHERE key = 'admin_commission_rate';
                SELECT value INTO v_base_delivery_fee FROM public.system_settings WHERE key = 'base_delivery_fee';
                SELECT value INTO v_free_delivery_threshold FROM public.system_settings WHERE key = 'free_delivery_threshold';

                v_admin_commission_rate := COALESCE(v_admin_commission_rate, 0.10);
                v_base_delivery_fee := COALESCE(v_base_delivery_fee, 15000);
                v_free_delivery_threshold := COALESCE(v_free_delivery_threshold, 100000);

                v_delivery_fee := CASE WHEN v_subtotal > v_free_delivery_threshold THEN 0 ELSE v_base_delivery_fee END;

                v_admin_commission := floor(v_subtotal * v_admin_commission_rate);
                IF v_admin_commission > (v_subtotal - NEW.discount_amount) THEN
                    v_admin_commission := GREATEST((v_subtotal - NEW.discount_amount)::numeric, 0);
                END IF;

                v_merchant_payout := GREATEST(v_subtotal - NEW.discount_amount - v_admin_commission, 0);
                v_shipper_payout := v_delivery_fee;

                SELECT id, method, status INTO v_payment
                FROM public.payments
                WHERE order_id = NEW.id
                LIMIT 1;

                IF v_payment.id IS NOT NULL THEN
                    IF v_payment.method IN ('FOODIE_PAY', 'CARD', 'WALLET') AND v_payment.status = 'SUCCESS' THEN
                        UPDATE public.profiles
                        SET balance = balance - (v_merchant_payout + v_shipper_payout),
                            updated_at = now()
                        WHERE id = 'd83d47d4-0994-4d8e-be25-1e0fcfd9b000';

                        INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                        VALUES (
                            'ESCROW-OUT-M-' || NEW.id::text,
                            'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
                            'WITHDRAW',
                            v_merchant_payout,
                            'Thanh toan Merchant don hang ' || substring(NEW.id::text from 1 for 8)
                        );

                        INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                        VALUES (
                            'ESCROW-OUT-S-' || NEW.id::text,
                            'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
                            'WITHDRAW',
                            v_shipper_payout,
                            'Thanh toan Shipper don hang ' || substring(NEW.id::text from 1 for 8)
                        );

                        UPDATE public.profiles
                        SET balance = balance + v_merchant_payout,
                            updated_at = now()
                        WHERE id = NEW.merchant_id;

                        INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                        VALUES (
                            'MERCHANT-PAY-' || NEW.id::text,
                            NEW.merchant_id,
                            'TOP_UP',
                            v_merchant_payout,
                            'Doanh thu don hang ' || substring(NEW.id::text from 1 for 8)
                        );

                        IF NEW.shipper_id IS NOT NULL THEN
                            UPDATE public.profiles
                            SET balance = balance + v_shipper_payout,
                                updated_at = now()
                            WHERE id = NEW.shipper_id;

                            INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                            VALUES (
                                'SHIPPER-PAY-' || NEW.id::text,
                                NEW.shipper_id,
                                'TOP_UP',
                                v_shipper_payout,
                                'Phi ship don hang ' || substring(NEW.id::text from 1 for 8)
                            );
                        END IF;

                        INSERT INTO public.financial_monitoring_logs (order_id, payment_method, subtotal, delivery_fee, discount_amount, admin_commission, merchant_payout, shipper_payout)
                        VALUES (NEW.id, v_payment.method, v_subtotal, v_delivery_fee, NEW.discount_amount, v_admin_commission, v_merchant_payout, v_shipper_payout);

                    ELSIF v_payment.method = 'COD' THEN
                        IF NEW.shipper_id IS NOT NULL THEN
                            UPDATE public.profiles
                            SET balance = balance - (v_merchant_payout + v_admin_commission),
                                updated_at = now()
                            WHERE id = NEW.shipper_id;

                            INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                            VALUES (
                                'SHIPPER-COD-DED-' || NEW.id::text,
                                NEW.shipper_id,
                                'WITHDRAW',
                                v_merchant_payout + v_admin_commission,
                                'Khau tru COD don hang ' || substring(NEW.id::text from 1 for 8)
                            );
                        END IF;

                        UPDATE public.profiles
                        SET balance = balance + v_merchant_payout,
                            updated_at = now()
                        WHERE id = NEW.merchant_id;

                        INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                        VALUES (
                            'MERCHANT-COD-' || NEW.id::text,
                            NEW.merchant_id,
                            'TOP_UP',
                            v_merchant_payout,
                            'Doanh thu COD don hang ' || substring(NEW.id::text from 1 for 8)
                        );

                        UPDATE public.profiles
                        SET balance = balance + v_admin_commission,
                            updated_at = now()
                        WHERE id = 'd83d47d4-0994-4d8e-be25-1e0fcfd9b000';

                        INSERT INTO public.wallet_transactions (id, user_id, type, amount, description)
                        VALUES (
                            'ADMIN-COD-COMM-' || NEW.id::text,
                            'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
                            'TOP_UP',
                            v_admin_commission,
                            'Hoa hong COD don hang ' || substring(NEW.id::text from 1 for 8)
                        );

                        INSERT INTO public.financial_monitoring_logs (order_id, payment_method, subtotal, delivery_fee, discount_amount, admin_commission, merchant_payout, shipper_payout)
                        VALUES (NEW.id, 'COD', v_subtotal, v_delivery_fee, NEW.discount_amount, v_admin_commission, v_merchant_payout, v_shipper_payout);
                    END IF;
                END IF;

                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_COMPLETED', 
                    '{"type":"ORDER_COMPLETED", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_COMPLETED", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.merchant_id, 
                    'TXT_ORDER_COMPLETED', 
                    '{"type":"ORDER_COMPLETED", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_COMPLETED", "order_id":"' || NEW.id::text || '"}',
                    'tab_only',
                    false
                );
                IF NEW.shipper_id IS NOT NULL THEN
                    INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                    VALUES (
                        NEW.shipper_id, 
                        'TXT_ORDER_COMPLETED', 
                        '{"type":"ORDER_COMPLETED", "order_id":"' || NEW.id::text || '"}',
                        '{"type":"ORDER_COMPLETED", "order_id":"' || NEW.id::text || '"}',
                        'tab_only',
                        false
                    );
                END IF;
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
