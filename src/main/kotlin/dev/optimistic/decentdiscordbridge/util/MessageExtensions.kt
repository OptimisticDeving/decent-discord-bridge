package dev.optimistic.decentdiscordbridge.util

import net.dv8tion.jda.api.entities.Message

object MessageExtensions {
    fun Message.hasContent() = (this.attachments.isNotEmpty() || this.contentDisplay.isNotEmpty())
}