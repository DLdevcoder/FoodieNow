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

COMMENT ON SCHEMA "public" IS 'standard public schema';

CREATE OR REPLACE FUNCTION "public"."process_payment"("p_customer_id" "uuid", "p_amount" numeric, "p_method" "text", "p_provider" "text" DEFAULT NULL::"text", "p_transaction_id" "text" DEFAULT NULL::"text", "p_delivery_address" "text" DEFAULT NULL::"text", "p_note" "text" DEFAULT NULL::"text", "p_used_reward_points" integer DEFAULT 0) RETURNS TABLE("order_id" "uuid", "payment_id" "uuid", "earned_points" integer)
    LANGUAGE "plpgsql" SECURITY DEFINER
    SET "search_path" TO 'public'
    AS $$
declare
    v_current_points integer;
    v_point_delta integer;
begin
    if p_amount <= 0 then
        raise exception 'payment amount must be positive';
    end if;

    if p_delivery_address is null or btrim(p_delivery_address) = '' then
        raise exception 'delivery address is required';
    end if;

    if auth.uid() is not null and auth.uid() <> p_customer_id then
        raise exception 'cannot process payment for another user';
    end if;

    select reward_points
    into v_current_points
    from public.profiles
    where id = p_customer_id
    for update;

    if not found then
        raise exception 'profile not found';
    end if;

    earned_points := floor(p_amount / 100)::integer;
    v_point_delta := earned_points - greatest(p_used_reward_points, 0);

    if v_current_points + v_point_delta < 0 then
        raise exception 'insufficient reward points';
    end if;

    insert into public.orders (
        customer_id,
        total_price,
        status,
        delivery_address,
        note
    )
    values (
        p_customer_id,
        p_amount,
        'PENDING',
        p_delivery_address,
        p_note
    )
    returning id into order_id;

    insert into public.payments (
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
    values (
        p_customer_id,
        order_id,
        p_amount,
        p_method,
        p_provider,
        p_transaction_id,
        'SUCCESS',
        p_delivery_address,
        p_note
    )
    returning id into payment_id;

    update public.profiles
    set reward_points = reward_points + v_point_delta,
        updated_at = now()
    where id = p_customer_id;

    insert into public.notifications (
        user_id,
        title,
        message
    )
    values (
        p_customer_id,
        'Thanh toan thanh cong',
        'Don hang ' || order_id::text || ' da thanh toan. Ban duoc cong ' || earned_points::text || ' FoodieCoins.'
    );

    return next;
end;
$$;

ALTER FUNCTION "public"."process_payment"("p_customer_id" "uuid", "p_amount" numeric, "p_method" "text", "p_provider" "text", "p_transaction_id" "text", "p_delivery_address" "text", "p_note" "text", "p_used_reward_points" integer) OWNER TO "postgres";

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

SET default_tablespace = '';

SET default_table_access_method = "heap";

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

CREATE TABLE IF NOT EXISTS "public"."order_items" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "order_id" "uuid",
    "food_id" "uuid",
    "quantity" integer NOT NULL,
    "price_at_time" double precision NOT NULL
);

ALTER TABLE "public"."order_items" OWNER TO "postgres";

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
    CONSTRAINT "orders_status_check" CHECK (("status" = ANY (ARRAY['PENDING'::"text", 'PREPARING'::"text", 'DELIVERING'::"text", 'COMPLETED'::"text", 'CANCELLED'::"text"])))
);

ALTER TABLE "public"."orders" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."payment_settings" (
    "user_id" "uuid" NOT NULL,
    "default_method" "text" DEFAULT 'COD'::"text" NOT NULL,
    "default_provider" "text" DEFAULT 'ZALOPAY'::"text" NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."payment_settings" OWNER TO "postgres";

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
    "transaction_id" "text",
    CONSTRAINT "payments_amount_check" CHECK (("amount" >= (0)::double precision)),
    CONSTRAINT "payments_method_check" CHECK (("method" = ANY (ARRAY['COD'::"text", 'CARD'::"text", 'WALLET'::"text"]))),
    CONSTRAINT "payments_status_check" CHECK (("status" = ANY (ARRAY['PENDING'::"text", 'SUCCESS'::"text", 'FAILED'::"text"])))
);

ALTER TABLE "public"."payments" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."profiles" (
    "id" "uuid" NOT NULL,
    "email" "text" NOT NULL,
    "full_name" "text" NOT NULL,
    "role" "text" NOT NULL,
    "phone" "text",
    "address" "text",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "balance" numeric DEFAULT 0 NOT NULL,
    "reward_points" integer DEFAULT 0 NOT NULL,
    "fcm_token" "text",
    CONSTRAINT "profiles_role_check" CHECK (("role" = ANY (ARRAY['CUSTOMER'::"text", 'MERCHANT'::"text", 'SHIPPER'::"text"])))
);

ALTER TABLE "public"."profiles" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."reviews" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "order_id" "uuid",
    "customer_id" "uuid",
    "rating" integer NOT NULL,
    "comment" "text",
    "created_at" timestamp with time zone DEFAULT "now"(),
    "food_id" "uuid",
    CONSTRAINT "reviews_rating_check" CHECK ((("rating" >= 1) AND ("rating" <= 5)))
);

ALTER TABLE "public"."reviews" OWNER TO "postgres";

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
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."vouchers" OWNER TO "postgres";

CREATE TABLE IF NOT EXISTS "public"."wallet_transactions" (
    "id" "text" NOT NULL,
    "user_id" "uuid" NOT NULL,
    "type" "text" NOT NULL,
    "amount" numeric NOT NULL,
    "description" "text" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."wallet_transactions" OWNER TO "postgres";

ALTER TABLE ONLY "public"."addresses"
    ADD CONSTRAINT "addresses_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."foods"
    ADD CONSTRAINT "foods_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."notifications"
    ADD CONSTRAINT "notifications_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."order_items"
    ADD CONSTRAINT "order_items_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."orders"
    ADD CONSTRAINT "orders_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."payment_settings"
    ADD CONSTRAINT "payment_settings_pkey" PRIMARY KEY ("user_id");

ALTER TABLE ONLY "public"."payments"
    ADD CONSTRAINT "payments_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."profiles"
    ADD CONSTRAINT "profiles_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."reviews"
    ADD CONSTRAINT "reviews_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."stores"
    ADD CONSTRAINT "stores_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."vouchers"
    ADD CONSTRAINT "vouchers_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."wallet_transactions"
    ADD CONSTRAINT "wallet_transactions_pkey" PRIMARY KEY ("id");

CREATE UNIQUE INDEX "addresses_one_default_per_user" ON "public"."addresses" USING "btree" ("user_id") WHERE "is_default";

CREATE INDEX "addresses_user_id_idx" ON "public"."addresses" USING "btree" ("user_id");

CREATE INDEX "idx_notifications_created_at" ON "public"."notifications" USING "btree" ("created_at" DESC);

CREATE INDEX "idx_notifications_unread" ON "public"."notifications" USING "btree" ("user_id", "is_read");

CREATE INDEX "idx_notifications_user" ON "public"."notifications" USING "btree" ("user_id");

CREATE INDEX "idx_orders_customer" ON "public"."orders" USING "btree" ("customer_id");

CREATE INDEX "idx_orders_merchant" ON "public"."orders" USING "btree" ("merchant_id");

CREATE INDEX "idx_payments_customer" ON "public"."payments" USING "btree" ("customer_id");

CREATE INDEX "idx_payments_order" ON "public"."payments" USING "btree" ("order_id");

CREATE INDEX "idx_profiles_email" ON "public"."profiles" USING "btree" ("email");

CREATE INDEX "orders_customer_id_idx" ON "public"."orders" USING "btree" ("customer_id");

CREATE INDEX "orders_merchant_id_idx" ON "public"."orders" USING "btree" ("merchant_id");

CREATE INDEX "payments_customer_id_idx" ON "public"."payments" USING "btree" ("customer_id");

CREATE INDEX "payments_order_id_idx" ON "public"."payments" USING "btree" ("order_id");

CREATE INDEX "payments_provider_idx" ON "public"."payments" USING "btree" ("provider");

CREATE INDEX "profiles_email_idx" ON "public"."profiles" USING "btree" ("email");

CREATE UNIQUE INDEX "vouchers_code_key" ON "public"."vouchers" USING "btree" ("code");

CREATE INDEX "wallet_transactions_user_created_idx" ON "public"."wallet_transactions" USING "btree" ("user_id", "created_at" DESC);

CREATE OR REPLACE TRIGGER "trg_update_store_rating" AFTER INSERT OR DELETE OR UPDATE ON "public"."reviews" FOR EACH ROW EXECUTE FUNCTION "public"."update_store_rating"();

ALTER TABLE ONLY "public"."addresses"
    ADD CONSTRAINT "addresses_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."foods"
    ADD CONSTRAINT "foods_store_id_fkey" FOREIGN KEY ("store_id") REFERENCES "public"."stores"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."notifications"
    ADD CONSTRAINT "notifications_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."order_items"
    ADD CONSTRAINT "order_items_food_id_fkey" FOREIGN KEY ("food_id") REFERENCES "public"."foods"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."order_items"
    ADD CONSTRAINT "order_items_order_id_fkey" FOREIGN KEY ("order_id") REFERENCES "public"."orders"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."orders"
    ADD CONSTRAINT "orders_customer_id_fkey" FOREIGN KEY ("customer_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."orders"
    ADD CONSTRAINT "orders_merchant_id_fkey" FOREIGN KEY ("merchant_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."orders"
    ADD CONSTRAINT "orders_shipper_id_fkey" FOREIGN KEY ("shipper_id") REFERENCES "public"."profiles"("id") ON DELETE SET NULL;

ALTER TABLE ONLY "public"."payment_settings"
    ADD CONSTRAINT "payment_settings_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."payments"
    ADD CONSTRAINT "payments_customer_id_fkey" FOREIGN KEY ("customer_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."payments"
    ADD CONSTRAINT "payments_order_id_fkey" FOREIGN KEY ("order_id") REFERENCES "public"."orders"("id") ON DELETE SET NULL;

ALTER TABLE ONLY "public"."profiles"
    ADD CONSTRAINT "profiles_id_fkey" FOREIGN KEY ("id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."reviews"
    ADD CONSTRAINT "reviews_customer_id_fkey" FOREIGN KEY ("customer_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."reviews"
    ADD CONSTRAINT "reviews_food_id_fkey" FOREIGN KEY ("food_id") REFERENCES "public"."foods"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."reviews"
    ADD CONSTRAINT "reviews_order_id_fkey" FOREIGN KEY ("order_id") REFERENCES "public"."orders"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."stores"
    ADD CONSTRAINT "stores_owner_id_fkey" FOREIGN KEY ("owner_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."vouchers"
    ADD CONSTRAINT "vouchers_merchant_id_fkey" FOREIGN KEY ("merchant_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."wallet_transactions"
    ADD CONSTRAINT "wallet_transactions_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;

CREATE POLICY "Enable update for authenticated users" ON "public"."orders" FOR UPDATE TO "authenticated" USING (true) WITH CHECK (true);

ALTER TABLE "public"."addresses" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "addresses_delete_own" ON "public"."addresses" FOR DELETE TO "authenticated" USING (("auth"."uid"() = "user_id"));

CREATE POLICY "addresses_insert_own" ON "public"."addresses" FOR INSERT TO "authenticated" WITH CHECK (("auth"."uid"() = "user_id"));

CREATE POLICY "addresses_select_own" ON "public"."addresses" FOR SELECT TO "authenticated" USING (("auth"."uid"() = "user_id"));

CREATE POLICY "addresses_update_own" ON "public"."addresses" FOR UPDATE TO "authenticated" USING (("auth"."uid"() = "user_id")) WITH CHECK (("auth"."uid"() = "user_id"));

ALTER TABLE "public"."foods" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "foods_select_all" ON "public"."foods" FOR SELECT USING (true);
CREATE POLICY "foods_insert_all" ON "public"."foods" FOR INSERT WITH CHECK (true);
CREATE POLICY "foods_update_all" ON "public"."foods" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "foods_delete_all" ON "public"."foods" FOR DELETE USING (true);

ALTER TABLE "public"."notifications" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "notifications_delete_own" ON "public"."notifications" FOR DELETE TO "authenticated", "anon" USING (true);

CREATE POLICY "notifications_insert_own" ON "public"."notifications" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);

CREATE POLICY "notifications_select_own" ON "public"."notifications" FOR SELECT TO "authenticated", "anon" USING (true);

CREATE POLICY "notifications_update_own" ON "public"."notifications" FOR UPDATE TO "authenticated", "anon" USING (true) WITH CHECK (true);

ALTER TABLE "public"."order_items" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "order_items_select_all" ON "public"."order_items" FOR SELECT USING (true);
CREATE POLICY "order_items_insert_all" ON "public"."order_items" FOR INSERT WITH CHECK (true);
CREATE POLICY "order_items_update_all" ON "public"."order_items" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "order_items_delete_all" ON "public"."order_items" FOR DELETE USING (true);

ALTER TABLE "public"."orders" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "orders_delete_all" ON "public"."orders" FOR DELETE USING (true);

CREATE POLICY "orders_insert_all" ON "public"."orders" FOR INSERT WITH CHECK (true);

CREATE POLICY "orders_insert_customer" ON "public"."orders" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);

CREATE POLICY "orders_select_all" ON "public"."orders" FOR SELECT USING (true);

CREATE POLICY "orders_select_related" ON "public"."orders" FOR SELECT TO "authenticated", "anon" USING (true);

CREATE POLICY "orders_update_all" ON "public"."orders" FOR UPDATE USING (true) WITH CHECK (true);

CREATE POLICY "orders_update_related" ON "public"."orders" FOR UPDATE TO "authenticated", "anon" USING (true);

ALTER TABLE "public"."payment_settings" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "payment_settings_insert_own" ON "public"."payment_settings" FOR INSERT TO "authenticated" WITH CHECK (("auth"."uid"() = "user_id"));

CREATE POLICY "payment_settings_select_own" ON "public"."payment_settings" FOR SELECT TO "authenticated" USING (("auth"."uid"() = "user_id"));

CREATE POLICY "payment_settings_update_own" ON "public"."payment_settings" FOR UPDATE TO "authenticated" USING (("auth"."uid"() = "user_id")) WITH CHECK (("auth"."uid"() = "user_id"));

ALTER TABLE "public"."payments" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "payments_delete_all" ON "public"."payments" FOR DELETE USING (true);

CREATE POLICY "payments_insert_all" ON "public"."payments" FOR INSERT WITH CHECK (true);

CREATE POLICY "payments_insert_own" ON "public"."payments" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);

CREATE POLICY "payments_select_all" ON "public"."payments" FOR SELECT USING (true);

CREATE POLICY "payments_select_own" ON "public"."payments" FOR SELECT TO "authenticated", "anon" USING (true);

CREATE POLICY "payments_update_all" ON "public"."payments" FOR UPDATE USING (true) WITH CHECK (true);

CREATE POLICY "payments_update_own" ON "public"."payments" FOR UPDATE TO "authenticated", "anon" USING (true) WITH CHECK (true);

ALTER TABLE "public"."profiles" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "profiles_delete_all" ON "public"."profiles" FOR DELETE USING (true);

CREATE POLICY "profiles_insert_all" ON "public"."profiles" FOR INSERT WITH CHECK (true);

CREATE POLICY "profiles_insert_own" ON "public"."profiles" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);

CREATE POLICY "profiles_select_all" ON "public"."profiles" FOR SELECT USING (true);

CREATE POLICY "profiles_select_own" ON "public"."profiles" FOR SELECT TO "authenticated", "anon" USING (true);

CREATE POLICY "profiles_update_all" ON "public"."profiles" FOR UPDATE USING (true) WITH CHECK (true);

CREATE POLICY "profiles_update_own" ON "public"."profiles" FOR UPDATE TO "authenticated", "anon" USING (true) WITH CHECK (true);

ALTER TABLE "public"."reviews" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "reviews_select_all" ON "public"."reviews" FOR SELECT USING (true);
CREATE POLICY "reviews_insert_all" ON "public"."reviews" FOR INSERT WITH CHECK (true);
CREATE POLICY "reviews_update_all" ON "public"."reviews" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "reviews_delete_all" ON "public"."reviews" FOR DELETE USING (true);

ALTER TABLE "public"."stores" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "stores_select_all" ON "public"."stores" FOR SELECT USING (true);
CREATE POLICY "stores_insert_all" ON "public"."stores" FOR INSERT WITH CHECK (true);
CREATE POLICY "stores_update_all" ON "public"."stores" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "stores_delete_all" ON "public"."stores" FOR DELETE USING (true);

ALTER TABLE "public"."vouchers" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "vouchers_select_active" ON "public"."vouchers" FOR SELECT TO "authenticated", "anon" USING (("is_active" AND (("expires_at" IS NULL) OR ("expires_at" > "now"()))));

ALTER TABLE "public"."wallet_transactions" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "wallet_transactions_insert_own" ON "public"."wallet_transactions" FOR INSERT TO "authenticated" WITH CHECK (("auth"."uid"() = "user_id"));

CREATE POLICY "wallet_transactions_select_own" ON "public"."wallet_transactions" FOR SELECT TO "authenticated" USING (("auth"."uid"() = "user_id"));

GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";

REVOKE ALL ON FUNCTION "public"."process_payment"("p_customer_id" "uuid", "p_amount" numeric, "p_method" "text", "p_provider" "text", "p_transaction_id" "text", "p_delivery_address" "text", "p_note" "text", "p_used_reward_points" integer) FROM PUBLIC;
REVOKE ALL ON FUNCTION "public"."process_payment"("p_customer_id" "uuid", "p_amount" numeric, "p_method" "text", "p_provider" "text", "p_transaction_id" "text", "p_delivery_address" "text", "p_note" "text", "p_used_reward_points" integer) FROM "anon";
REVOKE ALL ON FUNCTION "public"."process_payment"("p_customer_id" "uuid", "p_amount" numeric, "p_method" "text", "p_provider" "text", "p_transaction_id" "text", "p_delivery_address" "text", "p_note" "text", "p_used_reward_points" integer) FROM "authenticated";
GRANT ALL ON FUNCTION "public"."process_payment"("p_customer_id" "uuid", "p_amount" numeric, "p_method" "text", "p_provider" "text", "p_transaction_id" "text", "p_delivery_address" "text", "p_note" "text", "p_used_reward_points" integer) TO "service_role";

GRANT ALL ON FUNCTION "public"."set_updated_at"() TO "anon";
GRANT ALL ON FUNCTION "public"."set_updated_at"() TO "authenticated";
GRANT ALL ON FUNCTION "public"."set_updated_at"() TO "service_role";

GRANT ALL ON FUNCTION "public"."update_store_rating"() TO "anon";
GRANT ALL ON FUNCTION "public"."update_store_rating"() TO "authenticated";
GRANT ALL ON FUNCTION "public"."update_store_rating"() TO "service_role";

GRANT ALL ON TABLE "public"."addresses" TO "anon";
GRANT ALL ON TABLE "public"."addresses" TO "authenticated";
GRANT ALL ON TABLE "public"."addresses" TO "service_role";

GRANT ALL ON TABLE "public"."foods" TO "anon";
GRANT ALL ON TABLE "public"."foods" TO "authenticated";
GRANT ALL ON TABLE "public"."foods" TO "service_role";

GRANT ALL ON TABLE "public"."notifications" TO "anon";
GRANT ALL ON TABLE "public"."notifications" TO "authenticated";
GRANT ALL ON TABLE "public"."notifications" TO "service_role";

GRANT ALL ON TABLE "public"."order_items" TO "anon";
GRANT ALL ON TABLE "public"."order_items" TO "authenticated";
GRANT ALL ON TABLE "public"."order_items" TO "service_role";

GRANT ALL ON TABLE "public"."orders" TO "anon";
GRANT ALL ON TABLE "public"."orders" TO "authenticated";
GRANT ALL ON TABLE "public"."orders" TO "service_role";

GRANT ALL ON TABLE "public"."payment_settings" TO "anon";
GRANT ALL ON TABLE "public"."payment_settings" TO "authenticated";
GRANT ALL ON TABLE "public"."payment_settings" TO "service_role";

GRANT ALL ON TABLE "public"."payments" TO "anon";
GRANT ALL ON TABLE "public"."payments" TO "authenticated";
GRANT ALL ON TABLE "public"."payments" TO "service_role";

GRANT ALL ON TABLE "public"."profiles" TO "anon";
GRANT ALL ON TABLE "public"."profiles" TO "authenticated";
GRANT ALL ON TABLE "public"."profiles" TO "service_role";

GRANT ALL ON TABLE "public"."reviews" TO "anon";
GRANT ALL ON TABLE "public"."reviews" TO "authenticated";
GRANT ALL ON TABLE "public"."reviews" TO "service_role";

GRANT ALL ON TABLE "public"."stores" TO "anon";
GRANT ALL ON TABLE "public"."stores" TO "authenticated";
GRANT ALL ON TABLE "public"."stores" TO "service_role";

GRANT ALL ON TABLE "public"."vouchers" TO "anon";
GRANT ALL ON TABLE "public"."vouchers" TO "authenticated";
GRANT ALL ON TABLE "public"."vouchers" TO "service_role";

GRANT ALL ON TABLE "public"."wallet_transactions" TO "anon";
GRANT ALL ON TABLE "public"."wallet_transactions" TO "authenticated";
GRANT ALL ON TABLE "public"."wallet_transactions" TO "service_role";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "service_role";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "service_role";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "service_role";

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'orders'
    ) THEN
        EXECUTE 'ALTER PUBLICATION supabase_realtime ADD TABLE "public"."orders"';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'notifications'
    ) THEN
        EXECUTE 'ALTER PUBLICATION supabase_realtime ADD TABLE "public"."notifications"';
    END IF;
END $$;

CREATE OR REPLACE TRIGGER "trg_orders_updated_at" BEFORE UPDATE ON "public"."orders" FOR EACH ROW EXECUTE FUNCTION "public"."set_updated_at"();

CREATE OR REPLACE TRIGGER "trg_profiles_updated_at" BEFORE UPDATE ON "public"."profiles" FOR EACH ROW EXECUTE FUNCTION "public"."set_updated_at"();

CREATE OR REPLACE TRIGGER "send-push-notification" AFTER INSERT ON "public"."notifications" FOR EACH ROW EXECUTE FUNCTION "supabase_functions"."http_request"('https://ruyrncmsawymsrvsluae.supabase.co/functions/v1/push-notification', 'POST', '{"Content-type":"application/json","Authorization":"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ1eXJuY21zYXd5bXNydnNsdWFlIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NjY2NDA4OCwiZXhwIjoyMDkyMjQwMDg4fQ.8TNzNWIJgNIdVdU4paJ1eWO867lC8riYJA-kHDhzZp4"}', '{}', '5000');
