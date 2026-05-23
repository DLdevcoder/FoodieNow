ALTER TABLE "public"."profiles"
ADD COLUMN IF NOT EXISTS "avatar_url" text;

INSERT INTO storage.buckets (id, name, public)
VALUES ('profile_avatars', 'profile_avatars', true)
ON CONFLICT (id) DO UPDATE SET public = true;

DROP POLICY IF EXISTS "Public_Insert_Profile_Avatars" ON storage.objects;
DROP POLICY IF EXISTS "Public_Select_Profile_Avatars" ON storage.objects;
DROP POLICY IF EXISTS "Public_Update_Profile_Avatars" ON storage.objects;
DROP POLICY IF EXISTS "Public_Delete_Profile_Avatars" ON storage.objects;

CREATE POLICY "Public_Insert_Profile_Avatars" ON storage.objects
FOR INSERT TO public
WITH CHECK (bucket_id = 'profile_avatars');

CREATE POLICY "Public_Select_Profile_Avatars" ON storage.objects
FOR SELECT TO public
USING (bucket_id = 'profile_avatars');

CREATE POLICY "Public_Update_Profile_Avatars" ON storage.objects
FOR UPDATE TO public
USING (bucket_id = 'profile_avatars')
WITH CHECK (bucket_id = 'profile_avatars');

CREATE POLICY "Public_Delete_Profile_Avatars" ON storage.objects
FOR DELETE TO public
USING (bucket_id = 'profile_avatars');

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
