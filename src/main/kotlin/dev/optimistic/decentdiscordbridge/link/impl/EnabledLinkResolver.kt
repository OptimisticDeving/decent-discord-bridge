package dev.optimistic.decentdiscordbridge.link.impl

import dev.optimistic.decentdiscordbridge.link.LinkResolver
import dev.optimistic.decentdiscordbridge.text.DelimiterBasedStringTextConversion
import dev.optimistic.decentdiscordbridge.text.StringToTextConversion
import net.minecraft.text.ClickEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object EnabledLinkResolver : LinkResolver(), StringToTextConversion {
    private val linkRegex =
        Regex(
            "https?://[\\w-]{2,}(?:\\.[\\w-]+)*\\.?(?::\\d+)?(?:/.*)?",
            RegexOption.IGNORE_CASE,
        )

    override fun convert(input: String): MutableText {
        val matches = linkRegex.findAll(input)
        if (matches.none())
            return Text.literal(input)
        val component = Text.empty()

        for ((idx, match) in matches.withIndex()) {
            if (idx == 0) {
                val before = match.range.first
                if (before > 0) component.append(Text.literal(input.substring(0, before)))
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
}