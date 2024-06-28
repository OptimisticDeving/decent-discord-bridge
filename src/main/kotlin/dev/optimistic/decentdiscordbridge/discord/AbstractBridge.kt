package dev.optimistic.decentdiscordbridge.discord

import com.mojang.authlib.GameProfile
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

interface AbstractBridge {
    fun generateAvatarUrl(profile: GameProfile): String

    fun sendSystem(message: Text)
    fun sendPlayer(player: ServerPlayerEntity, message: SignedMessage)
    fun shutdown()
}