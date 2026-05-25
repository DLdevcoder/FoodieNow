CREATE OR REPLACE VIEW public.admin_account_stats AS
SELECT 
    role,
    count(*)::bigint AS total_users,
    coalesce(sum(balance), 0)::numeric AS total_balance
FROM public.profiles
GROUP BY role;
