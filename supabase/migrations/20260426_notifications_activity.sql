-- Notifications persistence for Activity + Notification screens.

create table if not exists public.notifications (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    title text not null,
    message text not null,
    is_read boolean not null default false,
    read_at timestamptz,
    created_at timestamptz not null default now()
);

create index if not exists idx_notifications_user on public.notifications(user_id);
create index if not exists idx_notifications_unread on public.notifications(user_id, is_read);
create index if not exists idx_notifications_created_at on public.notifications(created_at desc);

alter table public.notifications enable row level security;

drop policy if exists "notifications_select_own" on public.notifications;
create policy "notifications_select_own"
    on public.notifications
    for select
    to anon, authenticated
    using (true);

drop policy if exists "notifications_insert_own" on public.notifications;
create policy "notifications_insert_own"
    on public.notifications
    for insert
    to anon, authenticated
    with check (true);

drop policy if exists "notifications_update_own" on public.notifications;
create policy "notifications_update_own"
    on public.notifications
    for update
    to anon, authenticated
    using (true)
    with check (true);

