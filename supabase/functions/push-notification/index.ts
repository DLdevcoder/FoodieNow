import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import admin from "npm:firebase-admin@11.11.0";

const FIREBASE_PROJECT_ID = Deno.env.get("FIREBASE_PROJECT_ID") ?? "";
const FIREBASE_CLIENT_EMAIL = Deno.env.get("FIREBASE_CLIENT_EMAIL") ?? "";
const FIREBASE_PRIVATE_KEY = Deno.env.get("FIREBASE_PRIVATE_KEY")?.replace(/\\n/g, '\n') ?? "";

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: FIREBASE_PROJECT_ID,
      clientEmail: FIREBASE_CLIENT_EMAIL,
      privateKey: FIREBASE_PRIVATE_KEY,
    }),
  });
}

interface NotificationPayload {
  type: "INSERT";
  table: string;
  record: {
    id: string;
    user_id: string;
    title: string;
    message: string;
    is_read: boolean;
    created_at: string;
  };
}

serve(async (req) => {
  try {
    const payload: NotificationPayload = await req.json();

    if (payload.table !== "notifications" || payload.type !== "INSERT") {
      return new Response(JSON.stringify({ message: "Ignored" }), { status: 200 });
    }

    const { user_id, title, message } = payload.record;

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    const profileRes = await fetch(`${supabaseUrl}/rest/v1/profiles?id=eq.${user_id}&select=fcm_token`, {
      headers: {
        "apikey": supabaseServiceKey,
        "Authorization": `Bearer ${supabaseServiceKey}`,
      },
    });

    const profiles = await profileRes.json();
    const fcmToken = profiles?.[0]?.fcm_token;

    if (!fcmToken) {
      return new Response(JSON.stringify({ message: "No FCM token found" }), { status: 200 });
    }

    const fcmResult = await admin.messaging().send({
      token: fcmToken,
      notification: {
        title: title,
        body: message,
      },
      data: {
        title: title,
        body: message,
      },
    });

    return new Response(JSON.stringify({ success: true, fcmResult }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
