package dev.dani.velar.api.protocol.meta

import dev.dani.velar.api.protocol.enums.EntityPose
import dev.dani.velar.api.protocol.enums.EntityStatus
import java.lang.reflect.Type
import kotlin.experimental.or


/*
 * Project: velar
 * Created at: 25/05/2025 18:27
 * Created by: Dani-error
 */
interface EntityMetadata<O> {

    val index: Int
    val available: Boolean
    val value: O
    val type: Type

}

interface DefaultEntityMetadata {

    companion object {

        // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Entity - see index 0
        val ENTITY_STATUS = EntityMetadataFactory.metaFactoryBuilder<Collection<EntityStatus>, Byte>()
            .baseIndex(0)
            .type(java.lang.Byte::class.java)
            .inputConverter { rawEntries ->

                // if there are no entries the mask is always 0
                val size: Int = rawEntries.count()
                if (size == 0) {
                    return@inputConverter 0.toByte()
                }


                // ensure that there are no duplicates
                val entries: MutableSet<EntityStatus>
                if (rawEntries is Set<*>) {
                    // already a set - nice
                    entries = rawEntries as MutableSet<EntityStatus>
                } else {
                    // copy over the elements
                    entries = HashSet(size + 1, 1f)
                    entries.addAll(rawEntries)
                }


                // calculate the bitmask to send
                var entryMask: Byte = 0
                for (entry in entries) {
                    entryMask = entryMask or entry.bitmask
                }

                return@inputConverter entryMask
            }.build()

        // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Entity - see index 6
        val ENTITY_POSE = EntityMetadataFactory.metaFactoryBuilder<EntityPose, EntityPose>()
            .baseIndex(6)
            .type(EntityPose::class.java)
            .inputConverter { it }
            .availabilityChecker { it.atLeast(1, 14, 0) }
            .build()

        // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Entity - see index 0 and 6
        val SNEAKING = EntityMetadataFactory.metaFactoryBuilder<Boolean, Byte>()
            .baseIndex(0)
            .type(java.lang.Byte::class.java)
            .inputConverter { value -> ( if(value) 0x02 else 0x00 ).toByte() }
            .addRelatedMetadata(EntityMetadataFactory.metaFactoryBuilder<Boolean, Any>()
                .baseIndex(6)
                .type(EntityPose::class.java)
                .inputConverter { value -> if (value) EntityPose.CROUCHING else EntityPose.STANDING }
                .availabilityChecker { it.atLeast(1, 14, 0) }
                .build())
            .build()

        // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Entity - see index 7
        val SHAKING = EntityMetadataFactory.metaFactoryBuilder<Boolean, Int>()
            .baseIndex(7)
            .type(java.lang.Integer::class.java)
            .inputConverter { value -> if (value) 250 else 0 }
            .availabilityChecker { it.atLeast(1, 17, 0) }
            .build()

        // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Living_Entity - see index 8
        val USING_ITEM = EntityMetadataFactory.metaFactoryBuilder<Boolean, Byte>()
            .baseIndex(5)
            .type(java.lang.Byte::class.java)
            .indexShiftVersions(10, 14, 17)
            .inputConverter { value -> (if (value) 0x01 else 0x00).toByte() }
            .availabilityChecker { it.atLeast(1, 9, 0) }
            .build()

        // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Living_Entity - see index 12
        val ARROW_COUNT = EntityMetadataFactory.metaFactoryBuilder<Int, Int>()
            .baseIndex(9)
            .type(java.lang.Integer::class.java)
            .indexShiftVersions(10, 14, 17)
            .inputConverter { value -> 0.coerceAtLeast(value) }
            .build()

        // https://wiki.vg/Entity_metadata#Player - see index 10
        val SKIN_LAYERS = EntityMetadataFactory.metaFactoryBuilder<Boolean, Byte>()
            .baseIndex(10)
            .type(java.lang.Byte::class.java)
            .indexShiftVersions(9, 9, 10, 14, 14, 15, 17)
            .inputConverter { value -> ( if (value) 0xff else 0x00 ).toByte() }
            .build()

    }

}