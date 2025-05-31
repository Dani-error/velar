package dev.dani.velar.api.protocol


/*
 * Project: velar
 * Created at: 25/05/2025 18:09
 * Created by: Dani-error
 */
interface Component {

    val rawMessage: String?
    val encodedJsonMessage: String?

    companion object {

        fun ofRawMessage(rawMessage: String): Component =
            DefaultComponent(rawMessage, null)

        fun ofJsonEncodedMessage(jsonEncodedMessage: String): Component =
            DefaultComponent(null, jsonEncodedMessage)

        fun ofJsonEncodedOrRaw(rawMessage: String?, jsonEncodedMessage: String?): Component {
            require(!(rawMessage == null && jsonEncodedMessage == null)) {
                "Either rawMessage or jsonEncodedMessage must be given"
            }

            return DefaultComponent(rawMessage, jsonEncodedMessage)
        }

        fun empty() = ofRawMessage("")

    }

}

internal class DefaultComponent(
    override val rawMessage: String?,
    override val encodedJsonMessage: String?
) : Component