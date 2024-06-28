package dev.optimistic.decentdiscordbridge.message

import dev.optimistic.decentdiscordbridge.util.AttachmentExtensions.asText
import dev.optimistic.decentdiscordbridge.util.MessageExtensions.hasContent
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeMinecraftSpecial
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.text.Texts
import net.minecraft.util.Formatting

object DiscordMessageToMinecraftRenderer {
    private val renderedMessages = arrayListOf<Text>()

    private fun renderContent(message: Message, attachmentSeparator: Text, memberOverride: Member? = null): Text {
        val content = message.contentDisplay.escapeMinecraftSpecial();
        val member = memberOverride ?: message.member

        return Text.translatable(
            "chat.type.text",
            Text.literal((member?.effectiveName ?: message.author.effectiveName).escapeMinecraftSpecial())
                .formatted(Formatting.ITALIC)
                // TODO: Figure out why replies break this
                .styled { it.withColor(member?.colorRaw ?: Role.DEFAULT_COLOR_RAW) },
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
                    .append(
                        renderContent(
                            message = reply,
                            attachmentSeparator = ScreenTexts.SPACE,
                            memberOverride = reply.guild.retrieveMember(reply.author).complete(true)
                        )
                    )
                    .append(ScreenTexts.LINE_BREAK)
            )
        }

        component.append(renderContent(message, attachmentSeparator = ScreenTexts.LINE_BREAK))

        synchronized(renderedMessages) {
            renderedMessages.add(component)
        }

        return component
    }

    fun isRenderedAndRemoveIfSo(component: Text) = synchronized(renderedMessages) { renderedMessages.remove(component) }
}