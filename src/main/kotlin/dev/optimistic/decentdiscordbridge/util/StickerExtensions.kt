package dev.optimistic.decentdiscordbridge.util

import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeMinecraftSpecial
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object StickerExtensions {
    fun Sticker.asText(): Text =
        Text.empty()
            .append(Text.literal("[Sticker: "))
            .append(Text.literal(this.name.escapeMinecraftSpecial()))
            .append(Text.literal("]"))
            .styled { it.withColor(Formatting.LIGHT_PURPLE) }
}