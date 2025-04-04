package dev.optimistic.decentdiscordbridge.bridge.impl

import com.mojang.authlib.GameProfile
import dev.optimistic.decentdiscordbridge.bridge.AbstractBridge
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import dev.optimistic.decentdiscordbridge.message.impl.DisabledDiscordMessageToMinecraftRenderer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer

object DisabledBridge : AbstractBridge() {
    override val messageRenderer: DiscordMessageToMinecraftRenderer = DisabledDiscordMessageToMinecraftRenderer

    override fun generateAvatarUrl(profile: GameProfile): String = ""
    override fun sendSystem(message: Component) {}
    override fun sendPlayer(player: ServerPlayer, message: PlayerChatMessage) {}
    override fun onStartup() {}
    override fun onShutdown() {}
}