package dev.optimistic.decentdiscordbridge.filter.impl

import dev.optimistic.decentdiscordbridge.filter.FilterRenderer
import net.minecraft.network.chat.PlayerChatMessage

object AppliedFilterRenderer : FilterRenderer {
    override fun renderFilter(playerMessage: PlayerChatMessage): String? =
        playerMessage.filterMask.apply(playerMessage.signedBody.content)
}