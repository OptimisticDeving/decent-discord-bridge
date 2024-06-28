package dev.optimistic.decentdiscordbridge

import club.minnced.discord.webhook.send.AllowedMentions
import dev.optimistic.decentdiscordbridge.mention.filter.impl.PassthroughMentionFilter
import dev.optimistic.decentdiscordbridge.mention.filter.impl.RestrictedMentionFilter
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
) {
    fun canLoad(): Boolean {
        return this.token.isNotBlank() && this.channelId != -1L
                && this.webhookToken.isNotBlank() && this.webhookId != -1L
    }

    @ConfigSerializable
    data class AvatarConfiguration(
        val templateType: TemplateType = TemplateType.UNDASHED_UUID,
        val template: String = "https://minotar.net/avatar/%s"
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
            fun apply(
                allowedMentions: AllowedMentions,
                setParse: (Boolean) -> Unit,
                setSnowflakes: (Array<String>) -> Unit
            ): AllowedMentions {
                if (!allowed) {
                    setParse(false)
                    return allowedMentions
                }

                if (only.isEmpty()) {
                    setParse(true)
                } else {
                    setParse(false)
                    setSnowflakes(only.map { it.toString() }.toTypedArray())
                }

                return allowedMentions
            }

            fun asMentionFilter() = if (only.isEmpty()) {
                PassthroughMentionFilter
            } else {
                RestrictedMentionFilter(only)
            }
        }

        fun intoJda() = AllowedMentions()
            .withParseEveryone(this.everyone)
            .run { roles.apply(this, this::withParseRoles, this::withRoles) }
            .run { users.apply(this, this::withParseUsers, this::withUsers) }
    }
}