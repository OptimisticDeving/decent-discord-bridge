package dev.optimistic.decentdiscordbridge.discord.impl

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mojang.authlib.GameProfile
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import dev.optimistic.decentdiscordbridge.Configuration
import dev.optimistic.decentdiscordbridge.Configuration.AvatarConfiguration.TemplateType.*
import dev.optimistic.decentdiscordbridge.avatars.AbstractAvatarUrlGenerator
import dev.optimistic.decentdiscordbridge.avatars.impl.DashedUuidArgumentUrlGenerator
import dev.optimistic.decentdiscordbridge.avatars.impl.UndashedUuidArgumentUrlGenerator
import dev.optimistic.decentdiscordbridge.avatars.impl.UsernameArgumentUrlGenerator
import dev.optimistic.decentdiscordbridge.discord.AbstractBridge
import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck
import dev.optimistic.decentdiscordbridge.filter.FilterRenderer
import dev.optimistic.decentdiscordbridge.filter.impl.AppliedFilterRenderer
import dev.optimistic.decentdiscordbridge.filter.impl.NoOpFilterRenderer
import dev.optimistic.decentdiscordbridge.mention.AbstractMentionResolver
import dev.optimistic.decentdiscordbridge.mention.impl.DisabledMentionResolver
import dev.optimistic.decentdiscordbridge.mention.impl.EnabledMentionResolver
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeDiscordSpecial
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

class EnabledBridge(
    private val playerManager: PlayerManager,
    config: Configuration,
    private val seenUsersPath: Path
) : AbstractBridge {
    private val logger = LoggerFactory.getLogger("decent-discord-bridge")

    private val allowedMentions: AllowedMentions = config.mentions.intoJda()
    private val filter: FilterRenderer = when (config.applyFilterToWebhookMessages) {
        true -> AppliedFilterRenderer
        false -> NoOpFilterRenderer
    }
    private val urlGenerator: AbstractAvatarUrlGenerator = when (config.playerAvatars.templateType) {
        UNDASHED_UUID -> UndashedUuidArgumentUrlGenerator(config.playerAvatars.template)
        DASHED_UUID -> DashedUuidArgumentUrlGenerator(config.playerAvatars.template)
        USERNAME -> UsernameArgumentUrlGenerator(config.playerAvatars.template)
    }

    private val webhook: JDAWebhookClient
    private val jda: JDA
    private val mentionResolver: AbstractMentionResolver
    private val seenUsers: MutableSet<Long> = if (Files.notExists(seenUsersPath))
        mutableSetOf()
    else
        gson.fromJson(Files.newBufferedReader(seenUsersPath), object : TypeToken<MutableSet<Long>>() {})

    init {
        logger.info("Building webhook...")
        webhook = WebhookClientBuilder(config.webhookId, config.webhookToken)
            .setDaemon(true) // prevents blocking shutdown
            .buildJDA()

        logger.info("Logging into Discord...")
        val (jda, guild) = startClient(token = config.token, channelId = config.channelId)
        this.jda = jda

        mentionResolver = if (config.mentions.users.allowed) {
            EnabledMentionResolver(guild, mentionFilter = config.mentions.users.asMentionFilter())
        } else {
            DisabledMentionResolver
        }

        loadCache(guild)
    }

    private fun startClient(token: String, channelId: Long): Pair<JDA, Guild> {
        val jda = light(
            token,
            enableCoroutines = true,
        ) {
            enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            setMemberCachePolicy(MemberCachePolicy.ALL)
        }

        jda.listener<MessageReceivedEvent> {
            if (it.author.isBot || it.author.isSystem)
                return@listener

            synchronized(seenUsers) {
                seenUsers.add(it.author.idLong)
                it.message.referencedMessage?.author?.idLong?.apply { seenUsers.add(this) }
            }

            if (it.channel.idLong != channelId)
                return@listener

            val message = it.message
            if (message.isWebhookMessage)
                return@listener

            playerManager.broadcast(DiscordMessageToMinecraftRenderer.render(message), false)
        }

        jda.awaitReady()
        return Pair(jda, jda.getGuildChannelById(channelId)!!.guild)
    }

    private fun loadCache(guild: Guild) {
        synchronized(seenUsers) {
            if (seenUsers.isNotEmpty()) {
                logger.info("Loading ${seenUsers.size} saved users into cache")
                val members =
                    RestAction.allOf(seenUsers.map {
                        guild.retrieveMember(UserSnowflake.fromId(it)).onErrorMap { null }
                    })
                        .complete()
                seenUsers.clear()
                seenUsers.addAll(members.filterNotNull().map { it.idLong })
                logger.info("Loaded saved users!")
            }
        }
    }

    override fun generateAvatarUrl(profile: GameProfile): String {
        return urlGenerator.generateAvatarUrl(profile)
    }

    override fun sendSystem(message: Text) {
        webhook.send(
            WebhookMessageBuilder()
                .setUsername("System")
                .setContent(message.string.escapeDiscordSpecial())
                .setAllowedMentions(emptyMentions)
                .build()
        )
    }

    override fun sendPlayer(player: ServerPlayerEntity, message: SignedMessage) {
        val filtered = this.filter.renderFilter(message)?.trim()
        if (filtered === null)
            return // don't send fully filtered messages over the discord bridge

        if (filtered.isEmpty())
            return

        webhook.send(
            WebhookMessageBuilder()
                .setUsername(player.gameProfile.name)
                .setAvatarUrl((player as CachedAvatarUrlDuck).getAvatarUrl())
                .setContent(
                    mentionResolver.resolveMentionsInString(filtered)
                        .escapeDiscordSpecial()
                )
                .setAllowedMentions(allowedMentions)
                .build()
        )
    }

    override fun shutdown() {
        webhook.close()
        jda.shutdownNow()

        val writer = Files.newBufferedWriter(seenUsersPath)
        synchronized(seenUsers) {
            gson.toJson(seenUsers, writer)
        }
        writer.flush()
    }

    companion object {
        private val emptyMentions = AllowedMentions.none()
        private val gson = Gson()
    }
}