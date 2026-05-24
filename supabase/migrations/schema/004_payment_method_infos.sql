ALTER TABLE "public"."payment_settings"
ADD COLUMN IF NOT EXISTS "method_infos" jsonb DEFAULT '{}'::jsonb NOT NULL;
