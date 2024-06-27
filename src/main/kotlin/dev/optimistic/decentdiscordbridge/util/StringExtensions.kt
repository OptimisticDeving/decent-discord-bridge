package dev.optimistic.decentdiscordbridge.util

object StringExtensions {
    private val specialCharacterRegex = Regex("([*_#\\[(\\-`|\\\\]|(?<!<@\\d{17,50})>)")
    private val nonChat = Regex("[^\\p{L}\\p{N}\\p{P}\\p{S}\\p{Z}]")

    fun String.escapeDiscordSpecial() = this
        .replace(specialCharacterRegex, "\\\\$1")
        .replace(nonChat, "�")

    fun String.escapeMinecraftSpecial() = this
        .replace("§", "&")
        .replace(nonChat, "�")
}