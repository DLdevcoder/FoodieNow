CREATE OR REPLACE FUNCTION public.trigger_new_order_notification()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.notifications (user_id, title, body, message, is_read)
    VALUES (
        NEW.merchant_id, 
        'Đơn hàng mới!', 
        'Bạn có một đơn hàng mới trị giá ' || NEW.total_price::text || 'đ. Vui lòng kiểm tra!',
        'Bạn có một đơn hàng mới trị giá ' || NEW.total_price::text || 'đ. Vui lòng kiểm tra!',
        false
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_new_order_notification ON public.orders;
CREATE TRIGGER trg_new_order_notification
AFTER INSERT ON public.orders
FOR EACH ROW
EXECUTE FUNCTION public.trigger_new_order_notification();

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
            'Đánh giá mới!', 
            'Khách hàng vừa đánh giá ' || NEW.rating::text || ' sao cho món ' || v_food_name || '.',
            'Khách hàng vừa đánh giá ' || NEW.rating::text || ' sao cho món ' || v_food_name || '.',
            false
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_new_review_notification ON public.reviews;
CREATE TRIGGER trg_new_review_notification
AFTER INSERT ON public.reviews
FOR EACH ROW
EXECUTE FUNCTION public.trigger_new_review_notification();

CREATE OR REPLACE FUNCTION public.trigger_wallet_transaction_notification()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.notifications (user_id, title, body, message, is_read)
    VALUES (
        NEW.user_id, 
        'Biến động số dư', 
        NEW.description,
        NEW.description,
        false
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_wallet_transaction_notification ON public.wallet_transactions;
CREATE TRIGGER trg_wallet_transaction_notification
AFTER INSERT ON public.wallet_transactions
FOR EACH ROW
EXECUTE FUNCTION public.trigger_wallet_transaction_notification();
