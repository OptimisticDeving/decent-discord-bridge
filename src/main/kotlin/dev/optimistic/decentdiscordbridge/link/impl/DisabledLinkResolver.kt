package dev.optimistic.decentdiscordbridge.link.impl

import dev.optimistic.decentdiscordbridge.link.AbstractLinkResolver
import dev.optimistic.decentdiscordbridge.component.DelimiterBasedStringComponentConversion
import dev.optimistic.decentdiscordbridge.component.StringToComponentConversion
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeDiscordSpecial

object DisabledLinkResolver : AbstractLinkResolver() {
    override val delimitedConversion: DelimiterBasedStringComponentConversion =
        createDelimConversion(StringToComponentConversion.Literal)

    override fun escapeNotLinks(input: String): String = input.escapeDiscordSpecial()
}