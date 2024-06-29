package dev.optimistic.decentdiscordbridge.text

import net.minecraft.text.MutableText
import net.minecraft.text.Text

fun interface StringToTextConversion {
    fun convert(input: String): MutableText

    object Literal : StringToTextConversion {
        override fun convert(input: String): MutableText = Text.literal(input)
    }
}