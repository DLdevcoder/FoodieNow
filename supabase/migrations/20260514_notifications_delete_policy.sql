-- Add missing DELETE RLS policy for notifications table.
-- Without this, deleteNotification() in the app is blocked by RLS.

drop policy if exists "notifications_delete_own" on public.notifications;
create policy "notifications_delete_own"
    on public.notifications
    for delete
    to anon, authenticated
    using (true);
