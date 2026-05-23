SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

CREATE SCHEMA IF NOT EXISTS "public";
ALTER SCHEMA "public" OWNER TO "pg_database_owner";

CREATE OR REPLACE FUNCTION "public"."set_updated_at"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    SET search_path TO ''
    AS $$
BEGIN
    NEW.updated_at = timezone('utc'::text, now());
    RETURN NEW;
END;
$$;

ALTER FUNCTION "public"."set_updated_at"() OWNER TO "postgres";

CREATE OR REPLACE FUNCTION "public"."update_store_rating"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    SET search_path TO ''
    AS $$
DECLARE
    v_target_store_id uuid;
BEGIN
    IF TG_OP = 'DELETE' THEN
        SELECT store_id INTO v_target_store_id FROM public.foods WHERE id = OLD.food_id;
    ELSE
        SELECT store_id INTO v_target_store_id FROM public.foods WHERE id = NEW.food_id;
    END IF;

    UPDATE public.stores
    SET 
        rating = (
            SELECT COALESCE(AVG(r.rating), 0)
            FROM public.reviews r
            JOIN public.foods f ON r.food_id = f.id 
            WHERE f.store_id = v_target_store_id
        ),
        review_count = (
            SELECT COUNT(r.id)
            FROM public.reviews r
            JOIN public.foods f ON r.food_id = f.id 
            WHERE f.store_id = v_target_store_id
        )
    WHERE id = v_target_store_id;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$;

ALTER FUNCTION "public"."update_store_rating"() OWNER TO "postgres";

CREATE OR REPLACE FUNCTION public.trigger_order_status_notification()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        IF NEW.status = 'PREPARING' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'TXT_ORDER_PREPARING', 
                '{"type":"ORDER_PREPARING"}',
                '{"type":"ORDER_PREPARING"}',
                false
            );
        ELSIF NEW.status = 'DELIVERING' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'TXT_ORDER_DELIVERING', 
                '{"type":"ORDER_DELIVERING"}',
                '{"type":"ORDER_DELIVERING"}',
                false
            );
        ELSIF NEW.status = 'COMPLETED' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'TXT_ORDER_COMPLETED', 
                '{"type":"ORDER_COMPLETED"}',
                '{"type":"ORDER_COMPLETED"}',
                false
            );
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.merchant_id, 
                'TXT_ORDER_COMPLETED', 
                '{"type":"ORDER_COMPLETED"}',
                '{"type":"ORDER_COMPLETED"}',
                false
            );
        ELSIF NEW.status = 'CANCELLED' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'TXT_ORDER_CANCELLED', 
                '{"type":"ORDER_CANCELLED"}',
                '{"type":"ORDER_CANCELLED"}',
                false
            );
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.trigger_new_order_notification()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.notifications (user_id, title, body, message, is_read)
    VALUES (
        NEW.merchant_id, 
        'TXT_ORDER_NEW', 
        '{"type":"NEW_ORDER", "total_price":"' || NEW.total_price::text || '"}',
        '{"type":"NEW_ORDER", "total_price":"' || NEW.total_price::text || '"}',
        false
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.trigger_new_review_notification()
RETURNS TRIGGER AS $$
DECLARE
    v_merchant_id uuid;
    v_food_name text;
BEGIN
    SELECT s.owner_id, f.name INTO v_merchant_id, v_food_name
    FROM public.foods f
    JOIN public.stores s ON f.store_id = s.id
    WHERE f.id = NEW.food_id;

    IF v_merchant_id IS NOT NULL THEN
        INSERT INTO public.notifications (user_id, title, body, message, is_read)
        VALUES (
            v_merchant_id, 
            'TXT_NEW_REVIEW', 
            '{"type":"NEW_REVIEW", "rating":"' || NEW.rating::text || '", "food_name":"' || v_food_name || '"}',
            '{"type":"NEW_REVIEW", "rating":"' || NEW.rating::text || '", "food_name":"' || v_food_name || '"}',
            false
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.trigger_wallet_transaction_notification()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.notifications (user_id, title, body, message, is_read)
    VALUES (
        NEW.user_id, 
        'TXT_WALLET_TRANSACTION', 
        '{"type":"WALLET_TRANSACTION", "description":"' || NEW.description || '"}',
        '{"type":"WALLET_TRANSACTION", "description":"' || NEW.description || '"}',
        false
    );
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
    p_voucher_code text DEFAULT NULL::text
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
        discount_amount
    )
    VALUES (
        p_customer_id,
        v_merchant_id,
        amount_charged,
        'PENDING',
        p_delivery_address,
        p_note,
        p_voucher_code,
        discount_amount
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

CREATE OR REPLACE FUNCTION get_chat_summaries(p_user_id UUID)
RETURNS TABLE (
    store_id UUID,
    partner_id UUID,
    partner_name TEXT,
    partner_avatar TEXT,
    last_message TEXT,
    last_message_time TIMESTAMP WITH TIME ZONE,
    unread_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  WITH RankedMessages AS (
      SELECT
          m.store_id,
          CASE
              WHEN m.sender_id = p_user_id THEN m.receiver_id
              ELSE m.sender_id
          END AS partner_id,
          m.content,
          m.created_at,
          m.is_read,
          m.receiver_id,
          ROW_NUMBER() OVER (
              PARTITION BY
                  m.store_id,
                  CASE WHEN m.sender_id = p_user_id THEN m.receiver_id ELSE m.sender_id END
              ORDER BY m.created_at DESC
          ) as rn
      FROM public.messages m
      WHERE m.sender_id = p_user_id OR m.receiver_id = p_user_id
  )
  SELECT
      rm.store_id,
      rm.partner_id,
      COALESCE(p.full_name, 'Khách hàng') AS partner_name,
      p.avatar_url AS partner_avatar,
      rm.content AS last_message,
      rm.created_at AS last_message_time,
      (
          SELECT COUNT(*)
          FROM public.messages m2
          WHERE m2.store_id = rm.store_id
            AND m2.sender_id = rm.partner_id
            AND m2.receiver_id = p_user_id
            AND m2.is_read = false
      ) AS unread_count
  FROM RankedMessages rm
  JOIN public.profiles p ON rm.partner_id = p.id
  WHERE rm.rn = 1
  ORDER BY rm.created_at DESC;
END;
$$;

CREATE TABLE IF NOT EXISTS "public"."profiles" (
    "id" "uuid" NOT NULL,
    "email" "text" NOT NULL,
    "full_name" "text" NOT NULL,
    "role" "text" NOT NULL,
    "phone" "text",
    "address" "text",
    "avatar_url" "text",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "balance" numeric DEFAULT 0 NOT NULL,
    "reward_points" integer DEFAULT 0 NOT NULL,
    "fcm_token" "text"
);

ALTER TABLE "public"."profiles" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."stores" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "owner_id" "uuid",
    "name" "text" NOT NULL,
    "address" "text",
    "image_url" "text",
    "opening_time" time without time zone,
    "closing_time" time without time zone,
    "is_active" boolean DEFAULT true,
    "rating" double precision DEFAULT 0.0,
    "created_at" timestamp with time zone DEFAULT "timezone"('utc'::"text", "now"()) NOT NULL,
    "review_count" integer DEFAULT 0
);

ALTER TABLE "public"."stores" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."foods" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "store_id" "uuid",
    "name" "text" NOT NULL,
    "description" "text",
    "price" double precision NOT NULL,
    "image_url" "text",
    "is_available" boolean DEFAULT true,
    "created_at" timestamp with time zone DEFAULT "now"(),
    "rating" double precision DEFAULT 0.0,
    "sold_count" integer DEFAULT 0
);

ALTER TABLE "public"."foods" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."vouchers" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "merchant_id" "uuid",
    "code" "text" NOT NULL,
    "discount_percent" integer DEFAULT 0 NOT NULL,
    "max_discount" double precision DEFAULT 0 NOT NULL,
    "min_order_value" double precision DEFAULT 0 NOT NULL,
    "valid_until" timestamp with time zone DEFAULT ("now"() + '1 year'::interval) NOT NULL,
    "discount_amount" numeric DEFAULT 0 NOT NULL,
    "is_active" boolean DEFAULT true NOT NULL,
    "expires_at" timestamp with time zone DEFAULT ("now"() + '1 year'::interval),
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "max_usages_per_user" integer DEFAULT NULL,
    "total_usages_limit" integer DEFAULT NULL,
    "starts_at" timestamp with time zone DEFAULT NULL
);

ALTER TABLE "public"."vouchers" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."orders" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "customer_id" "uuid",
    "merchant_id" "uuid",
    "shipper_id" "uuid",
    "status" "text" DEFAULT 'PENDING'::"text",
    "total_price" double precision NOT NULL,
    "delivery_address" "text" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"(),
    "note" "text",
    "updated_at" timestamp with time zone DEFAULT "timezone"('utc'::"text", "now"()),
    "merchant_lat" double precision,
    "merchant_lng" double precision,
    "delivery_lat" double precision,
    "delivery_lng" double precision,
    "shipper_lat" double precision,
    "shipper_lng" double precision,
    "voucher_code" "text",
    "discount_amount" numeric DEFAULT 0
);

ALTER TABLE "public"."orders" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."order_items" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "order_id" "uuid",
    "food_id" "uuid",
    "quantity" integer NOT NULL,
    "price_at_time" double precision NOT NULL
);

ALTER TABLE "public"."order_items" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."payments" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "customer_id" "uuid" NOT NULL,
    "order_id" "uuid",
    "amount" double precision NOT NULL,
    "method" "text" NOT NULL,
    "status" "text" DEFAULT 'PENDING'::"text" NOT NULL,
    "delivery_address" "text" NOT NULL,
    "note" "text",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "provider" "text",
    "transaction_id" "text"
);

ALTER TABLE "public"."payments" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."payment_settings" (
    "user_id" "uuid" NOT NULL,
    "default_method" "text" DEFAULT 'COD'::"text" NOT NULL,
    "default_provider" "text" DEFAULT 'ZALOPAY'::"text" NOT NULL,
    "method_infos" jsonb DEFAULT '{}'::jsonb NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."payment_settings" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."addresses" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid" NOT NULL,
    "title" "text" NOT NULL,
    "detail" "text" NOT NULL,
    "is_default" boolean DEFAULT false NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."addresses" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."reviews" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "order_id" "uuid",
    "customer_id" "uuid",
    "rating" integer NOT NULL,
    "comment" "text",
    "created_at" timestamp with time zone DEFAULT "now"(),
    "food_id" "uuid"
);

ALTER TABLE "public"."reviews" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."notifications" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "user_id" "uuid",
    "title" "text" NOT NULL,
    "body" "text" NOT NULL,
    "is_read" boolean DEFAULT false,
    "created_at" timestamp with time zone DEFAULT "now"(),
    "message" "text" DEFAULT ''::"text" NOT NULL,
    "read_at" timestamp with time zone
);

ALTER TABLE "public"."notifications" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."wallet_transactions" (
    "id" "text" NOT NULL,
    "user_id" "uuid" NOT NULL,
    "type" "text" NOT NULL,
    "amount" numeric NOT NULL,
    "description" "text" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."wallet_transactions" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."voucher_usages" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "voucher_id" "uuid" NOT NULL,
    "order_id" "uuid" NOT NULL,
    "customer_id" "uuid" NOT NULL,
    "discount_amount" numeric NOT NULL,
    "used_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."voucher_usages" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."messages" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "sender_id" "uuid" NOT NULL,
    "receiver_id" "uuid" NOT NULL,
    "store_id" "uuid" NOT NULL,
    "content" "text" NOT NULL,
    "is_read" boolean DEFAULT false,
    "created_at" timestamp with time zone DEFAULT "timezone"('utc'::"text", "now"()) NOT NULL
);

ALTER TABLE "public"."messages" OWNER TO "postgres";
