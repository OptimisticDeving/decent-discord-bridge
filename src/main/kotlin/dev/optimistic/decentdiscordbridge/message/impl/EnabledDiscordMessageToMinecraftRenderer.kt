package dev.optimistic.decentdiscordbridge.message.impl

import dev.optimistic.decentdiscordbridge.link.AbstractLinkResolver
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import dev.optimistic.decentdiscordbridge.util.AttachmentExtensions.asComponent
import dev.optimistic.decentdiscordbridge.util.MessageExtensions.hasContent
import dev.optimistic.decentdiscordbridge.util.StickerExtensions.asComponent
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeMinecraftSpecial
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.HoverEvent

class EnabledDiscordMessageToMinecraftRenderer(private val linkResolver: AbstractLinkResolver) :
    DiscordMessageToMinecraftRenderer {
    private val renderedMessages = arrayListOf<Component>()
    private val suggestMention = Component.literal("Click to copy a mention for this user to your clipboard.")

    private fun renderContent(
        message: Message,
        messageComponentSeparator: Component,
        memberOverride: Member? = null,
        contentOverride: String? = null
    ): Component {
        val content = (contentOverride ?: message.contentDisplay).escapeMinecraftSpecial();
        val member = memberOverride ?: message.member

        return Component.translatable(
            "chat.type.text",
            Component.literal(
                (member?.effectiveName ?: message.author.effectiveName).escapeMinecraftSpecial()
            )
                .withStyle(ChatFormatting.ITALIC)
                .withStyle {
                    it.withColor(member?.colors?.primaryRaw ?: Role.DEFAULT_COLOR_RAW)
                        .withHoverEvent(HoverEvent.ShowText(suggestMention))
                        .withClickEvent(ClickEvent.CopyToClipboard(message.author.asMention))
                },
            linkResolver.resolveLinks(content).apply {
                val hasAttachments = message.attachments.isNotEmpty()

                if (hasAttachments) {
                    this.append(messageComponentSeparator)

                    this.append(
                        ComponentUtils.formatList(
                            message.attachments.map { it.asComponent() },
                            messageComponentSeparator
                        )
                    )
                }

                if (message.stickers.isNotEmpty()) {
                    if (hasAttachments) this.append(messageComponentSeparator)

                    this.append(
                        ComponentUtils.formatList(
                            message.stickers.map { it.asComponent() },
                            messageComponentSeparator
                        )
                    )
                }
            }
        )
    }


    override fun render(message: Message, content: String): Component {
        val reply = message.referencedMessage
        val component = Component.empty()

        if (reply !== null && reply.hasContent()) {
            component.append(
                Component.empty()
                    .append("⊢ ")
                    .append(
                        renderContent(
                            message = reply,
                            messageComponentSeparator = CommonComponents.SPACE,
                            memberOverride = reply.guild.retrieveMember(reply.author)
                                .onErrorMap { null }.complete()
                        )
                    )
                    .append(CommonComponents.NEW_LINE)
            )
        }

        component.append(
            renderContent(
                message,
                messageComponentSeparator = CommonComponents.NEW_LINE,
                contentOverride = content
            )
        )

        synchronized(renderedMessages) {
            renderedMessages.add(component)
        }

        return component
    }

    override fun isRenderedAndRemoveIfSo(component: Component) =
        synchronized(renderedMessages) { renderedMessages.remove(component) }
}