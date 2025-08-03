package com.akitsuki.storage.domain.entity

import jakarta.persistence.*

@Embeddable
data class PartChunkId(
    @Column(name = "multipart_part_id")
    var multipartPartId: Long = 0,

    @Column(name = "chunk_id")
    var chunkId: Long = 0
) : java.io.Serializable

@Entity
@Table(name = "part_chunk")
data class PartChunk(
    @EmbeddedId
    val id: PartChunkId = PartChunkId(),

    @Column(name = "sequence_index", nullable = false)
    var sequenceIndex: Int
)