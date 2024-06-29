package dev.optimistic.decentdiscordbridge.message

import net.dv8tion.jda.api.entities.Message
import net.minecraft.text.Text

interface DiscordMessageToMinecraftRenderer {
    fun render(message: Message, content: String): Text
    fun isRenderedAndRemoveIfSo(component: Text): Boolean
}