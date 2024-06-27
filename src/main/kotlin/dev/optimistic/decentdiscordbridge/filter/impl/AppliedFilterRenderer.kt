package dev.optimistic.decentdiscordbridge.filter.impl

import dev.optimistic.decentdiscordbridge.filter.FilterRenderer
import net.minecraft.network.message.SignedMessage

object AppliedFilterRenderer : FilterRenderer {
    override fun renderFilter(signedMessage: SignedMessage): String? =
        signedMessage.filterMask.filter(signedMessage.signedBody.content)
}