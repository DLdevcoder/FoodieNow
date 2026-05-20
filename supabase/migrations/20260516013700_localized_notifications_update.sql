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
        body,
        message
    )
    values (
        p_customer_id,
        'TXT_PAYMENT_SUCCESS',
        '{"type":"PAYMENT_SUCCESS", "order_id":"' || order_id::text || '", "earned_points":"' || earned_points::text || '"}',
        '{"type":"PAYMENT_SUCCESS", "order_id":"' || order_id::text || '", "earned_points":"' || earned_points::text || '"}'
    );

    return next;
end;
$$;
