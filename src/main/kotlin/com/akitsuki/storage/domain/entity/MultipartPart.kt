package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "multipart_parts",
    uniqueConstraints = [UniqueConstraint(name = "uq_upload_partnumber", columnNames = ["multipart_upload_id", "part_number"])]
)
data class MultipartPart(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "multipart_upload_id", nullable = false)
    var multipartUploadId: Long,

    @Column(name = "part_number", nullable = false)
    var partNumber: Int,

    @Column(nullable = false)
    var size: Long,

    var etag: String? = null,

    @Column(name = "sha256_checksum")
    var sha256Checksum: String? = null,

    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,

    @Column(name = "uploaded_at")
    var uploadedAt: Instant? = null
)