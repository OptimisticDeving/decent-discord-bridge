package dev.optimistic.decentdiscordbridge.util

import net.dv8tion.jda.api.entities.Message
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import java.net.URI

object AttachmentExtensions {
    private val hoverEventText = Component.literal("Click to open URL.")

    fun Message.Attachment.asComponent(): Component = Component.literal("[Attachment]")
        .withStyle(ChatFormatting.YELLOW)
        .withStyle {
            it.withClickEvent(ClickEvent.OpenUrl(URI(this.proxyUrl)))
                .withHoverEvent(HoverEvent.ShowText(hoverEventText))
        }
}