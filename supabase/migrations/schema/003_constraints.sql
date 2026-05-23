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

ALTER TABLE ONLY "public"."voucher_usages"
    ADD CONSTRAINT "voucher_usages_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."messages"
    ADD CONSTRAINT "messages_pkey" PRIMARY KEY ("id");

ALTER TABLE ONLY "public"."orders"
    ADD CONSTRAINT "orders_status_check" CHECK (("status" = ANY (ARRAY['PENDING'::"text", 'PREPARING'::"text", 'DELIVERING'::"text", 'COMPLETED'::"text", 'CANCELLED'::"text"])));

ALTER TABLE ONLY "public"."payments"
    ADD CONSTRAINT "payments_amount_check" CHECK (("amount" >= (0)::double precision));

ALTER TABLE ONLY "public"."payments"
    ADD CONSTRAINT "payments_method_check" CHECK (("method" = ANY (ARRAY['COD'::"text", 'CARD'::"text", 'WALLET'::"text", 'FOODIE_PAY'::"text"])));

ALTER TABLE ONLY "public"."payments"
    ADD CONSTRAINT "payments_status_check" CHECK (("status" = ANY (ARRAY['PENDING'::"text", 'SUCCESS'::"text", 'FAILED'::"text"])));

ALTER TABLE ONLY "public"."profiles"
    ADD CONSTRAINT "profiles_role_check" CHECK (("role" = ANY (ARRAY['CUSTOMER'::"text", 'MERCHANT'::"text", 'SHIPPER'::"text"])));

ALTER TABLE ONLY "public"."reviews"
    ADD CONSTRAINT "reviews_rating_check" CHECK ((("rating" >= 1) AND ("rating" <= 5)));

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

ALTER TABLE ONLY "public"."voucher_usages"
    ADD CONSTRAINT "voucher_usages_voucher_id_fkey" FOREIGN KEY ("voucher_id") REFERENCES "public"."vouchers"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."voucher_usages"
    ADD CONSTRAINT "voucher_usages_order_id_fkey" FOREIGN KEY ("order_id") REFERENCES "public"."orders"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."voucher_usages"
    ADD CONSTRAINT "voucher_usages_customer_id_fkey" FOREIGN KEY ("customer_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."messages"
    ADD CONSTRAINT "messages_sender_id_fkey" FOREIGN KEY ("sender_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."messages"
    ADD CONSTRAINT "messages_receiver_id_fkey" FOREIGN KEY ("receiver_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;

ALTER TABLE ONLY "public"."messages"
    ADD CONSTRAINT "messages_store_id_fkey" FOREIGN KEY ("store_id") REFERENCES "public"."stores"("id") ON DELETE CASCADE;

CREATE OR REPLACE TRIGGER "trg_update_store_rating" AFTER INSERT OR DELETE OR UPDATE ON "public"."reviews" FOR EACH ROW EXECUTE FUNCTION "public"."update_store_rating"();

CREATE OR REPLACE TRIGGER "trg_orders_updated_at" BEFORE UPDATE ON "public"."orders" FOR EACH ROW EXECUTE FUNCTION "public"."set_updated_at"();

CREATE OR REPLACE TRIGGER "trg_profiles_updated_at" BEFORE UPDATE ON "public"."profiles" FOR EACH ROW EXECUTE FUNCTION "public"."set_updated_at"();

CREATE OR REPLACE TRIGGER "send-push-notification" AFTER INSERT ON "public"."notifications" FOR EACH ROW EXECUTE FUNCTION "supabase_functions"."http_request"('https://ruyrncmsawymsrvsluae.supabase.co/functions/v1/push-notification', 'POST', '{"Content-type":"application/json","Authorization":"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ1eXJuY21zYXd5bXNydnNsdWFlIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NjY2NDA4OCwiZXhwIjoyMDkyMjQwMDg4fQ.8TNzNWIJgNIdVdU4paJ1eWO867lC8riYJA-kHDhzZp4"}', '{}', '5000');

CREATE OR REPLACE TRIGGER trg_order_status_notification
    AFTER UPDATE ON public.orders
    FOR EACH ROW
    EXECUTE FUNCTION public.trigger_order_status_notification();

CREATE OR REPLACE TRIGGER trg_new_order_notification
    AFTER INSERT ON public.orders
    FOR EACH ROW
    EXECUTE FUNCTION public.trigger_new_order_notification();

CREATE OR REPLACE TRIGGER trg_new_review_notification
    AFTER INSERT ON public.reviews
    FOR EACH ROW
    EXECUTE FUNCTION public.trigger_new_review_notification();

CREATE OR REPLACE TRIGGER trg_wallet_transaction_notification
    AFTER INSERT ON public.wallet_transactions
    FOR EACH ROW
    EXECUTE FUNCTION public.trigger_wallet_transaction_notification();

ALTER TABLE "public"."addresses" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."foods" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."notifications" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."order_items" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."orders" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."payment_settings" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."payments" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."profiles" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."reviews" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."stores" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."vouchers" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."wallet_transactions" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."voucher_usages" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "public"."messages" ENABLE ROW LEVEL SECURITY;

CREATE POLICY "addresses_delete_own" ON "public"."addresses" FOR DELETE TO "authenticated" USING (("auth"."uid"() = "user_id"));
CREATE POLICY "addresses_insert_own" ON "public"."addresses" FOR INSERT TO "authenticated" WITH CHECK (("auth"."uid"() = "user_id"));
CREATE POLICY "addresses_select_own" ON "public"."addresses" FOR SELECT TO "authenticated" USING (("auth"."uid"() = "user_id"));
CREATE POLICY "addresses_update_own" ON "public"."addresses" FOR UPDATE TO "authenticated" USING (("auth"."uid"() = "user_id")) WITH CHECK (("auth"."uid"() = "user_id"));

CREATE POLICY "foods_select_all" ON "public"."foods" FOR SELECT USING (true);
CREATE POLICY "foods_insert_all" ON "public"."foods" FOR INSERT WITH CHECK (true);
CREATE POLICY "foods_update_all" ON "public"."foods" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "foods_delete_all" ON "public"."foods" FOR DELETE USING (true);

CREATE POLICY "notifications_delete_own" ON "public"."notifications" FOR DELETE TO "authenticated", "anon" USING (true);
CREATE POLICY "notifications_insert_own" ON "public"."notifications" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);
CREATE POLICY "notifications_select_own" ON "public"."notifications" FOR SELECT TO "authenticated", "anon" USING (true);
CREATE POLICY "notifications_update_own" ON "public"."notifications" FOR UPDATE TO "authenticated", "anon" USING (true) WITH CHECK (true);

CREATE POLICY "order_items_select_all" ON "public"."order_items" FOR SELECT USING (true);
CREATE POLICY "order_items_insert_all" ON "public"."order_items" FOR INSERT WITH CHECK (true);
CREATE POLICY "order_items_update_all" ON "public"."order_items" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "order_items_delete_all" ON "public"."order_items" FOR DELETE USING (true);

CREATE POLICY "orders_delete_all" ON "public"."orders" FOR DELETE USING (true);
CREATE POLICY "orders_insert_all" ON "public"."orders" FOR INSERT WITH CHECK (true);
CREATE POLICY "orders_insert_customer" ON "public"."orders" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);
CREATE POLICY "orders_select_all" ON "public"."orders" FOR SELECT USING (true);
CREATE POLICY "orders_select_related" ON "public"."orders" FOR SELECT TO "authenticated", "anon" USING (true);
CREATE POLICY "orders_update_all" ON "public"."orders" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "orders_update_related" ON "public"."orders" FOR UPDATE TO "authenticated", "anon" USING (true);
CREATE POLICY "Enable update for authenticated users" ON "public"."orders" FOR UPDATE TO "authenticated" USING (true) WITH CHECK (true);

CREATE POLICY "payment_settings_insert_own" ON "public"."payment_settings" FOR INSERT TO "authenticated" WITH CHECK (("auth"."uid"() = "user_id"));
CREATE POLICY "payment_settings_select_own" ON "public"."payment_settings" FOR SELECT TO "authenticated" USING (("auth"."uid"() = "user_id"));
CREATE POLICY "payment_settings_update_own" ON "public"."payment_settings" FOR UPDATE TO "authenticated" USING (("auth"."uid"() = "user_id")) WITH CHECK (("auth"."uid"() = "user_id"));

CREATE POLICY "payments_delete_all" ON "public"."payments" FOR DELETE USING (true);
CREATE POLICY "payments_insert_all" ON "public"."payments" FOR INSERT WITH CHECK (true);
CREATE POLICY "payments_insert_own" ON "public"."payments" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);
CREATE POLICY "payments_select_all" ON "public"."payments" FOR SELECT USING (true);
CREATE POLICY "payments_select_own" ON "public"."payments" FOR SELECT TO "authenticated", "anon" USING (true);
CREATE POLICY "payments_update_all" ON "public"."payments" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "payments_update_own" ON "public"."payments" FOR UPDATE TO "authenticated", "anon" USING (true) WITH CHECK (true);

CREATE POLICY "profiles_delete_all" ON "public"."profiles" FOR DELETE USING (true);
CREATE POLICY "profiles_insert_all" ON "public"."profiles" FOR INSERT WITH CHECK (true);
CREATE POLICY "profiles_insert_own" ON "public"."profiles" FOR INSERT TO "authenticated", "anon" WITH CHECK (true);
CREATE POLICY "profiles_select_all" ON "public"."profiles" FOR SELECT USING (true);
CREATE POLICY "profiles_select_own" ON "public"."profiles" FOR SELECT TO "authenticated", "anon" USING (true);
CREATE POLICY "profiles_update_all" ON "public"."profiles" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "profiles_update_own" ON "public"."profiles" FOR UPDATE TO "authenticated", "anon" USING (true) WITH CHECK (true);

CREATE POLICY "reviews_select_all" ON "public"."reviews" FOR SELECT USING (true);
CREATE POLICY "reviews_insert_all" ON "public"."reviews" FOR INSERT WITH CHECK (true);
CREATE POLICY "reviews_update_all" ON "public"."reviews" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "reviews_delete_all" ON "public"."reviews" FOR DELETE USING (true);

CREATE POLICY "stores_select_all" ON "public"."stores" FOR SELECT USING (true);
CREATE POLICY "stores_insert_all" ON "public"."stores" FOR INSERT WITH CHECK (true);
CREATE POLICY "stores_update_all" ON "public"."stores" FOR UPDATE USING (true) WITH CHECK (true);
CREATE POLICY "stores_delete_all" ON "public"."stores" FOR DELETE USING (true);

CREATE POLICY "vouchers_select_active" ON "public"."vouchers" FOR SELECT TO "authenticated", "anon" USING (("is_active" AND (("expires_at" IS NULL) OR ("expires_at" > "now"()))));

CREATE POLICY "wallet_transactions_insert_own" ON "public"."wallet_transactions" FOR INSERT TO "authenticated" WITH CHECK (("auth"."uid"() = "user_id"));
CREATE POLICY "wallet_transactions_select_own" ON "public"."wallet_transactions" FOR SELECT TO "authenticated" USING (("auth"."uid"() = "user_id"));

CREATE POLICY "Users can view their own voucher usages" ON public.voucher_usages FOR SELECT USING (auth.uid() = customer_id);

CREATE POLICY "Allow users to send messages" ON public.messages FOR INSERT TO authenticated WITH CHECK (auth.uid() = sender_id);
CREATE POLICY "Allow users to read their own messages" ON public.messages FOR SELECT TO authenticated USING (auth.uid() = sender_id OR auth.uid() = receiver_id);
CREATE POLICY "Allow users to update received messages" ON public.messages FOR UPDATE TO authenticated USING (auth.uid() = receiver_id) WITH CHECK (auth.uid() = receiver_id);

CREATE POLICY "Public_Insert_Food_Images" ON storage.objects FOR INSERT TO public WITH CHECK (bucket_id = 'food_images');
CREATE POLICY "Public_Select_Food_Images" ON storage.objects FOR SELECT TO public USING (bucket_id = 'food_images');
CREATE POLICY "Public_Update_Food_Images" ON storage.objects FOR UPDATE TO public WITH CHECK (bucket_id = 'food_images');

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

GRANT ALL ON TABLE "public"."voucher_usages" TO "anon";
GRANT ALL ON TABLE "public"."voucher_usages" TO "authenticated";
GRANT ALL ON TABLE "public"."voucher_usages" TO "service_role";

GRANT ALL ON TABLE "public"."messages" TO "anon";
GRANT ALL ON TABLE "public"."messages" TO "authenticated";
GRANT ALL ON TABLE "public"."messages" TO "service_role";

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
