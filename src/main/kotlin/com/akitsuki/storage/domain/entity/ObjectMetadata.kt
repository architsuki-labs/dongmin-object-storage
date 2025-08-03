package com.akitsuki.storage.domain.entity

import jakarta.persistence.*

@Embeddable
data class ObjectMetadataId(
    @Column(name = "object_id")
    var objectId: Long = 0,

    @Column(name = "meta_key")
    var metaKey: String = ""
) : java.io.Serializable

@Entity
@Table(name = "object_metadata")
data class ObjectMetadata(
    @EmbeddedId
    val id: ObjectMetadataId = ObjectMetadataId(),

    @Column(name = "meta_value")
    var metaValue: String? = null
)
