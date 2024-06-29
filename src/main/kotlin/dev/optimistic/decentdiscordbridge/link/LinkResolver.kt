package dev.optimistic.decentdiscordbridge.link

import dev.optimistic.decentdiscordbridge.text.DelimiterBasedStringTextConversion
import dev.optimistic.decentdiscordbridge.text.StringToTextConversion

abstract class LinkResolver {
    protected abstract val delimitedConversion: DelimiterBasedStringTextConversion

    fun resolveLinks(input: String) = this.delimitedConversion.convert(input)

    protected companion object {
        fun createDelimConversion(conversion: StringToTextConversion) =
            DelimiterBasedStringTextConversion(" ", conversion)
    }
}