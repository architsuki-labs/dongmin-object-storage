package com.akitsuki.storage.interfaces.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema(description = "액세스 키 응답")
data class AccessKeyResponse(
    @Schema(description = "액세스 키 ID", example = "AKIAIOSFODNN7EXAMPLE")
    @JsonProperty("accessKeyId")
    val accessKeyId: String,
    
    @Schema(description = "시크릿 액세스 키 (생성 시에만 반환)", example = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
    @JsonProperty("secretAccessKey")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val secretAccessKey: String? = null,
    
    @Schema(description = "상태", example = "Active")
    @JsonProperty("status")
    val status: String,
    
    @Schema(description = "생성 일시")
    @JsonProperty("createdAt")
    val createdAt: Instant,
    
    @Schema(description = "만료 일시")
    @JsonProperty("expiresAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val expiresAt: Instant? = null
)