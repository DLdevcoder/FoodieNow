-- Mở quyền Upload (Insert) cho tất cả mọi người đối với bucket food_images
CREATE POLICY "Public_Insert_Food_Images"
ON storage.objects FOR INSERT
TO public
WITH CHECK (bucket_id = 'food_images');

-- Mở quyền Xem (Select) cho tất cả mọi người
CREATE POLICY "Public_Select_Food_Images"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'food_images');

-- Mở quyền Cập nhật (Update) cho tất cả mọi người
CREATE POLICY "Public_Update_Food_Images"
ON storage.objects FOR UPDATE
TO public
WITH CHECK (bucket_id = 'food_images');
CREATE TABLE public.messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    sender_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    store_id UUID NOT NULL REFERENCES public.stores(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Tạo các index để tăng tốc độ truy vấn khi load danh sách tin nhắn
CREATE INDEX idx_messages_sender_id ON public.messages(sender_id);
CREATE INDEX idx_messages_receiver_id ON public.messages(receiver_id);
CREATE INDEX idx_messages_store_id ON public.messages(store_id);
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
      NULL::TEXT AS partner_avatar, -- Tôi để NULL tạm thời để tránh lỗi nếu bảng profiles của bạn chưa có cột avatar
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

-- Đảm bảo RLS đã được bật
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- 1. Cấp quyền GỬI tin nhắn (INSERT)
-- Điều kiện: User chỉ được gửi tin nhắn bằng đúng ID của mình
CREATE POLICY "Allow users to send messages"
ON public.messages
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = sender_id);

-- 2. Cấp quyền ĐỌC tin nhắn (SELECT)
-- Điều kiện: User chỉ đọc được tin nhắn trong các cuộc hội thoại của chính mình
CREATE POLICY "Allow users to read their own messages"
ON public.messages
FOR SELECT
TO authenticated
USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Cho phép người dùng được quyền cập nhật trạng thái tin nhắn (is_read)
-- Điều kiện: Họ chỉ được phép cập nhật những tin nhắn mà họ là người nhận (receiver_id)
CREATE POLICY "Allow users to update received messages"
ON public.messages
FOR UPDATE
TO authenticated
USING (auth.uid() = receiver_id)
WITH CHECK (auth.uid() = receiver_id);