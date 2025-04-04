package dev.optimistic.decentdiscordbridge.bridge

import com.mojang.authlib.GameProfile
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer

abstract class AbstractBridge {
    abstract val messageRenderer: DiscordMessageToMinecraftRenderer

    abstract fun generateAvatarUrl(profile: GameProfile): String
    abstract fun sendSystem(message: Component)
    abstract fun sendPlayer(player: ServerPlayer, message: PlayerChatMessage)
    abstract fun onStartup()
    abstract fun onShutdown()
}