package com.akitsuki.storage.interfaces.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.akitsuki.storage.interfaces.exception.ExceptionCode
import com.akitsuki.storage.interfaces.exception.ExceptionMessage
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus

@Schema(description = "공통 API 응답 객체")
data class CommonRes<T>(
    @Schema(description = "반환 결과")
    val resultType: ResultType,
    @Schema(description = "반환 데이터")
    val data: T,
    @Schema(description = "반환 메시지 (실패시에만 포함)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val exception: ExceptionMessage? = null
) {
    override fun toString(): String {
        return if (exception != null) {
            """
            {
                "CommonRes": {
                    "resultType": "$resultType",
                    "data": $data,
                    "exception": "$exception"
                }
            }
            """.trimIndent()
        } else {
            """
            {
                "CommonRes": {
                    "resultType": "$resultType",
                    "data": $data
                }
            }
            """.trimIndent()
        }
    }

    companion object {
        fun <T> success(data: T): CommonRes<T> {
            return CommonRes(
                ResultType.SUCCESS,
                data,
                null
            )
        }

        fun error(error: Exception, status: HttpStatus): CommonRes<Map<String, Any>> {
            return CommonRes(
                ResultType.FAIL,
                emptyMap(),
                ExceptionMessage(error, status)
            )
        }

        fun error(error: ExceptionCode, errorData: Any?): CommonRes<Map<String, Any>> {
            return CommonRes(
                ResultType.FAIL,
                emptyMap(),
                ExceptionMessage(error, errorData)
            )
        }
    }
}
