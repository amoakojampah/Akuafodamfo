// OpenAIModels.kt
package com.example.akuafodamfo.utils

data class OpenAIResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val message: ResponseMessage,
    val finish_reason: String,
    val index: Int
)

data class ResponseMessage(
    val role: String,
    val content: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class OpenAIRequest(
    val model: String,
    val messages: List<RequestMessage>,
    val max_tokens: Int
)

data class RequestMessage(
    val role: String,
    val content: List<ContentItem>
)

data class ContentItem(
    val type: String,
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)