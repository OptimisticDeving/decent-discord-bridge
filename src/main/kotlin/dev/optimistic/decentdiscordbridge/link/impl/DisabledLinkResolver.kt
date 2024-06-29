package dev.optimistic.decentdiscordbridge.link.impl

import dev.optimistic.decentdiscordbridge.link.LinkResolver
import dev.optimistic.decentdiscordbridge.text.DelimiterBasedStringTextConversion
import dev.optimistic.decentdiscordbridge.text.StringToTextConversion

object DisabledLinkResolver : LinkResolver() {
    override val delimitedConversion: DelimiterBasedStringTextConversion =
        createDelimConversion(StringToTextConversion.Literal)
}