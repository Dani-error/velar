package dev.dani.velar.common.settings

import dev.dani.velar.api.settings.NPCProfileResolver
import dev.dani.velar.api.settings.NPCSettings
import dev.dani.velar.api.settings.NPCTrackingRule
import dev.dani.velar.common.flag.CommonNPCFlaggedBuilder


/*
 * Project: velar
 * Created at: 25/05/2025 19:14
 * Created by: Dani-error
 */
class CommonNPCSettingsBuilder<P> : CommonNPCFlaggedBuilder<NPCSettings.Builder<P>>(), NPCSettings.Builder<P> {

    private var trackingRule: NPCTrackingRule<P> = NPCTrackingRule.allPlayers()
    private var profileResolver: NPCProfileResolver<P> = NPCProfileResolver.ofSpawnedNPC()

    override fun trackingRule(trackingRule: NPCTrackingRule<P>): NPCSettings.Builder<P> {
        this.trackingRule = trackingRule
        return this
    }

    override fun profileResolver(profileResolver: NPCProfileResolver<P>): NPCSettings.Builder<P> {
        this.profileResolver = profileResolver
        return this
    }

    override fun build(): NPCSettings<P> =
        CommonNPCSettings(this.flags, this.trackingRule, this.profileResolver)

}