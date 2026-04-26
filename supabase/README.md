# Supabase schema notes

This folder contains SQL migrations used by the Android app.

## Migration included

- `migrations/20260426_account_profile_payment.sql`
  - Creates and secures `profiles`, `orders`, and `payments` tables.
  - Adds temporary broad RLS policies (`anon` + `authenticated`) for current client flow.
  - Adds `updated_at` triggers.
- `migrations/20260426_notifications_activity.sql`
  - Creates and secures `notifications` table.
  - Adds indexes for unread and timeline queries.
  - Adds temporary broad RLS policies (`anon` + `authenticated`) for current client flow.

## Apply migration

Run this SQL in Supabase SQL Editor (or through the Supabase CLI migration workflow) before testing profile/payment persistence from the app.

After moving app requests to authenticated Supabase sessions, tighten policies to `auth.uid()` ownership checks.

