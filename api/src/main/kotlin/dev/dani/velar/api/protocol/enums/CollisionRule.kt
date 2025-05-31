package dev.dani.velar.api.protocol.enums


/*
 * Project: velar
 * Created at: 31/05/2025 20:38
 * Created by: Dani-error
 */
enum class CollisionRule(val id: String) {
    ALWAYS("always"),
    NEVER("never"),
    PUSH_OTHER_TEAMS("pushOtherTeams"),
    PUSH_OWN_TEAM("pushOwnTeam");
}