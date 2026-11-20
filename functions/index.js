const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { VertexAI } = require("@google-cloud/vertexai");

initializeApp();
const vertex = new VertexAI({ project: process.env.GCLOUD_PROJECT, location: "us-central1" });
const model = vertex.getGenerativeModel({
  model: "gemini-2.0-flash-001",
  generationConfig: { temperature: 0.3, maxOutputTokens: 1500 },
});
const SYS = "Kamu asisten ujian. Baca soal dari gambar. Kalau pilihan ganda, sebut opsi benar lalu alasan satu baris. Kalau esai, jawab ringkas dan tepat.";

exports.api_ask = onCall(
  { region: "us-central1", enforceAppCheck: true, timeoutSeconds: 90, memory: "512MiB" },
  async (req) => {
    if (!req.auth) throw new HttpsError("unauthenticated", "auth dulu");
    const img = (req.data && req.data.image_b64) || "";
    if (typeof img !== "string" || img.length < 100) {
      throw new HttpsError("invalid-argument", "image_b64 wajib");
    }
    const started = Date.now();
    const result = await model.generateContent({
      contents: [{ role: "user", parts: [{ text: SYS }, { inlineData: { mimeType: "image/jpeg", data: img } }] }],
    });
    const candidates = result.response.candidates || [];
    const parts = (candidates[0] && candidates[0].content && candidates[0].content.parts) || [];
    const answer = parts.map((part) => part.text || "").join("").trim() || "[jawaban kosong]";
    return { answer, model: "gemini-2.0-flash-001", latencyMs: Date.now() - started };
  },
);
