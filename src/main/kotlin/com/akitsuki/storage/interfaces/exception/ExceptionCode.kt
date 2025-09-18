package com.akitsuki.storage.interfaces.exception

import org.springframework.http.HttpStatus

enum class ExceptionCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common errors
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E001", "Invalid request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E002", "Unauthorized access"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E003", "Access forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E004", "Resource not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E005", "Internal server error"),
    
    // User related
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "User already exists"),
    
    // Auth related
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "Invalid credentials"),
    ACCESS_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "A002", "Access key not found"),
    ACCESS_KEY_EXPIRED(HttpStatus.UNAUTHORIZED, "A003", "Access key expired"),
    ACCESS_KEY_INACTIVE(HttpStatus.UNAUTHORIZED, "A004", "Access key is inactive"),
    
    // Bucket related
    BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "Bucket not found"),
    BUCKET_ALREADY_EXISTS(HttpStatus.CONFLICT, "B002", "Bucket already exists"),
    BUCKET_NOT_EMPTY(HttpStatus.CONFLICT, "B003", "Bucket is not empty"),
    INVALID_BUCKET_NAME(HttpStatus.BAD_REQUEST, "B004", "Invalid bucket name"),
    
    // Object related
    OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "Object not found"),
    OBJECT_ALREADY_EXISTS(HttpStatus.CONFLICT, "O002", "Object already exists"),
    INVALID_OBJECT_KEY(HttpStatus.BAD_REQUEST, "O003", "Invalid object key"),
    STORAGE_LIMIT_EXCEEDED(HttpStatus.INSUFFICIENT_STORAGE, "O004", "Storage limit exceeded"),
    
    // Multipart upload related
    UPLOAD_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "Upload not found"),
    INVALID_PART_NUMBER(HttpStatus.BAD_REQUEST, "M002", "Invalid part number"),
    PART_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "Part not found"),
    UPLOAD_ALREADY_COMPLETED(HttpStatus.CONFLICT, "M004", "Upload already completed")
}