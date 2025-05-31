@file:Suppress("UNCHECKED_CAST")

package dev.dani.velar.bukkit.protocol.impl

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.InternalStructure
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.utility.MinecraftVersion
import com.comphenix.protocol.wrappers.*
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject
import com.google.common.collect.Lists
import dev.dani.velar.api.NPC
import dev.dani.velar.api.event.InteractNPCEvent
import dev.dani.velar.api.platform.Platform
import dev.dani.velar.api.platform.PlatformVersionAccessor
import dev.dani.velar.api.protocol.Component
import dev.dani.velar.api.protocol.OutboundPacket
import dev.dani.velar.api.protocol.PlatformPacketAdapter
import dev.dani.velar.api.protocol.TeamInfo
import dev.dani.velar.api.protocol.enums.*
import dev.dani.velar.api.protocol.meta.EntityMetadataFactory
import dev.dani.velar.bukkit.util.toLegacy
import dev.dani.velar.common.event.DefaultAttackNPCEvent
import dev.dani.velar.common.event.DefaultInteractNPCEvent
import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.TypeFactory
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.String
import kotlin.math.floor


/*
 * Project: velar
 * Created at: 25/05/2025 21:25
 * Created by: Dani-error
 */
object ProtocolLibPacketAdapter : PlatformPacketAdapter<World, Player, ItemStack, Plugin> {

    private val OPTIONAL_COMPONENT_TYPE: Type = TypeFactory.parameterizedClass(
        Optional::class.java,
        MinecraftReflection.getIChatBaseComponentClass()
    )

    private val PROTOCOL_MANAGER: ProtocolManager = ProtocolLibrary.getProtocolManager()
    private val SERVER_VERSION: MinecraftVersion = MinecraftVersion.fromServerVersion(Bukkit.getVersion())

    private val ITEM_SLOT_CONVERTER: EnumMap<ItemSlot, EnumWrappers.ItemSlot> by lazy {
        EnumMap<ItemSlot, EnumWrappers.ItemSlot>(ItemSlot::class.java).apply {
            put(ItemSlot.MAIN_HAND, EnumWrappers.ItemSlot.MAINHAND)
            put(ItemSlot.OFF_HAND, EnumWrappers.ItemSlot.OFFHAND)
            put(ItemSlot.FEET, EnumWrappers.ItemSlot.FEET)
            put(ItemSlot.LEGS, EnumWrappers.ItemSlot.LEGS)
            put(ItemSlot.CHEST, EnumWrappers.ItemSlot.CHEST)
            put(ItemSlot.HEAD, EnumWrappers.ItemSlot.HEAD)
        }
    }

    val HAND_CONVERTER: EnumMap<EnumWrappers.Hand, InteractNPCEvent.Hand> by lazy {
        EnumMap<EnumWrappers.Hand, InteractNPCEvent.Hand>(EnumWrappers.Hand::class.java).apply {
            put(EnumWrappers.Hand.MAIN_HAND, InteractNPCEvent.Hand.MAIN_HAND)
            put(EnumWrappers.Hand.OFF_HAND, InteractNPCEvent.Hand.OFF_HAND)
        }
    }

    val ENTITY_POSE_CONVERTER: EnumMap<EntityPose, Any> by lazy {
        EnumMap<EntityPose, Any>(EntityPose::class.java).apply {
            EnumWrappers.getEntityPoseClass()?.let {
                put(EntityPose.STANDING, EnumWrappers.EntityPose.STANDING.toNms())
                put(EntityPose.FALL_FLYING, EnumWrappers.EntityPose.FALL_FLYING.toNms())
                put(EntityPose.SLEEPING, EnumWrappers.EntityPose.SLEEPING.toNms())
                put(EntityPose.SWIMMING, EnumWrappers.EntityPose.SWIMMING.toNms())
                put(EntityPose.SPIN_ATTACK, EnumWrappers.EntityPose.SPIN_ATTACK.toNms())
                put(EntityPose.CROUCHING, EnumWrappers.EntityPose.CROUCHING.toNms())
                put(EntityPose.DYING, EnumWrappers.EntityPose.DYING.toNms())

                if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
                    put(EntityPose.LONG_JUMPING, EnumWrappers.EntityPose.LONG_JUMPING.toNms())
                }
                if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                    put(EntityPose.CROAKING, EnumWrappers.EntityPose.CROAKING.toNms())
                    put(EntityPose.USING_TONGUE, EnumWrappers.EntityPose.USING_TONGUE.toNms())
                    put(EntityPose.ROARING, EnumWrappers.EntityPose.ROARING.toNms())
                    put(EntityPose.SNIFFING, EnumWrappers.EntityPose.SNIFFING.toNms())
                    put(EntityPose.EMERGING, EnumWrappers.EntityPose.EMERGING.toNms())
                    put(EntityPose.DIGGING, EnumWrappers.EntityPose.DIGGING.toNms())
                }
                if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                    put(EntityPose.SITTING, EnumWrappers.EntityPose.SITTING.toNms())
                }
                if (MinecraftVersion.v1_20_4.atOrAbove()) {
                    put(EntityPose.SLIDING, EnumWrappers.EntityPose.SLIDING.toNms())
                    put(EntityPose.SHOOTING, EnumWrappers.EntityPose.SHOOTING.toNms())
                    put(EntityPose.INHALING, EnumWrappers.EntityPose.INHALING.toNms())
                }
            }
        }
    }

    val PLAYER_INFO_ACTION_CONVERTER: EnumMap<PlayerInfoAction, EnumWrappers.PlayerInfoAction> by lazy {
        EnumMap<PlayerInfoAction, EnumWrappers.PlayerInfoAction>(PlayerInfoAction::class.java).apply {
            put(PlayerInfoAction.ADD_PLAYER, EnumWrappers.PlayerInfoAction.ADD_PLAYER)
            put(PlayerInfoAction.REMOVE_PLAYER, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER)
        }
    }

    val SERIALIZER_CONVERTERS: Map<Type, (PlatformVersionAccessor, Any?) -> Map.Entry<Type, Any?>> by lazy {

        mapOf(
            EntityPose::class.java to { _, value ->
                AbstractMap.SimpleEntry(
                    EnumWrappers.getEntityPoseClass(),
                    ENTITY_POSE_CONVERTER[value]
                )
            },
            TypeFactory.parameterizedClass(Optional::class.java, Component::class.java) to { versionAccess, value ->
                val optionalComponent = value as Optional<Component>
                if (versionAccess.atLeast(1, 13, 0)) {
                    AbstractMap.SimpleImmutableEntry(
                        OPTIONAL_COMPONENT_TYPE,
                        optionalComponent.map { comp ->
                            if (comp.rawMessage != null) {
                                WrappedChatComponent.fromLegacyText(comp.rawMessage)
                            } else {
                                WrappedChatComponent.fromJson(comp.encodedJsonMessage)
                            }
                        }.map { it.handle }
                    )
                } else {
                    AbstractMap.SimpleImmutableEntry(
                        String::class.java,
                        optionalComponent.map { comp ->
                            requireNotNull(comp.rawMessage) {
                                "Versions older than 1.13 don't support json component"
                            }
                        }.orElse(null)
                    )
                }
            }
        )
    }



    val ADD_ACTIONS: EnumSet<EnumWrappers.PlayerInfoAction> = EnumSet.of(
        EnumWrappers.PlayerInfoAction.ADD_PLAYER,
        EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
        EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
        EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE,
        EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME
    )


    private fun createWatchableObject(
        index: Int,
        type: Type,
        value: Any,
        versionAccessor: PlatformVersionAccessor
    ): WrappedWatchableObject? {
        // pre-convert the value if needed
        var newType = type
        var newValue: Any? = value
        val converter = SERIALIZER_CONVERTERS[newType]
        if (converter != null) {
            val converted: Map.Entry<Type, Any?> = converter(versionAccessor, newValue)
            // re-assign the type and value
            newType = converted.key
            newValue = converted.value

            // the value might not be accessible on the current version
            if (newValue == null) {
                return null
            }
        }

        if (MinecraftVersion.COMBAT_UPDATE.atOrAbove()) {
            // mc 1.9: watchable object now contains a serializer for the type
            if (newType is ParameterizedType) {
                val parameterized = newType
                val optional = parameterized.rawType === Optional::class.java
                if (optional) {
                    val serializerType = parameterized.actualTypeArguments[0]
                    val rawSerializerType = GenericTypeReflector.erase(serializerType)

                    val serializer = WrappedDataWatcher.Registry.get(rawSerializerType, true)
                    return WrappedWatchableObject(WrappedDataWatcherObject(index, serializer), newValue)
                }
            }

            val raw = GenericTypeReflector.erase(newType)
            val serializer = WrappedDataWatcher.Registry.get(raw, false)
            return WrappedWatchableObject(WrappedDataWatcherObject(index, serializer), newValue)
        } else {
            // mc 1.8: watchable object id
            return WrappedWatchableObject(index, newValue)
        }
    }

    override fun createEntitySpawnPacket(): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            val container = if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
                // SpawnEntity (https://wiki.vg/Protocol#Spawn_Entity)
                PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)
            } else {
                // SpawnPlayer (https://wiki.vg/Protocol#Spawn_Player)
                PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN)
            }
            // base information
            container.integers.write(0, npc.entityId)
            container.uuiDs.write(0, npc.profile.uniqueId)

            if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
                container.entityTypeModifier.write(0, EntityType.PLAYER)
            }

            // position
            if (MinecraftVersion.COMBAT_UPDATE.atOrAbove()) {
                // mc 1.9: new position format (plain doubles)
                container.doubles
                    .write(0, npc.position.x)
                    .write(1, npc.position.y)
                    .write(2, npc.position.z)
            } else {
                // mc 1.8: old position format (rotation angles)
                container.integers
                    .write(1, floor(npc.position.x * 32.0).toInt())
                    .write(2, floor(npc.position.y * 32.0).toInt())
                    .write(3, floor(npc.position.z * 32.0).toInt())
            }

            // rotation (angles)
            if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
                // yaw is byte 1, pitch is byte 0
                container.bytes
                    .write(1, (npc.position.yaw * 256f / 360f).toInt().toByte())
                    .write(0, (npc.position.pitch * 256f / 360f).toInt().toByte())
            } else {
                // yaw is byte 0, pitch is byte 1
                container.bytes
                    .write(0, (npc.position.yaw * 256f / 360f).toInt().toByte())
                    .write(1, (npc.position.pitch * 256f / 360f).toInt().toByte())
            }

            // metadata if on an old server version (< 15)
            if (MinecraftVersion.VILLAGE_UPDATE.isAtLeast(SERVER_VERSION)) {
                container.dataWatcherModifier.write(0, WrappedDataWatcher())
            }

            // send the packet without notifying any bound packet listeners
            PROTOCOL_MANAGER.sendServerPacket(player, container, false)
        }
    }

    override fun createEntityRemovePacket(): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player?, npc: NPC<World, Player, ItemStack, Plugin> ->
            // DestroyEntities (https://wiki.vg/Protocol#Destroy_Entities)
            val container = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

            // entity id
            if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
                // mc 1.17: entity ids is a list
                container.intLists.write(0, Lists.newArrayList(npc.entityId))
            } else {
                // mc 1.8: entity ids is an int array
                container.integerArrays.write(0, intArrayOf(npc.entityId))
            }

            // send the packet without notifying any bound packet listeners
            PROTOCOL_MANAGER.sendServerPacket(player, container, false)
        }
    }

    override fun createPlayerInfoPacket(
        action: PlayerInfoAction
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            npc.settings.profileResolver.resolveNPCProfile(player, npc).thenAcceptAsync { profile ->
                // since 1.19.3 removing of players is handled in a separate packet
                if (action === PlayerInfoAction.REMOVE_PLAYER && MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                    // PlayerRemove (https://wiki.vg/Protocol#Player_Remove)
                    val container = PacketContainer(PacketType.Play.Server.PLAYER_INFO_REMOVE)

                    // write the npc uuid to remove
                    val uuidsToRemove: List<UUID> =
                        listOf(profile.uniqueId)
                    container.uuidLists.write(0, uuidsToRemove)

                    // send the packet without notifying any bound packet listeners
                    PROTOCOL_MANAGER.sendServerPacket(player, container, false)
                    return@thenAcceptAsync
                }

                // PlayerInfo (https://wiki.vg/Protocol#Player_Info)
                val container = PacketContainer(PacketType.Play.Server.PLAYER_INFO)

                // action
                var playerInfoDataIndex = 0
                if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                    // at this point the only way this could be called is because we want to register a new player
                    playerInfoDataIndex = 1
                    container.playerInfoActions.write(0, ADD_ACTIONS)
                } else {
                    // old system, just add the translated action
                    val playerInfoAction: EnumWrappers.PlayerInfoAction = PLAYER_INFO_ACTION_CONVERTER[action]!!
                    container.playerInfoAction.write(0, playerInfoAction)
                }

                // convert to a protocol lib profile
                val wrappedGameProfile = WrappedGameProfile(profile.uniqueId, profile.name)
                for (prop in profile.properties!!) {
                    val wrapped = WrappedSignedProperty(prop.name, prop.value, prop.signature)
                    wrappedGameProfile.properties.put(prop.name, wrapped)
                }

                // add the player info data
                val playerInfoData = PlayerInfoData(
                    profile.uniqueId,
                    20,
                    false,
                    EnumWrappers.NativeGameMode.CREATIVE,
                    wrappedGameProfile,
                    null
                )
                container.playerInfoDataLists
                    .write(playerInfoDataIndex, Lists.newArrayList(playerInfoData))

                // send the packet without notifying any bound packet listeners
                PROTOCOL_MANAGER.sendServerPacket(player, container, false)
            }
        }
    }

    override fun createRotationPacket(yaw: Float, pitch: Float): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            // pre-calculate the yaw and pitch angle values
            val yawAngle = (yaw * 256f / 360f).toInt().toByte()
            val pitchAngle = (pitch * 256f / 360f).toInt().toByte()

            // head rotation (https://wiki.vg/Protocol#Entity_Head_Look)
            val headRotation = PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION)
            headRotation.bytes.write(0, yawAngle)
            headRotation.integers.write(0, npc.entityId)

            // entity teleport (https://wiki.vg/Protocol#Entity_Teleport) or Player Rotation (https://wiki.vg/Protocol#Player_Rotation)
            val rotation: PacketContainer
            if (MinecraftVersion.COMBAT_UPDATE.atOrAbove()) {
                // mc 1.9: player rotation
                rotation = PacketContainer(PacketType.Play.Server.ENTITY_LOOK)
            } else {
                // mc 1.8: entity teleport
                rotation = PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT)
                rotation.integers
                    .write(1, floor(npc.position.x * 32.0).toInt())
                    .write(2, floor(npc.position.y * 32.0).toInt())
                    .write(3, floor(npc.position.z * 32.0).toInt())
            }

            // entity id
            rotation.integers.write(0, npc.entityId)

            // rotation (angles)
            rotation.bytes
                .write(0, yawAngle)
                .write(1, pitchAngle)

            // ground status
            rotation.booleans.write(0, true)

            // send the packet without notifying any bound packet listeners
            PROTOCOL_MANAGER.sendServerPacket(player, rotation, false)
            PROTOCOL_MANAGER.sendServerPacket(player, headRotation, false)
        }
    }

    override fun createAnimationPacket(
        animation: EntityAnimation
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player?, npc: NPC<World, Player, ItemStack, Plugin> ->
            // EntityAnimation (https://wiki.vg/Protocol#Entity_Animation_.28clientbound.29)
            val container = PacketContainer(PacketType.Play.Server.ANIMATION)

            // entity id & animation id
            container.integers
                .write(0, npc.entityId)
                .write(1, animation.id)

            // send the packet without notifying any bound packet listeners
            PROTOCOL_MANAGER.sendServerPacket(player, container, false)
        }
    }

    override fun createEquipmentPacket(
        slot: ItemSlot,
        item: ItemStack
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            // EntityEquipment (https://wiki.vg/Protocol#Entity_Equipment)
            val container = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)

            // entity id
            container.integers.write(0, npc.entityId)

            // item
            if (MinecraftVersion.NETHER_UPDATE.atOrAbove()) {
                // mc 1.16: item slot & item stack pairs
                val itemSlot: EnumWrappers.ItemSlot = ITEM_SLOT_CONVERTER[slot]!!
                container.slotStackPairLists.write(0, Lists.newArrayList(Pair(itemSlot, item)))
            } else {
                if (MinecraftVersion.COMBAT_UPDATE.atOrAbove()) {
                    // mc 1.9: item slot
                    container.itemSlots.write(0, ITEM_SLOT_CONVERTER[slot])
                } else {
                    // mc 1.8: item slot id
                    var slotId: Int = slot.ordinal
                    if (slotId > 0) {
                        // off-hand did not exist in 1.8, so all ids are shifted one down
                        slotId -= 1
                    }

                    container.integers.write(1, slotId)
                }

                // the actual item
                container.itemModifier.write(0, item)
            }

            // send the packet without notifying any bound packet listeners
            PROTOCOL_MANAGER.sendServerPacket(player, container, false)
        }
    }

    override fun createCustomPayloadPacket(
        channelId: String,
        payload: ByteArray
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            val plugin: Plugin =
                npc.platform.extension
            player.sendPluginMessage(plugin, channelId, payload)
        }
    }

    override fun <T, O> createEntityMetaPacket(
        metadata: EntityMetadataFactory<T, O>,
        value: T
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            // create the entity meta
            val versionAcc: PlatformVersionAccessor = npc.platform.versionAccessor
            val entityMetadata = metadata.create(value, versionAcc)

            // check if the meta is available
            if (!entityMetadata.available) {
                return@OutboundPacket
            }

            // construct the meta we want to send out
            val watchableObjects: MutableList<WrappedWatchableObject?> = ArrayList()
            watchableObjects.add(
                createWatchableObject(
                    entityMetadata.index,
                    entityMetadata.type,
                    entityMetadata.value as Any,
                    versionAcc
                )
            )

            // add all dependant metas
            for (relatedMetadata in metadata.relatedMetadata) {
                val related = relatedMetadata.create(value, versionAcc)
                if (related.available) {
                    // create & register the watchable object
                    val watchableObject = createWatchableObject(
                        related.index,
                        related.type,
                        related.value,
                        versionAcc
                    )
                    if (watchableObject != null) {
                        watchableObjects.add(watchableObject)
                    }
                }
            }

            // EntityMetadata (https://wiki.vg/Protocol#Entity_Metadata)
            val container = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)

            // entity id
            container.integers.write(0, npc.entityId)

            // since 1.19.3 the metadata is wrapped in a specified object, we therefore need to convert all values
            if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                // convert the given values
                val wrappedDataValues: MutableList<WrappedDataValue> = ArrayList(watchableObjects.size)
                for (`object` in watchableObjects) {
                    val dataValue = WrappedDataValue(
                        `object`!!.index,
                        `object`.watcherObject.serializer,
                        `object`.rawValue
                    )
                    wrappedDataValues.add(dataValue)
                }

                // write the data values
                container.dataValueCollectionModifier.write(0, wrappedDataValues)
            } else {
                // entity id & metadata
                container.watchableCollectionModifier.write(0, watchableObjects)
            }

            // send the packet without notifying any bound packet listeners
            PROTOCOL_MANAGER.sendServerPacket(player, container, false)
        }
    }

    override fun createTeamsPacket(
        mode: TeamMode,
        teamName: String,
        info: TeamInfo?,
        players: List<String>
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            val versionAccessor: PlatformVersionAccessor = npc.platform.versionAccessor
            val is1_13Plus = MinecraftVersion.AQUATIC_UPDATE.atOrAbove()
            val is1_18Plus = MinecraftVersion.CAVES_CLIFFS_2.atOrAbove()
            val is1_20_1Plus = versionAccessor.atLeast(1, 20, 1)
            val is1_17Plus = MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()

            fun convertComponent(component: Component): WrappedChatComponent {
                return if (is1_13Plus) {
                    component.rawMessage?.let { raw -> WrappedChatComponent.fromLegacyText(raw) }
                        ?: WrappedChatComponent.fromJson(component.encodedJsonMessage)
                } else {
                    val rawMessage = requireNotNull(component.rawMessage) {
                        "Versions older than 1.13 don't support json component"
                    }

                    return WrappedChatComponent.fromLegacyText(rawMessage)
                }
            }

            // UpdateTeams (https://minecraft.wiki/w/Java_Edition_protocol/Packets#Update_Teams)
            val container = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)

            // 0: team name
            container.strings.write(0, teamName.take(if (is1_18Plus) 32767 else 16))

            // 1: mode (0 = create, 1 = remove, 2 = update, 3 = add-players, 4 = remove-players)
            container.integers.write(0, mode.ordinal)

            if (mode == TeamMode.CREATE || mode == TeamMode.UPDATE) {
                val scoreboardInfo = info ?: TeamInfo()

                val convertedDisplay = convertComponent(scoreboardInfo.displayName)
                val convertedPrefix = convertComponent(scoreboardInfo.prefix)
                val convertedSuffix = convertComponent(scoreboardInfo.suffix)

                if (!is1_13Plus) {
                    val display = convertedDisplay.toLegacy().take(32)
                    val prefix = convertedPrefix.toLegacy().take(16)
                    val suffix = convertedSuffix.toLegacy().take(16)

                    container.strings.write(1, display)
                    container.strings.write(2, prefix)
                    container.strings.write(3, suffix)

                    container.integers.write(1, scoreboardInfo.optionData.ordinal)

                    container.strings.write(4, scoreboardInfo.tagVisibility.id)
                    if (versionAccessor.atLeast(1, 9, 0)) {
                        container.strings.write(5, scoreboardInfo.collisionRule.id)
                    }

                    container.integers.write(2, scoreboardInfo.color.ordinal)
                } else {

                    if (is1_20_1Plus) {
                        container.optionalTeamParameters.write(0, Optional.of(WrappedTeamParameters.newBuilder()
                            .displayName(convertedDisplay)
                            .prefix(convertedPrefix)
                            .suffix(convertedSuffix)
                            .collisionRule(scoreboardInfo.collisionRule.id)
                            .options(scoreboardInfo.optionData.ordinal)
                            .nametagVisibility(scoreboardInfo.tagVisibility.id)
                            .color(EnumWrappers.ChatFormatting.entries[scoreboardInfo.color.ordinal])
                            .build()
                        ))
                    } else {
                        val struct = if (is1_17Plus) container.optionalStructures.readSafely(0).get() else container

                        struct.chatComponents.write(0, convertedDisplay)
                        struct.chatComponents.write(1, convertedPrefix)
                        struct.chatComponents.write(2, convertedSuffix)

                        struct.integers.write(if (is1_17Plus) 0 else 1, scoreboardInfo.optionData.ordinal)
                        struct.strings.write(
                            if (is1_17Plus) 0 else 1,
                            scoreboardInfo.tagVisibility.id
                        )
                        struct.strings.write(
                            if (is1_17Plus) 1 else 2,
                            scoreboardInfo.collisionRule.id
                        )

                        struct.getEnumModifier(
                            ChatColor::class.java,
                            MinecraftReflection.getMinecraftClass("EnumChatFormat")
                        ).write(0, ChatColor.entries[scoreboardInfo.color.ordinal])

                        if (is1_17Plus) {
                            container.optionalStructures.write(0, Optional.of(struct as InternalStructure))
                        }
                    }
                }
            }

            if (mode == TeamMode.CREATE || mode == TeamMode.ADD_ENTITIES || mode == TeamMode.REMOVE_ENTITIES) {
                container.getSpecificModifier(Collection::class.java).write(0, players.take(40))
            }

            // send the packet without notifying any bound packet listeners
            PROTOCOL_MANAGER.sendServerPacket(player, container, false)
        }
    }

    override fun initialize(platform: Platform<World, Player, ItemStack, Plugin>) {
        PROTOCOL_MANAGER.addPacketListener(NPCUsePacketAdapter(platform))
    }

    private class NPCUsePacketAdapter(private val platform: Platform<World, Player, ItemStack, Plugin>) :
        PacketAdapter(params(platform.extension, PacketType.Play.Client.USE_ENTITY).optionAsync()) {

        override fun onPacketReceiving(event: PacketEvent) {
            // get the entity id of the clicked entity
            val player: Player = event.player
            val packet: PacketContainer = event.packet
            val entityId = packet.integers.read(0)

            // get the associated npc from the tracked entities
            val npc: NPC<World, Player, ItemStack, Plugin>? =
                platform.npcTracker.npcById(entityId)

            if (npc != null) {
                // extract the used hand and interact action
                val action: EntityUseAction
                var hand: EnumWrappers.Hand? = EnumWrappers.Hand.MAIN_HAND

                if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
                    // mc 1.17: hand & action are now in an internal wrapper class
                    val useAction = packet.enumEntityUseActions.read(0)
                    action = useAction.action

                    // the hand is not explicitly send for attacks (always the main hand)
                    if (action != EntityUseAction.ATTACK) {
                        hand = useAction.hand
                    }
                } else {
                    // mc 1.8: hand & action are fields in the packet (or the hand is not even present)
                    action = packet.entityUseActions.read(0)

                    // the hand is not explicitly send for attacks (always the main hand)
                    if (action != EntityUseAction.ATTACK && MinecraftVersion.COMBAT_UPDATE.atOrAbove()) {
                        // mc 1.9: hand is now a thing
                        hand = packet.hands.read(0)
                    }
                }

                // call the event
                when (action) {
                    EntityUseAction.ATTACK -> platform.eventManager.post(DefaultAttackNPCEvent.attackNPC(npc, player))
                    EntityUseAction.INTERACT -> {
                        val usedHand: InteractNPCEvent.Hand = HAND_CONVERTER[hand]!!
                        platform.eventManager.post(DefaultInteractNPCEvent.interactNPC(npc, player, usedHand))
                    }

                    else -> {}
                }

                // don't pass the packet to the server
                event.isCancelled = true
            }
        }
    }
}