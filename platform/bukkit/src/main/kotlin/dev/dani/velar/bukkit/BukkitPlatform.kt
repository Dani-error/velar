package dev.dani.velar.bukkit

import dev.dani.velar.api.NPCActionController
import dev.dani.velar.api.platform.Platform
import dev.dani.velar.api.platform.log.PlatformLogger
import dev.dani.velar.bukkit.BukkitProfileResolver.profileResolver
import dev.dani.velar.bukkit.BukkitVersionAccessor.versionAccessor
import dev.dani.velar.bukkit.BukkitWorldAccessor.worldAccessor
import dev.dani.velar.bukkit.protocol.impl.BukkitProtocolAdapter.packetAdapter
import dev.dani.velar.bukkit.util.BukkitPlatformUtil.runsOnFolia
import dev.dani.velar.common.platform.CommonPlatform
import dev.dani.velar.common.platform.CommonPlatformBuilder
import dev.dani.velar.common.task.AsyncPlatformTaskManager
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin


/*
 * Project: velar
 * Created at: 25/05/2025 21:48
 * Created by: Dani-error
 */
class BukkitPlatform : CommonPlatformBuilder<World, Player, ItemStack, Plugin>() {

    override fun prepareBuild() {
        // set the profile resolver to a native platform one if not given
        if (this.profileResolver == null) {
            this.profileResolver = profileResolver()
        }

        // set the default task manager
        if (this.taskManager == null) {
            if (runsOnFolia()) {
                this.taskManager = AsyncPlatformTaskManager.taskManager(extension!!.name)
            } else {
                this.taskManager = BukkitPlatformTaskManager.taskManager(extension!!)
            }
        }

        // set the default version accessor
        if (this.versionAccessor == null) {
            this.versionAccessor = versionAccessor()
        }

        // set the default world accessor
        if (this.worldAccessor == null) {
            this.worldAccessor = worldAccessor()
        }

        // set the default packet adapter
        if (this.packetAdapter == null) {
            this.packetAdapter = packetAdapter()
        }

        // set the default logger if no logger was provided
        if (this.logger == null) {
            this.logger = PlatformLogger.fromJul(extension!!.logger)
        }
    }

    override fun doBuild(): Platform<World, Player, ItemStack, Plugin> {
        // check if we need an action controller
        var actionController: NPCActionController? = null
        if (this.actionControllerDecorator != null) {
            val builder: NPCActionController.Builder = BukkitActionController.actionControllerBuilder(
                extension!!,
                eventManager!!,
                versionAccessor!!,
                npcTracker!!
            )
            actionControllerDecorator?.invoke(builder)
            actionController = builder.build()
        }

        // build the platform
        return CommonPlatform(
            this.debug,
            this.extension!!,
            logger!!,
            this.npcTracker!!,
            profileResolver!!,
            taskManager!!,
            actionController,
            versionAccessor!!,
            eventManager!!,
            this.worldAccessor!!,
            this.packetAdapter!!
        )
    }

    companion object {
        fun bukkitNpcPlatformBuilder(): BukkitPlatform {
            return BukkitPlatform()
        }
    }
}