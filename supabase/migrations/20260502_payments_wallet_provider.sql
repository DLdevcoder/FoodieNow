-- Add wallet provider metadata to payments

alter table public.payments
    add column if not exists provider text,
    add column if not exists transaction_id text;

create index if not exists payments_provider_idx on public.payments(provider);

