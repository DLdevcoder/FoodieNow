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
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."users" ("id", "email", "full_name", "role", "phone", "avatar_url", "created_at") VALUES
	('11111111-1111-1111-1111-111111111111', 'khachhang@gmail.com', 'Nguyễn Văn Khách', 'CUSTOMER', '0901234567', NULL, '2026-04-20 08:05:22.905572+00'),
	('22222222-2222-2222-2222-222222222222', 'quanan@gmail.com', 'Tiệm Cơm Tấm Ngon', 'MERCHANT', '0987654321', NULL, '2026-04-20 08:05:22.905572+00'),
	('33333333-3333-3333-3333-333333333333', 'shipper@gmail.com', 'Trần Văn Giao', 'SHIPPER', '0911222333', NULL, '2026-04-20 08:05:22.905572+00'),
	('7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'tl1133210@gmail.com', 'Chủ quán', 'MERCHANT', NULL, NULL, '2026-04-28 07:55:24.976583+00'),
	('ea545185-a6bc-48d3-9277-84f1a1bf021b', 'customer1@test.com', 'Khách 1', 'CUSTOMER', '0900000001', NULL, '2026-04-30 03:47:15.164116+00'),
	('4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'customer2@test.com', 'Khách 2', 'CUSTOMER', '0900000002', NULL, '2026-04-30 03:47:15.164116+00'),
	('1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'shipper1@test.com', 'Tài xế 1', 'SHIPPER', '0920000001', NULL, '2026-04-30 03:47:15.164116+00');


--
-- Data for Name: stores; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."stores" ("id", "owner_id", "name", "address", "image_url", "opening_time", "closing_time", "is_active", "rating", "created_at", "review_count") VALUES
	('22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'Tiệm Cơm Tấm Ngon', NULL, NULL, NULL, NULL, true, 0, '2026-04-26 08:39:01.077892+00', 0),
	('1ccade33-4070-4956-982f-f636f52932bd', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'Chicken Fly', '144 Xuân Thủy', 'https://ruyrncmsawymsrvsluae.supabase.co/storage/v1/object/public/store_images/store_1777429735911.jpg', NULL, NULL, true, 4.538461538461538, '2026-04-28 07:58:10.553583+00', 13),
	('7e1e0b74-972a-4897-a789-1c5f3344a6d5', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'mc đona', 'Khu vực 1', 'https://ruyrncmsawymsrvsluae.supabase.co/storage/v1/object/public/store_images/store_1778461225987.jpg', '07:00:00', '22:00:00', true, 4.5, '2026-04-30 03:47:15.164116+00', 12);


--
-- Data for Name: foods; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."foods" ("id", "store_id", "name", "description", "price", "image_url", "is_available", "created_at", "rating", "sold_count") VALUES
	('a6f37f94-63b5-40b1-a8ef-c360407007c5', '1ccade33-4070-4956-982f-f636f52932bd', 'Mystery scream', 'Tasty like your best food', 10000, 'https://ruyrncmsawymsrvsluae.supabase.co/storage/v1/object/public/food_images/food_1777364552102.jpg', true, '2026-04-28 08:22:50.446534+00', 0, 0),
	('e1c67d13-aa19-49ba-88db-104e806505a4', '1ccade33-4070-4956-982f-f636f52932bd', 'Chân gà bà tuyết', 'Mô tả chi tiết', 31000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTDjszJy-cZPY3z7MeLxzc2REb6D8sGoSAY-A&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 127),
	('f015c844-e41e-44da-a1fd-0b9a66554c64', '1ccade33-4070-4956-982f-f636f52932bd', 'Sườn nướng', 'Mô tả chi tiết', 200000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTZRnExqbv7AX4xDSHUhF4eHyzCypH72b9qzA&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 158),
	('4bf3a404-aace-416d-8513-53bf31bb5a63', '1ccade33-4070-4956-982f-f636f52932bd', 'Cà ri', 'Mô tả chi tiết', 75000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQyMrvrB3QXiABGXTQQcMD-CUX-OxiSLo1QIw&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 457),
	('1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', '1ccade33-4070-4956-982f-f636f52932bd', 'Giả cầy', 'Mô tả chi tiết', 61000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSUeCWZTGV-IpApzjyiMRzSQEsF9V71bQE7aA&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 391),
	('4ffa9305-7d4f-4f44-86d2-4ad86b30a530', '1ccade33-4070-4956-982f-f636f52932bd', 'Bún đậu', 'Mô tả chi tiết', 37000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQeVOJKTKbkzGFbr-x37QDh-mWFys4rSGsMoA&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 461),
	('2ed0f966-e15f-45cd-bc0e-95542f01662f', '1ccade33-4070-4956-982f-f636f52932bd', 'Cơm rang hoa kỳ', 'Mô tả chi tiết', 68000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS93KOfBtF-A3x0wqlAJ2eFtMEYyQnE_ceygw&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 367),
	('5d1a9866-de75-41d3-8004-0d5bf19a915a', '1ccade33-4070-4956-982f-f636f52932bd', 'Nem nướng Nha Tràn', 'Mô tả chi tiết', 52000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQSbXGUoxkIvn8g_4yCLCJzTBhThICNoEal0A&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 227),
	('2fbe8604-3117-4f43-a554-b1705035203d', '1ccade33-4070-4956-982f-f636f52932bd', 'Canh cá lóc', 'Mô tả chi tiết', 45000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQldivONiT8mdxnMuWY5m3dyCYjMPToTjWKDg&s', true, '2026-04-30 03:47:15.164116+00', 4.2, 51),
	('3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', '1ccade33-4070-4956-982f-f636f52932bd', 'Nem chua', 'Mô tả chi tiết', 36000, 'https://static.vinwonders.com/production/gia-nem-chua-o-thanh-hoa.jpg', true, '2026-04-30 03:47:15.164116+00', 4.2, 283),
	('8fad5a05-3959-41ae-a652-1ff2af0b6961', '1ccade33-4070-4956-982f-f636f52932bd', 'Bánh tráng phơi sương', 'Mô tả chi tiết', 31000, 'https://dichvuhutchankhong.vn/wp-content/uploads/2024/01/cach-bao-quan-banh-trang-phoi-suong.jpg', true, '2026-04-30 03:47:15.164116+00', 4.2, 467),
	('44444444-4444-4444-4444-444444444441', '1ccade33-4070-4956-982f-f636f52932bd', 'Cơm tấm sườn bì chả', 'Đặc sản Sài Gòn, sườn nướng than hoa', 45000, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT2bSCipNr_64nVQwjRHxN71hZxgXlFLPBF0A&s', true, '2026-04-20 08:05:22.905572+00', 0, 0),
	('44444444-4444-4444-4444-444444444442', '1ccade33-4070-4956-982f-f636f52932bd', 'Trà đá', 'Giải khát mát lạnh', 5000, 'https://anyscore.s3.ap-southeast-1.amazonaws.com/tra-da4400-1741416978.png', true, '2026-04-20 08:05:22.905572+00', 0, 0);


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."notifications" ("id", "user_id", "title", "body", "is_read", "created_at", "message", "read_at") VALUES
	('c49d3459-f34a-4e77-9109-8fc2da526d02', '11111111-1111-1111-1111-111111111111', 'Đơn hàng hoàn tất', 'Chúc bạn ngon miệng với món Cơm tấm sườn bì chả! Đừng quên đánh giá nhé.', false, '2026-04-20 08:05:22.905572+00', '', NULL),
	('ef7a3565-0fdb-45ce-b8f4-4f31cbdfae63', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 1', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-24 05:00:09.016428+00', '', NULL),
	('1cc87623-5fbf-46b8-a266-443e7c5a669b', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 3', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-28 00:48:05.057388+00', '', NULL),
	('ebce3ffd-9107-4178-af6e-626fa2317d67', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 5', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-23 22:13:23.895467+00', '', NULL),
	('1c787bca-a004-4bee-be8c-df3ecfb149cb', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 6', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-25 06:25:43.956953+00', '', NULL),
	('eeecadba-25c7-4897-a8de-84a996bcf2a7', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 7', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-27 19:39:19.778473+00', '', NULL),
	('6f7debfe-c215-4135-a149-ece6c09d7171', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 8', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-23 20:32:21.445549+00', '', NULL),
	('6ec45161-1282-4230-8dec-f3b738de7901', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 9', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-27 12:02:56.994337+00', '', NULL),
	('6bc517c4-3680-42bd-8d57-156655592b36', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 10', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-23 21:40:15.119903+00', '', NULL),
	('3f7c9e33-08ad-4072-9b74-2d3ce27ee85c', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 12', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-24 20:48:06.001566+00', '', NULL),
	('18181d5e-483c-4dcf-8a5d-02f65d890b0d', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 14', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-23 15:55:33.904915+00', '', NULL),
	('43a56816-1f2e-4765-96c2-b5431375c231', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 16', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-25 16:08:43.612992+00', '', NULL),
	('b696383e-ca72-466d-901a-6729a36c4154', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 17', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-23 19:59:35.870204+00', '', NULL),
	('ed82eb5a-d315-4040-98b7-e27be1da1fae', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 21', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-24 19:21:30.435833+00', '', NULL),
	('f45e7be4-14e2-4936-b8fd-d904253d754f', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 22', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 04:34:09.438873+00', '', NULL),
	('61e7e57d-25a7-4ca6-8605-d2f66edd55a1', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 23', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-28 23:30:24.839868+00', '', NULL),
	('7010a551-0e99-42f5-bdca-04f6cfc8aea2', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 27', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 06:01:38.821628+00', '', NULL),
	('2d74c9a1-8cbd-4141-b7c7-93ae16e99eea', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 28', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-24 23:21:40.452309+00', '', NULL),
	('31765c99-f6c3-4f88-92c0-af821d248daf', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 29', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-24 12:38:09.089007+00', '', NULL),
	('958e66cf-61ec-41e1-9c6f-cfbbd0eaacdf', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 30', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 20:15:14.165131+00', '', NULL),
	('33098078-be73-450f-a883-ab517acdd0bd', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 31', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-29 05:53:21.107297+00', '', NULL),
	('0e8f61dd-3190-447d-a0ed-47081c20ddd7', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 32', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-27 13:44:46.168637+00', '', NULL),
	('3265a5b2-14fb-423f-91ed-d4001d4bb163', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 33', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-27 18:02:53.270181+00', '', NULL),
	('4d69e22d-6c2d-4fa6-9470-51d7a6c39ff6', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 34', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 15:24:51.023265+00', '', NULL),
	('90c379e1-316d-42e9-8127-8d21f79e0c47', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 35', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-29 23:42:56.113917+00', '', NULL),
	('381c108e-54d4-4ab1-b629-253518d932e2', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 36', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-25 00:18:35.128407+00', '', NULL),
	('f6086393-7938-4466-a20e-4af3466ec8b0', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 37', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-27 13:53:58.719978+00', '', NULL),
	('c3a13647-0904-4264-974e-fddfdf340a3a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 38', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-26 22:34:15.804644+00', '', NULL),
	('dadeb0ab-a197-4c55-8ee3-b02036e2e3e2', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 39', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-26 14:08:17.973508+00', '', NULL),
	('de32e126-9187-4bc5-9993-674661080423', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 40', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-23 11:59:26.649756+00', '', NULL),
	('908eb85c-a8d3-4fcd-97ad-1353c180a63e', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 41', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-25 21:30:59.528944+00', '', NULL),
	('7633ca42-606a-4dfb-b2ee-d7361f9536bf', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 42', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-26 11:52:53.754318+00', '', NULL),
	('e705f006-a1f4-444f-9e38-3a5ceb5373e1', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 43', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-23 19:56:06.917668+00', '', NULL),
	('6a8b338b-0d77-45ba-b059-3e76b2ddd529', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 44', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 22:25:07.708028+00', '', NULL),
	('298c041b-7fc9-4172-ba8b-70e978ccddef', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 45', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-29 00:00:57.15142+00', '', NULL),
	('bb2f84e7-6844-47cb-a73d-8c0fffe1668f', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 47', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-23 11:08:33.575455+00', '', NULL),
	('63987406-8d6b-4977-8dc3-b00b247e9622', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 48', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-25 16:52:56.396741+00', '', NULL),
	('be77f6b4-ae5c-44ee-aa82-fed9fbbe7438', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 49', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-25 01:37:39.185693+00', '', NULL),
	('923a04f4-9876-44fb-9fb2-7c47cc505b3f', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'Thông báo 50', 'Cập nhật trạng thái đơn hàng.', false, '2026-04-24 18:39:53.037362+00', '', NULL),
	('1f007a7d-6711-46d7-969d-ea659b5d50ab', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 13', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-28 16:27:41.834185+00', '', '2026-05-14 10:36:03.070128+00'),
	('48227e5e-8ae4-443e-9308-e00aa2bb738b', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 2', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 01:51:31.343021+00', '', '2026-05-14 10:36:07.612437+00'),
	('8294ce8e-4784-4797-a183-a8e720c806c9', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 4', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-28 02:39:40.819848+00', '', '2026-05-14 10:36:07.612437+00'),
	('7cf2d293-f10a-4c2e-aa95-47379020eff7', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 11', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 15:04:43.168039+00', '', '2026-05-14 10:36:07.612437+00'),
	('990221a9-b8d5-4274-b6df-10c988939f64', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 18', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-24 19:12:40.597301+00', '', '2026-05-14 10:36:07.612437+00'),
	('8ce7e6b8-eaf3-4e11-8b95-665144d15abc', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 19', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-28 13:11:00.510629+00', '', '2026-05-14 10:36:07.612437+00'),
	('32b99eec-caea-497c-bcab-bbe509e16f3a', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 24', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 20:02:18.627877+00', '', '2026-05-14 10:36:07.612437+00'),
	('b84398de-b087-47fc-9442-85cb922cece2', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 25', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-24 10:28:43.178275+00', '', '2026-05-14 10:36:07.612437+00'),
	('b2e18270-2458-4a81-afa8-99c8f43202fe', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 26', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-24 12:23:15.903157+00', '', '2026-05-14 10:36:07.612437+00'),
	('2483dc14-8206-41af-8a8e-84c363243ba6', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'Thông báo 46', 'Cập nhật trạng thái đơn hàng.', true, '2026-04-26 05:41:02.675632+00', '', '2026-05-14 10:36:07.612437+00'),
	('59deed7e-b6ed-4fdd-b708-1e4e7f00c711', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '🎉 Siêu Sale Cuối Tuần!', 'Tặng bạn mã freeship 100% cho mọi đơn hàng hôm nay. Nhanh tay kẻo lỡ!', false, '2026-05-15 05:37:24.039483+00', 'Tặng bạn mã freeship 100% cho mọi đơn hàng hôm nay. Nhanh tay kẻo lỡ!', NULL);


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."orders" ("id", "customer_id", "merchant_id", "shipper_id", "status", "total_price", "delivery_address", "created_at", "note", "updated_at", "merchant_lat", "merchant_lng", "delivery_lat", "delivery_lng", "shipper_lat", "shipper_lng") VALUES
	('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'COMPLETED', 50000, '123 Đường Lê Lợi, Quận 1, TP.HCM', '2026-04-20 08:05:22.905572+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('93519f47-62c5-4986-b1ae-3dac3b0f2e9c', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 429000, 'Số 67 Đường Y', '2026-04-19 07:07:26.8376+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('8f2a2132-06da-411c-a952-2b6134b134ac', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 122000, 'Số 79 Đường Y', '2026-04-27 20:43:02.889273+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('77606771-2f14-400f-96e6-38f30569cf4c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 117000, 'Số 58 Đường Y', '2026-04-24 17:43:47.227351+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('cadf147c-5a2f-438f-bbe0-7d7f5e4211bb', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 188000, 'Số 57 Đường Y', '2026-04-22 18:33:36.285559+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('39c12aa9-9006-43ef-92d3-157300ebd7b0', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 254000, 'Số 49 Đường Y', '2026-04-29 23:03:14.733603+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('9ec92c91-6565-483a-8afb-ab94642396ee', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 152000, 'Số 55 Đường Y', '2026-04-21 20:19:24.874298+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('ca5a5da1-fd82-473d-ade5-b5d493ba235c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 176000, 'Số 36 Đường Y', '2026-04-28 02:04:03.277117+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('d5234be2-6e17-418a-876d-96e4b85dba1b', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'CANCELLED', 266000, 'Số 25 Đường Y', '2026-04-28 08:06:11.062554+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('c0f05da2-a174-418f-ae2d-1d04b1c99efd', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 261000, 'Số 5 Đường Y', '2026-04-26 02:02:03.401255+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('37d38727-7bd7-49ae-b64b-a62691a1e44e', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'CANCELLED', 219000, 'Số 42 Đường Y', '2026-04-17 11:20:41.579892+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('29a4a201-2b37-4538-87e5-b1bc75e7bd78', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 216000, 'Số 16 Đường Y', '2026-04-18 02:27:35.12613+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('3c50e920-43be-492c-ad90-a02d2f9eb5c2', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', NULL, NULL, 'PENDING', 135000, '123', '2026-05-01 09:00:27.946645+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('b98af2a2-e900-4a57-9698-4e39f285815a', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 166000, 'Số 35 Đường Y', '2026-04-24 01:33:16.1114+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('c532de61-2c1a-4a2a-b905-7687170b11a0', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 240000, 'Số 69 Đường Y', '2026-04-20 12:07:06.411758+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('4a42d0ff-eb47-4d81-8aae-01dac824f67e', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 366000, 'Số 43 Đường Y', '2026-04-18 13:49:04.081486+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('eea37661-eef0-42e9-baec-e909a9d2aa63', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 167000, 'Số 58 Đường Y', '2026-04-27 03:39:31.654486+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('9cad6cc2-a4b3-48d9-8229-d3cee98e48d7', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 271000, 'Số 65 Đường Y', '2026-04-16 20:46:07.807129+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('7c0ca252-8726-42cd-a38a-7a369b625896', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'CANCELLED', 278000, 'Số 53 Đường Y', '2026-04-18 21:50:04.859206+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('0986a12e-6056-49d9-b39e-0b7ea9f2d716', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 110000, 'Số 44 Đường Y', '2026-04-17 15:45:00.98024+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('a2b76fec-0c70-433b-b4c2-c8075382ff1a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 105000, 'Số 97 Đường Y', '2026-04-23 15:34:36.050525+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('6a2565d8-2484-4b77-abb5-73d75ff18a3d', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 154000, 'Số 29 Đường Y', '2026-04-24 08:49:19.165658+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('5246d6cf-ea83-4305-a9db-796c8958cae6', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 200000, 'Số 0 Đường Y', '2026-04-17 00:05:05.333078+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('db168c3b-69b5-482b-815f-8c16a66689f3', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'CANCELLED', 384000, 'Số 31 Đường Y', '2026-04-16 11:49:08.930464+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('3496afc0-4e99-48a8-819d-d23cc81c83b3', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 276000, 'Số 84 Đường Y', '2026-04-22 21:13:50.196185+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('ff3b66f3-5679-4094-8b59-a8da6024e43a', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 360000, 'Số 5 Đường Y', '2026-04-26 00:14:28.622606+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('9dbfa11b-afd2-4831-9de0-2b6329dc904a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 238000, 'Số 8 Đường Y', '2026-04-19 07:41:32.229429+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('c7e7ab4b-4fe9-4ae2-99a0-d4c2ceaa80e6', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 210000, 'Số 31 Đường Y', '2026-04-19 02:18:32.669018+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('8781812e-27c7-4640-a194-8196280078fb', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 240000, 'Số 72 Đường Y', '2026-04-18 04:24:40.725866+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('d87d3e62-78d4-4adc-8133-5512361002a0', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 204000, 'Số 87 Đường Y', '2026-04-27 11:19:33.121204+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('365cf1f1-3858-4f6f-9b88-62c251c0f921', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'CANCELLED', 243000, 'Số 47 Đường Y', '2026-04-18 10:18:11.182521+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('cf7978d9-74ea-4b62-8179-26f92542a0e4', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 305000, 'Số 89 Đường Y', '2026-04-18 15:28:39.518334+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('c60fc82f-ee02-4b6e-a560-20645b343d6c', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 294000, 'Số 13 Đường Y', '2026-04-21 21:43:23.831695+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('6e3aadd5-4180-4618-aa52-12d7ac93551c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 252000, 'Số 65 Đường Y', '2026-04-24 22:21:03.319634+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('db9bb944-e756-42ad-8191-2fbf0578ba10', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 369000, 'Số 44 Đường Y', '2026-04-22 15:58:47.434743+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('0daa8810-473e-4695-9a92-b3014df4ac8a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 127000, 'Số 15 Đường Y', '2026-04-24 20:01:33.201744+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('472dc2c0-cdfa-4621-ac32-2faeea40a341', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 286000, 'Số 78 Đường Y', '2026-04-29 03:45:51.258163+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('75070f7b-8784-46e0-9794-aeff994fbb48', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', NULL, NULL, 'PENDING', 135000, 'Hà Nội', '2026-05-02 02:29:38.022456+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('6ae935b7-0184-49c1-a694-d93e2c246aa1', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', NULL, NULL, 'PENDING', 135000, 'Hà Nội', '2026-05-02 02:31:07.760515+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('f64ba6e7-4a3f-48e3-9d5a-507875cb9ab0', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', NULL, NULL, 'PENDING', 135000, 'Hà Nội', '2026-05-02 02:31:19.564774+00', 'â', '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('e2e35b64-6a84-4d59-9217-297afcdb0c4d', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'CANCELLED', 152000, 'Số 56 Đường Y', '2026-04-24 16:11:40.451009+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('3676b8d5-6b0b-4f02-a85c-a2427c6caf7e', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 130000, 'Số 72 Đường Y', '2026-04-23 11:27:58.698978+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('752687f8-58b6-45cd-b486-6186f83ad4d4', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 204000, 'Số 75 Đường Y', '2026-04-20 18:28:38.893117+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('ff85b99d-ff76-496c-aa05-b41c32de010c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 156000, 'Số 29 Đường Y', '2026-04-26 04:36:36.651151+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('c124ae99-c451-4cb3-a5ab-747b7a87a0d6', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PREPARING', 210000, 'Số 31 Đường Y', '2026-04-19 04:16:40.444218+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('b6887fd7-dae1-4b3c-8344-4fd6a96ada7b', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'PENDING', 306000, 'Số 13 Đường Y', '2026-04-26 23:00:50.349655+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('fce84650-c740-4c5c-a35d-afbefa1c7555', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 218000, 'Số 20 Đường Y', '2026-04-29 04:11:38.144438+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('7ccb9dbb-f6f9-48c9-b3e9-b871f33effbf', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 152000, 'Số 44 Đường Y', '2026-04-25 02:06:43.529319+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('1474a49a-757b-4ece-b759-0f1f428d32c4', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 324000, 'Số 81 Đường Y', '2026-04-25 05:28:38.380995+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('e7028869-4e44-4924-9e7c-c33dfeb06645', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 111000, 'Số 21 Đường Y', '2026-04-25 09:17:24.80998+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('2692d624-6dba-4728-bf35-685c9544a563', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 206000, 'Số 49 Đường Y', '2026-04-26 03:37:25.98707+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('1a38f35d-4e65-4a6f-9727-013b04453145', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 303000, 'Số 17 Đường Y', '2026-04-30 02:14:13.170966+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('261ad192-eecd-4a29-99cb-7225aec30e1e', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', NULL, NULL, 'PENDING', 135000, 'Hà Nội', '2026-05-02 02:31:21.647509+00', 'â', '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('7b15b343-df05-4b9a-a645-c386b434e8eb', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', NULL, NULL, 'PENDING', 31000, 'jsns', '2026-05-02 04:03:19.589919+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('46f5cfb6-1ea7-4b9f-aa3a-78f9ab6fc365', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'COMPLETED', 127000, 'Số 25 Đường Y', '2026-04-28 08:58:49.063737+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, NULL, NULL),
	('5eb7870f-26a3-4a6a-bf1d-b2bcf6a7ebab', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'DELIVERING', 342000, 'Số 51 Đường Y', '2026-04-28 05:13:43.885478+00', NULL, '2026-05-13 16:11:48.561922+00', 21.0285, 105.8542, 21.0318, 105.8124, 37.4219983, -122.084),
	('d444bb1d-9206-43cd-86f3-bf037bf02a41', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', '1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'DELIVERING', 236000, 'Số 62 Đường Y', '2026-04-18 19:23:32.843465+00', NULL, '2026-05-13 16:13:12.951271+00', 21.0285, 105.8542, 21.0318, 105.8124, 37.4219983, -122.084);


--
-- Data for Name: order_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."order_items" ("id", "order_id", "food_id", "quantity", "price_at_time") VALUES
	('a58b612f-7a01-431e-bd07-07cb265f82eb', '66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444441', 1, 45000),
	('3a559251-efcf-4506-a4be-852097acc708', '66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444442', 1, 5000),
	('6a3d349b-2e3f-4b02-bd1f-28a62397b211', '93519f47-62c5-4986-b1ae-3dac3b0f2e9c', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 76000),
	('f1102d24-bc71-45de-b95b-121f5525ad66', '93519f47-62c5-4986-b1ae-3dac3b0f2e9c', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 67000),
	('951c40ca-b4aa-4f0d-ba7d-97f787b7e4a0', '8f2a2132-06da-411c-a952-2b6134b134ac', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 2, 32000),
	('da100056-563d-4be8-a0d8-398ed5ad408d', '8f2a2132-06da-411c-a952-2b6134b134ac', '5d1a9866-de75-41d3-8004-0d5bf19a915a', 1, 58000),
	('a9cb30e5-a15c-444b-b3e8-db95d25ddc77', '77606771-2f14-400f-96e6-38f30569cf4c', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 1, 68000),
	('5a14d067-d8b0-467c-babc-7cb4dcc59c69', '77606771-2f14-400f-96e6-38f30569cf4c', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 1, 49000),
	('ef28ffac-0132-4811-8e2f-4788dc2c1f71', 'cadf147c-5a2f-438f-bbe0-7d7f5e4211bb', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 2, 74000),
	('efb9ffef-23b7-44cf-aa09-beb917a2edb8', 'cadf147c-5a2f-438f-bbe0-7d7f5e4211bb', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 1, 40000),
	('6f3bf8be-f960-44b6-b4c3-8a4eaedf93cc', '46f5cfb6-1ea7-4b9f-aa3a-78f9ab6fc365', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 3, 32000),
	('23a2ea17-73b2-44a7-ad6e-b5fba13fb1b9', '46f5cfb6-1ea7-4b9f-aa3a-78f9ab6fc365', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 1, 31000),
	('18d62095-68e8-4202-a4af-cbf9b1ec8b92', '39c12aa9-9006-43ef-92d3-157300ebd7b0', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 2, 75000),
	('031234d8-0815-46ac-9715-2be810202384', '39c12aa9-9006-43ef-92d3-157300ebd7b0', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 2, 52000),
	('833ef256-182b-4c4a-946f-0eeaa7614415', '9ec92c91-6565-483a-8afb-ab94642396ee', '8fad5a05-3959-41ae-a652-1ff2af0b6961', 3, 37000),
	('f2527e52-b704-4725-88b6-7438332e58af', '9ec92c91-6565-483a-8afb-ab94642396ee', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 1, 41000),
	('d6dea502-ac93-4ff2-a4e8-0492766e7af4', 'b98af2a2-e900-4a57-9698-4e39f285815a', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 1, 55000),
	('a63ce105-1f25-429e-b221-7cfc4e9e076d', 'b98af2a2-e900-4a57-9698-4e39f285815a', '4bf3a404-aace-416d-8513-53bf31bb5a63', 3, 37000),
	('ef3e82fb-c900-4651-b971-b1e7a74cfb87', 'c532de61-2c1a-4a2a-b905-7687170b11a0', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 42000),
	('e1990ce0-d657-4960-aefa-ea595edd4a8d', 'c532de61-2c1a-4a2a-b905-7687170b11a0', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 2, 57000),
	('04bd158f-8111-44db-8fe5-fcfcefb4bb93', '4a42d0ff-eb47-4d81-8aae-01dac824f67e', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 70000),
	('9a4c0ae7-2fce-4fbc-a8ed-22bcc3e86e36', '4a42d0ff-eb47-4d81-8aae-01dac824f67e', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 52000),
	('d002ee73-127e-44de-8b0b-26694ab5e048', 'eea37661-eef0-42e9-baec-e909a9d2aa63', '4bf3a404-aace-416d-8513-53bf31bb5a63', 1, 41000),
	('ce533d3f-e29a-4307-808b-2ebc3504fac8', 'eea37661-eef0-42e9-baec-e909a9d2aa63', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 42000),
	('ebedd17b-2f1c-41b8-b6cd-0e8cb7f13c21', '9cad6cc2-a4b3-48d9-8229-d3cee98e48d7', '4bf3a404-aace-416d-8513-53bf31bb5a63', 3, 71000),
	('a2486184-7815-40ce-8ff3-2defca1b9e6c', '9cad6cc2-a4b3-48d9-8229-d3cee98e48d7', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 1, 58000),
	('524f06dc-3cf1-4d5a-a0c2-56b89c01ef24', 'e7028869-4e44-4924-9e7c-c33dfeb06645', '4bf3a404-aace-416d-8513-53bf31bb5a63', 1, 37000),
	('afe20ad3-e819-4d04-abae-d40c6be94bda', 'e7028869-4e44-4924-9e7c-c33dfeb06645', '4bf3a404-aace-416d-8513-53bf31bb5a63', 1, 74000),
	('250d4236-ca3f-461b-9f64-94f3202239f4', '7c0ca252-8726-42cd-a38a-7a369b625896', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 2, 70000),
	('7e2f3960-d920-43b6-8050-18eaf3542065', '7c0ca252-8726-42cd-a38a-7a369b625896', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 3, 46000),
	('37d10fb0-92a3-46ad-a49e-41f7e2189bbb', '0986a12e-6056-49d9-b39e-0b7ea9f2d716', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 1, 32000),
	('e66b3118-754f-45a3-87f6-7e96e7094547', '0986a12e-6056-49d9-b39e-0b7ea9f2d716', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 2, 39000),
	('20b16534-2505-4a38-98f8-61228375a084', 'a2b76fec-0c70-433b-b4c2-c8075382ff1a', '4bf3a404-aace-416d-8513-53bf31bb5a63', 1, 44000),
	('7ed91dc5-cc30-4398-8a8b-dad80d218bbf', 'a2b76fec-0c70-433b-b4c2-c8075382ff1a', '4bf3a404-aace-416d-8513-53bf31bb5a63', 1, 61000),
	('3dfbfe3b-518a-4cec-a2b6-d571b674dc86', '6a2565d8-2484-4b77-abb5-73d75ff18a3d', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 2, 38000),
	('52e96f2d-f944-4ffb-98d0-3d5f144d1758', '6a2565d8-2484-4b77-abb5-73d75ff18a3d', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 1, 78000),
	('84675f27-7616-47e4-81da-856cca54012b', '5246d6cf-ea83-4305-a9db-796c8958cae6', '8fad5a05-3959-41ae-a652-1ff2af0b6961', 1, 44000),
	('e80c3741-dfc8-42f2-a68f-9d5bfc5866e7', '5246d6cf-ea83-4305-a9db-796c8958cae6', '2fbe8604-3117-4f43-a554-b1705035203d', 2, 78000),
	('ec2850f7-0f0d-46b8-a56f-075cb8ef3a7d', 'db168c3b-69b5-482b-815f-8c16a66689f3', '2fbe8604-3117-4f43-a554-b1705035203d', 3, 65000),
	('be46ed64-578c-4ca3-b90e-ee8d00e0d171', 'db168c3b-69b5-482b-815f-8c16a66689f3', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 3, 63000),
	('ea6043d9-371f-4b92-815f-0eab71dbd9ac', '3496afc0-4e99-48a8-819d-d23cc81c83b3', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 1, 63000),
	('7b244738-64d4-41e1-a28c-caa9ef9e116d', '3496afc0-4e99-48a8-819d-d23cc81c83b3', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 3, 71000),
	('dcab9670-1463-4459-9700-cf73f5486357', 'ff3b66f3-5679-4094-8b59-a8da6024e43a', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 44000),
	('4b81fa69-13d9-47b5-a3ab-e8995f5645f5', 'ff3b66f3-5679-4094-8b59-a8da6024e43a', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 76000),
	('28c780d9-32d3-47ef-a10e-36bbda4a6585', '9dbfa11b-afd2-4831-9de0-2b6329dc904a', '5d1a9866-de75-41d3-8004-0d5bf19a915a', 2, 53000),
	('bf2ee0b5-4b7a-461b-a3d0-c44c4580c5a0', '9dbfa11b-afd2-4831-9de0-2b6329dc904a', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 2, 66000),
	('825d5369-0cd6-4b73-9524-25b0ed4b0506', 'c7e7ab4b-4fe9-4ae2-99a0-d4c2ceaa80e6', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 1, 60000),
	('22cbb969-272b-4b21-987e-83b86916e036', 'c7e7ab4b-4fe9-4ae2-99a0-d4c2ceaa80e6', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 3, 50000),
	('e39920b9-ab08-486c-85a7-4997b8086fc8', '8781812e-27c7-4640-a194-8196280078fb', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 3, 55000),
	('5c0ec20a-93ab-460a-99f5-73b1a6c11216', '8781812e-27c7-4640-a194-8196280078fb', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 1, 75000),
	('6e8e9fa2-7d0b-4974-9aa6-dec19f7a92d0', 'd87d3e62-78d4-4adc-8133-5512361002a0', '2fbe8604-3117-4f43-a554-b1705035203d', 2, 79000),
	('64351bad-33e6-43cb-b12b-f9aa8b845895', 'd87d3e62-78d4-4adc-8133-5512361002a0', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 1, 46000),
	('213d346d-24b0-4cfe-ba15-679b7d86637c', '365cf1f1-3858-4f6f-9b88-62c251c0f921', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 3, 34000),
	('3f24bfcb-7dd8-4408-b400-a93aee052a0e', '365cf1f1-3858-4f6f-9b88-62c251c0f921', '4bf3a404-aace-416d-8513-53bf31bb5a63', 3, 47000),
	('ae68f54b-0e19-45fa-8649-95bf16d7100f', '2692d624-6dba-4728-bf35-685c9544a563', '8fad5a05-3959-41ae-a652-1ff2af0b6961', 1, 50000),
	('d0d94547-a368-4932-b3c5-0ff4ddef5079', '2692d624-6dba-4728-bf35-685c9544a563', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 52000),
	('517d58e0-b1cc-4095-9601-ae5eae397bec', '1a38f35d-4e65-4a6f-9727-013b04453145', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 41000),
	('a1d1d22d-8f20-4519-9275-2956911e686e', '1a38f35d-4e65-4a6f-9727-013b04453145', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 60000),
	('eca91331-bab8-4ea9-9ec9-33fcef575ae3', 'cf7978d9-74ea-4b62-8179-26f92542a0e4', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 3, 76000),
	('7728f3bd-fd63-4a46-bb3e-b925a5b80a08', 'cf7978d9-74ea-4b62-8179-26f92542a0e4', '5d1a9866-de75-41d3-8004-0d5bf19a915a', 1, 77000),
	('bcd5055a-3229-4c05-b1ae-5fe657ef4370', 'c60fc82f-ee02-4b6e-a560-20645b343d6c', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 31000),
	('7da5065d-7bc6-4003-a18d-1a3236a31509', 'c60fc82f-ee02-4b6e-a560-20645b343d6c', '8fad5a05-3959-41ae-a652-1ff2af0b6961', 3, 67000),
	('9ffb9e5c-b1fb-4592-a66a-69edf6e8d830', '7ccb9dbb-f6f9-48c9-b3e9-b871f33effbf', '5d1a9866-de75-41d3-8004-0d5bf19a915a', 2, 48000),
	('833569aa-d4b7-43d2-b99e-e05c24a10213', '7ccb9dbb-f6f9-48c9-b3e9-b871f33effbf', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 1, 56000),
	('025fc51a-20b9-46ba-8460-9c2234a4ace1', '6e3aadd5-4180-4618-aa52-12d7ac93551c', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 70000),
	('5b44da7f-a788-4e10-9e39-fdfd58d912be', '6e3aadd5-4180-4618-aa52-12d7ac93551c', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 1, 42000),
	('c63fb68e-ec71-40e5-8a82-e2c482ba0d3a', 'db9bb944-e756-42ad-8191-2fbf0578ba10', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 3, 69000),
	('7723b244-21fc-4b37-9259-223f10b250b0', 'db9bb944-e756-42ad-8191-2fbf0578ba10', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 54000),
	('7540760b-f0cf-4ae3-a769-0e7c4bab49a3', '0daa8810-473e-4695-9a92-b3014df4ac8a', '8fad5a05-3959-41ae-a652-1ff2af0b6961', 1, 75000),
	('44a84dc4-111a-44d1-b37d-57cf915a6469', '0daa8810-473e-4695-9a92-b3014df4ac8a', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 1, 52000),
	('b5cafa8c-23be-47e8-8cef-fc96ed7c14ab', '472dc2c0-cdfa-4621-ac32-2faeea40a341', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 76000),
	('6c8c3074-692f-418c-a3b6-bdfa28b96494', '472dc2c0-cdfa-4621-ac32-2faeea40a341', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 1, 58000),
	('e76cefbd-0435-45d9-be41-dd208d0a12da', 'e2e35b64-6a84-4d59-9217-297afcdb0c4d', '2fbe8604-3117-4f43-a554-b1705035203d', 2, 33000),
	('d3228164-2b55-4cdd-ab62-e611f1cb2e8a', 'e2e35b64-6a84-4d59-9217-297afcdb0c4d', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 2, 43000),
	('ec35ae1e-c10c-4651-83ac-10cbb531bfdf', '3676b8d5-6b0b-4f02-a85c-a2427c6caf7e', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 1, 50000),
	('755fa4fd-1a04-4d96-a36f-cde44b819bcf', '3676b8d5-6b0b-4f02-a85c-a2427c6caf7e', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 2, 40000),
	('6d4c9c52-74c2-4be3-a8b5-6f2564f6139e', '752687f8-58b6-45cd-b486-6186f83ad4d4', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 3, 49000),
	('83d38a3e-9688-4c46-8860-578e3899b008', '752687f8-58b6-45cd-b486-6186f83ad4d4', '2fbe8604-3117-4f43-a554-b1705035203d', 1, 57000),
	('a5c6806a-5e6a-4164-8219-ea9d39e984b8', 'ff85b99d-ff76-496c-aa05-b41c32de010c', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 1, 34000),
	('b5f2384a-3939-4cad-883e-03919cd46c90', 'ff85b99d-ff76-496c-aa05-b41c32de010c', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 2, 61000),
	('319a1f68-7699-41bb-888b-f794a9376528', '1474a49a-757b-4ece-b759-0f1f428d32c4', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 3, 37000),
	('19c5a8a4-3e49-4be4-bd1f-0a4f73b8b6d6', '1474a49a-757b-4ece-b759-0f1f428d32c4', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 3, 71000),
	('43ff34c3-28e0-42a8-b1da-43b2483bc7e5', 'c124ae99-c451-4cb3-a5ab-747b7a87a0d6', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 1, 60000),
	('eb913670-cad4-4575-a83b-0e6783caf0af', 'c124ae99-c451-4cb3-a5ab-747b7a87a0d6', 'e1c67d13-aa19-49ba-88db-104e806505a4', 2, 75000),
	('a136a712-79bb-40bc-8e2f-83cdf07b5f9c', 'b6887fd7-dae1-4b3c-8344-4fd6a96ada7b', 'e1c67d13-aa19-49ba-88db-104e806505a4', 3, 60000),
	('8afcb557-e7ba-48f8-b9e4-61943d9b7f95', 'b6887fd7-dae1-4b3c-8344-4fd6a96ada7b', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 3, 42000),
	('be0efb37-ed3d-46c4-8b8e-46d1561bdbc7', 'fce84650-c740-4c5c-a35d-afbefa1c7555', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 3, 34000),
	('3b9f7124-16d8-466a-bfff-a5f848531b5c', 'fce84650-c740-4c5c-a35d-afbefa1c7555', '4bf3a404-aace-416d-8513-53bf31bb5a63', 2, 58000),
	('c752ca24-2a9d-4902-b53a-c38ce8ccde94', 'd444bb1d-9206-43cd-86f3-bf037bf02a41', 'e1c67d13-aa19-49ba-88db-104e806505a4', 2, 59000),
	('83b52488-0179-4225-9a93-ab3c8320e1b9', 'd444bb1d-9206-43cd-86f3-bf037bf02a41', 'e1c67d13-aa19-49ba-88db-104e806505a4', 2, 59000),
	('f7d073f6-135c-4ef5-8989-ebf8c70e8f40', '5eb7870f-26a3-4a6a-bf1d-b2bcf6a7ebab', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 3, 65000),
	('38e74e79-587d-4711-a65b-c136153e77c5', '5eb7870f-26a3-4a6a-bf1d-b2bcf6a7ebab', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 3, 49000),
	('505fa6eb-a94f-4d01-a46a-d1c62d2d771f', 'ca5a5da1-fd82-473d-ade5-b5d493ba235c', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 2, 58000),
	('b329a353-107f-443c-989c-b170d2c81804', 'ca5a5da1-fd82-473d-ade5-b5d493ba235c', '8fad5a05-3959-41ae-a652-1ff2af0b6961', 2, 30000),
	('56e10b28-f7f8-4ccc-8db7-1ac5e4460187', 'd5234be2-6e17-418a-876d-96e4b85dba1b', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530', 2, 49000),
	('51086afc-e63c-408b-9f7c-bc30cb15ecba', 'd5234be2-6e17-418a-876d-96e4b85dba1b', '2ed0f966-e15f-45cd-bc0e-95542f01662f', 3, 56000),
	('6bb074c0-4297-46c7-941a-6c0bcc6cce60', 'c0f05da2-a174-418f-ae2d-1d04b1c99efd', '2fbe8604-3117-4f43-a554-b1705035203d', 3, 37000),
	('28f18414-eea0-494b-8809-4093ca39cce3', 'c0f05da2-a174-418f-ae2d-1d04b1c99efd', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88', 2, 75000),
	('a5a1ca1e-3ca0-4e02-82b7-3bb3554bdfc5', '37d38727-7bd7-49ae-b64b-a62691a1e44e', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 2, 42000),
	('921a77fa-669a-4a4e-8052-7e43f17cfa58', '37d38727-7bd7-49ae-b64b-a62691a1e44e', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe', 3, 45000),
	('ac5587fb-c558-47b6-84a0-a22c5ef248c8', '29a4a201-2b37-4538-87e5-b1bc75e7bd78', '4bf3a404-aace-416d-8513-53bf31bb5a63', 3, 58000),
	('e00930a2-51ab-47bd-9b0e-5eab9f1e83bb', '29a4a201-2b37-4538-87e5-b1bc75e7bd78', 'f015c844-e41e-44da-a1fd-0b9a66554c64', 1, 42000);


--
-- Data for Name: payment_settings; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."payments" ("id", "customer_id", "order_id", "amount", "method", "status", "delivery_address", "note", "created_at", "provider", "transaction_id") VALUES
	('5a11a825-1d92-48be-8cdc-21ef73d76fe5', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '93519f47-62c5-4986-b1ae-3dac3b0f2e9c', 429000, 'WALLET', 'SUCCESS', 'Số 82 Đường Y', NULL, '2026-04-19 07:07:26.8376+00', NULL, NULL),
	('a32745e2-c79c-414d-a692-c1f912ea42d9', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '8f2a2132-06da-411c-a952-2b6134b134ac', 122000, 'CARD', 'SUCCESS', 'Số 78 Đường Y', NULL, '2026-04-27 20:43:02.889273+00', NULL, NULL),
	('aeb6a675-96a9-4348-bf4f-0e4328be9675', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '77606771-2f14-400f-96e6-38f30569cf4c', 117000, 'CARD', 'SUCCESS', 'Số 67 Đường Y', NULL, '2026-04-24 17:43:47.227351+00', NULL, NULL),
	('b13b32fd-fb5d-460a-9bec-1dfe0cbaeb57', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'cadf147c-5a2f-438f-bbe0-7d7f5e4211bb', 188000, 'WALLET', 'SUCCESS', 'Số 42 Đường Y', NULL, '2026-04-22 18:33:36.285559+00', NULL, NULL),
	('dc5af51c-e349-4d9b-bc62-42d000a6e6ea', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '46f5cfb6-1ea7-4b9f-aa3a-78f9ab6fc365', 127000, 'WALLET', 'SUCCESS', 'Số 3 Đường Y', NULL, '2026-04-28 08:58:49.063737+00', NULL, NULL),
	('cdcd586c-b47b-4843-8fcd-ae8fe21e199e', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '39c12aa9-9006-43ef-92d3-157300ebd7b0', 254000, 'CARD', 'SUCCESS', 'Số 66 Đường Y', NULL, '2026-04-29 23:03:14.733603+00', NULL, NULL),
	('2221f047-a0fb-42de-89b3-4fc2f7de99c0', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '9ec92c91-6565-483a-8afb-ab94642396ee', 152000, 'WALLET', 'SUCCESS', 'Số 67 Đường Y', NULL, '2026-04-21 20:19:24.874298+00', NULL, NULL),
	('1ef1a4ae-7192-406f-917c-3178fee3676a', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'b98af2a2-e900-4a57-9698-4e39f285815a', 166000, 'CARD', 'SUCCESS', 'Số 68 Đường Y', NULL, '2026-04-24 01:33:16.1114+00', NULL, NULL),
	('be818618-76da-4a23-8b98-98dee142e05a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'c532de61-2c1a-4a2a-b905-7687170b11a0', 240000, 'COD', 'SUCCESS', 'Số 48 Đường Y', NULL, '2026-04-20 12:07:06.411758+00', NULL, NULL),
	('f160b2d0-04b4-49ac-a0c8-772b04623105', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '4a42d0ff-eb47-4d81-8aae-01dac824f67e', 366000, 'CARD', 'SUCCESS', 'Số 31 Đường Y', NULL, '2026-04-18 13:49:04.081486+00', NULL, NULL),
	('dbe67c5a-2975-411a-905e-90884fcdc7c7', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'eea37661-eef0-42e9-baec-e909a9d2aa63', 167000, 'COD', 'SUCCESS', 'Số 36 Đường Y', NULL, '2026-04-27 03:39:31.654486+00', NULL, NULL),
	('5cd31ebc-9ff5-4cb3-a337-c14894983616', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '9cad6cc2-a4b3-48d9-8229-d3cee98e48d7', 271000, 'WALLET', 'SUCCESS', 'Số 84 Đường Y', NULL, '2026-04-16 20:46:07.807129+00', NULL, NULL),
	('41366e92-ed36-4561-a758-bffef87e1c88', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'e7028869-4e44-4924-9e7c-c33dfeb06645', 111000, 'CARD', 'SUCCESS', 'Số 28 Đường Y', NULL, '2026-04-25 09:17:24.80998+00', NULL, NULL),
	('15698905-91b2-407e-aaab-570f23b63cd2', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '7c0ca252-8726-42cd-a38a-7a369b625896', 278000, 'CARD', 'SUCCESS', 'Số 27 Đường Y', NULL, '2026-04-18 21:50:04.859206+00', NULL, NULL),
	('582b5536-54dd-4705-8d5b-52abbf640e6b', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '0986a12e-6056-49d9-b39e-0b7ea9f2d716', 110000, 'CARD', 'SUCCESS', 'Số 55 Đường Y', NULL, '2026-04-17 15:45:00.98024+00', NULL, NULL),
	('5737def2-7814-4f03-a56c-074bd39d448d', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'a2b76fec-0c70-433b-b4c2-c8075382ff1a', 105000, 'COD', 'SUCCESS', 'Số 88 Đường Y', NULL, '2026-04-23 15:34:36.050525+00', NULL, NULL),
	('280e9fbb-cca5-4133-8bb7-48070a1eeeba', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '6a2565d8-2484-4b77-abb5-73d75ff18a3d', 154000, 'COD', 'SUCCESS', 'Số 70 Đường Y', NULL, '2026-04-24 08:49:19.165658+00', NULL, NULL),
	('a0a5adaf-2e12-4d70-9640-f271a88f5cc8', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '5246d6cf-ea83-4305-a9db-796c8958cae6', 200000, 'COD', 'SUCCESS', 'Số 10 Đường Y', NULL, '2026-04-17 00:05:05.333078+00', NULL, NULL),
	('ba3574c5-645a-456c-8f1e-070ce753e087', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'db168c3b-69b5-482b-815f-8c16a66689f3', 384000, 'CARD', 'SUCCESS', 'Số 46 Đường Y', NULL, '2026-04-16 11:49:08.930464+00', NULL, NULL),
	('ef52156c-0d06-4c14-a236-8169d2ad2778', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '3496afc0-4e99-48a8-819d-d23cc81c83b3', 276000, 'CARD', 'SUCCESS', 'Số 0 Đường Y', NULL, '2026-04-22 21:13:50.196185+00', NULL, NULL),
	('f152c4c8-5527-40ae-b88e-490f0fe8ee31', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'ff3b66f3-5679-4094-8b59-a8da6024e43a', 360000, 'WALLET', 'SUCCESS', 'Số 78 Đường Y', NULL, '2026-04-26 00:14:28.622606+00', NULL, NULL),
	('75b05bc3-b40d-4311-aba5-5dc250feef47', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '9dbfa11b-afd2-4831-9de0-2b6329dc904a', 238000, 'COD', 'SUCCESS', 'Số 22 Đường Y', NULL, '2026-04-19 07:41:32.229429+00', NULL, NULL),
	('880b7c8d-5e22-466f-b56d-fa5db78c9c54', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'c7e7ab4b-4fe9-4ae2-99a0-d4c2ceaa80e6', 210000, 'CARD', 'SUCCESS', 'Số 6 Đường Y', NULL, '2026-04-19 02:18:32.669018+00', NULL, NULL),
	('9a479afe-2d3e-4208-8a34-0c8d29f16222', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '8781812e-27c7-4640-a194-8196280078fb', 240000, 'WALLET', 'SUCCESS', 'Số 67 Đường Y', NULL, '2026-04-18 04:24:40.725866+00', NULL, NULL),
	('9fdd85f0-f78d-4359-bc3d-98213c2cd8a6', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'd87d3e62-78d4-4adc-8133-5512361002a0', 204000, 'CARD', 'SUCCESS', 'Số 60 Đường Y', NULL, '2026-04-27 11:19:33.121204+00', NULL, NULL),
	('6492de57-d37f-4386-91d2-0601b2286b82', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '365cf1f1-3858-4f6f-9b88-62c251c0f921', 243000, 'COD', 'SUCCESS', 'Số 22 Đường Y', NULL, '2026-04-18 10:18:11.182521+00', NULL, NULL),
	('2f20e1fd-b178-4086-bae9-082b9dc2ba04', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '2692d624-6dba-4728-bf35-685c9544a563', 206000, 'COD', 'SUCCESS', 'Số 99 Đường Y', NULL, '2026-04-26 03:37:25.98707+00', NULL, NULL),
	('958870a5-e6fd-4927-a529-d4e6cc5f5944', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '1a38f35d-4e65-4a6f-9727-013b04453145', 303000, 'CARD', 'SUCCESS', 'Số 97 Đường Y', NULL, '2026-04-30 02:14:13.170966+00', NULL, NULL),
	('5d1f3bab-551d-4b88-837e-83767946a61a', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'cf7978d9-74ea-4b62-8179-26f92542a0e4', 305000, 'COD', 'SUCCESS', 'Số 12 Đường Y', NULL, '2026-04-18 15:28:39.518334+00', NULL, NULL),
	('4f6c05cc-3c8a-489c-9689-b4c24b8bc252', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'c60fc82f-ee02-4b6e-a560-20645b343d6c', 294000, 'COD', 'SUCCESS', 'Số 34 Đường Y', NULL, '2026-04-21 21:43:23.831695+00', NULL, NULL),
	('9d39b406-1221-4ef1-8ea8-1cc076b9851e', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7ccb9dbb-f6f9-48c9-b3e9-b871f33effbf', 152000, 'CARD', 'SUCCESS', 'Số 24 Đường Y', NULL, '2026-04-25 02:06:43.529319+00', NULL, NULL),
	('b08028a0-3c48-4dc8-af85-b9b66e450e0e', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '6e3aadd5-4180-4618-aa52-12d7ac93551c', 252000, 'CARD', 'SUCCESS', 'Số 5 Đường Y', NULL, '2026-04-24 22:21:03.319634+00', NULL, NULL),
	('85d1a63c-a411-4da3-968c-c1928c9249dd', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'db9bb944-e756-42ad-8191-2fbf0578ba10', 369000, 'CARD', 'SUCCESS', 'Số 39 Đường Y', NULL, '2026-04-22 15:58:47.434743+00', NULL, NULL),
	('d7343277-bb38-42ac-b5fd-7b3664894a19', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '0daa8810-473e-4695-9a92-b3014df4ac8a', 127000, 'COD', 'SUCCESS', 'Số 46 Đường Y', NULL, '2026-04-24 20:01:33.201744+00', NULL, NULL),
	('65515ad7-f7bd-4bb8-ad22-2cee0f6f45cc', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '472dc2c0-cdfa-4621-ac32-2faeea40a341', 286000, 'COD', 'SUCCESS', 'Số 6 Đường Y', NULL, '2026-04-29 03:45:51.258163+00', NULL, NULL),
	('8d68c2fc-0f68-4e96-9586-4b5196cf8938', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'e2e35b64-6a84-4d59-9217-297afcdb0c4d', 152000, 'WALLET', 'SUCCESS', 'Số 10 Đường Y', NULL, '2026-04-24 16:11:40.451009+00', NULL, NULL),
	('c69951fa-c0c7-4678-b854-23934e83edf8', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '3676b8d5-6b0b-4f02-a85c-a2427c6caf7e', 130000, 'WALLET', 'SUCCESS', 'Số 47 Đường Y', NULL, '2026-04-23 11:27:58.698978+00', NULL, NULL),
	('c6fe7ebc-349c-4d7f-9e74-5f88c21e9443', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '752687f8-58b6-45cd-b486-6186f83ad4d4', 204000, 'COD', 'SUCCESS', 'Số 65 Đường Y', NULL, '2026-04-20 18:28:38.893117+00', NULL, NULL),
	('c7a1cb21-9796-4c1f-83f7-46a7e936c0f2', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'ff85b99d-ff76-496c-aa05-b41c32de010c', 156000, 'CARD', 'SUCCESS', 'Số 49 Đường Y', NULL, '2026-04-26 04:36:36.651151+00', NULL, NULL),
	('60514409-0754-4de4-b011-6dcbb302252c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '1474a49a-757b-4ece-b759-0f1f428d32c4', 324000, 'WALLET', 'SUCCESS', 'Số 86 Đường Y', NULL, '2026-04-25 05:28:38.380995+00', NULL, NULL),
	('d0fb6aab-9869-42fc-8e4e-6850dbb9f9be', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'c124ae99-c451-4cb3-a5ab-747b7a87a0d6', 210000, 'COD', 'SUCCESS', 'Số 34 Đường Y', NULL, '2026-04-19 04:16:40.444218+00', NULL, NULL),
	('5ed6f3d6-752e-4ec1-a3d0-dd60fe98eb1d', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'b6887fd7-dae1-4b3c-8344-4fd6a96ada7b', 306000, 'CARD', 'SUCCESS', 'Số 87 Đường Y', NULL, '2026-04-26 23:00:50.349655+00', NULL, NULL),
	('9a910265-c2f0-4856-8c4c-c633dc376ca0', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'fce84650-c740-4c5c-a35d-afbefa1c7555', 218000, 'CARD', 'SUCCESS', 'Số 98 Đường Y', NULL, '2026-04-29 04:11:38.144438+00', NULL, NULL),
	('fb19ff78-2cf1-43d0-abbb-ed332e0239bd', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'd444bb1d-9206-43cd-86f3-bf037bf02a41', 236000, 'CARD', 'SUCCESS', 'Số 68 Đường Y', NULL, '2026-04-18 19:23:32.843465+00', NULL, NULL),
	('e21af606-5b55-465d-8863-e3b8e70eb8c6', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '5eb7870f-26a3-4a6a-bf1d-b2bcf6a7ebab', 342000, 'COD', 'SUCCESS', 'Số 19 Đường Y', NULL, '2026-04-28 05:13:43.885478+00', NULL, NULL),
	('18d52dae-a244-4be2-8442-af5636b1eaf8', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'ca5a5da1-fd82-473d-ade5-b5d493ba235c', 176000, 'COD', 'SUCCESS', 'Số 51 Đường Y', NULL, '2026-04-28 02:04:03.277117+00', NULL, NULL),
	('7ce29772-649b-49b4-8181-bdebd886eef6', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'd5234be2-6e17-418a-876d-96e4b85dba1b', 266000, 'WALLET', 'SUCCESS', 'Số 82 Đường Y', NULL, '2026-04-28 08:06:11.062554+00', NULL, NULL),
	('4184e253-f199-4100-a7d0-86f558d1b6d8', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 'c0f05da2-a174-418f-ae2d-1d04b1c99efd', 261000, 'CARD', 'SUCCESS', 'Số 41 Đường Y', NULL, '2026-04-26 02:02:03.401255+00', NULL, NULL),
	('8cecf874-eded-4447-bd9c-c7b6e426f77e', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '37d38727-7bd7-49ae-b64b-a62691a1e44e', 219000, 'CARD', 'SUCCESS', 'Số 1 Đường Y', NULL, '2026-04-17 11:20:41.579892+00', NULL, NULL),
	('c3a5e230-57b6-48eb-86ef-7c67bf4d16c7', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '29a4a201-2b37-4538-87e5-b1bc75e7bd78', 216000, 'WALLET', 'SUCCESS', 'Số 0 Đường Y', NULL, '2026-04-18 02:27:35.12613+00', NULL, NULL),
	('7d74c042-099a-4459-b753-be5c95dc07de', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', '3c50e920-43be-492c-ad90-a02d2f9eb5c2', 135000, 'COD', 'PENDING', '123', NULL, '2026-05-01 09:00:28.517769+00', NULL, NULL),
	('3638dd27-17a0-4f25-9d31-a0858b9fe886', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '75070f7b-8784-46e0-9794-aeff994fbb48', 135000, 'COD', 'PENDING', 'Hà Nội', NULL, '2026-05-02 02:29:38.191197+00', NULL, NULL),
	('19f67179-6a16-498e-952c-d7ea00c51225', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', '7b15b343-df05-4b9a-a645-c386b434e8eb', 31000, 'CARD', 'PENDING', 'jsns', NULL, '2026-05-02 04:03:19.817355+00', NULL, NULL);


--
-- Data for Name: profiles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."profiles" ("id", "email", "full_name", "role", "phone", "address", "created_at", "updated_at", "balance", "reward_points", "fcm_token") VALUES
	('ea545185-a6bc-48d3-9277-84f1a1bf021b', 'dtl1233210@gmail.com', 'dtl1233210', 'CUSTOMER', NULL, NULL, '2026-04-27 03:36:43.914957+00', '2026-04-27 08:21:03.750776+00', 0, 0, NULL),
	('1b51bd12-ba0e-41d9-9f49-f61a583da0b6', 'shipper1@test.com', 'Tài xế 1', 'SHIPPER', '0920000001', 'Địa chỉ TX 1', '2026-04-30 03:47:15.164116+00', '2026-04-30 03:47:15.164116+00', 0, 0, NULL),
	('7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'tl1133210@gmail.com', 'Chủ quán 1', 'MERCHANT', '0910000001', '123', '2026-04-30 03:47:15.164116+00', '2026-05-11 01:07:47.873614+00', 0, 0, NULL),
	('4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 'customer2@test.com', 'Guest', 'CUSTOMER', '0912491270', 'Địa chỉ KH 2', '2026-04-30 03:47:15.164116+00', '2026-05-15 05:39:46.775201+00', 0, 0, 'fbcHDXjnRwun12S-XTOrnn:APA91bGVplE3j_Vtjb_78w4fR3PAm5SCHljCH7-Clv-ma8orFIPTeL5LFcUm5iEnfL2BwdcMbJs7Q7MQfS7qhRr3-ETcnQoyVFAdOdReOGb5Hp-Ee_KeGEg');


--
-- Data for Name: reviews; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."reviews" ("id", "order_id", "customer_id", "rating", "comment", "created_at", "food_id") VALUES
	('37de7a4f-e51f-4d4d-8118-90ab56f0ad6e', '66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 5, 'Quán làm đồ ăn ngon, shipper giao rất nhanh và thân thiện!', '2026-04-20 08:05:22.905572+00', NULL),
	('e1897168-a2f9-4aae-9324-57c24e153f4c', '93519f47-62c5-4986-b1ae-3dac3b0f2e9c', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 5, 'Đồ ăn ngon', '2026-04-19 09:07:26.8376+00', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88'),
	('aa9c2d3b-83e4-4ef2-adcb-3c3d3a69f462', '77606771-2f14-400f-96e6-38f30569cf4c', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 5, 'Đồ ăn ngon', '2026-04-24 19:43:47.227351+00', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530'),
	('ebd4f857-b576-4be4-a94b-0b8822e206dd', 'cadf147c-5a2f-438f-bbe0-7d7f5e4211bb', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 5, 'Đồ ăn ngon', '2026-04-22 20:33:36.285559+00', '2ed0f966-e15f-45cd-bc0e-95542f01662f'),
	('d5fcbd6c-f892-46e8-a4e3-b88586f8a0ea', 'c532de61-2c1a-4a2a-b905-7687170b11a0', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 5, 'Đồ ăn ngon', '2026-04-20 14:07:06.411758+00', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88'),
	('c8d56616-caec-4343-8a2c-d7dab337c885', '9cad6cc2-a4b3-48d9-8229-d3cee98e48d7', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 4, 'Đồ ăn ngon', '2026-04-16 22:46:07.807129+00', '4bf3a404-aace-416d-8513-53bf31bb5a63'),
	('e07528f9-5db4-44bf-8b3f-df5725f2d0fd', '0986a12e-6056-49d9-b39e-0b7ea9f2d716', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 5, 'Đồ ăn ngon', '2026-04-17 17:45:00.98024+00', '2ed0f966-e15f-45cd-bc0e-95542f01662f'),
	('996b2233-8f3d-4fc5-8505-b9c7db4a8420', '5246d6cf-ea83-4305-a9db-796c8958cae6', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 5, 'Đồ ăn ngon', '2026-04-17 02:05:05.333078+00', '8fad5a05-3959-41ae-a652-1ff2af0b6961'),
	('10b564cf-e473-420f-a232-e3a6ba2d66c7', '3496afc0-4e99-48a8-819d-d23cc81c83b3', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 4, 'Đồ ăn ngon', '2026-04-22 23:13:50.196185+00', '3f2c3f9a-7bd3-4c73-916a-b568a7bddb88'),
	('a5c41012-37bc-43b8-9864-987c70824120', '8781812e-27c7-4640-a194-8196280078fb', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 4, 'Đồ ăn ngon', '2026-04-18 06:24:40.725866+00', '4ffa9305-7d4f-4f44-86d2-4ad86b30a530'),
	('023a30c2-9568-4c1a-ac9f-247ebd357bd5', 'cf7978d9-74ea-4b62-8179-26f92542a0e4', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 4, 'Đồ ăn ngon', '2026-04-18 17:28:39.518334+00', '2ed0f966-e15f-45cd-bc0e-95542f01662f'),
	('ebaa9390-34ca-40b4-842f-27034590ecc4', '29a4a201-2b37-4538-87e5-b1bc75e7bd78', '4d29d1c0-a622-4d6d-85ee-6c1b0f14f078', 4, 'Đồ ăn ngon', '2026-04-18 04:27:35.12613+00', '4bf3a404-aace-416d-8513-53bf31bb5a63'),
	('34ad02ca-6577-44f3-aeb0-f9047cbabe29', 'fce84650-c740-4c5c-a35d-afbefa1c7555', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 4, 'hơi mặn quá', '2026-05-08 09:43:40.29461+00', NULL),
	('9ba9cbca-ffb9-4b05-860c-ff02f644e9ed', 'fce84650-c740-4c5c-a35d-afbefa1c7555', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 3, 'không ngon lắm', '2026-05-08 09:49:15.061612+00', NULL),
	('22e0b93f-742f-431b-b094-75949edcbe94', 'fce84650-c740-4c5c-a35d-afbefa1c7555', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 2, 'bad feel', '2026-05-08 15:30:18.796664+00', NULL),
	('4a15f2f0-952c-4dc1-aca0-855e801dcff2', 'fce84650-c740-4c5c-a35d-afbefa1c7555', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 5, 'ngon', '2026-04-29 06:11:38.144438+00', '1d1235ac-6edf-48ac-a695-ae87cf6fe5fe'),
	('df28c5ab-c798-4035-937b-1d6f3df985bd', 'fce84650-c740-4c5c-a35d-afbefa1c7555', 'ea545185-a6bc-48d3-9277-84f1a1bf021b', 4, 'mid', '2026-05-08 15:36:27.727574+00', '4bf3a404-aace-416d-8513-53bf31bb5a63');


--
-- Data for Name: vouchers; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."vouchers" ("id", "merchant_id", "code", "discount_percent", "max_discount", "min_order_value", "valid_until", "discount_amount", "is_active", "expires_at", "created_at") VALUES
	('55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 'GIAM10', 10, 20000, 40000, '2026-12-31 23:59:59+00', 0, true, NULL, '2026-05-14 09:44:50.620845+00'),
	('2223b7b0-1cfe-4b2b-af51-c6e09989be36', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'KM8119', 10, 50000, 100000, '2026-05-15 03:47:15.164116+00', 0, true, NULL, '2026-05-14 09:44:50.620845+00'),
	('8b5982d2-5895-47c5-bf07-9b66eaef56d9', '7eac7482-0cb0-40fa-8141-2c8f746c84bc', 'HOT8755', 20, 100000, 200000, '2026-05-15 03:47:15.164116+00', 0, true, NULL, '2026-05-14 09:44:50.620845+00'),
	('c11040bc-a637-4ee4-ac4b-70e5c3d534a2', NULL, 'GIAM20K', 0, 0, 0, '2027-05-14 09:44:50.620845+00', 20000, true, '2027-05-14 09:44:50.620845+00', '2026-05-14 09:44:50.620845+00'),
	('88d812dd-893b-436d-a42e-f18766246820', NULL, 'FREESHIP', 0, 0, 0, '2027-05-14 09:44:50.620845+00', 15000, true, '2027-05-14 09:44:50.620845+00', '2026-05-14 09:44:50.620845+00'),
	('bcd52c32-d946-4d69-82f0-025d2355ce89', NULL, 'WELCOME50', 0, 0, 0, '2027-05-14 09:44:50.620845+00', 50000, true, '2027-05-14 09:44:50.620845+00', '2026-05-14 09:44:50.620845+00'),
	('50170d8f-ea56-47c5-b6de-c972df921351', NULL, 'FOODIE10', 0, 0, 0, '2027-05-14 09:44:50.620845+00', 10000, true, '2027-05-14 09:44:50.620845+00', '2026-05-14 09:44:50.620845+00');


--
-- Data for Name: wallet_transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- PostgreSQL database dump complete
--

-- \unrestrict ZuSF5FUqx0Ddk5jw8sXgE3KEy5nHkFh0WelmhKNTYBIYejdmLzwazfEdxbKPt4a

RESET ALL;
