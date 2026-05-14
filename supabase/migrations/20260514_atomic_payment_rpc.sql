-- Create an atomic server-side payment flow.
-- PostgreSQL functions execute in the caller's transaction; any raised error
-- rolls back order, payment, reward-points, and notification writes together.

alter table public.profiles
    add column if not exists balance numeric not null default 0,
    add column if not exists reward_points integer not null default 0;

alter table public.notifications
    add column if not exists user_id uuid references auth.users(id) on delete cascade,
    add column if not exists title text not null default '',
    add column if not exists message text not null default '',
    add column if not exists is_read boolean not null default false,
    add column if not exists read_at timestamptz,
    add column if not exists created_at timestamptz not null default now();

create or replace function public.process_payment(
    p_customer_id uuid,
    p_amount numeric,
    p_method text,
    p_provider text default null,
    p_transaction_id text default null,
    p_delivery_address text default null,
    p_note text default null,
    p_used_reward_points integer default 0
)
returns table (
    order_id uuid,
    payment_id uuid,
    earned_points integer
)
language plpgsql
security definer
set search_path = public
as $$
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

grant execute on function public.process_payment(uuid, numeric, text, text, text, text, text, integer)
    to anon, authenticated;
