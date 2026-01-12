package dev.optimistic.decentdiscordbridge

import dev.optimistic.decentdiscordbridge.mention.filter.impl.PassthroughMentionFilter
import dev.optimistic.decentdiscordbridge.mention.filter.impl.RestrictedMentionFilter
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class Configuration(
    val token: String = "",
    val channelId: Long = -1,
    val webhookId: Long = -1,
    val webhookToken: String = "",
    val playerAvatars: AvatarConfiguration = AvatarConfiguration(),
    val applyFilterToWebhookMessages: Boolean = false,
    val mentions: MentionConfiguration = MentionConfiguration(),
    val resolveLinks: Boolean = true,
    val broadcastLifecycleEvents: Boolean = false,
) {
    fun shouldLoad(): Boolean {
        return this.token.isNotBlank() && this.channelId != -1L
                && this.webhookToken.isNotBlank() && this.webhookId != -1L
    }

    @ConfigSerializable
    data class AvatarConfiguration(
        val templateType: TemplateType = TemplateType.UNDASHED_UUID,
        val template: String = "https://minotar.net/helm/%s"
    ) {
        enum class TemplateType {
            UNDASHED_UUID,
            DASHED_UUID,
            USERNAME
        }
    }

    @ConfigSerializable
    data class MentionConfiguration(
        val everyone: Boolean = false,
        val roles: SnowflakeMentions = SnowflakeMentions(),
        val users: SnowflakeMentions = SnowflakeMentions(allowed = true)
    ) {
        @ConfigSerializable
        data class SnowflakeMentions(val allowed: Boolean = false, val only: Set<Long> = emptySet()) {
            val snowflakes = only.map { it.toString() }.toTypedArray()

            fun apply(
                builder: MessageCreateBuilder,
                setSnowflakes: (Array<String>) -> Unit
            ): MessageCreateBuilder {
                if (!allowed || only.isEmpty()) return builder
                setSnowflakes(snowflakes)

                return builder
            }

            fun asMentionFilter() = if (only.isEmpty()) {
                PassthroughMentionFilter
            } else {
                RestrictedMentionFilter(only)
            }
        }

        private val allowedMentions = buildList {
            val config = this@MentionConfiguration

            if (config.everyone) add(Message.MentionType.EVERYONE)
            if (config.roles.allowed) add(Message.MentionType.ROLE)
            if (config.users.allowed) add(Message.MentionType.USER)
        }

        fun apply(builder: MessageCreateBuilder) = builder
            .setAllowedMentions(allowedMentions)
            .run { roles.apply(this, this::mentionRoles) }
            .run { users.apply(this, this::mentionUsers) }
    }
}
