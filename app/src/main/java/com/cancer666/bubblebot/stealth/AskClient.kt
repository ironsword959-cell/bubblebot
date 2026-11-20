package com.cancer666.bubblebot.stealth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object AskClient {

    suspend fun ensureSignedIn() {
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously().await()
        }
    }

    suspend fun askImage(b64: String): String {
        ensureSignedIn()
        val fn = Firebase.functions.getHttpsCallable("api_ask")
        val res = fn.call(mapOf("image_b64" to b64)).await()
        val data = res.data as? Map<*, *> ?: error("respons invalid")
        return (data["answer"] as? String) ?: error("answer kosong")
    }
}
