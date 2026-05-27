CREATE TABLE IF NOT EXISTS public.financial_monitoring_logs (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id uuid NOT NULL,
    payment_method text NOT NULL,
    subtotal numeric NOT NULL,
    delivery_fee numeric NOT NULL,
    discount_amount numeric NOT NULL,
    admin_commission numeric NOT NULL,
    merchant_payout numeric NOT NULL,
    shipper_payout numeric NOT NULL,
    logged_at timestamp with time zone DEFAULT now()
);

CREATE OR REPLACE VIEW public.admin_financial_dashboard AS
SELECT 
    date_trunc('day', logged_at) AS date,
    count(DISTINCT order_id) AS total_orders,
    sum(subtotal) AS total_subtotal,
    sum(delivery_fee) AS total_delivery_fees,
    sum(admin_commission) AS total_commissions,
    sum(merchant_payout) AS total_merchant_payouts,
    sum(shipper_payout) AS total_shipper_payouts
FROM public.financial_monitoring_logs
GROUP BY 1
ORDER BY 1 DESC;

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

                v_delivery_fee := CASE WHEN v_subtotal > 100000 THEN 0 ELSE 15000 END;

                v_admin_commission := floor(v_subtotal * 0.1);
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
