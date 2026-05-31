CREATE TABLE IF NOT EXISTS "public"."categories" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL PRIMARY KEY,
    "name" "text" NOT NULL,
    "image_url" "text",
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL
);

ALTER TABLE "public"."categories" OWNER TO "postgres";

ALTER TABLE "public"."foods"
    ADD COLUMN IF NOT EXISTS "category_id" "uuid";

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'foods_category_id_fkey' AND table_name = 'foods'
    ) THEN
        ALTER TABLE ONLY "public"."foods"
            ADD CONSTRAINT "foods_category_id_fkey" FOREIGN KEY ("category_id") REFERENCES "public"."categories"("id") ON DELETE SET NULL;
    END IF;
END $$;

ALTER TABLE "public"."categories" ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "categories_select_all" ON "public"."categories";
CREATE POLICY "categories_select_all" ON "public"."categories" FOR SELECT USING (true);

DROP POLICY IF EXISTS "categories_insert_all" ON "public"."categories";
CREATE POLICY "categories_insert_all" ON "public"."categories" FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "categories_update_all" ON "public"."categories";
CREATE POLICY "categories_update_all" ON "public"."categories" FOR UPDATE USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "categories_delete_all" ON "public"."categories";
CREATE POLICY "categories_delete_all" ON "public"."categories" FOR DELETE USING (true);

GRANT ALL ON TABLE "public"."categories" TO "anon";
GRANT ALL ON TABLE "public"."categories" TO "authenticated";
GRANT ALL ON TABLE "public"."categories" TO "service_role";
