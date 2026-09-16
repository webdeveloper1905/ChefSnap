package com.yazilim.chefsnap.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    @SerialName("contents")
    val contents: List<Content>,
    @SerialName("generationConfig")
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    @SerialName("parts")
    val parts: List<Part>
)

@Serializable
data class Part(
    @SerialName("text")
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mime_type")
    val mimeType: String,
    @SerialName("data")
    val data: String
)

@Serializable
data class GenerationConfig(
    @SerialName("response_mime_type")
    val responseMimeType: String = "application/json",
    @SerialName("temperature")
    val temperature: Float = 0.4f
)

@Serializable
data class GeminiRawResponse(
    @SerialName("candidates")
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    @SerialName("content")
    val content: ContentResponse? = null
)

@Serializable
data class ContentResponse(
    @SerialName("parts")
    val parts: List<PartResponse> = emptyList()
)

@Serializable
data class PartResponse(
    @SerialName("text")
    val text: String = ""
)