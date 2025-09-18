package com.akitsuki.storage.interfaces.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "액세스 키 생성 요청")
data class AccessKeyCreateRequest(
    @Schema(description = "액세스 키 설명", example = "Production API Key")
    @JsonProperty("description")
    val description: String? = null,
    
    @Schema(description = "만료 일수 (null이면 무제한)", example = "90")
    @JsonProperty("expiresInDays")
    val expiresInDays: Int? = null
)