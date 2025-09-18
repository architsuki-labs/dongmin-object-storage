package com.akitsuki.storage.interfaces.exception

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@Schema(description = "Exception message details")
data class ExceptionMessage(
    @Schema(description = "Error code")
    val code: String,
    
    @Schema(description = "Error message")
    val message: String,
    
    @Schema(description = "HTTP status code")
    val status: Int,
    
    @Schema(description = "Timestamp when error occurred")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @Schema(description = "Additional error data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: Any? = null
) {
    constructor(exception: Exception, status: HttpStatus) : this(
        code = "E000",
        message = exception.message ?: "Unknown error",
        status = status.value(),
        data = null
    )
    
    constructor(exceptionCode: ExceptionCode, errorData: Any? = null) : this(
        code = exceptionCode.code,
        message = exceptionCode.message,
        status = exceptionCode.status.value(),
        data = errorData
    )
}