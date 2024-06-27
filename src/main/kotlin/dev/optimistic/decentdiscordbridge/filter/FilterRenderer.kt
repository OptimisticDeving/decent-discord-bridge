package dev.optimistic.decentdiscordbridge.filter

import net.minecraft.network.message.SignedMessage

interface FilterRenderer {
    fun renderFilter(signedMessage: SignedMessage): String?
}