package dev.optimistic.decentdiscordbridge.link

import dev.optimistic.decentdiscordbridge.component.DelimiterBasedStringComponentConversion
import dev.optimistic.decentdiscordbridge.component.StringToComponentConversion
import dev.optimistic.decentdiscordbridge.util.StringExtensions.stripDiscordEscapes
import net.minecraft.network.chat.Component

abstract class AbstractLinkResolver {
    protected abstract val delimitedConversion: DelimiterBasedStringComponentConversion

    fun resolveLinks(input: String) = this.delimitedConversion.convert(input) { Component.literal(it.stripDiscordEscapes()) }
    abstract fun escapeNotLinks(input: String): String

    protected companion object {
        fun createDelimConversion(conversion: StringToComponentConversion) =
            DelimiterBasedStringComponentConversion(" ", conversion)
    }
}