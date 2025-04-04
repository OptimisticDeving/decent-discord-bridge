package dev.optimistic.decentdiscordbridge.filter

import net.minecraft.network.chat.PlayerChatMessage

interface FilterRenderer {
    fun renderFilter(playerMessage: PlayerChatMessage): String?
}