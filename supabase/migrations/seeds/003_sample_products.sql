SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

INSERT INTO "public"."stores" ("id", "owner_id", "name", "address", "image_url", "opening_time", "closing_time", "is_active", "rating", "review_count") VALUES
	('c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'Pho Thin Lo Duc', '13 Lo Duc, Hai Ba Trung, Ha Noi', 'https://images.unsplash.com/photo-1582878826629-29b7ad1cb438?q=80&w=1000', '06:00:00', '22:00:00', true, 4.7, 3),
	('57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'Bun Cha Huong Lien', '24 Le Van Huu, Hai Ba Trung, Ha Noi', 'https://images.unsplash.com/photo-1626804475297-41607ea0d4eb?q=80&w=1000', '08:00:00', '20:30:00', true, 4.5, 2);

INSERT INTO "public"."foods" ("id", "store_id", "name", "description", "price", "image_url", "is_available", "rating", "sold_count") VALUES
	('f9a65f94-6b2a-464a-bc91-2dc04a60b943', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Pho Bo Tai Lan', 'Banh pho mem dai, thit bo tai lan dam vi, nuoc dung thanh ngot nau tu xuong ong 24h.', 65000, 'https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=1000', true, 4.9, 15000),
	('43b0431f-824c-4a11-8fcb-2b4a5d89cf24', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Pho Bo Chin', 'Thit bo chin nam mem, gau gion san sat, an kem quay nong cuc cuon.', 60000, 'https://images.unsplash.com/photo-1555126634-323283e090fa?q=80&w=1000', true, 4.7, 8500),
	('b07ea432-8dfb-4652-9b24-9b2c3a50bc44', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Tra Da', 'Tra pha loang mat lanh, giai khat tuc thi.', 5000, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?q=80&w=1000', true, 5.0, 30000),
	('d4e5f6a7-b8c9-4d0e-af12-345678901234', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Pho Bo Dac Biet', 'Gom tai, chin, gau, gan, sach. Phan pho lon, nhieu thit.', 85000, 'https://images.unsplash.com/photo-1555126634-323283e090fa?q=80&w=1000', true, 4.8, 5200),
	('1dbf2122-83b6-4b8c-b0cf-53e2d6b38c11', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Bun Cha Dac Biet', 'Bao gom bun roi, cha bam mem ngot, cha mieng xem canh va nem cua be gion rum.', 75000, 'https://images.unsplash.com/photo-1626804475297-41607ea0d4eb?q=80&w=1000', true, 4.9, 21000),
	('7a4f91bb-bc3a-4467-bc7e-2e0f8072183c', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Nem Cua Be', 'Nem cua be Hai Phong, vo gion, nhan tom thit cua thom lung.', 20000, 'https://images.unsplash.com/photo-1580828369019-1813204cd1ce?q=80&w=1000', true, 4.8, 45000),
	('a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Bun Cha Thuong', 'Bun roi kem cha bam va cha mieng nuong than hoa, nuoc cham vua an.', 55000, 'https://images.unsplash.com/photo-1626804475297-41607ea0d4eb?q=80&w=1000', true, 4.6, 12000),
	('b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Nuoc Chanh Tuoi', 'Chanh tuoi vat, duong phen, da lanh.', 15000, 'https://images.unsplash.com/photo-1621263764928-df1444c5e859?q=80&w=1000', true, 4.5, 8000);

INSERT INTO "public"."vouchers" ("id", "merchant_id", "code", "discount_percent", "max_discount", "min_order_value", "valid_until", "discount_amount", "is_active", "expires_at") VALUES
	('b7f5d68d-8a21-4d1d-91b4-1eb4b8f5d0b1', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'GIAMGIA20K', 0, 0, 100000, '2026-12-31 23:59:59+00', 20000, true, '2026-12-31 23:59:59+00'),
	('91e0a8d7-d76c-48c2-a7f4-b1fcdbb39550', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'FREESHIP', 100, 15000, 50000, '2026-12-31 23:59:59+00', 0, true, '2026-12-31 23:59:59+00'),
	('c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'WELCOME50', 50, 30000, 60000, '2026-12-31 23:59:59+00', 0, true, '2026-12-31 23:59:59+00');

INSERT INTO "public"."orders" ("id", "customer_id", "merchant_id", "shipper_id", "status", "total_price", "delivery_address", "note") VALUES
	('e3f89a20-410a-42fc-8a17-386f6d0f8a3d', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'DELIVERING', 135000, '144 Xuan Thuy, Cau Giay, Ha Noi', 'Giao nhanh giup minh'),
	('9c0490b4-3c66-4148-8eb1-4d7a8e8b6b14', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', NULL, 'PENDING', 75000, 'Toa Nha Lotte, Lieu Giai, Ha Noi', 'Khong hanh la'),
	('a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 150000, '144 Xuan Thuy, Cau Giay, Ha Noi', 'Them tuong ot'),
	('b2345678-c901-4def-0123-456789abcdef', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 110000, 'Toa Nha Lotte, Lieu Giai, Ha Noi', NULL),
	('c3456789-d012-4ef0-1234-56789abcdef0', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', NULL, 'CANCELLED', 65000, '144 Xuan Thuy, Cau Giay, Ha Noi', 'Huy don vi doi lau');

INSERT INTO "public"."order_items" ("id", "order_id", "food_id", "quantity", "price_at_time") VALUES
	('2804d9ca-dfcc-433e-b83c-1b7db5b85ef3', 'e3f89a20-410a-42fc-8a17-386f6d0f8a3d', 'f9a65f94-6b2a-464a-bc91-2dc04a60b943', 2, 65000),
	('4b1c7f42-498c-4fa2-bcda-3eb6f0d927ab', 'e3f89a20-410a-42fc-8a17-386f6d0f8a3d', 'b07ea432-8dfb-4652-9b24-9b2c3a50bc44', 1, 5000),
	('a412f171-8930-4e08-9af1-1f9f2fb1b0fb', '9c0490b4-3c66-4148-8eb1-4d7a8e8b6b14', '1dbf2122-83b6-4b8c-b0cf-53e2d6b38c11', 1, 75000),
	('d1e2f3a4-b5c6-4d7e-8f90-a1b2c3d4e5f6', 'a1234567-b890-4cde-f012-3456789abcde', 'd4e5f6a7-b8c9-4d0e-af12-345678901234', 1, 85000),
	('e2f3a4b5-c6d7-4e8f-90a1-b2c3d4e5f6a7', 'a1234567-b890-4cde-f012-3456789abcde', 'f9a65f94-6b2a-464a-bc91-2dc04a60b943', 1, 65000),
	('f3a4b5c6-d7e8-4f90-a1b2-c3d4e5f6a7b8', 'b2345678-c901-4def-0123-456789abcdef', '1dbf2122-83b6-4b8c-b0cf-53e2d6b38c11', 1, 75000),
	('a4b5c6d7-e8f9-40a1-b2c3-d4e5f6a7b8c9', 'b2345678-c901-4def-0123-456789abcdef', '7a4f91bb-bc3a-4467-bc7e-2e0f8072183c', 1, 20000),
	('b5c6d7e8-f9a0-41b2-c3d4-e5f6a7b8c9d0', 'b2345678-c901-4def-0123-456789abcdef', 'b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e', 1, 15000),
	('c6d7e8f9-a0b1-42c3-d4e5-f6a7b8c9d0e1', 'c3456789-d012-4ef0-1234-56789abcdef0', 'f9a65f94-6b2a-464a-bc91-2dc04a60b943', 1, 65000);

INSERT INTO "public"."payments" ("id", "customer_id", "order_id", "amount", "method", "status", "delivery_address", "note") VALUES
	('11a2b3c4-d5e6-4f7a-8b9c-0d1e2f3a4b5c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'a1234567-b890-4cde-f012-3456789abcde', 150000, 'COD', 'SUCCESS', '144 Xuan Thuy, Cau Giay, Ha Noi', 'Them tuong ot'),
	('22b3c4d5-e6f7-4a8b-9c0d-1e2f3a4b5c6d', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'b2345678-c901-4def-0123-456789abcdef', 110000, 'WALLET', 'SUCCESS', 'Toa Nha Lotte, Lieu Giai, Ha Noi', NULL);

INSERT INTO "public"."notifications" ("id", "user_id", "title", "body", "is_read", "message") VALUES
	('e7d3cf2f-7f72-46a2-b2d9-1abf4b8c0a9d', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Don hang dang giao!', 'Shipper Pham Hoang Nam dang tren duong giao don hang den ban.', false, '{"order_id": "e3f89a20-410a-42fc-8a17-386f6d0f8a3d"}'),
	('91d240d1-0329-4ab5-bba3-2d2d85600cb8', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Xac nhan don hang', 'Quan Bun Cha Huong Lien da nhan don hang cua ban.', true, '{"order_id": "9c0490b4-3c66-4148-8eb1-4d7a8e8b6b14"}'),
	('f42bcf81-f230-4c31-90a6-8025e1975e53', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Khuyen mai sieu hot', 'Tang ban ma GIAMGIA20K giam 20.000d cho don tu 100.000d!', false, '{"promo_code": "GIAMGIA20K"}'),
	('a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Don hang hoan thanh', 'Don hang #a1234567 da giao thanh cong. Cam on ban da su dung FoodieNow!', true, '{"order_id": "a1234567-b890-4cde-f012-3456789abcde"}'),
	('b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Don hang hoan thanh', 'Don hang #b2345678 da giao thanh cong. Ban duoc cong 1100 FoodieCoins!', true, '{"order_id": "b2345678-c901-4def-0123-456789abcdef"}'),
	('c3d4e5f6-a7b8-49c0-d1e2-f3a4b5c6d7e8', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Don hang da huy', 'Don hang #c3456789 da bi huy theo yeu cau cua ban.', true, '{"order_id": "c3456789-d012-4ef0-1234-56789abcdef0"}'),
	('d4e5f6a7-b8c9-40d1-e2f3-a4b5c6d7e8f9', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'Don giao moi', 'Ban co don giao hang moi den 144 Xuan Thuy. Nhan don ngay!', false, '{"order_id": "e3f89a20-410a-42fc-8a17-386f6d0f8a3d"}');

INSERT INTO "public"."reviews" ("id", "order_id", "customer_id", "food_id", "rating", "comment") VALUES
	('3f2e7f30-f8b1-40e1-9549-3e3a479a3b68', 'a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'd4e5f6a7-b8c9-4d0e-af12-345678901234', 5, 'Pho dac biet ngon xuat sac!'),
	('a1111111-1111-4111-8111-111111111111', 'a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'f9a65f94-6b2a-464a-bc91-2dc04a60b943', 5, 'Pho tai lan dam vi, nuoc dung tuyet voi.'),
	('b2222222-2222-4222-8222-222222222222', 'a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '43b0431f-824c-4a11-8fcb-2b4a5d89cf24', 4, 'Pho chin ngon, nhung muon them it gau.'),
	('c3333333-3333-4333-8333-333333333333', 'b2345678-c901-4def-0123-456789abcdef', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '1dbf2122-83b6-4b8c-b0cf-53e2d6b38c11', 5, 'Bun cha ngon nhu an o Ha Noi!'),
	('d4444444-4444-4444-8444-444444444444', 'b2345678-c901-4def-0123-456789abcdef', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7a4f91bb-bc3a-4467-bc7e-2e0f8072183c', 4, 'Nem cua be gion rum, se quay lai.');
