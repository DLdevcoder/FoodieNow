SET session_replication_role = replica;

--
-- PostgreSQL database dump
--

-- \restrict Wywgjchh2hfrSEOwn6PEWBMOJzMD8CfF9vGasJqn7y400CZaitgFMA4s4F8MZ4j

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

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

--
-- Data for Name: audit_log_entries; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: custom_oauth_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: flow_state; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."users" ("instance_id", "id", "aud", "role", "email", "encrypted_password", "email_confirmed_at", "invited_at", "confirmation_token", "confirmation_sent_at", "recovery_token", "recovery_sent_at", "email_change_token_new", "email_change", "email_change_sent_at", "last_sign_in_at", "raw_app_meta_data", "raw_user_meta_data", "is_super_admin", "created_at", "updated_at", "phone", "phone_confirmed_at", "phone_change", "phone_change_token", "phone_change_sent_at", "email_change_token_current", "email_change_confirm_status", "banned_until", "reauthentication_token", "reauthentication_sent_at", "is_sso_user", "deleted_at", "is_anonymous") VALUES
	('00000000-0000-0000-0000-000000000000', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'authenticated', 'authenticated', 'optimus1072005@gmail.com', '$2a$10$IULRVPITJzgeIkPpg4kgeO1P.fs99sz597tccYk6YcQLbFIIeM97O', '2026-04-24 12:08:52.47177+00', NULL, '', '2026-04-24 12:08:35.364266+00', '', NULL, '', '', NULL, '2026-05-03 00:56:01.74446+00', '{"provider": "email", "providers": ["email"]}', '{"sub": "4d29d1c0-a622-4d6d-85ee-6c1b0f14f078", "role": "CUSTOMER", "email": "optimus1072005@gmail.com", "email_verified": true, "phone_verified": false}', NULL, '2026-04-24 12:08:35.326052+00', '2026-05-03 00:56:01.813305+00', NULL, NULL, '', '', NULL, '', 0, NULL, '', NULL, false, NULL, false),
	('00000000-0000-0000-0000-000000000000', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'authenticated', 'authenticated', 'tl1133210@gmail.com', '$2a$10$GPQNDw.BbeDNQ0N15HjHBOU6ytHaRLZeDpztbvy8OWfmu5A07yXQ6', '2026-04-27 08:25:10.090749+00', NULL, '', '2026-04-27 08:24:46.587376+00', '', NULL, '', '', NULL, '2026-05-11 01:52:22.137046+00', '{"provider": "email", "providers": ["email"]}', '{"sub": "7eac7482-0cb0-40fa-8141-2c8f746c84bc", "role": "MERCHANT", "email": "tl1133210@gmail.com", "email_verified": true, "phone_verified": false}', NULL, '2026-04-27 08:24:46.582381+00', '2026-05-11 01:52:22.198945+00', NULL, NULL, '', '', NULL, '', 0, NULL, '', NULL, false, NULL, false),
	('00000000-0000-0000-0000-000000000000', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'authenticated', 'authenticated', '23021614@vnu.edu.vn', '$2a$10$zk4ECPAsUmMiKfBdch2QFOOf3g51HEYtML2VDOqtijbW8/kDlVYve', '2026-04-30 03:29:52.679072+00', NULL, '', '2026-04-30 03:29:32.58312+00', '', NULL, '', '', NULL, '2026-05-13 15:31:19.981141+00', '{"provider": "email", "providers": ["email"]}', '{"sub": "1b51bd12-ba0e-41d9-9f49-f61a583da0b6", "role": "SHIPPER", "email": "23021614@vnu.edu.vn", "email_verified": true, "phone_verified": false}', NULL, '2026-04-30 03:29:32.552731+00', '2026-05-13 15:31:20.040678+00', NULL, NULL, '', '', NULL, '', 0, NULL, '', NULL, false, NULL, false),
	('00000000-0000-0000-0000-000000000000', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'authenticated', 'authenticated', 'dtl1233210@gmail.com', '$2a$10$TEe4BVsXtOaCSGJMpnJxru0dXhKozmf2Oya27nSqWQWE/0ODjkfrC', '2026-04-26 01:00:19.222148+00', NULL, '', '2026-04-26 00:59:52.699089+00', '', NULL, '', '', NULL, '2026-05-15 02:47:59.105095+00', '{"provider": "email", "providers": ["email"]}', '{"sub": "ea545185-a6bc-48d3-9277-84f1a1bf021b", "role": "CUSTOMER", "email": "dtl1233210@gmail.com", "email_verified": true, "phone_verified": false}', NULL, '2026-04-26 00:59:52.660048+00', '2026-05-15 02:47:59.14539+00', NULL, NULL, '', '', NULL, '', 0, NULL, '', NULL, false, NULL, false);


--
-- Data for Name: identities; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."identities" ("provider_id", "user_id", "identity_data", "provider", "last_sign_in_at", "created_at", "updated_at", "id") VALUES
	('4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '{"sub": "4d29d1c0-a622-4d6d-85ee-6c1b0f14f078", "role": "CUSTOMER", "email": "optimus1072005@gmail.com", "email_verified": true, "phone_verified": false}', 'email', '2026-04-24 12:08:35.354626+00', '2026-04-24 12:08:35.354674+00', '2026-04-24 12:08:35.354674+00', '629245b6-944c-43aa-91c3-cb066bd56b4b'),
	('ea545185-a6bc-48d3-9277-84f1a1bf021b', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '{"sub": "ea545185-a6bc-48d3-9277-84f1a1bf021b", "role": "CUSTOMER", "email": "dtl1233210@gmail.com", "email_verified": true, "phone_verified": false}', 'email', '2026-04-26 00:59:52.686369+00', '2026-04-26 00:59:52.687032+00', '2026-04-26 00:59:52.687032+00', '09fd25a6-2e30-41cf-b91b-8def613f8507'),
	('7eac7482-0cb0-40fa-8141-2c8f746c84bc', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '{"sub": "7eac7482-0cb0-40fa-8141-2c8f746c84bc", "role": "MERCHANT", "email": "tl1133210@gmail.com", "email_verified": true, "phone_verified": false}', 'email', '2026-04-27 08:24:46.58497+00', '2026-04-27 08:24:46.585016+00', '2026-04-27 08:24:46.585016+00', '70d364ff-6e67-473a-9d8a-b06f13d116a2'),
	('1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '{"sub": "1b51bd12-ba0e-41d9-9f49-f61a583da0b6", "role": "SHIPPER", "email": "23021614@vnu.edu.vn", "email_verified": true, "phone_verified": false}', 'email', '2026-04-30 03:29:32.570976+00', '2026-04-30 03:29:32.571028+00', '2026-04-30 03:29:32.571028+00', '7c6e5aff-16b3-44eb-b983-647ea92e987a');


--
-- Data for Name: instances; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_clients; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: sessions; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."sessions" ("id", "user_id", "created_at", "updated_at", "factor_id", "aal", "not_after", "refreshed_at", "user_agent", "ip", "tag", "oauth_client_id", "refresh_token_hmac_key", "refresh_token_counter", "scopes") VALUES
	('e53cba52-fa20-46e6-9091-9099d14c5625', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '2026-04-24 12:08:52.477759+00', '2026-04-24 12:08:52.477759+00', NULL, 'aal1', NULL, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36 Edg/147.0.0.0', '117.1.166.251', NULL, NULL, NULL, NULL, NULL),
	('9ba51323-dc0c-4487-85c4-61111628eaa6', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '2026-04-24 12:09:20.38631+00', '2026-04-24 12:09:20.38631+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 13; M2101K7BG Build/TP1A.220624.014)', '117.1.166.251', NULL, NULL, NULL, NULL, NULL),
	('a68a1215-7279-4030-a8d1-7d6f7b8b2c9e', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '2026-04-25 11:47:11.760854+00', '2026-04-25 11:47:11.760854+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 13; M2101K7BG Build/TP1A.220624.014)', '117.1.166.251', NULL, NULL, NULL, NULL, NULL),
	('57fe7c94-fabc-4cb6-b8e0-01b5b0cb9dc3', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-04-26 01:00:19.227493+00', '2026-04-26 01:00:19.227493+00', NULL, 'aal1', NULL, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36', '14.191.38.95', NULL, NULL, NULL, NULL, NULL),
	('4e78cde4-6456-459a-8759-91e67f7367b9', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-04-26 01:00:41.290047+00', '2026-04-26 01:00:41.290047+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.38.95', NULL, NULL, NULL, NULL, NULL),
	('5b1bb39f-1aa9-429f-ac5b-229121c7055a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-04-27 08:22:07.024378+00', '2026-04-27 08:22:07.024378+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.38.95', NULL, NULL, NULL, NULL, NULL),
	('9cdf69ee-eb8a-48fd-bc9a-475a965a99fb', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-27 08:25:10.097607+00', '2026-04-27 08:25:10.097607+00', NULL, 'aal1', NULL, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36', '14.191.38.95', NULL, NULL, NULL, NULL, NULL),
	('8f0da386-7844-4f8e-be70-cd4154b43c2e', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-27 08:25:37.371112+00', '2026-04-27 08:25:37.371112+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.38.95', NULL, NULL, NULL, NULL, NULL),
	('d6cf36fd-44fb-4c32-a985-464281e45569', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-28 10:40:07.710255+00', '2026-04-28 10:40:07.710255+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('3e0be9c2-b09f-4609-b9a9-b0c4da2ee106', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-29 08:18:04.174602+00', '2026-04-29 08:18:04.174602+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('9228cfdd-2058-4cfd-b54b-dcc8251c885a', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-29 08:27:37.717933+00', '2026-04-29 08:27:37.717933+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('0dabbe4d-16f5-4785-b944-86c33937d459', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '2026-04-30 01:28:38.245686+00', '2026-04-30 01:28:38.245686+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 13; M2101K7BG Build/TP1A.220624.014)', '171.234.217.158', NULL, NULL, NULL, NULL, NULL),
	('7eb21cc2-e4bc-4194-9445-ca9406933afa', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '2026-04-30 02:29:07.640526+00', '2026-04-30 02:29:07.640526+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 13; M2101K7BG Build/TP1A.220624.014)', '171.234.217.158', NULL, NULL, NULL, NULL, NULL),
	('a3f979dc-9125-4046-a92a-a8e67ce1238d', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-04-30 03:26:52.54714+00', '2026-04-30 03:26:52.54714+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('662e204d-f628-4e22-b169-75845ad04271', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-04-30 03:27:56.473248+00', '2026-04-30 03:27:56.473248+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('878fe631-62d8-4253-a3f9-23a00ca259af', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-30 03:28:23.607304+00', '2026-04-30 03:28:23.607304+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('b828d719-f4ee-4e35-8937-23543f4d82a1', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '2026-04-30 03:29:52.687103+00', '2026-04-30 03:29:52.687103+00', NULL, 'aal1', NULL, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('8d880986-108e-469b-883e-9c5cbf1cf285', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '2026-04-30 03:30:08.543408+00', '2026-04-30 03:30:08.543408+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('3f8eb338-37dd-48e8-beb2-727184395ce6', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-30 03:58:19.584899+00', '2026-04-30 03:58:19.584899+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('c756536f-f49e-471b-bb3d-dc4f18e5512a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-04-30 03:59:47.283774+00', '2026-04-30 03:59:47.283774+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('0996ca06-a2a7-4eb7-94e2-9745429425a8', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-04-30 04:06:11.968962+00', '2026-04-30 04:06:11.968962+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('027c3145-671c-441b-8e5a-2cc63e7be634', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-05-01 08:56:55.991346+00', '2026-05-01 08:56:55.991346+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.39.151', NULL, NULL, NULL, NULL, NULL),
	('b23998b2-f301-4484-a58d-e01e4eddb13c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '2026-05-02 02:30:47.702086+00', '2026-05-02 02:30:47.702086+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 13; M2101K7BG Build/TP1A.220624.014)', '14.191.157.214', NULL, NULL, NULL, NULL, NULL),
	('eafb8324-4e51-4fb3-be24-c2bdfbdd4808', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '2026-05-03 00:56:01.745537+00', '2026-05-03 00:56:01.745537+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 17; sdk_gphone16k_x86_64 Build/CP21.260330.005)', '14.191.156.143', NULL, NULL, NULL, NULL, NULL),
	('f94f079b-0055-4f88-a27b-68761eeace77', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-05-06 10:37:29.562641+00', '2026-05-06 10:37:29.562641+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '42.118.51.38', NULL, NULL, NULL, NULL, NULL),
	('5526de3f-55b9-4f6b-b6e9-67ec1026aae0', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-05-06 10:42:24.27199+00', '2026-05-06 10:42:24.27199+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '42.118.51.38', NULL, NULL, NULL, NULL, NULL),
	('b6794975-8358-4a2b-9a4e-68cc8e673a7c', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-05-06 14:33:09.279897+00', '2026-05-06 14:33:09.279897+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '42.118.51.38', NULL, NULL, NULL, NULL, NULL),
	('c776c087-6f44-454f-afcf-df3cdcc91ebe', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '2026-05-06 14:38:36.972145+00', '2026-05-06 14:38:36.972145+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '42.118.51.38', NULL, NULL, NULL, NULL, NULL),
	('3152998d-181f-45bd-a7a4-4b77f5fcfd9c', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-05-07 15:36:56.83998+00', '2026-05-07 15:36:56.83998+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.35.9', NULL, NULL, NULL, NULL, NULL),
	('ab3e0276-c7ba-4154-90ba-afb7380ff8a7', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '2026-05-08 03:51:16.554378+00', '2026-05-08 03:51:16.554378+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.35.9', NULL, NULL, NULL, NULL, NULL),
	('b270687c-b64c-4e54-88d3-ca5ad6a4ac88', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-05-08 09:01:30.071668+00', '2026-05-08 09:01:30.071668+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '14.191.35.9', NULL, NULL, NULL, NULL, NULL),
	('6648b276-e0f9-4435-9c4d-a93e449e62b2', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '2026-05-11 00:53:19.928573+00', '2026-05-11 00:53:19.928573+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '116.97.117.125', NULL, NULL, NULL, NULL, NULL),
	('42d1c5bb-acd0-4175-bc1e-b1bf7f97320b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-05-11 00:54:22.021531+00', '2026-05-11 00:54:22.021531+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '116.97.117.125', NULL, NULL, NULL, NULL, NULL),
	('08f4bbc1-a3b2-4834-9426-b901a10dbb4f', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-05-11 01:10:45.05236+00', '2026-05-11 01:10:45.05236+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '123.31.18.242', NULL, NULL, NULL, NULL, NULL),
	('e21ce12f-4be9-4587-b662-99b4d0c5bed3', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '2026-05-11 01:52:22.137988+00', '2026-05-11 01:52:22.137988+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 16; sdk_gphone64_x86_64 Build/BE2A.250530.026.D1)', '123.31.18.242', NULL, NULL, NULL, NULL, NULL),
	('de04de5e-0ae4-4d0b-9216-423edbf277ef', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '2026-05-13 15:31:19.982104+00', '2026-05-13 15:31:19.982104+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 17; sdk_gphone16k_x86_64 Build/CP21.260330.005)', '1.54.211.174', NULL, NULL, NULL, NULL, NULL),
	('7f81a808-9008-4dc1-9be4-f2e316c05d60', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2026-05-15 02:47:59.106862+00', '2026-05-15 02:47:59.106862+00', NULL, 'aal1', NULL, NULL, 'Dalvik/2.1.0 (Linux; U; Android 17; sdk_gphone16k_x86_64 Build/CP21.260330.005)', '14.191.38.171', NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: mfa_amr_claims; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."mfa_amr_claims" ("session_id", "created_at", "updated_at", "authentication_method", "id") VALUES
	('e53cba52-fa20-46e6-9091-9099d14c5625', '2026-04-24 12:08:52.490259+00', '2026-04-24 12:08:52.490259+00', 'otp', '45fb30b2-b9ed-4166-925f-78394d83e45f'),
	('9ba51323-dc0c-4487-85c4-61111628eaa6', '2026-04-24 12:09:20.398602+00', '2026-04-24 12:09:20.398602+00', 'password', '3f102b11-ce92-4675-bedf-7e3d68518f06'),
	('a68a1215-7279-4030-a8d1-7d6f7b8b2c9e', '2026-04-25 11:47:11.792879+00', '2026-04-25 11:47:11.792879+00', 'password', 'fbe2fc21-5ac3-494f-9c92-87ff21c5d22a'),
	('57fe7c94-fabc-4cb6-b8e0-01b5b0cb9dc3', '2026-04-26 01:00:19.253237+00', '2026-04-26 01:00:19.253237+00', 'otp', 'f5bbe02b-accc-4a18-9155-5fec2a53b673'),
	('4e78cde4-6456-459a-8759-91e67f7367b9', '2026-04-26 01:00:41.292548+00', '2026-04-26 01:00:41.292548+00', 'password', '3d4381c6-7d36-431a-a4f2-f47fde9e9fbe'),
	('5b1bb39f-1aa9-429f-ac5b-229121c7055a', '2026-04-27 08:22:07.079183+00', '2026-04-27 08:22:07.079183+00', 'password', '080b6d27-75ff-460e-b6ec-4d1e7a3b5fc2'),
	('9cdf69ee-eb8a-48fd-bc9a-475a965a99fb', '2026-04-27 08:25:10.104126+00', '2026-04-27 08:25:10.104126+00', 'otp', 'd337bae8-4919-407d-8ad3-49a4efc74347'),
	('8f0da386-7844-4f8e-be70-cd4154b43c2e', '2026-04-27 08:25:37.384241+00', '2026-04-27 08:25:37.384241+00', 'password', '90d387a6-f242-4ae4-957c-960521890ed2'),
	('d6cf36fd-44fb-4c32-a985-464281e45569', '2026-04-28 10:40:07.755486+00', '2026-04-28 10:40:07.755486+00', 'password', 'a36f0afe-58a1-4d6a-a0e6-3b4450ebe7b8'),
	('3e0be9c2-b09f-4609-b9a9-b0c4da2ee106', '2026-04-29 08:18:04.209148+00', '2026-04-29 08:18:04.209148+00', 'password', 'c6c9480e-7376-4dc8-892c-e83bc4303945'),
	('9228cfdd-2058-4cfd-b54b-dcc8251c885a', '2026-04-29 08:27:37.740723+00', '2026-04-29 08:27:37.740723+00', 'password', 'aeee7122-c290-4046-a831-8069130b0ca6'),
	('0dabbe4d-16f5-4785-b944-86c33937d459', '2026-04-30 01:28:38.309522+00', '2026-04-30 01:28:38.309522+00', 'password', 'a8f431f2-dc4b-43c4-94ba-ddafee064658'),
	('7eb21cc2-e4bc-4194-9445-ca9406933afa', '2026-04-30 02:29:07.683937+00', '2026-04-30 02:29:07.683937+00', 'password', '20bd2303-7d1b-4fc2-bea3-1c5a18ce3eba'),
	('a3f979dc-9125-4046-a92a-a8e67ce1238d', '2026-04-30 03:26:52.591114+00', '2026-04-30 03:26:52.591114+00', 'password', 'ae1bc02e-6b81-4869-bafd-c62304ff14ef'),
	('662e204d-f628-4e22-b169-75845ad04271', '2026-04-30 03:27:56.490496+00', '2026-04-30 03:27:56.490496+00', 'password', 'ec3956e2-dd69-4aa4-a176-6bf3cd5ffed5'),
	('878fe631-62d8-4253-a3f9-23a00ca259af', '2026-04-30 03:28:23.609938+00', '2026-04-30 03:28:23.609938+00', 'password', 'b37cea4a-9dd4-4c4b-b0ae-f907630a7c50'),
	('b828d719-f4ee-4e35-8937-23543f4d82a1', '2026-04-30 03:29:52.692016+00', '2026-04-30 03:29:52.692016+00', 'otp', 'cc8a707c-0371-4535-bde8-b800b699a99d'),
	('8d880986-108e-469b-883e-9c5cbf1cf285', '2026-04-30 03:30:08.546206+00', '2026-04-30 03:30:08.546206+00', 'password', '259b72c3-2b0d-4683-ba0a-d3c19f742efa'),
	('3f8eb338-37dd-48e8-beb2-727184395ce6', '2026-04-30 03:58:19.627712+00', '2026-04-30 03:58:19.627712+00', 'password', 'd8121371-c601-4ce8-8a0c-5d37c1c5f0ed'),
	('c756536f-f49e-471b-bb3d-dc4f18e5512a', '2026-04-30 03:59:47.298352+00', '2026-04-30 03:59:47.298352+00', 'password', '45557aee-a4ad-4790-a6b5-87014f310346'),
	('0996ca06-a2a7-4eb7-94e2-9745429425a8', '2026-04-30 04:06:11.976052+00', '2026-04-30 04:06:11.976052+00', 'password', 'e3512f8a-3e8e-4af7-b25d-b38c47d0c062'),
	('027c3145-671c-441b-8e5a-2cc63e7be634', '2026-05-01 08:56:56.058021+00', '2026-05-01 08:56:56.058021+00', 'password', '41650934-8f4a-46c7-b977-4cf720e8e9ff'),
	('b23998b2-f301-4484-a58d-e01e4eddb13c', '2026-05-02 02:30:47.760322+00', '2026-05-02 02:30:47.760322+00', 'password', '37404744-8381-4c54-a3d8-0db4d63a676b'),
	('eafb8324-4e51-4fb3-be24-c2bdfbdd4808', '2026-05-03 00:56:01.832042+00', '2026-05-03 00:56:01.832042+00', 'password', '7a178563-50c7-4b65-9ce5-61c1c306db3e'),
	('f94f079b-0055-4f88-a27b-68761eeace77', '2026-05-06 10:37:29.639896+00', '2026-05-06 10:37:29.639896+00', 'password', '7b98cee3-3d8e-4264-8c69-2fe3bcc3e244'),
	('5526de3f-55b9-4f6b-b6e9-67ec1026aae0', '2026-05-06 10:42:24.287417+00', '2026-05-06 10:42:24.287417+00', 'password', '84a52f0c-eaa0-415e-83a5-8dc3e25daf14'),
	('b6794975-8358-4a2b-9a4e-68cc8e673a7c', '2026-05-06 14:33:09.347251+00', '2026-05-06 14:33:09.347251+00', 'password', 'e95654f7-7e08-4a65-91e9-7f6f8822f202'),
	('c776c087-6f44-454f-afcf-df3cdcc91ebe', '2026-05-06 14:38:36.988439+00', '2026-05-06 14:38:36.988439+00', 'password', 'd30f5a44-9226-4ec0-a438-d0e11da80840'),
	('3152998d-181f-45bd-a7a4-4b77f5fcfd9c', '2026-05-07 15:36:56.897058+00', '2026-05-07 15:36:56.897058+00', 'password', '2da30453-ce21-4033-be74-02434abb49cc'),
	('ab3e0276-c7ba-4154-90ba-afb7380ff8a7', '2026-05-08 03:51:16.627825+00', '2026-05-08 03:51:16.627825+00', 'password', '89ab9e30-f69c-40d7-ab02-7f4936620b54'),
	('b270687c-b64c-4e54-88d3-ca5ad6a4ac88', '2026-05-08 09:01:30.129744+00', '2026-05-08 09:01:30.129744+00', 'password', 'd673b391-37e1-4cd6-a22e-602de2a28f8a'),
	('6648b276-e0f9-4435-9c4d-a93e449e62b2', '2026-05-11 00:53:20.002587+00', '2026-05-11 00:53:20.002587+00', 'password', '23c164a5-1ac0-41b6-8ad4-7d490ed4949f'),
	('42d1c5bb-acd0-4175-bc1e-b1bf7f97320b', '2026-05-11 00:54:22.036131+00', '2026-05-11 00:54:22.036131+00', 'password', 'd760c302-e8c8-4c5c-a8f8-6329a15b0021'),
	('08f4bbc1-a3b2-4834-9426-b901a10dbb4f', '2026-05-11 01:10:45.101798+00', '2026-05-11 01:10:45.101798+00', 'password', 'c23e1ab4-b64d-4daf-833e-c100ce50dfb5'),
	('e21ce12f-4be9-4587-b662-99b4d0c5bed3', '2026-05-11 01:52:22.210391+00', '2026-05-11 01:52:22.210391+00', 'password', '0fe823f0-5cb4-415a-901b-91f6a05806c1'),
	('de04de5e-0ae4-4d0b-9216-423edbf277ef', '2026-05-13 15:31:20.05684+00', '2026-05-13 15:31:20.05684+00', 'password', '2dd8dd52-3534-415c-8bbe-a831787270cc'),
	('7f81a808-9008-4dc1-9be4-f2e316c05d60', '2026-05-15 02:47:59.151268+00', '2026-05-15 02:47:59.151268+00', 'password', '16e9e060-6486-4e53-9fd7-7eb0ef25ba12');


--
-- Data for Name: mfa_factors; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: mfa_challenges; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_authorizations; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_client_states; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_consents; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: one_time_tokens; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--

INSERT INTO "auth"."refresh_tokens" ("instance_id", "id", "token", "user_id", "revoked", "created_at", "updated_at", "parent", "session_id") VALUES
	('00000000-0000-0000-0000-000000000000', 1, 'wko3gwuom4ri', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', false, '2026-04-24 12:08:52.482789+00', '2026-04-24 12:08:52.482789+00', NULL, 'e53cba52-fa20-46e6-9091-9099d14c5625'),
	('00000000-0000-0000-0000-000000000000', 2, 'k5a5ka6l2ebe', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', false, '2026-04-24 12:09:20.395412+00', '2026-04-24 12:09:20.395412+00', NULL, '9ba51323-dc0c-4487-85c4-61111628eaa6'),
	('00000000-0000-0000-0000-000000000000', 3, 'abehkeqzcj3u', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', false, '2026-04-25 11:47:11.777534+00', '2026-04-25 11:47:11.777534+00', NULL, 'a68a1215-7279-4030-a8d1-7d6f7b8b2c9e'),
	('00000000-0000-0000-0000-000000000000', 4, 'rmxxdyxkpazt', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-04-26 01:00:19.238547+00', '2026-04-26 01:00:19.238547+00', NULL, '57fe7c94-fabc-4cb6-b8e0-01b5b0cb9dc3'),
	('00000000-0000-0000-0000-000000000000', 5, '54znd4jnvmkn', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-04-26 01:00:41.291188+00', '2026-04-26 01:00:41.291188+00', NULL, '4e78cde4-6456-459a-8759-91e67f7367b9'),
	('00000000-0000-0000-0000-000000000000', 6, 'jnmeqnn4phlj', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-04-27 08:22:07.053511+00', '2026-04-27 08:22:07.053511+00', NULL, '5b1bb39f-1aa9-429f-ac5b-229121c7055a'),
	('00000000-0000-0000-0000-000000000000', 7, 'zeiudggvadzq', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-27 08:25:10.101434+00', '2026-04-27 08:25:10.101434+00', NULL, '9cdf69ee-eb8a-48fd-bc9a-475a965a99fb'),
	('00000000-0000-0000-0000-000000000000', 8, 'pyv5lpqtdvak', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-27 08:25:37.379576+00', '2026-04-27 08:25:37.379576+00', NULL, '8f0da386-7844-4f8e-be70-cd4154b43c2e'),
	('00000000-0000-0000-0000-000000000000', 9, 'u3pem45dpzgz', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-28 10:40:07.732534+00', '2026-04-28 10:40:07.732534+00', NULL, 'd6cf36fd-44fb-4c32-a985-464281e45569'),
	('00000000-0000-0000-0000-000000000000', 10, 'vq3qh4ze5bie', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-29 08:18:04.195583+00', '2026-04-29 08:18:04.195583+00', NULL, '3e0be9c2-b09f-4609-b9a9-b0c4da2ee106'),
	('00000000-0000-0000-0000-000000000000', 11, 'dm5kpv23uglg', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-29 08:27:37.731665+00', '2026-04-29 08:27:37.731665+00', NULL, '9228cfdd-2058-4cfd-b54b-dcc8251c885a'),
	('00000000-0000-0000-0000-000000000000', 12, 'cq5bteoyvgli', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', false, '2026-04-30 01:28:38.278935+00', '2026-04-30 01:28:38.278935+00', NULL, '0dabbe4d-16f5-4785-b944-86c33937d459'),
	('00000000-0000-0000-0000-000000000000', 13, 'ufruhs4w4o4n', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', false, '2026-04-30 02:29:07.661307+00', '2026-04-30 02:29:07.661307+00', NULL, '7eb21cc2-e4bc-4194-9445-ca9406933afa'),
	('00000000-0000-0000-0000-000000000000', 14, 'fmkccehrhc4m', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-04-30 03:26:52.564283+00', '2026-04-30 03:26:52.564283+00', NULL, 'a3f979dc-9125-4046-a92a-a8e67ce1238d'),
	('00000000-0000-0000-0000-000000000000', 15, 'wjrchzumbih7', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-04-30 03:27:56.486747+00', '2026-04-30 03:27:56.486747+00', NULL, '662e204d-f628-4e22-b169-75845ad04271'),
	('00000000-0000-0000-0000-000000000000', 16, 'p7bkboomwjuo', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-30 03:28:23.608577+00', '2026-04-30 03:28:23.608577+00', NULL, '878fe631-62d8-4253-a3f9-23a00ca259af'),
	('00000000-0000-0000-0000-000000000000', 17, '2g76bxmpkchw', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', false, '2026-04-30 03:29:52.688858+00', '2026-04-30 03:29:52.688858+00', NULL, 'b828d719-f4ee-4e35-8937-23543f4d82a1'),
	('00000000-0000-0000-0000-000000000000', 18, 'u66zpqfzuvtg', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', false, '2026-04-30 03:30:08.544846+00', '2026-04-30 03:30:08.544846+00', NULL, '8d880986-108e-469b-883e-9c5cbf1cf285'),
	('00000000-0000-0000-0000-000000000000', 19, 'af4qr35zxpzk', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-30 03:58:19.606839+00', '2026-04-30 03:58:19.606839+00', NULL, '3f8eb338-37dd-48e8-beb2-727184395ce6'),
	('00000000-0000-0000-0000-000000000000', 20, 'oea3qlhl5bon', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-04-30 03:59:47.296004+00', '2026-04-30 03:59:47.296004+00', NULL, 'c756536f-f49e-471b-bb3d-dc4f18e5512a'),
	('00000000-0000-0000-0000-000000000000', 21, 'd326ebbnkmdz', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-04-30 04:06:11.972748+00', '2026-04-30 04:06:11.972748+00', NULL, '0996ca06-a2a7-4eb7-94e2-9745429425a8'),
	('00000000-0000-0000-0000-000000000000', 22, 'wzjczokzkhwq', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-05-01 08:56:56.021985+00', '2026-05-01 08:56:56.021985+00', NULL, '027c3145-671c-441b-8e5a-2cc63e7be634'),
	('00000000-0000-0000-0000-000000000000', 23, '6x6kget7eixx', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', false, '2026-05-02 02:30:47.727097+00', '2026-05-02 02:30:47.727097+00', NULL, 'b23998b2-f301-4484-a58d-e01e4eddb13c'),
	('00000000-0000-0000-0000-000000000000', 24, 'p6x6rj6akqlr', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', false, '2026-05-03 00:56:01.785668+00', '2026-05-03 00:56:01.785668+00', NULL, 'eafb8324-4e51-4fb3-be24-c2bdfbdd4808'),
	('00000000-0000-0000-0000-000000000000', 25, 'zdasnqtfkmu4', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-05-06 10:37:29.598469+00', '2026-05-06 10:37:29.598469+00', NULL, 'f94f079b-0055-4f88-a27b-68761eeace77'),
	('00000000-0000-0000-0000-000000000000', 26, 'tgrri2a352as', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-05-06 10:42:24.281505+00', '2026-05-06 10:42:24.281505+00', NULL, '5526de3f-55b9-4f6b-b6e9-67ec1026aae0'),
	('00000000-0000-0000-0000-000000000000', 27, '75eojykgmf4n', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-05-06 14:33:09.313925+00', '2026-05-06 14:33:09.313925+00', NULL, 'b6794975-8358-4a2b-9a4e-68cc8e673a7c'),
	('00000000-0000-0000-0000-000000000000', 28, 'vhy3qfdtrayg', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', false, '2026-05-06 14:38:36.98479+00', '2026-05-06 14:38:36.98479+00', NULL, 'c776c087-6f44-454f-afcf-df3cdcc91ebe'),
	('00000000-0000-0000-0000-000000000000', 29, '7zsyf4md4y35', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-05-07 15:36:56.874836+00', '2026-05-07 15:36:56.874836+00', NULL, '3152998d-181f-45bd-a7a4-4b77f5fcfd9c'),
	('00000000-0000-0000-0000-000000000000', 30, 'eomdlhhagapx', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', false, '2026-05-08 03:51:16.58837+00', '2026-05-08 03:51:16.58837+00', NULL, 'ab3e0276-c7ba-4154-90ba-afb7380ff8a7'),
	('00000000-0000-0000-0000-000000000000', 31, '7as54ce4q2qi', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-05-08 09:01:30.100691+00', '2026-05-08 09:01:30.100691+00', NULL, 'b270687c-b64c-4e54-88d3-ca5ad6a4ac88'),
	('00000000-0000-0000-0000-000000000000', 32, 'inah5mhcwkzr', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', false, '2026-05-11 00:53:19.961078+00', '2026-05-11 00:53:19.961078+00', NULL, '6648b276-e0f9-4435-9c4d-a93e449e62b2'),
	('00000000-0000-0000-0000-000000000000', 33, '5zuqej3uf3zl', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-05-11 00:54:22.032895+00', '2026-05-11 00:54:22.032895+00', NULL, '42d1c5bb-acd0-4175-bc1e-b1bf7f97320b'),
	('00000000-0000-0000-0000-000000000000', 34, '75z3hv5trtky', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-05-11 01:10:45.073733+00', '2026-05-11 01:10:45.073733+00', NULL, '08f4bbc1-a3b2-4834-9426-b901a10dbb4f'),
	('00000000-0000-0000-0000-000000000000', 35, 'hmhy6jy4ppg2', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', false, '2026-05-11 01:52:22.180187+00', '2026-05-11 01:52:22.180187+00', NULL, 'e21ce12f-4be9-4587-b662-99b4d0c5bed3'),
	('00000000-0000-0000-0000-000000000000', 36, 'q6afl2hiuj75', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', false, '2026-05-13 15:31:20.018173+00', '2026-05-13 15:31:20.018173+00', NULL, 'de04de5e-0ae4-4d0b-9216-423edbf277ef'),
	('00000000-0000-0000-0000-000000000000', 37, 'rxwx6djcm4pc', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', false, '2026-05-15 02:47:59.128587+00', '2026-05-15 02:47:59.128587+00', NULL, '7f81a808-9008-4dc1-9be4-f2e316c05d60');


--
-- Data for Name: sso_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: saml_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: saml_relay_states; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: sso_domains; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: webauthn_challenges; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: webauthn_credentials; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE SET; Schema: auth; Owner: supabase_auth_admin
--

SELECT pg_catalog.setval('"auth"."refresh_tokens_id_seq"', 37, true);


--
-- PostgreSQL database dump complete
--

-- \unrestrict Wywgjchh2hfrSEOwn6PEWBMOJzMD8CfF9vGasJqn7y400CZaitgFMA4s4F8MZ4j

RESET ALL;
SET session_replication_role = replica;

--
-- PostgreSQL database dump
--

-- \restrict ZuSF5FUqx0Ddk5jw8sXgE3KEy5nHkFh0WelmhKNTYBIYejdmLzwazfEdxbKPt4a

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

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

--
-- Data for Name: addresses; Type: TABLE DATA; Schema: public; Owner: postgres
--





--
-- Data for Name: profiles; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."profiles" ("id", "email", "full_name", "role", "phone", "address", "balance", "reward_points", "fcm_token") VALUES
	('4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'optimus1072005@gmail.com', 'Nguyen Van Minh', 'CUSTOMER', '0901234567', '144 Xuan Thuy, Cau Giay, Ha Noi', 500000, 1500, NULL),
	('ea545185-a6bc-48d3-9277-84f1a1bf021b', 'dtl1233210@gmail.com', 'Tran Thi Lan', 'CUSTOMER', '0912345678', 'Toa Nha Lotte, Lieu Giai, Ha Noi', 250000, 800, NULL),
	('7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'tl1133210@gmail.com', 'Le Duc Thinh', 'MERCHANT', '0987654321', '13 Lo Duc, Hai Ba Trung, Ha Noi', 25000000, 0, NULL),
	('1b51bd12-ba0e-41d9-9f49-f61a583da0b6', '23021614@vnu.edu.vn', 'Pham Hoang Nam', 'SHIPPER', '0978123456', 'KTX Me Tri, Thanh Xuan, Ha Noi', 350000, 0, NULL);

--
-- Data for Name: stores; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."stores" ("id", "owner_id", "name", "address", "image_url", "opening_time", "closing_time", "is_active", "rating", "review_count") VALUES
	('c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'Pho Thin Lo Duc', '13 Lo Duc, Hai Ba Trung, Ha Noi', 'https://images.unsplash.com/photo-1582878826629-29b7ad1cb438?q=80&w=1000', '06:00:00', '22:00:00', true, 4.7, 3),
	('57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'Bun Cha Huong Lien', '24 Le Van Huu, Hai Ba Trung, Ha Noi', 'https://images.unsplash.com/photo-1626804475297-41607ea0d4eb?q=80&w=1000', '08:00:00', '20:30:00', true, 4.5, 2);

--
-- Data for Name: foods; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."foods" ("id", "store_id", "name", "description", "price", "image_url", "is_available", "rating", "sold_count") VALUES
	('f9a65f94-6b2a-464a-bc91-2dc04a60b943', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Pho Bo Tai Lan', 'Banh pho mem dai, thit bo tai lan dam vi, nuoc dung thanh ngot nau tu xuong ong 24h.', 65000, 'https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=1000', true, 4.9, 15000),
	('43b0431f-824c-4a11-8fcb-2b4a5d89cf24', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Pho Bo Chin', 'Thit bo chin nam mem, gau gion san sat, an kem quay nong cuc cuon.', 60000, 'https://images.unsplash.com/photo-1555126634-323283e090fa?q=80&w=1000', true, 4.7, 8500),
	('b07ea432-8dfb-4652-9b24-9b2c3a50bc44', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Tra Da', 'Tra pha loang mat lanh, giai khat tuc thi.', 5000, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?q=80&w=1000', true, 5.0, 30000),
	('d4e5f6a7-b8c9-4d0e-af12-345678901234', 'c9fb0550-6ed6-4ec8-b6dc-d7bb7b91d295', 'Pho Bo Dac Biet', 'Gom tai, chin, gau, gan, sach. Phan pho lon, nhieu thit.', 85000, 'https://images.unsplash.com/photo-1555126634-323283e090fa?q=80&w=1000', true, 4.8, 5200),
	('1dbf2122-83b6-4b8c-b0cf-53e2d6b38c11', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Bun Cha Dac Biet', 'Bao gom bun roi, cha bam mem ngot, cha mieng xem canh va nem cua be gion rum.', 75000, 'https://images.unsplash.com/photo-1626804475297-41607ea0d4eb?q=80&w=1000', true, 4.9, 21000),
	('7a4f91bb-bc3a-4467-bc7e-2e0f8072183c', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Nem Cua Be', 'Nem cua be Hai Phong, vo gion, nhan tom thit cua thom lung.', 20000, 'https://images.unsplash.com/photo-1580828369019-1813204cd1ce?q=80&w=1000', true, 4.8, 45000),
	('a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Bun Cha Thuong', 'Bun roi kem cha bam va cha mieng nuong than hoa, nuoc cham vua an.', 55000, 'https://images.unsplash.com/photo-1626804475297-41607ea0d4eb?q=80&w=1000', true, 4.6, 12000),
	('b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e', '57a7d4a2-25d2-45e0-82d2-8b4e724f71a1', 'Nuoc Chanh Tuoi', 'Chanh tuoi vat, duong phen, da lanh.', 15000, 'https://images.unsplash.com/photo-1621263764928-df1444c5e859?q=80&w=1000', true, 4.5, 8000);

--
-- Data for Name: vouchers; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."vouchers" ("id", "merchant_id", "code", "discount_percent", "max_discount", "min_order_value", "valid_until", "discount_amount", "is_active", "expires_at") VALUES
	('b7f5d68d-8a21-4d1d-91b4-1eb4b8f5d0b1', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'GIAMGIA20K', 0, 0, 100000, '2026-12-31 23:59:59+00', 20000, true, '2026-12-31 23:59:59+00'),
	('91e0a8d7-d76c-48c2-a7f4-b1fcdbb39550', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'FREESHIP', 100, 15000, 50000, '2026-12-31 23:59:59+00', 0, true, '2026-12-31 23:59:59+00'),
	('c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'WELCOME50', 50, 30000, 60000, '2026-12-31 23:59:59+00', 0, true, '2026-12-31 23:59:59+00');

--
-- Data for Name: orders; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."orders" ("id", "customer_id", "merchant_id", "shipper_id", "status", "total_price", "delivery_address", "note") VALUES
	('e3f89a20-410a-42fc-8a17-386f6d0f8a3d', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'DELIVERING', 135000, '144 Xuan Thuy, Cau Giay, Ha Noi', 'Giao nhanh giup minh'),
	('9c0490b4-3c66-4148-8eb1-4d7a8e8b6b14', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', NULL, 'PENDING', 75000, 'Toa Nha Lotte, Lieu Giai, Ha Noi', 'Khong hanh la'),
	('a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 150000, '144 Xuan Thuy, Cau Giay, Ha Noi', 'Them tuong ot'),
	('b2345678-c901-4def-0123-456789abcdef', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 110000, 'Toa Nha Lotte, Lieu Giai, Ha Noi', NULL),
	('c3456789-d012-4ef0-1234-56789abcdef0', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', NULL, 'CANCELLED', 65000, '144 Xuan Thuy, Cau Giay, Ha Noi', 'Huy don vi doi lau');

--
-- Data for Name: order_items; Type: TABLE DATA; Schema: public;
--

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

--
-- Data for Name: payments; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."payments" ("id", "customer_id", "order_id", "amount", "method", "status", "delivery_address", "note") VALUES
	('11a2b3c4-d5e6-4f7a-8b9c-0d1e2f3a4b5c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'a1234567-b890-4cde-f012-3456789abcde', 150000, 'COD', 'SUCCESS', '144 Xuan Thuy, Cau Giay, Ha Noi', 'Them tuong ot'),
	('22b3c4d5-e6f7-4a8b-9c0d-1e2f3a4b5c6d', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'b2345678-c901-4def-0123-456789abcdef', 110000, 'WALLET', 'SUCCESS', 'Toa Nha Lotte, Lieu Giai, Ha Noi', NULL);

--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."notifications" ("id", "user_id", "title", "body", "is_read", "message") VALUES
	('e7d3cf2f-7f72-46a2-b2d9-1abf4b8c0a9d', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Don hang dang giao!', 'Shipper Pham Hoang Nam dang tren duong giao don hang den ban.', false, '{"order_id": "e3f89a20-410a-42fc-8a17-386f6d0f8a3d"}'),
	('91d240d1-0329-4ab5-bba3-2d2d85600cb8', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Xac nhan don hang', 'Quan Bun Cha Huong Lien da nhan don hang cua ban.', true, '{"order_id": "9c0490b4-3c66-4148-8eb1-4d7a8e8b6b14"}'),
	('f42bcf81-f230-4c31-90a6-8025e1975e53', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Khuyen mai sieu hot', 'Tang ban ma GIAMGIA20K giam 20.000d cho don tu 100.000d!', false, '{"promo_code": "GIAMGIA20K"}'),
	('a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Don hang hoan thanh', 'Don hang #a1234567 da giao thanh cong. Cam on ban da su dung FoodieNow!', true, '{"order_id": "a1234567-b890-4cde-f012-3456789abcde"}'),
	('b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Don hang hoan thanh', 'Don hang #b2345678 da giao thanh cong. Ban duoc cong 1100 FoodieCoins!', true, '{"order_id": "b2345678-c901-4def-0123-456789abcdef"}'),
	('c3d4e5f6-a7b8-49c0-d1e2-f3a4b5c6d7e8', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Don hang da huy', 'Don hang #c3456789 da bi huy theo yeu cau cua ban.', true, '{"order_id": "c3456789-d012-4ef0-1234-56789abcdef0"}'),
	('d4e5f6a7-b8c9-40d1-e2f3-a4b5c6d7e8f9', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'Don giao moi', 'Ban co don giao hang moi den 144 Xuan Thuy. Nhan don ngay!', false, '{"order_id": "e3f89a20-410a-42fc-8a17-386f6d0f8a3d"}');

--
-- Data for Name: reviews; Type: TABLE DATA; Schema: public;
--

INSERT INTO "public"."reviews" ("id", "order_id", "customer_id", "food_id", "rating", "comment") VALUES
	('3f2e7f30-f8b1-40e1-9549-3e3a479a3b68', 'a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'd4e5f6a7-b8c9-4d0e-af12-345678901234', 5, 'Pho dac biet ngon xuat sac!'),
	('a1111111-1111-4111-8111-111111111111', 'a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'f9a65f94-6b2a-464a-bc91-2dc04a60b943', 5, 'Pho tai lan dam vi, nuoc dung tuyet voi.'),
	('b2222222-2222-4222-8222-222222222222', 'a1234567-b890-4cde-f012-3456789abcde', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '43b0431f-824c-4a11-8fcb-2b4a5d89cf24', 4, 'Pho chin ngon, nhung muon them it gau.'),
	('c3333333-3333-4333-8333-333333333333', 'b2345678-c901-4def-0123-456789abcdef', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '1dbf2122-83b6-4b8c-b0cf-53e2d6b38c11', 5, 'Bun cha ngon nhu an o Ha Noi!'),
	('d4444444-4444-4444-8444-444444444444', 'b2345678-c901-4def-0123-456789abcdef', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7a4f91bb-bc3a-4467-bc7e-2e0f8072183c', 4, 'Nem cua be gion rum, se quay lai.');

