package dev.dani.velar.api.protocol.meta

import dev.dani.velar.api.PlatformVersionAccessor
import dev.dani.velar.api.protocol.enums.EntityStatus
import java.lang.reflect.Type


/*
 * Project: velar
 * Created at: 25/05/2025 18:28
 * Created by: Dani-error
 */
interface EntityMetadataFactory<I, O> {

    val relatedMetadata: Collection<EntityMetadataFactory<I, Any>>

    fun create(input: I, versionAccessor: PlatformVersionAccessor): EntityMetadata<O>

    companion object {

        fun <I, O> metaFactoryBuilder(): Builder<I, O> =
            DefaultEntityMetadataFactoryBuilder()

        fun sneakingMetaFactory(): EntityMetadataFactory<Boolean, Byte> =
            DefaultEntityMetadata.SNEAKING

        fun skinLayerMetaFactory(): EntityMetadataFactory<Boolean, Byte> =
            DefaultEntityMetadata.SKIN_LAYERS

        fun entityStatusMetaFactory(): EntityMetadataFactory<Collection<EntityStatus>, Byte> =
            DefaultEntityMetadata.ENTITY_STATUS

    }

    interface Builder<I, O> {

        fun baseIndex(index: Int): Builder<I, O>

        fun indexShiftVersions(vararg versions: Int): Builder<I, O>

        fun type(type: Type): Builder<I, O>

        fun inputConverter(mapper: (I) -> O): Builder<I, O>

        fun addRelatedMetadata(relatedMetadata: EntityMetadataFactory<I, Any>): Builder<I, O>

        fun availabilityChecker(checker: (PlatformVersionAccessor) -> Boolean): Builder<I, O>

        fun build(): EntityMetadataFactory<I, O>
    }

}

internal class DefaultEntityMetadataFactory<I, O>(
    private val baseIndex: Int,
    private val indexShiftVersions: IntArray,
    private val type: Type,
    private val inputConverter: ((I) -> O),
    override val relatedMetadata: Collection<EntityMetadataFactory<I, Any>>,
    private val availabilityChecker: (PlatformVersionAccessor) -> Boolean
) : EntityMetadataFactory<I, O> {

    @Suppress("UNCHECKED_CAST")
    override fun create(input: I, versionAccessor: PlatformVersionAccessor): EntityMetadata<O> {

        // check if the meta is available
        if (availabilityChecker(versionAccessor)) {
            // try to convert the given input value
            val value: O = inputConverter(input)

            if (value != null) {
                // calculate the index & create the meta
                val index = this.baseIndex + this.calcIndexShift(versionAccessor)
                return AvailableEntityMetadata(index, value, this.type)
            }
        }


        // not available
        return UnavailableEntityMetadata as EntityMetadata<O>
    }

    private fun calcIndexShift(versionAccessor: PlatformVersionAccessor): Int {
        var shift = 0
        for (version in this.indexShiftVersions) {
            if (versionAccessor.minor >= version) {
                shift++
            }
        }

        return shift
    }

    private class AvailableEntityMetadata<O>(
        override val index: Int,
        override val value: O,
        override val type: Type
    ) : EntityMetadata<O> {

        override val available: Boolean
            get() = true

    }


    object UnavailableEntityMetadata : EntityMetadata<Any> {

        override val index: Int
            get() = throw UnsupportedOperationException("Unavailable entity metadata cannot be accessed")

        override val available: Boolean
            get() = false

        override val value: Any
            get() = throw UnsupportedOperationException("Unavailable entity metadata cannot be accessed")

        override val type: Type
            get() = throw UnsupportedOperationException("Unavailable entity metadata cannot be accessed")

    }

}

internal class DefaultEntityMetadataFactoryBuilder<I, O> : EntityMetadataFactory.Builder<I, O> {

    private var baseIndex: Int = 0
    private var indexShiftVersions: IntArray = intArrayOf()

    private var type: Type? = null
    private var inputConverter: ((I) -> O)? = null

    private var relatedMetadata: MutableCollection<EntityMetadataFactory<I, Any>>? = null
    private var availabilityChecker: ((PlatformVersionAccessor) -> Boolean)? = null

    override fun baseIndex(index: Int): EntityMetadataFactory.Builder<I, O> {
        baseIndex = index
        return this
    }

    override fun indexShiftVersions(vararg versions: Int): EntityMetadataFactory.Builder<I, O> {
        indexShiftVersions = versions
        return this
    }

    override fun type(type: Type): EntityMetadataFactory.Builder<I, O> {
        if (type is Class<*> && (type.isPrimitive || type == Void.TYPE)) {
            throw IllegalArgumentException("Entity metadata type must not be a primitive or void")
        }

        this.type = type
        return this
    }

    override fun inputConverter(mapper: (I) -> O): EntityMetadataFactory.Builder<I, O> {
        inputConverter = mapper
        return this
    }

    override fun addRelatedMetadata(relatedMetadata: EntityMetadataFactory<I, Any>): EntityMetadataFactory.Builder<I, O> {
        if (this.relatedMetadata == null) {
            this.relatedMetadata = HashSet()
        }

        this.relatedMetadata!!.add(relatedMetadata)
        return this
    }

    override fun availabilityChecker(checker: (PlatformVersionAccessor) -> Boolean): EntityMetadataFactory.Builder<I, O> {
        availabilityChecker = checker
        return this
    }

    override fun build(): EntityMetadataFactory<I, O> {
        val finalRelatedMetadata = relatedMetadata ?: emptySet()
        val finalChecker = availabilityChecker ?: { true }

        return DefaultEntityMetadataFactory(
            baseIndex,
            indexShiftVersions,
            requireNotNull(type) { "type" },
            requireNotNull(inputConverter) { "inputConverter" },
            finalRelatedMetadata,
            finalChecker
        )
    }

}