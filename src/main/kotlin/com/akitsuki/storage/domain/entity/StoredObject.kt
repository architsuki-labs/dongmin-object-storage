package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "objects",
    uniqueConstraints = [UniqueConstraint(name = "uq_bucket_key_version", columnNames = ["bucket_id", "key", "version_number"])]
)
data class StoredObject(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "bucket_id", nullable = false)
    var bucketId: Long,

    @Column(name = "key", nullable = false)
    var key: String,

    @Column(name = "version_number", nullable = false)
    var versionNumber: Int = 1,

    @Column(name = "is_latest", nullable = false)
    var isLatest: Boolean = true,

    @Column(nullable = false)
    var size: Long,

    @Column(name = "content_type")
    var contentType: String? = null,

    var etag: String? = null,

    @Column(name = "sha256_checksum")
    var sha256Checksum: String? = null,

    @Column(name = "delete_marker", nullable = false)
    var deleteMarker: Boolean = false,

    @Column(name = "storage_class")
    var storageClass: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null
)
