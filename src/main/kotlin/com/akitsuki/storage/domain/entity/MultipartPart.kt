package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "multipart_parts",
    indexes = [
        Index(name = "idx_multipart_parts_upload", columnList = "upload_id, part_number")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_multipart_parts_upload_part",
            columnNames = ["upload_id", "part_number"]
        )
    ]
)
class MultipartPart(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "upload_id", nullable = false, columnDefinition = "uuid")
    val uploadId: UUID,

    @Column(name = "part_number", nullable = false)
    val partNumber: Int,

    @Column(nullable = false)
    val size: Long,

    @Column(nullable = false)
    val etag: String,

    @Column(name = "storage_path", nullable = false, length = 500)
    val storagePath: String,

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    val uploadedAt: Instant = Instant.now()
)