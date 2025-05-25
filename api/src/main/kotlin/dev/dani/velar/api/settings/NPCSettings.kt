package dev.dani.velar.api.settings

import dev.dani.velar.api.flag.flagged.NPCFlaggedBuilder
import dev.dani.velar.api.flag.flagged.NPCFlaggedObject


/*
 * Project: velar
 * Created at: 25/05/2025 17:56
 * Created by: Dani-error
 */
interface NPCSettings<P> : NPCFlaggedObject {

    val trackingRule: NPCTrackingRule<P>
    val profileResolver: NPCProfileResolver<P>

    interface Builder<P> : NPCFlaggedBuilder<Builder<P>> {

        fun trackingRule(trackingRule: NPCTrackingRule<P>): Builder<P>

        fun profileResolver(profileResolver: NPCProfileResolver<P>): Builder<P>

        fun build(): NPCSettings<P>

    }

}