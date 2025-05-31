package dev.dani.velar.api.protocol

import dev.dani.velar.api.protocol.enums.CollisionRule
import dev.dani.velar.api.protocol.enums.NameTagVisibility
import dev.dani.velar.api.protocol.enums.OptionData
import dev.dani.velar.api.protocol.enums.TextColor


/*
 * Project: velar
 * Created at: 31/05/2025 20:35
 * Created by: Dani-error
 */
data class TeamInfo(
    var displayName: Component = Component.empty(),
    var prefix: Component = Component.empty(),
    var suffix: Component = Component.empty(),
    var tagVisibility: NameTagVisibility = NameTagVisibility.ALWAYS,
    var collisionRule: CollisionRule = CollisionRule.ALWAYS,
    var color: TextColor = TextColor.WHITE,
    var optionData: OptionData = OptionData.NONE
)
