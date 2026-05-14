-- Persist profile/payment helper data that was previously held by mock repositories.

create table if not exists public.addresses (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    title text not null,
    detail text not null,
    is_default boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.addresses
    add column if not exists user_id uuid references auth.users(id) on delete cascade,
    add column if not exists title text,
    add column if not exists detail text,
    add column if not exists is_default boolean not null default false,
    add column if not exists created_at timestamptz not null default now(),
    add column if not exists updated_at timestamptz not null default now();

create unique index if not exists addresses_one_default_per_user
    on public.addresses(user_id)
    where is_default;

create index if not exists addresses_user_id_idx on public.addresses(user_id);

create table if not exists public.vouchers (
    code text primary key,
    discount_amount numeric not null,
    is_active boolean not null default true,
    expires_at timestamptz,
    created_at timestamptz not null default now()
);

alter table public.vouchers
    add column if not exists code text,
    add column if not exists discount_amount numeric not null default 0,
    add column if not exists is_active boolean not null default true,
    add column if not exists expires_at timestamptz,
    add column if not exists created_at timestamptz not null default now();

create unique index if not exists vouchers_code_key on public.vouchers(code);

do $$
declare
    numeric_legacy_column text;
    text_legacy_column text;
begin
    foreach numeric_legacy_column in array array[
        'discount_percent',
        'max_discount',
        'min_order_amount',
        'min_order_value',
        'usage_limit',
        'used_count'
    ] loop
        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'vouchers'
              and column_name = numeric_legacy_column
        ) then
            execute format(
                'update public.vouchers set %I = 0 where %I is null',
                numeric_legacy_column,
                numeric_legacy_column
            );
            execute format(
                'alter table public.vouchers alter column %I set default 0',
                numeric_legacy_column
            );
        end if;
    end loop;

    foreach text_legacy_column in array array[
        'title',
        'description'
    ] loop
        if exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'vouchers'
              and column_name = text_legacy_column
        ) then
            execute format(
                'update public.vouchers set %I = '''' where %I is null',
                text_legacy_column,
                text_legacy_column
            );
            execute format(
                'alter table public.vouchers alter column %I set default ''''',
                text_legacy_column
            );
        end if;
    end loop;

    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'vouchers'
          and column_name = 'expires_at'
    ) then
        alter table public.vouchers
            alter column expires_at set default (now() + interval '1 year');
    end if;

    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'vouchers'
          and column_name = 'valid_until'
    ) then
        update public.vouchers
        set valid_until = now() + interval '1 year'
        where valid_until is null;

        alter table public.vouchers
            alter column valid_until set default (now() + interval '1 year');
    end if;

    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'vouchers'
          and column_name = 'valid_from'
    ) then
        update public.vouchers
        set valid_from = now()
        where valid_from is null;

        alter table public.vouchers
            alter column valid_from set default now();
    end if;
end;
$$;

insert into public.vouchers (code, discount_amount)
values
    ('GIAM20K', 20000),
    ('FREESHIP', 15000),
    ('WELCOME50', 50000),
    ('FOODIE10', 10000)
on conflict (code) do update
set discount_amount = excluded.discount_amount,
    is_active = true;

create table if not exists public.wallet_transactions (
    id text primary key,
    user_id uuid not null references auth.users(id) on delete cascade,
    type text not null,
    amount numeric not null,
    description text not null,
    created_at timestamptz not null default now()
);

alter table public.wallet_transactions
    add column if not exists user_id uuid references auth.users(id) on delete cascade,
    add column if not exists type text,
    add column if not exists amount numeric not null default 0,
    add column if not exists description text,
    add column if not exists created_at timestamptz not null default now();

create index if not exists wallet_transactions_user_created_idx
    on public.wallet_transactions(user_id, created_at desc);

create table if not exists public.payment_settings (
    user_id uuid primary key references auth.users(id) on delete cascade,
    default_method text not null default 'COD',
    default_provider text not null default 'ZALOPAY',
    updated_at timestamptz not null default now()
);

alter table public.payment_settings
    add column if not exists user_id uuid references auth.users(id) on delete cascade,
    add column if not exists default_method text not null default 'COD',
    add column if not exists default_provider text not null default 'ZALOPAY',
    add column if not exists updated_at timestamptz not null default now();

alter table public.addresses enable row level security;
alter table public.vouchers enable row level security;
alter table public.wallet_transactions enable row level security;
alter table public.payment_settings enable row level security;

drop policy if exists "addresses_select_own" on public.addresses;
create policy "addresses_select_own" on public.addresses
    for select to authenticated
    using (auth.uid() = user_id);

drop policy if exists "addresses_insert_own" on public.addresses;
create policy "addresses_insert_own" on public.addresses
    for insert to authenticated
    with check (auth.uid() = user_id);

drop policy if exists "addresses_update_own" on public.addresses;
create policy "addresses_update_own" on public.addresses
    for update to authenticated
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);

drop policy if exists "addresses_delete_own" on public.addresses;
create policy "addresses_delete_own" on public.addresses
    for delete to authenticated
    using (auth.uid() = user_id);

drop policy if exists "vouchers_select_active" on public.vouchers;
create policy "vouchers_select_active" on public.vouchers
    for select to anon, authenticated
    using (is_active and (expires_at is null or expires_at > now()));

drop policy if exists "wallet_transactions_select_own" on public.wallet_transactions;
create policy "wallet_transactions_select_own" on public.wallet_transactions
    for select to authenticated
    using (auth.uid() = user_id);

drop policy if exists "wallet_transactions_insert_own" on public.wallet_transactions;
create policy "wallet_transactions_insert_own" on public.wallet_transactions
    for insert to authenticated
    with check (auth.uid() = user_id);

drop policy if exists "payment_settings_select_own" on public.payment_settings;
create policy "payment_settings_select_own" on public.payment_settings
    for select to authenticated
    using (auth.uid() = user_id);

drop policy if exists "payment_settings_insert_own" on public.payment_settings;
create policy "payment_settings_insert_own" on public.payment_settings
    for insert to authenticated
    with check (auth.uid() = user_id);

drop policy if exists "payment_settings_update_own" on public.payment_settings;
create policy "payment_settings_update_own" on public.payment_settings
    for update to authenticated
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);
