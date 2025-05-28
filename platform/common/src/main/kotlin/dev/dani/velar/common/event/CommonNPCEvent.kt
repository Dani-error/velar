package dev.dani.velar.common.event

import dev.dani.velar.api.NPC
import dev.dani.velar.api.event.NPCEvent


/*
 * Project: velar
 * Created at: 25/05/2025 19:18
 * Created by: Dani-error
 */
abstract class CommonNPCEvent(override val npc: NPC<*, *, *, *>) : NPCEvent