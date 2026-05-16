CREATE OR REPLACE FUNCTION public.trigger_order_status_notification()
RETURNS TRIGGER AS $$
BEGIN
    -- Chỉ thực thi khi có sự thay đổi trạng thái
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        
        -- Kịch bản 1: Quán xác nhận đơn (Chuyển sang PREPARING)
        IF NEW.status = 'PREPARING' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'Đã xác nhận đơn hàng', 
                'Quán đã nhận đơn hàng và đang chuẩn bị.',
                'Quán đã nhận đơn hàng và đang chuẩn bị.',
                false
            );

        -- Kịch bản 2: Shipper nhận đơn và bắt đầu giao (Chuyển sang DELIVERING)
        ELSIF NEW.status = 'DELIVERING' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'Đơn hàng đang giao!', 
                'Shipper đang trên đường giao đơn hàng đến bạn. Vui lòng chú ý điện thoại nhé.',
                'Shipper đang trên đường giao đơn hàng đến bạn. Vui lòng chú ý điện thoại nhé.',
                false
            );

        -- Kịch bản 3: Giao hàng hoàn thành (Chuyển sang COMPLETED)
        ELSIF NEW.status = 'COMPLETED' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'Đơn hàng hoàn thành', 
                'Đơn hàng đã được giao thành công. Cảm ơn bạn đã sử dụng FoodieNow!',
                'Đơn hàng đã được giao thành công. Cảm ơn bạn đã sử dụng FoodieNow!',
                false
            );

            -- Thêm thông báo cho Merchant nếu cần
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.merchant_id, 
                'Đơn hàng hoàn thành', 
                'Một đơn hàng đã được giao thành công.',
                'Một đơn hàng đã được giao thành công.',
                false
            );

        -- Kịch bản 4: Đơn hàng bị hủy (Chuyển sang CANCELLED)
        ELSIF NEW.status = 'CANCELLED' THEN
            INSERT INTO public.notifications (user_id, title, body, message, is_read)
            VALUES (
                NEW.customer_id, 
                'Đơn hàng đã hủy', 
                'Đơn hàng của bạn đã bị hủy.',
                'Đơn hàng của bạn đã bị hủy.',
                false
            );
        END IF;

    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Xóa trigger cũ nếu có để tránh lỗi trùng lặp khi chạy lại nhiều lần
DROP TRIGGER IF EXISTS trg_order_status_notification ON public.orders;

-- Tạo trigger chạy sau khi cập nhật bảng orders
CREATE TRIGGER trg_order_status_notification
AFTER UPDATE ON public.orders
FOR EACH ROW
EXECUTE FUNCTION public.trigger_order_status_notification();
