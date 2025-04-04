package dev.optimistic.decentdiscordbridge.message

import net.dv8tion.jda.api.entities.Message
import net.minecraft.network.chat.Component

interface DiscordMessageToMinecraftRenderer {
    fun render(message: Message, content: String): Component
    fun isRenderedAndRemoveIfSo(component: Component): Boolean
}