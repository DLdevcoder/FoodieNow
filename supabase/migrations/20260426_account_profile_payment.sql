-- FoodieNow persistence schema for account/profile/payment/order data.

create extension if not exists pgcrypto;

create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text not null,
    full_name text not null,
    role text not null check (role in ('CUSTOMER', 'MERCHANT', 'SHIPPER')),
    phone text,
    address text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.orders (
    id uuid primary key default gen_random_uuid(),
    customer_id uuid not null references auth.users(id) on delete cascade,
    merchant_id uuid references auth.users(id) on delete set null,
    shipper_id uuid references auth.users(id) on delete set null,
    total_price double precision not null check (total_price >= 0),
    status text not null default 'PENDING' check (status in ('PENDING', 'PREPARING', 'DELIVERING', 'COMPLETED', 'CANCELLED')),
    delivery_address text not null,
    note text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.payments (
    id uuid primary key default gen_random_uuid(),
    customer_id uuid not null references auth.users(id) on delete cascade,
    order_id uuid references public.orders(id) on delete set null,
    amount double precision not null check (amount >= 0),
    method text not null check (method in ('COD', 'CARD', 'WALLET')),
    status text not null default 'PENDING' check (status in ('PENDING', 'SUCCESS', 'FAILED')),
    delivery_address text not null,
    note text,
    created_at timestamptz not null default now()
);

create index if not exists idx_profiles_email on public.profiles(email);
create index if not exists idx_orders_customer on public.orders(customer_id);
create index if not exists idx_orders_merchant on public.orders(merchant_id);
create index if not exists idx_payments_customer on public.payments(customer_id);
create index if not exists idx_payments_order on public.payments(order_id);

alter table public.profiles enable row level security;
alter table public.orders enable row level security;
alter table public.payments enable row level security;

-- Temporary broad policies because current app uses PostgREST with anon key.
-- Tighten these after wiring authenticated Supabase sessions on the client.
drop policy if exists "profiles_select_own" on public.profiles;
create policy "profiles_select_own"
    on public.profiles
    for select
    to anon, authenticated
    using (true);

drop policy if exists "profiles_insert_own" on public.profiles;
create policy "profiles_insert_own"
    on public.profiles
    for insert
    to anon, authenticated
    with check (true);

drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own"
    on public.profiles
    for update
    to anon, authenticated
    using (true)
    with check (true);

drop policy if exists "orders_select_related" on public.orders;
create policy "orders_select_related"
    on public.orders
    for select
    to anon, authenticated
    using (true);

drop policy if exists "orders_insert_customer" on public.orders;
create policy "orders_insert_customer"
    on public.orders
    for insert
    to anon, authenticated
    with check (true);

drop policy if exists "orders_update_related" on public.orders;
create policy "orders_update_related"
    on public.orders
    for update
    to anon, authenticated
    using (true);

drop policy if exists "payments_select_own" on public.payments;
create policy "payments_select_own"
    on public.payments
    for select
    to anon, authenticated
    using (true);

drop policy if exists "payments_insert_own" on public.payments;
create policy "payments_insert_own"
    on public.payments
    for insert
    to anon, authenticated
    with check (true);

drop policy if exists "payments_update_own" on public.payments;
create policy "payments_update_own"
    on public.payments
    for update
    to anon, authenticated
    using (true)
    with check (true);

-- Keep updated_at fresh.
create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists trg_profiles_updated_at on public.profiles;
create trigger trg_profiles_updated_at
before update on public.profiles
for each row
execute function public.set_updated_at();

drop trigger if exists trg_orders_updated_at on public.orders;
create trigger trg_orders_updated_at
before update on public.orders
for each row
execute function public.set_updated_at();

