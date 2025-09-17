package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(
    name = "multipart_uploads",
    indexes = [
        Index(name = "idx_multipart_uploads_bucket", columnList = "bucket_id, initiated_at"),
        Index(name = "idx_multipart_uploads_expires", columnList = "expires_at"),
        Index(name = "idx_multipart_uploads_upload_id", columnList = "upload_id", unique = true)
    ]
)
class MultipartUpload(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "bucket_id", nullable = false, columnDefinition = "uuid")
    val bucketId: UUID,

    @Column(name = "object_key", nullable = false, length = 1024)
    val objectKey: String,

    @Column(name = "upload_id", nullable = false, unique = true, length = 100)
    val uploadId: String = generateUploadId(),

    @Column(name = "initiated_by", nullable = false, columnDefinition = "uuid")
    val initiatedBy: UUID,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val metadata: Map<String, Any>? = null,

    @CreationTimestamp
    @Column(name = "initiated_at", nullable = false, updatable = false)
    val initiatedAt: Instant = Instant.now(),

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant = Instant.now().plus(7, ChronoUnit.DAYS)
) {
    companion object {
        private fun generateUploadId(): String {
            return "2~${UUID.randomUUID()}"
        }
    }
}