package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "multipart_uploads")
data class MultipartUpload(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "bucket_id", nullable = false)
    var bucketId: Long,

    @Column(name = "object_key", nullable = false)
    var objectKey: String,

    @Column(name = "initiated_by")
    var initiatedBy: Long? = null,

    @Column(name = "storage_class")
    var storageClass: String? = null,

    @CreationTimestamp
    @Column(name = "initiated_at", nullable = false, updatable = false)
    val initiatedAt: Instant? = null,

    @Column(name = "is_aborted", nullable = false)
    var isAborted: Boolean = false
)