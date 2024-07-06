package dev.optimistic.decentdiscordbridge.link.impl

import dev.optimistic.decentdiscordbridge.link.AbstractLinkResolver
import dev.optimistic.decentdiscordbridge.text.DelimiterBasedStringTextConversion
import dev.optimistic.decentdiscordbridge.text.StringToTextConversion
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeDiscordSpecial
import net.minecraft.text.ClickEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object EnabledLinkResolver : AbstractLinkResolver(), StringToTextConversion {
    private val linkRegex =
        Regex(
            "https?://[\\w-]{2,}(?:\\.[\\w-]+)*\\.?(?::\\d+)?(?:/.*)?",
            RegexOption.IGNORE_CASE,
        )

    override fun convert(input: String, escapedLiteralFactory: (String) -> MutableText): MutableText {
        val matches = linkRegex.findAll(input)
        if (matches.none())
            return escapedLiteralFactory(input)
        val component = Text.empty()

        for ((idx, match) in matches.withIndex()) {
            if (idx == 0) {
                val before = match.range.first
                if (before > 0) component.append(escapedLiteralFactory(input.substring(0, before)))
            }

            component.append(
                Text.literal(match.value)
                    .formatted(Formatting.BLUE)
                    .styled { it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, match.value)) }
            )
        }

        return component
    }

    override val delimitedConversion: DelimiterBasedStringTextConversion = createDelimConversion(this)

    override fun escapeNotLinks(input: String): String {
        val matches = linkRegex.findAll(input)
        if (matches.none())
            return input.escapeDiscordSpecial()

        val stringBuilder = StringBuilder()
        for ((idx, match) in matches.withIndex()) {
            if (idx == 0) {
                val before = match.range.first
                if (before > 0) stringBuilder.append(input.substring(0, before).escapeDiscordSpecial())
            }

            stringBuilder.append(match.value)
        }

        return stringBuilder.toString()
    }
}