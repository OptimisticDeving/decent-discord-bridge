package dev.optimistic.decentdiscordbridge.util

import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeMinecraftSpecial
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object StickerExtensions {
    fun Sticker.asComponent(): Component =
        Component.empty()
            .append(Component.literal("[Sticker: "))
            .append(Component.literal(this.name.escapeMinecraftSpecial()))
            .append(Component.literal("]"))
            .withStyle { it.withColor(ChatFormatting.LIGHT_PURPLE) }
}