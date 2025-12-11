package dev.optimistic.decentdiscordbridge.link.impl

import dev.optimistic.decentdiscordbridge.link.AbstractLinkResolver
import dev.optimistic.decentdiscordbridge.component.DelimiterBasedStringComponentConversion
import dev.optimistic.decentdiscordbridge.component.StringToComponentConversion
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeDiscordSpecial
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.ClickEvent.Action.OPEN_URL
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.net.URI
import java.net.URISyntaxException

object EnabledLinkResolver : AbstractLinkResolver(), StringToComponentConversion {
    private val linkRegex =
        Regex(
            "https?://[\\w-]{2,}(?:\\.[\\w-]+)*\\.?(?::\\d+)?(?:/.*)?",
            RegexOption.IGNORE_CASE,
        )

    override fun convert(input: String, escapedLiteralFactory: (String) -> MutableComponent): MutableComponent {
        val matches = linkRegex.findAll(input)
        if (matches.none())
            return escapedLiteralFactory(input)
        val component = Component.empty()

        for ((idx, match) in matches.withIndex()) {
            if (idx == 0) {
                val before = match.range.first
                if (before > 0) component.append(escapedLiteralFactory(input.substring(0, before)))
            }

            val uri: URI
            try {
                uri = URI(match.value)
            } catch (_: URISyntaxException) {
                component.append(escapedLiteralFactory(match.value))
                continue
            }

            component.append(
                Component.literal(match.value)
                    .withStyle(ChatFormatting.BLUE)
                    .withStyle { it.withClickEvent(ClickEvent(OPEN_URL, uri.toString())) }
            )
        }

        return component
    }

    override val delimitedConversion: DelimiterBasedStringComponentConversion = createDelimConversion(this)

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