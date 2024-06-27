package dev.optimistic.decentdiscordbridge.util

import net.dv8tion.jda.api.entities.Message
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object AttachmentExtensions {
    private val hoverEventText = Text.literal("Click to open URL.")

    fun Message.Attachment.asText(): Text = Text.literal("[Attachment]")
        .formatted(Formatting.YELLOW)
        .styled {
            it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, this.proxyUrl))
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventText))
        }
}