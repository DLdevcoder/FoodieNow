-- Supabase migration: profiles, orders, payments

create extension if not exists pgcrypto;

create table if not exists public.profiles (
    id uuid primary key,
    email text not null,
    full_name text not null,
    role text not null,
    phone text,
    address text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.orders (
    id uuid primary key default gen_random_uuid(),
    customer_id uuid not null,
    merchant_id uuid,
    shipper_id uuid,
    total_price numeric not null,
    status text not null,
    delivery_address text not null,
    note text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.payments (
    id uuid primary key default gen_random_uuid(),
    customer_id uuid not null,
    order_id uuid references public.orders(id) on delete cascade,
    amount numeric not null,
    method text not null,
    status text not null,
    delivery_address text,
    note text,
    created_at timestamptz not null default now()
);

create index if not exists profiles_email_idx on public.profiles(email);
create index if not exists orders_customer_id_idx on public.orders(customer_id);
create index if not exists orders_merchant_id_idx on public.orders(merchant_id);
create index if not exists payments_customer_id_idx on public.payments(customer_id);
create index if not exists payments_order_id_idx on public.payments(order_id);

create or replace function public.set_updated_at()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create trigger profiles_set_updated_at
before update on public.profiles
for each row
execute function public.set_updated_at();

create trigger orders_set_updated_at
before update on public.orders
for each row
execute function public.set_updated_at();

alter table public.profiles enable row level security;
alter table public.orders enable row level security;
alter table public.payments enable row level security;

-- Temporary permissive RLS for anon/authenticated (tighten later)
create policy "profiles_select_all"
    on public.profiles for select
    using (true);

create policy "profiles_insert_all"
    on public.profiles for insert
    with check (true);

create policy "profiles_update_all"
    on public.profiles for update
    using (true)
    with check (true);

create policy "profiles_delete_all"
    on public.profiles for delete
    using (true);

create policy "orders_select_all"
    on public.orders for select
    using (true);

create policy "orders_insert_all"
    on public.orders for insert
    with check (true);

create policy "orders_update_all"
    on public.orders for update
    using (true)
    with check (true);

create policy "orders_delete_all"
    on public.orders for delete
    using (true);

create policy "payments_select_all"
    on public.payments for select
    using (true);

create policy "payments_insert_all"
    on public.payments for insert
    with check (true);

create policy "payments_update_all"
    on public.payments for update
    using (true)
    with check (true);

create policy "payments_delete_all"
    on public.payments for delete
    using (true);
