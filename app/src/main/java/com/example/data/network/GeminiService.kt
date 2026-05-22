package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Gemini API structures optimized for Moshi
@JsonClass(generateAdapter = true)
data class MoshiGenerateContentRequest(
    @Json(name = "contents") val contents: List<MoshiContent>,
    @Json(name = "generationConfig") val generationConfig: MoshiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: MoshiContent? = null
)

@JsonClass(generateAdapter = true)
data class MoshiContent(
    @Json(name = "parts") val parts: List<MoshiPart>
)

@JsonClass(generateAdapter = true)
data class MoshiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class MoshiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class MoshiGenerateContentResponse(
    @Json(name = "candidates") val candidates: List<MoshiCandidate>?
)

@JsonClass(generateAdapter = true)
data class MoshiCandidate(
    @Json(name = "content") val content: MoshiContent?
)

@JsonClass(generateAdapter = true)
data class ServiceParseResult(
    @Json(name = "clientName") val clientName: String = "",
    @Json(name = "serviceExecuted") val serviceExecuted: String = "",
    @Json(name = "amountCharged") val amountCharged: Double = 0.0,
    @Json(name = "date") val date: String = "", // dd/MM/yyyy HH:mm
    @Json(name = "phone") val phone: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "saveAsContact") val saveAsContact: Boolean = false
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: MoshiGenerateContentRequest
    ): MoshiGenerateContentResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApi by lazy {
        retrofit.create(GeminiApi::class.java)
    }

    suspend fun parseServiceDescription(userText: String, currentDateStr: String): ServiceParseResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder!")
            return null
        }

        val systemPrompt = """
            Você é a inteligência artificial do aplicativo 'Rotina+' para profissionais de serviços autônomos.
            Seu trabalho é extrair dados estruturados de um comando ou relato de serviço e retornar um objeto JSON.
            Os dados do dia atual do sistema são: $currentDateStr. Use esses dados para preencher a data se nenhuma data for detalhada no texto.
            
            Retorne OBRIGATORIAMENTE um objeto JSON válido correspondente aos seguintes campos:
            - clientName (String: Nome do cliente. Se não houver, deixe vazio "")
            - serviceExecuted (String: Resumo ou descrição do serviço executado. Se não houver, deixe vazio "")
            - amountCharged (Double: Valor numérico cobrado em Reais (R${'$'}). Se não houver, defina como 0.0)
            - date (String: Data e hora do serviço no formato 'dd/MM/yyyy HH:mm'. Se não houver horário, use '12:00'. Se não houver data, preencha com a data de hoje '$currentDateStr')
            - phone (String: Telefone do cliente. Se não houver, deixe vazio "")
            - address (String: Endereço do cliente. Se não houver, deixe vazio "")
            - saveAsContact (Boolean: True se o usuário explicitamente pediu para salvar o contato, salvar número de telefone ou salvar o cliente de alguma forma; caso contrário, de preferência False)
            
            Retorne EXCLUSIVAMENTE o conteúdo JSON puro. Não utilize marcações markdown ```json ou ``` na resposta.
        """.trimIndent()

        val request = MoshiGenerateContentRequest(
            contents = listOf(MoshiContent(parts = listOf(MoshiPart(text = "Texto do usuário: \"$userText\"")))),
            generationConfig = MoshiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.1f
            ),
            systemInstruction = MoshiContent(parts = listOf(MoshiPart(text = systemPrompt)))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                Log.d(TAG, "Gemini Response: $jsonText")
                moshi.adapter(ServiceParseResult::class.java).fromJson(jsonText)
            } else {
                Log.w(TAG, "No response candidates or content text found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            null
        }
    }
}
