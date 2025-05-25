package dev.dani.velar.api

import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.api.flag.flagged.NPCFlaggedBuilder
import dev.dani.velar.api.flag.flagged.NPCFlaggedObject


/*
 * Project: velar
 * Created at: 18/05/2025 21:51
 * Created by: Dani-error
 */
interface NPCActionController : NPCFlaggedObject {

    companion object {

        val AUTO_SYNC_POSITION_ON_SPAWN = NPCFlag.flag("auto_sync_position_on_spawn", true)
        val SPAWN_DISTANCE = NPCFlag.flag("action_spawn_distance", 50) { it!! >= 0 }
        val TAB_REMOVAL_TICKS = NPCFlag.flag("action_tab_removal", 30) { it!! >= 0 }
        val IMITATE_DISTANCE = NPCFlag.flag("action_imitate_distance", 20) { it!! >= 0 }
        
    }

    interface Builder : NPCFlaggedBuilder<Builder> {
        fun build(): NPCActionController
    }
}
