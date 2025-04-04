package dev.optimistic.decentdiscordbridge.component

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.MutableComponent

class DelimiterBasedStringComponentConversion(private val delim: String, private val conversion: StringToComponentConversion) :
    StringToComponentConversion {
    private val delimComponent = Component.literal(delim)

    override fun convert(input: String, escapedLiteralFactory: (String) -> MutableComponent): MutableComponent {
        val split = input.split(delim)
        return ComponentUtils.formatList(split.map { conversion.convert(it, escapedLiteralFactory) }, delimComponent)
            .copy()
    }
}