package dev.optimistic.decentdiscordbridge.text

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.Texts

class DelimiterBasedStringTextConversion(private val delim: String, private val conversion: StringToTextConversion) :
    StringToTextConversion {
    private val delimComponent = Text.literal(delim)

    override fun convert(input: String): MutableText {
        val split = input.split(delim)
        return Texts.join(split.map { conversion.convert(it) }, delimComponent).copy()
    }
}