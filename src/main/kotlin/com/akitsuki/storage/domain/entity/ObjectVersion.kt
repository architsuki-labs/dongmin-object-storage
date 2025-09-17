package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "object_versions",
    indexes = [
        Index(name = "idx_object_versions_bucket_key", columnList = "bucket_id, object_key, created_at"),
        Index(name = "idx_object_versions_version", columnList = "version_id", unique = true)
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "idx_object_versions_latest",
            columnNames = ["bucket_id", "object_key", "is_latest"]
        )
    ]
)
class ObjectVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "bucket_id", nullable = false, columnDefinition = "uuid")
    val bucketId: UUID,

    @Column(name = "object_key", nullable = false, length = 1024)
    val objectKey: String,

    @Column(name = "version_id", nullable = false, unique = true, length = 50)
    val versionId: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val size: Long,

    @Column(name = "content_type", length = 255)
    val contentType: String = "application/octet-stream",

    @Column(nullable = false)
    val etag: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_class", length = 30)
    val storageClass: StorageClass = StorageClass.STANDARD,

    @Column(name = "is_latest", nullable = false)
    var isLatest: Boolean = true,

    @Column(name = "is_delete_marker", nullable = false)
    val isDeleteMarker: Boolean = false,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val metadata: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_metadata", columnDefinition = "jsonb")
    val userMetadata: Map<String, String>? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "created_by", columnDefinition = "uuid")
    val createdBy: UUID? = null
) {
    enum class StorageClass {
        STANDARD,
        INFREQUENT_ACCESS,
        GLACIER
    }
}