package dev.optimistic.decentdiscordbridge.message

import dev.optimistic.decentdiscordbridge.util.AttachmentExtensions.asText
import dev.optimistic.decentdiscordbridge.util.MessageExtensions.hasContent
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeMinecraftSpecial
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.text.Texts
import net.minecraft.util.Formatting

object DiscordMessageToMinecraftRenderer {
    private val renderedMessages = arrayListOf<Text>()

    private fun renderContent(message: Message, attachmentSeparator: Text): Text {
        val content = message.contentDisplay.escapeMinecraftSpecial();

        return Text.translatable(
            "chat.type.text",
            Text.literal(message.member?.effectiveName ?: message.author.effectiveName)
                .formatted(Formatting.ITALIC)
                // TODO: Figure out why replies break this
                .styled { it.withColor(message.member?.colorRaw ?: Role.DEFAULT_COLOR_RAW) },
            Text.literal(content).run {
                if (message.attachments.isEmpty())
                    return@run this

                if (content.isNotEmpty())
                    this.append(attachmentSeparator)
                this.append(
                    Texts.join(
                        message.attachments.map { it.asText() },
                        attachmentSeparator
                    )
                )
            }
        )
    }


    fun render(message: Message): Text {
        val reply = message.referencedMessage
        val component = Text.empty()

        if (reply !== null && reply.hasContent()) {
            component.append(
                Text.empty()
                    .append("⊢ ")
                    .append(renderContent(reply, ScreenTexts.SPACE))
                    .append(ScreenTexts.LINE_BREAK)
            )
        }

        component.append(renderContent(message, ScreenTexts.LINE_BREAK))

        synchronized(renderedMessages) {
            renderedMessages.add(component)
        }

        return component
    }

    fun isRenderedAndRemoveIfSo(component: Text) = synchronized(renderedMessages) { renderedMessages.remove(component) }
}