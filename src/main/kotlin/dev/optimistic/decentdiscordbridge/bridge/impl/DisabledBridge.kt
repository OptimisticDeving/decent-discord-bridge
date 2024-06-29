package dev.optimistic.decentdiscordbridge.bridge.impl

import com.mojang.authlib.GameProfile
import dev.optimistic.decentdiscordbridge.bridge.AbstractBridge
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import dev.optimistic.decentdiscordbridge.message.impl.DisabledDiscordMessageToMinecraftRenderer
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object DisabledBridge : AbstractBridge() {
    override val messageRenderer: DiscordMessageToMinecraftRenderer = DisabledDiscordMessageToMinecraftRenderer

    override fun generateAvatarUrl(profile: GameProfile): String = ""
    override fun sendSystem(message: Text) {}
    override fun sendPlayer(player: ServerPlayerEntity, message: SignedMessage) {}
    override fun onStartup() {}
    override fun onShutdown() {}
}