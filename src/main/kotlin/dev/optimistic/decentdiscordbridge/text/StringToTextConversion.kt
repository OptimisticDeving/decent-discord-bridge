package dev.optimistic.decentdiscordbridge.text

import net.minecraft.text.MutableText

fun interface StringToTextConversion {
    fun convert(input: String, escapedLiteralFactory: (String) -> MutableText): MutableText

    object Literal : StringToTextConversion {
        override fun convert(input: String, escapedLiteralFactory: (String) -> MutableText): MutableText =
            escapedLiteralFactory(input)
    }
}