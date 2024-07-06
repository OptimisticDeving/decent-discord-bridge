package dev.optimistic.decentdiscordbridge.util

object StringExtensions {
    private const val SPECIAL_CHARACTER_TEMPLATE =
        "([*#\\[(\\-`|\\\\]|(?<!:\\w{1,32})_(?!\\w+:)|(?<!<@\\d{17,50})>)"
    private val specialCharacterRegex = Regex(SPECIAL_CHARACTER_TEMPLATE)
    private val specialCharacterEscapeRegex = Regex("\\\\$SPECIAL_CHARACTER_TEMPLATE")
    private val nonChat = Regex("[^\\p{L}\\p{N}\\p{P}\\p{S}\\p{Z}]")

    fun String.escapeDiscordSpecial() = this
        .replace(specialCharacterRegex, "\\\\$1")
        .replace(nonChat, "�")

    fun String.escapeMinecraftSpecial() = this
        .replace("§", "&")
        .replace(nonChat, "�")

    fun String.stripDiscordEscapes() = this
        .replace(specialCharacterEscapeRegex, "$1")
}