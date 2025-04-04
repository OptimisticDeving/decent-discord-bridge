package dev.optimistic.decentdiscordbridge.component

import net.minecraft.network.chat.MutableComponent

fun interface StringToComponentConversion {
    fun convert(input: String, escapedLiteralFactory: (String) -> MutableComponent): MutableComponent

    object Literal : StringToComponentConversion {
        override fun convert(input: String, escapedLiteralFactory: (String) -> MutableComponent): MutableComponent =
            escapedLiteralFactory(input)
    }
}