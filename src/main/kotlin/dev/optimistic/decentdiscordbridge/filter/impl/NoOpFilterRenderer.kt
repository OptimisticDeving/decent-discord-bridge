package dev.optimistic.decentdiscordbridge.filter.impl

import dev.optimistic.decentdiscordbridge.filter.FilterRenderer
import net.minecraft.network.message.SignedMessage

object NoOpFilterRenderer : FilterRenderer {
    override fun renderFilter(signedMessage: SignedMessage): String? = signedMessage.signedBody.content
}