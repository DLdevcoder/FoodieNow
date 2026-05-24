ALTER TABLE "public"."addresses" ADD COLUMN IF NOT EXISTS "latitude" double precision;
ALTER TABLE "public"."addresses" ADD COLUMN IF NOT EXISTS "longitude" double precision;

DROP FUNCTION IF EXISTS public.process_payment(
    uuid,
    numeric,
    text,
    text,
    text,
    text,
    text,
    integer,
    jsonb,
    text
);

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

    delivery_fee := CASE WHEN v_subtotal > 100000 THEN 0 ELSE 15000 END;
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
        'PENDING',
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
        'SUCCESS',
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

    INSERT INTO public.notifications (
        user_id,
        title,
        body,
        message
    )
    VALUES (
        p_customer_id,
        'TXT_PAYMENT_SUCCESS',
        '{"type":"PAYMENT_SUCCESS", "order_id":"' || v_order_id::text || '", "earned_points":"' || earned_points::text || '"}',
        '{"type":"PAYMENT_SUCCESS", "order_id":"' || v_order_id::text || '", "earned_points":"' || earned_points::text || '"}'
    );

    order_id := v_order_id;
    payment_id := v_payment_id;

    RETURN NEXT;
END;
$$;

REVOKE ALL ON FUNCTION public.process_payment(uuid, numeric, text, text, text, text, text, integer, jsonb, text, double precision, double precision) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.process_payment(uuid, numeric, text, text, text, text, text, integer, jsonb, text, double precision, double precision) FROM anon;
REVOKE ALL ON FUNCTION public.process_payment(uuid, numeric, text, text, text, text, text, integer, jsonb, text, double precision, double precision) FROM authenticated;
GRANT ALL ON FUNCTION public.process_payment(uuid, numeric, text, text, text, text, text, integer, jsonb, text, double precision, double precision) TO service_role;
