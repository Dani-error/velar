package dev.dani.velar.bukkit.protocol.impl

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent
import com.github.retrooper.packetevents.manager.player.PlayerManager
import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.*
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.settings.PacketEventsSettings
import com.github.retrooper.packetevents.util.TimeStampMode
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction
import com.github.retrooper.packetevents.wrapper.play.server.*
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation.*
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo.PlayerData
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo
import dev.dani.velar.api.NPC
import dev.dani.velar.api.platform.Platform
import dev.dani.velar.api.platform.PlatformVersionAccessor
import dev.dani.velar.api.util.Position
import dev.dani.velar.api.event.InteractNPCEvent
import dev.dani.velar.api.protocol.Component
import dev.dani.velar.api.protocol.OutboundPacket
import dev.dani.velar.api.protocol.PlatformPacketAdapter
import dev.dani.velar.api.protocol.enums.EntityAnimation
import dev.dani.velar.api.protocol.enums.EntityPose
import dev.dani.velar.api.protocol.enums.ItemSlot
import dev.dani.velar.api.protocol.enums.PlayerInfoAction
import dev.dani.velar.api.protocol.meta.EntityMetadataFactory
import dev.dani.velar.bukkit.util.enumMapOf
import dev.dani.velar.common.event.DefaultAttackNPCEvent
import dev.dani.velar.common.event.DefaultInteractNPCEvent
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import io.leangen.geantyref.TypeFactory
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.lang.reflect.Type
import java.util.*


/*
 * Project: velar
 * Created at: 25/05/2025 21:01
 * Created by: Dani-error
 */
object PacketEventsPacketAdapter : PlatformPacketAdapter<World, Player, ItemStack, Plugin> {

    private val PACKET_EVENTS_SETTINGS: PacketEventsSettings = PacketEventsSettings()
        .debug(false)
        .checkForUpdates(false)
        .reEncodeByDefault(false)
        .timeStampMode(TimeStampMode.NONE)

    private val OPTIONAL_CHAT_COMPONENT_TYPE: Type = TypeFactory.parameterizedClass(
        Optional::class.java,
        Component::class.java
    )


    // lazy initialized, then never null again
    private var serverVersion: ServerVersion? = null
    private var packetPlayerManager: PlayerManager? = null

    private fun npcLocation(npc: NPC<*, *, *, *>): Location {
        return npcLocation(npc, npc.position.yaw, npc.position.pitch)
    }

    private fun npcLocation(npc: NPC<*, *, *, *>, yaw: Float, pitch: Float): Location {
        val pos: Position = npc.position
        return Location(pos.x, pos.y, pos.z, yaw, pitch)
    }

    private fun createEntityData(
        index: Int,
        type: Type,
        value: Any,
        versionAccessor: PlatformVersionAccessor
    ): EntityData<*> {
        var newType = type
        var newValue: Any? = value

        val converter = Lazy.SERIALIZER_CONVERTERS[type]
        if (converter != null) {
            val converted: Map.Entry<Type, Any?> = converter(versionAccessor, value)
            // re-assign the type and value
            newType = converted.key
            newValue = converted.value
        }

        val lazyType: EntityDataType<Any?> = Lazy.ENTITY_DATA_TYPE_LOOKUP[newType] as EntityDataType<Any?>
        return EntityData(index, lazyType, newValue)
    }

    override fun createEntitySpawnPacket(): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            val location: Location = npcLocation(npc)
            val wrapper: PacketWrapper<*> = if (this.serverVersion!!.isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            // SpawnEntity (https://wiki.vg/Protocol#Spawn_Entity)
            WrapperPlayServerSpawnEntity(
                npc.entityId,
                Optional.ofNullable(npc.profile.uniqueId),
                EntityTypes.PLAYER,
                location.position,
                location.pitch,
                location.yaw,
                0f,
                0,
                Optional.empty()
            )
        } else {
            // SpawnPlayer (https://wiki.vg/Protocol#Spawn_Player)
            WrapperPlayServerSpawnPlayer(npc.entityId, npc.profile.uniqueId, location)
        }

            // send the packet without notifying any listeners
            packetPlayerManager!!.sendPacketSilently(player, wrapper)
        }
    }

    override fun createEntityRemovePacket(): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            // DestroyEntities (https://wiki.vg/Protocol#Destroy_Entities)
            val wrapper: PacketWrapper<*> = WrapperPlayServerDestroyEntities(npc.entityId)
            packetPlayerManager!!.sendPacketSilently(player, wrapper)
        }
    }

    override fun createPlayerInfoPacket(
        action: PlayerInfoAction
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            npc.settings.profileResolver.resolveNPCProfile(player, npc).thenAcceptAsync { profile ->
                // convert the profile to a UserProfile
                val userProfile = UserProfile(profile.uniqueId, profile.name)
                for (property in profile.properties!!) {
                    val textureProperty =
                        TextureProperty(property.name, property.value, property.signature)
                    userProfile.textureProperties.add(textureProperty)
                }

                // the wrapper we want to send
                val wrapper: PacketWrapper<*>

                // check if we need to apply the old handling or new handling
                if (serverVersion!!.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
                    if (action == PlayerInfoAction.REMOVE_PLAYER) {
                        // PlayerRemove (https://wiki.vg/Protocol#Player_Remove)
                        val uuidsToRemove: List<UUID> =
                            listOf(profile.uniqueId)
                        wrapper = WrapperPlayServerPlayerInfoRemove(uuidsToRemove)
                    } else {
                        // create the player
                        val playerInfo = PlayerInfo(
                            userProfile,
                            false,
                            20,
                            GameMode.CREATIVE,
                            null,
                            null,
                            0,
                            true
                        )

                        // PlayerInfo (https://wiki.vg/Protocol#Player_Info)
                        wrapper = WrapperPlayServerPlayerInfoUpdate(Lazy.ADD_ACTIONS, listOf(playerInfo))
                    }
                } else {
                    // create the player profile data
                    val playerData = PlayerData(
                        null,
                        userProfile,
                        GameMode.CREATIVE,
                        20
                    )

                    // PlayerInfo (https://wiki.vg/Protocol#Player_Info)
                    val playerInfoAction: WrapperPlayServerPlayerInfo.Action =
                        Lazy.PLAYER_INFO_ACTION_CONVERTER[action]!!
                    wrapper = WrapperPlayServerPlayerInfo(playerInfoAction, playerData)
                }

                // send the packet without notifying any listeners
                packetPlayerManager!!.sendPacketSilently(player, wrapper)
            }
        }
    }

    override fun createRotationPacket(yaw: Float, pitch: Float): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            // head rotation (https://wiki.vg/Protocol#Entity_Head_Look)
            val headRotation: PacketWrapper<*> = WrapperPlayServerEntityHeadLook(npc.entityId, yaw)

            // entity teleport (https://wiki.vg/Protocol#Entity_Teleport) or Player Rotation (https://wiki.vg/Protocol#Player_Rotation)
            val rotation: PacketWrapper<*> = if (this.serverVersion!!.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            // mc 1.9: player rotation
            WrapperPlayServerEntityRotation(npc.entityId, yaw, pitch, true)
        } else {
            // mc 1.8: entity teleport
            WrapperPlayServerEntityTeleport(npc.entityId, npcLocation(npc, yaw, pitch), true)
        }

            // send the packet without notifying any listeners
            packetPlayerManager!!.sendPacketSilently(player, rotation)
            packetPlayerManager!!.sendPacketSilently(player, headRotation)
        }
    }

    override fun createAnimationPacket(
        animation: EntityAnimation
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            // EntityAnimation (https://wiki.vg/Protocol#Entity_Animation_.28clientbound.29)
            val animationType
                    : EntityAnimationType = Lazy.ENTITY_ANIMATION_CONVERTER[animation]!!
            val wrapper: PacketWrapper<*> = WrapperPlayServerEntityAnimation(npc.entityId, animationType)

            // send the packet without notifying any listeners
            packetPlayerManager!!.sendPacketSilently(player, wrapper)
        }
    }

    override fun createEquipmentPacket(
        slot: ItemSlot,
        item: ItemStack
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            val equipmentSlot: EquipmentSlot = Lazy.ITEM_SLOT_CONVERTER[slot]!!
            val `is` = SpigotReflectionUtil.decodeBukkitItemStack(item)

            // EntityEquipment (https://wiki.vg/Protocol#Entity_Equipment)
            val equipment = Equipment(equipmentSlot, `is`)
            val wrapper: PacketWrapper<*> = WrapperPlayServerEntityEquipment(
                npc.entityId,
                listOf(equipment)
            )

            // send the packet without notifying any listeners
            packetPlayerManager!!.sendPacketSilently(player, wrapper)
        }
    }

    override fun createCustomPayloadPacket(
        channelId: String,
        payload: ByteArray
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, _: NPC<World, Player, ItemStack, Plugin>? ->
            // CustomPayload (https://wiki.vg/Protocol#Custom_Payload)
            val wrapper: PacketWrapper<*> = WrapperPlayServerPluginMessage(channelId, payload)
            packetPlayerManager!!.sendPacketSilently(player, wrapper)
        }
    }

    override fun <T, O> createEntityMetaPacket(
        metadata: EntityMetadataFactory<T, O>,
        value: T
    ): OutboundPacket<World, Player, ItemStack, Plugin> {
        return OutboundPacket { player: Player, npc: NPC<World, Player, ItemStack, Plugin> ->
            // create the entity meta
            val versionAccessor: PlatformVersionAccessor = npc.platform.versionAccessor
            val entityMetadata = metadata.create(value, versionAccessor)

            // check if the meta is available
            if (!entityMetadata.available) {
                return@OutboundPacket
            }

            // construct the meta we want to send out
            val entityData: MutableList<EntityData<*>> = ArrayList()
            entityData.add(
                createEntityData(
                    entityMetadata.index,
                    entityMetadata.type,
                    entityMetadata.value as Any,
                    versionAccessor
                )
            )

            // add ll dependant metas
            for (relatedMetadata in metadata.relatedMetadata) {
                val related = relatedMetadata.create(value, versionAccessor)
                if (related.available) {
                    entityData.add(createEntityData(related.index, related.type, related.value, versionAccessor))
                }
            }

            // EntityMetadata (https://wiki.vg/Protocol#Entity_Metadata)
            val wrapper: PacketWrapper<*> = WrapperPlayServerEntityMetadata(npc.entityId, entityData)
            packetPlayerManager!!.sendPacketSilently(player, wrapper)
        }
    }

    override fun initialize(platform: Platform<World, Player, ItemStack, Plugin>) {
        // build the packet events api
        val packetEventsApi = SpigotPacketEventsBuilder.buildNoCache(
            platform.extension,
            PACKET_EVENTS_SETTINGS
        )

        // while I am not the biggest fan of that, it looks like
        // that packet events is using the instance internally everywhere
        // instead of passing the created instance around, which leaves us
        // no choice than setting it as well :/
        PacketEvents.setAPI(packetEventsApi)

        // ensure that our api instance is initialized
        packetEventsApi.init()

        // store the packet player manager & server version
        this.packetPlayerManager = packetEventsApi.playerManager
        this.serverVersion = packetEventsApi.serverManager.version

        // add the packet listener
        packetEventsApi.eventManager.registerListener(NPCUsePacketAdapter(platform))
    }

    internal class NPCUsePacketAdapter(private val platform: Platform<World, Player, ItemStack, Plugin>) :
        SimplePacketListenerAbstract(PacketListenerPriority.MONITOR) {

        override fun onPacketPlayReceive(event: PacketPlayReceiveEvent) {
            // check for an entity use packet
            val player = event.getPlayer<Any>()
            if (event.packetType == PacketType.Play.Client.INTERACT_ENTITY) {
                val packet = WrapperPlayClientInteractEntity(event)

                // get the associated npc from the tracked entities
                val npc: NPC<World, Player, ItemStack, Plugin>? =
                    platform.npcTracker.npcById(packet.entityId)
                if (npc != null) {
                    // call the event
                    when (packet.action) {
                        InteractAction.ATTACK -> platform.eventManager
                            .post(DefaultAttackNPCEvent.attackNPC(npc, player))

                        InteractAction.INTERACT -> {
                            val hand: InteractNPCEvent.Hand = Lazy.HAND_CONVERTER[packet.hand]!!
                            platform.eventManager.post(DefaultInteractNPCEvent.interactNPC(npc, player, hand))
                        }

                        else -> {}
                    }

                    // don't pass the packet to the server
                    event.isCancelled = true
                }
            }
        }
    }

    object Lazy {
        val ITEM_SLOT_CONVERTER: EnumMap<ItemSlot, EquipmentSlot> by lazy {
            enumMapOf(
                ItemSlot.MAIN_HAND to EquipmentSlot.MAIN_HAND,
                ItemSlot.OFF_HAND to EquipmentSlot.OFF_HAND,
                ItemSlot.FEET to EquipmentSlot.BOOTS,
                ItemSlot.LEGS to EquipmentSlot.LEGGINGS,
                ItemSlot.CHEST to EquipmentSlot.CHEST_PLATE,
                ItemSlot.HEAD to EquipmentSlot.HELMET
            )
        }

        val HAND_CONVERTER: EnumMap<InteractionHand, InteractNPCEvent.Hand> by lazy {
            enumMapOf(
                InteractionHand.MAIN_HAND to InteractNPCEvent.Hand.MAIN_HAND,
                InteractionHand.OFF_HAND to InteractNPCEvent.Hand.OFF_HAND
            )
        }

        val PLAYER_INFO_ACTION_CONVERTER: EnumMap<PlayerInfoAction, WrapperPlayServerPlayerInfo.Action> by lazy {
            enumMapOf(
                PlayerInfoAction.ADD_PLAYER to WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                PlayerInfoAction.REMOVE_PLAYER to WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER
            )
        }

        val ENTITY_ANIMATION_CONVERTER: EnumMap<EntityAnimation, EntityAnimationType> by lazy {
            enumMapOf(
                EntityAnimation.SWING_MAIN_ARM to EntityAnimationType.SWING_MAIN_ARM,
                EntityAnimation.TAKE_DAMAGE to EntityAnimationType.HURT,
                EntityAnimation.LEAVE_BED to EntityAnimationType.WAKE_UP,
                EntityAnimation.SWING_OFF_HAND to EntityAnimationType.SWING_OFF_HAND,
                EntityAnimation.CRITICAL_EFFECT to EntityAnimationType.CRITICAL_HIT,
                EntityAnimation.MAGIC_CRITICAL_EFFECT to EntityAnimationType.MAGIC_CRITICAL_HIT
            )
        }

        val ENTITY_POSE_CONVERTER: EnumMap<EntityPose, com.github.retrooper.packetevents.protocol.entity.pose.EntityPose> by lazy {
            enumMapOf(
                EntityPose.STANDING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.STANDING,
                EntityPose.FALL_FLYING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.FALL_FLYING,
                EntityPose.SLEEPING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SLEEPING,
                EntityPose.SWIMMING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SWIMMING,
                EntityPose.SPIN_ATTACK to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SPIN_ATTACK,
                EntityPose.CROUCHING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.CROUCHING,
                EntityPose.LONG_JUMPING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.LONG_JUMPING,
                EntityPose.DYING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.DYING,
                EntityPose.CROAKING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.CROAKING,
                EntityPose.USING_TONGUE to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.USING_TONGUE,
                EntityPose.ROARING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.ROARING,
                EntityPose.SNIFFING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SNIFFING,
                EntityPose.EMERGING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.EMERGING,
                EntityPose.DIGGING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.DIGGING,
                EntityPose.SLIDING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SLIDING,
                EntityPose.SHOOTING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SHOOTING,
                EntityPose.INHALING to com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.INHALING
            )
        }

        val ADD_ACTIONS: EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> by lazy {
            EnumSet.of(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_HAT,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME
            )
        }

        val SERIALIZER_CONVERTERS: Map<Type, (PlatformVersionAccessor, Any) -> Map.Entry<Type, Any?>> by lazy {
            mapOf(
                EntityPose::class.java to { _, value ->
                    AbstractMap.SimpleImmutableEntry(
                        com.github.retrooper.packetevents.protocol.entity.pose.EntityPose::class.java,
                        ENTITY_POSE_CONVERTER[value]
                    )
                },
                TypeFactory.parameterizedClass(Optional::class.java, Component::class.java) to { accessor, value ->
                    val optional = value as Optional<*>
                    if (accessor.atLeast(1, 13, 0)) {
                        AbstractMap.SimpleImmutableEntry(
                            OPTIONAL_CHAT_COMPONENT_TYPE,
                            optional.map {
                                val component = it as Component
                                component.rawMessage?.let { raw -> AdventureSerializer.fromLegacyFormat(raw) }
                                    ?: AdventureSerializer.parseComponent(component.encodedJsonMessage)
                            }
                        )
                    } else {
                        AbstractMap.SimpleImmutableEntry(
                            String::class.java,
                            optional.map {
                                val component = it as Component
                                requireNotNull(component.rawMessage) {
                                    "Versions older than 1.13 don't support json component"
                                }
                            }.orElse(null)
                        )
                    }
                }
            )
        }

        val ENTITY_DATA_TYPE_LOOKUP: Map<Type, EntityDataType<*>> by lazy {
            mapOf(
                java.lang.Byte::class.java to EntityDataTypes.BYTE,
                java.lang.Integer::class.java to EntityDataTypes.INT,
                java.lang.Float::class.java to EntityDataTypes.FLOAT,
                java.lang.Boolean::class.java to EntityDataTypes.BOOLEAN,
                java.lang.String::class.java to EntityDataTypes.STRING,
                OPTIONAL_CHAT_COMPONENT_TYPE to EntityDataTypes.OPTIONAL_ADV_COMPONENT,
                com.github.retrooper.packetevents.protocol.entity.pose.EntityPose::class.java to EntityDataTypes.ENTITY_POSE
            )
        }
    }

}