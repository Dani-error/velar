package dev.dani.velar.common

import dev.dani.velar.api.NPCActionController
import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.common.flag.CommonNPCFlaggedObject


/*
 * Project: velar
 * Created at: 25/05/2025 19:06
 * Created by: Dani-error
 */
abstract class CommonNPCActionController(flags: Map<NPCFlag<*>, Any?>) : CommonNPCFlaggedObject(flags), NPCActionController