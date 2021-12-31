package com.github.tomislaw.pickyourautocompletion.web

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration

//import org.apache.http.client.fluent.Request


//import org.apache.http.client.fluent.Request

object OpenAiApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(15))
        .readTimeout(Duration.ofSeconds(15))
        .build()

    private val ENGINES = "https://api.openai.com/v1/engines"

    fun engineList(apiKey: String): Result<List<String>> =
        client.newCall(
            Request.Builder().get()
                .url(ENGINES)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
        ).runCatching { execute() }
            .mapCatching { response ->
                if (!response.isSuccessful)
                    throw HttpException("Failed to get response from server, code: ${response.code}")
                ObjectMapper()
                    .readValue(response.body?.string(), EnginesResponse::class.java).engines
                    .map { it.name }
            }

    @JsonIgnoreProperties(ignoreUnknown = true)
    internal data class EnginesResponse(
        @JsonProperty("data") val engines: List<Engine>
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Engine(@JsonProperty("id") val name: String)
    }
}