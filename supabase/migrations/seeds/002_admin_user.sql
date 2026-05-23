INSERT INTO "auth"."users" (
    "instance_id",
    "id",
    "aud",
    "role",
    "email",
    "encrypted_password",
    "email_confirmed_at",
    "raw_app_meta_data",
    "raw_user_meta_data",
    "is_super_admin",
    "created_at",
    "updated_at",
    "is_anonymous"
) VALUES (
    '00000000-0000-0000-0000-000000000000',
    'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
    'authenticated',
    'authenticated',
    'admin@foodienow.com',
    '$2a$10$IULRVPITJzgeIkPpg4kgeO1P.fs99sz597tccYk6YcQLbFIIeM97O',
    now(),
    '{"provider": "email", "providers": ["email"]}',
    '{"sub": "d83d47d4-0994-4d8e-be25-1e0fcfd9b000", "role": "ADMIN", "email": "admin@foodienow.com", "email_verified": true}',
    true,
    now(),
    now(),
    false
);

INSERT INTO "auth"."identities" (
    "provider_id",
    "user_id",
    "identity_data",
    "provider",
    "last_sign_in_at",
    "created_at",
    "updated_at",
    "id"
) VALUES (
    'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
    'd83d47d4-0994-4d8e-be25-1e0fcfd9b000',
    '{"sub": "d83d47d4-0994-4d8e-be25-1e0fcfd9b000", "role": "ADMIN", "email": "admin@foodienow.com", "email_verified": true}',
    'email',
    now(),
    now(),
    now(),
    gen_random_uuid()
);
