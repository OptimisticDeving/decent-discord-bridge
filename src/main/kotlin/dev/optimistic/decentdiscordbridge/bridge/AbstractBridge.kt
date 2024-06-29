package dev.optimistic.decentdiscordbridge.bridge

import com.mojang.authlib.GameProfile
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

abstract class AbstractBridge {
    abstract val messageRenderer: DiscordMessageToMinecraftRenderer

    abstract fun generateAvatarUrl(profile: GameProfile): String
    abstract fun sendSystem(message: Text)
    abstract fun sendPlayer(player: ServerPlayerEntity, message: SignedMessage)
    abstract fun onStartup()
    abstract fun onShutdown()
}