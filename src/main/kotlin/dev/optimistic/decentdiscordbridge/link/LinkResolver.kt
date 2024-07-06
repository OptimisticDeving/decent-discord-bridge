package dev.optimistic.decentdiscordbridge.link

import dev.optimistic.decentdiscordbridge.text.DelimiterBasedStringTextConversion
import dev.optimistic.decentdiscordbridge.text.StringToTextConversion
import dev.optimistic.decentdiscordbridge.util.StringExtensions.stripDiscordEscapes
import net.minecraft.text.Text

abstract class LinkResolver {
    protected abstract val delimitedConversion: DelimiterBasedStringTextConversion

    fun resolveLinks(input: String) = this.delimitedConversion.convert(input) { Text.literal(it.stripDiscordEscapes()) }

    protected companion object {
        fun createDelimConversion(conversion: StringToTextConversion) =
            DelimiterBasedStringTextConversion(" ", conversion)
    }
}