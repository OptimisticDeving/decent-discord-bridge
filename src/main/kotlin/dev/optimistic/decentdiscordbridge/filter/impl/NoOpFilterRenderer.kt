package dev.optimistic.decentdiscordbridge.filter.impl

import dev.optimistic.decentdiscordbridge.filter.FilterRenderer
import net.minecraft.network.chat.PlayerChatMessage

object NoOpFilterRenderer : FilterRenderer {
    override fun renderFilter(playerMessage: PlayerChatMessage): String? = playerMessage.signedBody.content
}