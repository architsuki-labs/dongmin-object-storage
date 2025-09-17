package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "object_tags",
    indexes = [
        Index(name = "idx_object_tags_version", columnList = "version_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_object_tags_version_key",
            columnNames = ["version_id", "tag_key"]
        )
    ]
)
class ObjectTag(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "version_id", nullable = false, columnDefinition = "uuid")
    val versionId: UUID,

    @Column(name = "tag_key", nullable = false, length = 128)
    val tagKey: String,

    @Column(name = "tag_value", nullable = false, length = 256)
    val tagValue: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
}