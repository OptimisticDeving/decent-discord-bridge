package dev.optimistic.decentdiscordbridge.message.impl

import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import net.dv8tion.jda.api.entities.Message
import net.minecraft.network.chat.Component

object DisabledDiscordMessageToMinecraftRenderer : DiscordMessageToMinecraftRenderer {
    override fun render(message: Message, content: String) =
        throw AssertionError("method should not be called with a disabled bridge")

    override fun isRenderedAndRemoveIfSo(component: Component) = false
}