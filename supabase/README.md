# Supabase schema notes

This folder contains SQL migrations for the FoodieNow demo/local Supabase schema.

## Run order

Run the files in `supabase/migrations` in filename order:

1. `20260426_account_profile_payment.sql`
2. `20260426_notifications_activity.sql`
3. `20260502_payments_wallet_provider.sql`
4. `20260514_notifications_delete_policy.sql`
5. `20260514_atomic_payment_rpc.sql`
6. `20260514_addresses_vouchers_wallet_tx.sql`

## What each file does

- `20260426_account_profile_payment.sql`
  - Creates `profiles`, `orders`, and `payments`.
  - Enables RLS with permissive demo policies.
  - Adds idempotent `updated_at` triggers.
- `20260426_notifications_activity.sql`
  - Creates `notifications`.
  - Adds indexes for unread/timeline queries.
  - Enables RLS with permissive demo policies.
- `20260502_payments_wallet_provider.sql`
  - Adds `provider` and `transaction_id` to `payments`.
- `20260514_notifications_delete_policy.sql`
  - Adds the missing DELETE policy for notifications.
- `20260514_atomic_payment_rpc.sql`
  - Adds `balance` and `reward_points` to `profiles` if missing.
  - Creates `process_payment(...)` RPC for atomic order/payment/points/notification writes.
- `20260514_addresses_vouchers_wallet_tx.sql`
  - Creates or patches `addresses`, `vouchers`, `wallet_transactions`, and `payment_settings`.
  - Seeds demo voucher codes.

## How to apply

For a local/demo setup, the easiest path is Supabase SQL Editor:

1. Open each SQL file.
2. Run them one by one in the order above.
3. If a file was partially run before, run it again. The migrations are written to be mostly idempotent.

The Supabase CLI is not installed in this workspace, so these migrations were reviewed locally rather than pushed from here.

## Demo security note

These migrations use permissive RLS policies so the current Android demo app can keep using its existing auth flow. This is fine for demo/local work, but not production security.
