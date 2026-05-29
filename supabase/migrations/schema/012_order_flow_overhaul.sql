ALTER TABLE public.orders DROP CONSTRAINT IF EXISTS orders_status_check;

UPDATE public.orders SET status = 'WAITING_STORE_CONFIRMATION' WHERE status = 'PENDING';
UPDATE public.orders SET status = 'DELIVERING' WHERE status = 'DRIVER_ASSIGNED';
UPDATE public.orders SET status = 'CANCELLED_BY_CUSTOMER' WHERE status = 'CANCELLED';

ALTER TABLE public.orders ADD CONSTRAINT orders_status_check CHECK (status IN (
    'WAITING_PAYMENT',
    'WAITING_STORE_CONFIRMATION',
    'PREPARING',
    'WAITING_SHIPPER',
    'DELIVERING',
    'COMPLETED',
    'CANCELLED_BY_CUSTOMER',
    'CANCELLED_BY_STORE',
    'NO_SHIPPER_FOUND',
    'PAYMENT_FAILED',
    'DELIVERY_TIMEOUT'
));

ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS cancelled_by text;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS cancellation_reason text;

INSERT INTO public.system_settings (key, value) VALUES
  ('shipper_search_timeout_minutes', 30)
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;

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
    r RECORD;
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        IF NEW.status IN ('CANCELLED_BY_CUSTOMER', 'CANCELLED_BY_STORE', 'NO_SHIPPER_FOUND', 'DELIVERY_TIMEOUT') THEN
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
                '{"type":"ORDER_CANCELLED", "order_id":"' || NEW.id::text || '", "is_refunded":"' || v_is_refunded::text || '", "status":"' || NEW.status || '"}',
                '{"type":"ORDER_CANCELLED", "order_id":"' || NEW.id::text || '", "is_refunded":"' || v_is_refunded::text || '", "status":"' || NEW.status || '"}',
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
            IF NEW.status = 'WAITING_PAYMENT' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_WAITING_PAYMENT', 
                    '{"type":"ORDER_WAITING_PAYMENT", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_WAITING_PAYMENT", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
            ELSIF NEW.status = 'WAITING_STORE_CONFIRMATION' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_WAITING_STORE_CONFIRMATION', 
                    '{"type":"ORDER_WAITING_STORE_CONFIRMATION", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_WAITING_STORE_CONFIRMATION", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
            ELSIF NEW.status = 'PREPARING' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_PREPARING', 
                    '{"type":"ORDER_PREPARING", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_PREPARING", "order_id":"' || NEW.id::text || '"}',
                    'push_and_tab',
                    false
                );
            ELSIF NEW.status = 'WAITING_SHIPPER' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_WAITING_SHIPPER', 
                    '{"type":"ORDER_WAITING_SHIPPER", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_WAITING_SHIPPER", "order_id":"' || NEW.id::text || '"}',
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
            ELSIF NEW.status = 'PAYMENT_FAILED' THEN
                INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
                VALUES (
                    NEW.customer_id, 
                    'TXT_ORDER_PAYMENT_FAILED', 
                    '{"type":"ORDER_PAYMENT_FAILED", "order_id":"' || NEW.id::text || '"}',
                    '{"type":"ORDER_PAYMENT_FAILED", "order_id":"' || NEW.id::text || '"}',
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

CREATE OR REPLACE FUNCTION public.trigger_preparing_order_shipper_notification()
RETURNS TRIGGER AS $$
DECLARE
    r RECORD;
BEGIN
    IF (OLD.status IS DISTINCT FROM NEW.status OR OLD.status IS NULL) AND NEW.status = 'WAITING_SHIPPER' THEN
        FOR r IN SELECT id FROM public.profiles WHERE role = 'SHIPPER' LOOP
            INSERT INTO public.notifications (user_id, title, body, message, channel, is_read)
            VALUES (
                r.id,
                'TXT_SHIPPER_NEW_ORDER',
                '{"type":"SHIPPER_NEW_ORDER", "order_id":"' || NEW.id::text || '"}',
                '{"type":"SHIPPER_NEW_ORDER", "order_id":"' || NEW.id::text || '"}',
                'push_and_tab',
                false
            );
        END LOOP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.process_payment(
    p_customer_id uuid,
    p_amount numeric,
    p_method text,
    p_provider text DEFAULT NULL::text,
    p_transaction_id text DEFAULT NULL::text,
    p_delivery_address text DEFAULT NULL::text,
    p_note text DEFAULT NULL::text,
    p_used_reward_points integer DEFAULT 0,
    p_items jsonb DEFAULT '[]'::jsonb,
    p_voucher_code text DEFAULT NULL::text,
    p_delivery_lat double precision DEFAULT NULL::double precision,
    p_delivery_lng double precision DEFAULT NULL::double precision
) RETURNS TABLE(
    order_id uuid,
    payment_id uuid,
    amount_charged numeric,
    delivery_fee numeric,
    discount_amount numeric,
    earned_points integer,
    new_reward_points integer,
    new_balance numeric
)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO 'public'
AS $$
DECLARE
    v_items jsonb := COALESCE(p_items, '[]'::jsonb);
    v_item_count integer;
    v_store_count integer;
    v_store_id uuid;
    v_merchant_id uuid;
    v_order_id uuid;
    v_payment_id uuid;
    v_subtotal numeric;
    v_current_points integer;
    v_current_balance numeric;
    v_points_to_use integer;
    v_method text := upper(btrim(COALESCE(p_method, '')));
    v_provider text := nullif(upper(btrim(COALESCE(p_provider, ''))), '');
    v_voucher record;
    v_foodiepay_transaction_id text;
    v_total_usages integer;
    v_user_usages integer;
    v_base_delivery_fee numeric;
    v_free_delivery_threshold numeric;
    v_initial_status text;
    v_payment_status text;
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'authentication required';
    END IF;

    IF auth.uid() <> p_customer_id THEN
        RAISE EXCEPTION 'cannot process payment for another user';
    END IF;

    IF p_amount IS NULL OR p_amount < 0 THEN
        RAISE EXCEPTION 'payment amount must not be negative';
    END IF;

    IF v_method NOT IN ('COD', 'CARD', 'WALLET', 'FOODIE_PAY') THEN
        RAISE EXCEPTION 'unsupported payment method';
    END IF;

    IF p_delivery_address IS NULL OR btrim(p_delivery_address) = '' THEN
        RAISE EXCEPTION 'delivery address is required';
    END IF;

    IF jsonb_typeof(v_items) <> 'array' THEN
        RAISE EXCEPTION 'payment items must be an array';
    END IF;

    SELECT count(*)
    INTO v_item_count
    FROM jsonb_array_elements(v_items);

    IF v_item_count = 0 THEN
        RAISE EXCEPTION 'cart is empty';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(v_items) AS item
        WHERE nullif(item->>'food_id', '') IS NULL
           OR nullif(item->>'quantity', '') IS NULL
     ) THEN
        RAISE EXCEPTION 'invalid payment item';
    END IF;

    WITH raw_items AS (
        SELECT
            (item->>'food_id')::uuid AS food_id,
            (item->>'quantity')::integer AS quantity
        FROM jsonb_array_elements(v_items) AS item
    )
    SELECT count(*)
    INTO v_item_count
    FROM raw_items
    WHERE quantity <= 0;

    IF v_item_count > 0 THEN
        RAISE EXCEPTION 'payment item quantity must be positive';
    END IF;

    WITH raw_items AS (
        SELECT
            (item->>'food_id')::uuid AS food_id,
            (item->>'quantity')::integer AS quantity
        FROM jsonb_array_elements(v_items) AS item
    ),
    normalized_items AS (
        SELECT food_id, sum(quantity)::integer AS quantity
        FROM raw_items
        GROUP BY food_id
    )
    SELECT count(*)
    INTO v_item_count
    FROM normalized_items ni
    LEFT JOIN public.foods f ON f.id = ni.food_id
    WHERE f.id IS NULL;

    IF v_item_count > 0 THEN
        RAISE EXCEPTION 'food item not found';
    END IF;

    WITH raw_items AS (
        SELECT
            (item->>'food_id')::uuid AS food_id,
            (item->>'quantity')::integer AS quantity
        FROM jsonb_array_elements(v_items) AS item
    ),
    normalized_items AS (
        SELECT food_id, sum(quantity)::integer AS quantity
        FROM raw_items
        GROUP BY food_id
    )
    SELECT count(*)
    INTO v_item_count
    FROM normalized_items ni
    JOIN public.foods f ON f.id = ni.food_id
    WHERE NOT COALESCE(f.is_available, false);

    IF v_item_count > 0 THEN
        RAISE EXCEPTION 'food item is not available';
    END IF;

    WITH raw_items AS (
        SELECT
            (item->>'food_id')::uuid AS food_id,
            (item->>'quantity')::integer AS quantity
        FROM jsonb_array_elements(v_items) AS item
    ),
    normalized_items AS (
        SELECT food_id, sum(quantity)::integer AS quantity
        FROM raw_items
        GROUP BY food_id
    )
    SELECT count(DISTINCT f.store_id)
    INTO v_store_count
    FROM normalized_items ni
    JOIN public.foods f ON f.id = ni.food_id;

    IF v_store_count <> 1 THEN
        RAISE EXCEPTION 'all payment items must belong to one store';
    END IF;

    WITH raw_items AS (
        SELECT
            (item->>'food_id')::uuid AS food_id,
            (item->>'quantity')::integer AS quantity
        FROM jsonb_array_elements(v_items) AS item
    ),
    normalized_items AS (
        SELECT food_id, sum(quantity)::integer AS quantity
        FROM raw_items
        GROUP BY food_id
    )
    SELECT f.store_id, s.owner_id, sum((ni.quantity::numeric * f.price::numeric))
    INTO v_store_id, v_merchant_id, v_subtotal
    FROM normalized_items ni
    JOIN public.foods f ON f.id = ni.food_id
    JOIN public.stores s ON s.id = f.store_id
    GROUP BY f.store_id, s.owner_id;

    IF v_merchant_id IS NULL THEN
        RAISE EXCEPTION 'merchant not found';
    END IF;

    SELECT reward_points, balance
    INTO v_current_points, v_current_balance
    FROM public.profiles
    WHERE id = p_customer_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'profile not found';
    END IF;

    SELECT value INTO v_base_delivery_fee FROM public.system_settings WHERE key = 'base_delivery_fee';
    SELECT value INTO v_free_delivery_threshold FROM public.system_settings WHERE key = 'free_delivery_threshold';

    v_base_delivery_fee := COALESCE(v_base_delivery_fee, 15000);
    v_free_delivery_threshold := COALESCE(v_free_delivery_threshold, 100000);

    delivery_fee := CASE WHEN v_subtotal > v_free_delivery_threshold THEN 0 ELSE v_base_delivery_fee END;
    discount_amount := 0;

    IF p_voucher_code IS NOT NULL AND btrim(p_voucher_code) <> '' THEN
        SELECT
            v.id,
            v.discount_amount,
            v.discount_percent,
            v.max_discount,
            v.min_order_value,
            v.max_usages_per_user,
            v.total_usages_limit,
            v.starts_at
        INTO v_voucher
        FROM public.vouchers v
        WHERE upper(v.code) = upper(btrim(p_voucher_code))
          AND v.is_active
          AND (v.expires_at IS NULL OR v.expires_at > now())
          AND (v.merchant_id IS NULL OR v.merchant_id = v_merchant_id)
        LIMIT 1;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Voucher khong hop le, da het han hoac khong ap dung cho cua hang nay.';
        END IF;

        IF v_voucher.starts_at IS NOT NULL AND v_voucher.starts_at > now() THEN
            RAISE EXCEPTION 'Voucher chua den thoi gian ap dung.';
        END IF;

        IF v_voucher.total_usages_limit IS NOT NULL THEN
            SELECT count(*)
            INTO v_total_usages
            FROM public.voucher_usages
            WHERE voucher_id = v_voucher.id;

            IF v_total_usages >= v_voucher.total_usages_limit THEN
                RAISE EXCEPTION 'Voucher da dat gioi han su dung cho toan he thong.';
            END IF;
        END IF;

        IF v_voucher.max_usages_per_user IS NOT NULL THEN
            SELECT count(*)
            INTO v_user_usages
            FROM public.voucher_usages
            WHERE voucher_id = v_voucher.id
              AND customer_id = p_customer_id;

            IF v_user_usages >= v_voucher.max_usages_per_user THEN
                RAISE EXCEPTION 'Voucher da dat gioi han su dung cho tai khoan cua ban.';
            END IF;
        END IF;

        IF v_subtotal < COALESCE(v_voucher.min_order_value, 0) THEN
            RAISE EXCEPTION 'Don hang chua dat gia tri toi thieu de ap dung voucher nay.';
        END IF;

        IF COALESCE(v_voucher.discount_amount, 0) > 0 THEN
            discount_amount := v_voucher.discount_amount;
        ELSE
            discount_amount := floor(v_subtotal * COALESCE(v_voucher.discount_percent, 0) / 100);
            IF COALESCE(v_voucher.max_discount, 0) > 0 THEN
                discount_amount := LEAST(discount_amount, v_voucher.max_discount);
            END IF;
        END IF;
        discount_amount := LEAST(GREATEST(discount_amount, 0), v_subtotal);
    END IF;

    v_points_to_use := LEAST(
        GREATEST(COALESCE(p_used_reward_points, 0), 0),
        v_current_points,
        GREATEST((v_subtotal + delivery_fee - discount_amount)::integer, 0)
    );

    amount_charged := GREATEST(v_subtotal + delivery_fee - discount_amount - v_points_to_use, 0);

    IF p_amount <> amount_charged THEN
        RAISE EXCEPTION 'payment amount mismatch';
    END IF;

    IF v_method = 'WALLET' AND amount_charged > 0 AND v_provider IS NULL THEN
        RAISE EXCEPTION 'wallet provider is required';
    END IF;

    earned_points := floor(amount_charged / 100)::integer;
    new_reward_points := v_current_points - v_points_to_use + earned_points;
    new_balance := v_current_balance;

    IF v_method = 'COD' THEN
        v_initial_status := 'WAITING_STORE_CONFIRMATION';
        v_payment_status := 'SUCCESS';
    ELSE
        v_initial_status := 'WAITING_PAYMENT';
        v_payment_status := 'PENDING';
    END IF;

    IF v_method = 'FOODIE_PAY' AND amount_charged > 0 THEN
        IF v_current_balance < amount_charged THEN
            RAISE EXCEPTION 'insufficient FoodiePay balance';
        END IF;

        new_balance := v_current_balance - amount_charged;
        v_foodiepay_transaction_id := COALESCE(
            nullif(p_transaction_id, ''),
            'FPAY-' || replace(gen_random_uuid()::text, '-', '')
        );

        INSERT INTO public.wallet_transactions (
            id,
            user_id,
            type,
            amount,
            description
        )
        VALUES (
            v_foodiepay_transaction_id,
            p_customer_id,
            'PAYMENT',
            amount_charged,
            'Thanh toan don hang'
        );

        v_initial_status := 'WAITING_STORE_CONFIRMATION';
        v_payment_status := 'SUCCESS';
    ELSIF v_method IN ('WALLET', 'CARD') AND amount_charged > 0 THEN
        IF p_transaction_id IS NOT NULL AND btrim(p_transaction_id) <> '' THEN
            v_initial_status := 'WAITING_STORE_CONFIRMATION';
            v_payment_status := 'SUCCESS';
        END IF;
    END IF;

    INSERT INTO public.orders (
        customer_id,
        merchant_id,
        total_price,
        status,
        delivery_address,
        note,
        voucher_code,
        discount_amount,
        delivery_lat,
        delivery_lng
    )
    VALUES (
        p_customer_id,
        v_merchant_id,
        amount_charged,
        v_initial_status,
        p_delivery_address,
        p_note,
        p_voucher_code,
        discount_amount,
        p_delivery_lat,
        p_delivery_lng
    )
    RETURNING id INTO v_order_id;

    WITH raw_items AS (
        SELECT
            (item->>'food_id')::uuid AS food_id,
            (item->>'quantity')::integer AS quantity
        FROM jsonb_array_elements(v_items) AS item
    ),
    normalized_items AS (
        SELECT food_id, sum(quantity)::integer AS quantity
        FROM raw_items
        GROUP BY food_id
    )
    INSERT INTO public.order_items (
        order_id,
        food_id,
        quantity,
        price_at_time
    )
    SELECT
        v_order_id,
        ni.food_id,
        ni.quantity,
        f.price
    FROM normalized_items ni
    JOIN public.foods f ON f.id = ni.food_id;

    INSERT INTO public.payments (
        customer_id,
        order_id,
        amount,
        method,
        provider,
        transaction_id,
        status,
        delivery_address,
        note
    )
    VALUES (
        p_customer_id,
        v_order_id,
        amount_charged,
        v_method,
        v_provider,
        CASE WHEN v_method = 'FOODIE_PAY' THEN v_foodiepay_transaction_id ELSE p_transaction_id END,
        v_payment_status,
        p_delivery_address,
        p_note
    )
    RETURNING id INTO v_payment_id;

    IF p_voucher_code IS NOT NULL AND btrim(p_voucher_code) <> '' THEN
        INSERT INTO public.voucher_usages (
            voucher_id,
            order_id,
            customer_id,
            discount_amount
        )
        VALUES (
            v_voucher.id,
            v_order_id,
            p_customer_id,
            discount_amount
        );
    END IF;

    UPDATE public.profiles
    SET reward_points = new_reward_points,
        balance = new_balance,
        updated_at = now()
    WHERE id = p_customer_id;

    IF v_payment_status = 'SUCCESS' AND v_method IN ('FOODIE_PAY', 'CARD', 'WALLET') AND amount_charged > 0 THEN
        UPDATE public.profiles
        SET balance = balance + amount_charged,
            updated_at = now()
        WHERE id = 'd83d47d4-0994-4d8e-be25-1e0fcfd9b000';

        INSERT INTO public.wallet_transactions (
            id,
            user_id,
            type,
            amount,
            description
        )
        VALUES (
            'ESCROW-DEP-' || replace(gen_random_uuid()::text, '-', ''),
            'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
            'TOP_UP',
            amount_charged,
            'Nhan thanh toan don hang ' || v_order_id::text
        );
    END IF;

    INSERT INTO public.notifications (
        user_id,
        title,
        body,
        message,
        channel
    )
    VALUES (
        p_customer_id,
        'TXT_PAYMENT_SUCCESS',
        '{"type":"PAYMENT_SUCCESS", "order_id":"' || v_order_id::text || '", "earned_points":"' || earned_points::text || '"}',
        '{"type":"PAYMENT_SUCCESS", "order_id":"' || v_order_id::text || '", "earned_points":"' || earned_points::text || '"}',
        'tab_only'
    );

    order_id := v_order_id;
    payment_id := v_payment_id;

    RETURN NEXT;
END;
$$;

CREATE OR REPLACE FUNCTION public.handle_bank_transfer_payment_v2(p_order_id uuid, p_transaction_id text)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_amount numeric;
    v_customer_id uuid;
    v_payment_id uuid;
    v_payment_status text;
    v_payment_method text;
BEGIN
    SELECT customer_id, total_price INTO v_customer_id, v_amount
    FROM public.orders
    WHERE id = p_order_id;

    IF v_customer_id IS NULL THEN
        RAISE EXCEPTION 'order not found';
    END IF;

    SELECT id, method, status INTO v_payment_id, v_payment_method, v_payment_status
    FROM public.payments
    WHERE order_id = p_order_id
    LIMIT 1;

    IF v_payment_id IS NULL THEN
        RAISE EXCEPTION 'payment record not found';
    END IF;

    IF v_payment_status = 'SUCCESS' THEN
        RETURN;
    END IF;

    UPDATE public.payments
    SET status = 'SUCCESS',
        transaction_id = p_transaction_id,
        created_at = now()
    WHERE id = v_payment_id;

    UPDATE public.orders
    SET status = 'WAITING_STORE_CONFIRMATION',
        updated_at = now()
    WHERE id = p_order_id;

    IF v_payment_method IN ('FOODIE_PAY', 'CARD', 'WALLET') AND v_amount > 0 THEN
        UPDATE public.profiles
        SET balance = balance + v_amount,
            updated_at = now()
        WHERE id = 'd83d47d4-0994-4d8e-be25-1e0fcfd9b000';

        INSERT INTO public.wallet_transactions (
            id,
            user_id,
            type,
            amount,
            description
        )
        VALUES (
            'ESCROW-DEP-' || replace(gen_random_uuid()::text, '-', ''),
            'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
            'TOP_UP',
            v_amount,
            'Nhan thanh toan don hang ' || p_order_id::text
        );
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION public.handle_payment_failure(p_order_id uuid)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_payment_id uuid;
BEGIN
    SELECT id INTO v_payment_id
    FROM public.payments
    WHERE order_id = p_order_id
    LIMIT 1;

    IF v_payment_id IS NOT NULL THEN
        UPDATE public.payments
        SET status = 'FAILED'
        WHERE id = v_payment_id;
    END IF;

    UPDATE public.orders
    SET status = 'PAYMENT_FAILED',
        updated_at = now()
    WHERE id = p_order_id;
END;
$$;

CREATE OR REPLACE FUNCTION public.store_confirm_order(p_order_id uuid)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.orders
    SET status = 'PREPARING',
        updated_at = now()
    WHERE id = p_order_id
      AND status = 'WAITING_STORE_CONFIRMATION';
END;
$$;

CREATE OR REPLACE FUNCTION public.store_reject_order(p_order_id uuid, p_reason text)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.orders
    SET status = 'CANCELLED_BY_STORE',
        cancelled_by = 'MERCHANT',
        cancellation_reason = p_reason,
        updated_at = now()
    WHERE id = p_order_id
      AND status IN ('WAITING_STORE_CONFIRMATION', 'PREPARING');
END;
$$;

CREATE OR REPLACE FUNCTION public.store_mark_ready(p_order_id uuid)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.orders
    SET status = 'WAITING_SHIPPER',
        updated_at = now()
    WHERE id = p_order_id
      AND status = 'PREPARING';
END;
$$;

CREATE OR REPLACE FUNCTION public.shipper_accept_order(p_order_id uuid, p_shipper_id uuid)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.orders
    SET status = 'DELIVERING',
        shipper_id = p_shipper_id,
        updated_at = now()
    WHERE id = p_order_id
      AND status = 'WAITING_SHIPPER';
END;
$$;

CREATE OR REPLACE FUNCTION public.shipper_cancel_order(p_order_id uuid)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.orders
    SET status = 'WAITING_SHIPPER',
        shipper_id = NULL,
        shipper_lat = NULL,
        shipper_lng = NULL,
        updated_at = now()
    WHERE id = p_order_id
      AND status = 'DELIVERING';
END;
$$;

CREATE OR REPLACE FUNCTION public.handle_order_cancellation_v3(p_order_id uuid, p_cancelled_by text, p_reason text)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_status text;
    v_new_status text;
BEGIN
    SELECT status INTO v_status
    FROM public.orders
    WHERE id = p_order_id;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'order not found';
    END IF;

    IF p_cancelled_by = 'CUSTOMER' THEN
        IF v_status NOT IN ('WAITING_PAYMENT', 'WAITING_STORE_CONFIRMATION', 'PREPARING', 'WAITING_SHIPPER') THEN
            RAISE EXCEPTION 'customer cannot cancel order at this stage';
        END IF;
        v_new_status := 'CANCELLED_BY_CUSTOMER';
    ELSIF p_cancelled_by = 'MERCHANT' THEN
        IF v_status NOT IN ('WAITING_STORE_CONFIRMATION', 'PREPARING') THEN
            RAISE EXCEPTION 'merchant cannot cancel order at this stage';
        END IF;
        v_new_status := 'CANCELLED_BY_STORE';
    ELSE
        v_new_status := 'CANCELLED_BY_CUSTOMER';
    END IF;

    UPDATE public.orders
    SET status = v_new_status,
        cancelled_by = p_cancelled_by,
        cancellation_reason = p_reason,
        updated_at = now()
    WHERE id = p_order_id;
END;
$$;

CREATE OR REPLACE FUNCTION public.check_order_timeout_v3()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_order_timeout numeric;
    v_preparing_timeout numeric;
    v_shipper_timeout numeric;
    v_delivery_timeout numeric;
    r RECORD;
BEGIN
    SELECT value INTO v_order_timeout FROM public.system_settings WHERE key = 'order_timeout_minutes';
    SELECT value INTO v_preparing_timeout FROM public.system_settings WHERE key = 'preparing_timeout_minutes';
    SELECT value INTO v_shipper_timeout FROM public.system_settings WHERE key = 'shipper_search_timeout_minutes';
    SELECT value INTO v_delivery_timeout FROM public.system_settings WHERE key = 'delivery_timeout_minutes';

    v_order_timeout := COALESCE(v_order_timeout, 15);
    v_preparing_timeout := COALESCE(v_preparing_timeout, 30);
    v_shipper_timeout := COALESCE(v_shipper_timeout, 30);
    v_delivery_timeout := COALESCE(v_delivery_timeout, 60);

    FOR r IN 
        SELECT id, status, created_at, updated_at 
        FROM public.orders 
        WHERE status IN ('WAITING_PAYMENT', 'WAITING_STORE_CONFIRMATION', 'PREPARING', 'WAITING_SHIPPER', 'DELIVERING')
    LOOP
        IF r.status = 'WAITING_PAYMENT' AND r.created_at < (now() - (v_order_timeout || ' minutes')::interval) THEN
            PERFORM public.handle_payment_failure(r.id);
        ELSIF r.status = 'WAITING_STORE_CONFIRMATION' AND r.created_at < (now() - (v_order_timeout || ' minutes')::interval) THEN
            UPDATE public.orders
            SET status = 'CANCELLED_BY_STORE',
                cancelled_by = 'SYSTEM',
                cancellation_reason = 'Store did not confirm within timeout limit',
                updated_at = now()
            WHERE id = r.id;
        ELSIF r.status = 'PREPARING' AND r.updated_at < (now() - (v_preparing_timeout || ' minutes')::interval) THEN
            UPDATE public.orders
            SET status = 'CANCELLED_BY_STORE',
                cancelled_by = 'SYSTEM',
                cancellation_reason = 'Store preparation timed out',
                updated_at = now()
            WHERE id = r.id;
        ELSIF r.status = 'WAITING_SHIPPER' AND r.updated_at < (now() - (v_shipper_timeout || ' minutes')::interval) THEN
            UPDATE public.orders
            SET status = 'NO_SHIPPER_FOUND',
                cancelled_by = 'SYSTEM',
                cancellation_reason = 'No shipper found within timeout limit',
                updated_at = now()
            WHERE id = r.id;
        ELSIF r.status = 'DELIVERING' AND r.updated_at < (now() - (v_delivery_timeout || ' minutes')::interval) THEN
            UPDATE public.orders
            SET status = 'DELIVERY_TIMEOUT',
                cancelled_by = 'SYSTEM',
                cancellation_reason = 'Delivery timeout exceeded',
                updated_at = now()
            WHERE id = r.id;
        END IF;
    END LOOP;
END;
$$;

CREATE OR REPLACE VIEW public.admin_detailed_financial_stats AS
SELECT
  (SELECT coalesce(sum(balance), 0)::bigint FROM public.profiles) AS total_system_balance,
  (SELECT coalesce(sum(p.amount), 0)::bigint 
   FROM public.payments p 
   JOIN public.orders o ON o.id = p.order_id 
   WHERE p.status = 'SUCCESS' 
     AND p.method IN ('FOODIE_PAY', 'WALLET', 'CARD') 
     AND o.status IN ('WAITING_PAYMENT', 'WAITING_STORE_CONFIRMATION', 'PREPARING', 'WAITING_SHIPPER', 'DELIVERING')
  ) AS pending_escrow_balance,
  (SELECT coalesce(sum(admin_commission), 0)::bigint FROM public.financial_monitoring_logs) AS total_commissions,
  (SELECT coalesce(sum(balance), 0)::bigint FROM public.profiles WHERE role = 'SHIPPER') AS total_shipper_balance,
  (SELECT coalesce(sum(balance), 0)::bigint FROM public.profiles WHERE role = 'MERCHANT') AS total_merchant_balance;
