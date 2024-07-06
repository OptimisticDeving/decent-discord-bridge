package dev.optimistic.decentdiscordbridge.link.impl

import dev.optimistic.decentdiscordbridge.link.AbstractLinkResolver
import dev.optimistic.decentdiscordbridge.text.DelimiterBasedStringTextConversion
import dev.optimistic.decentdiscordbridge.text.StringToTextConversion
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeDiscordSpecial

object DisabledLinkResolver : AbstractLinkResolver() {
    override val delimitedConversion: DelimiterBasedStringTextConversion =
        createDelimConversion(StringToTextConversion.Literal)

    override fun escapeNotLinks(input: String): String = input.escapeDiscordSpecial()
}