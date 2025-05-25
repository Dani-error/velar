package dev.dani.velar.common.settings

import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.api.settings.NPCProfileResolver
import dev.dani.velar.api.settings.NPCSettings
import dev.dani.velar.api.settings.NPCTrackingRule
import dev.dani.velar.common.flag.CommonNPCFlaggedObject


/*
 * Project: velar
 * Created at: 25/05/2025 19:13
 * Created by: Dani-error
 */
class CommonNPCSettings<P>(
    flags: Map<NPCFlag<*>, Any?>,
    override val trackingRule: NPCTrackingRule<P>,
    override val profileResolver: NPCProfileResolver<P>
) : CommonNPCFlaggedObject(flags), NPCSettings<P>