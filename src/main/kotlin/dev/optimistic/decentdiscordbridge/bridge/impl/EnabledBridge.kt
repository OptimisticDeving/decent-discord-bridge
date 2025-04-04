package dev.optimistic.decentdiscordbridge.bridge.impl

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.receive.ReadonlyMessage
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
import dev.optimistic.decentdiscordbridge.bridge.AbstractBridge
import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck
import dev.optimistic.decentdiscordbridge.filter.FilterRenderer
import dev.optimistic.decentdiscordbridge.filter.impl.AppliedFilterRenderer
import dev.optimistic.decentdiscordbridge.filter.impl.NoOpFilterRenderer
import dev.optimistic.decentdiscordbridge.link.AbstractLinkResolver
import dev.optimistic.decentdiscordbridge.link.impl.DisabledLinkResolver
import dev.optimistic.decentdiscordbridge.link.impl.EnabledLinkResolver
import dev.optimistic.decentdiscordbridge.mention.AbstractMentionResolver
import dev.optimistic.decentdiscordbridge.mention.impl.DisabledMentionResolver
import dev.optimistic.decentdiscordbridge.mention.impl.EnabledMentionResolver
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import dev.optimistic.decentdiscordbridge.message.impl.EnabledDiscordMessageToMinecraftRenderer
import dev.optimistic.decentdiscordbridge.util.MessageExtensions.hasContent
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class EnabledBridge(
    private val playerManager: PlayerManager,
    config: Configuration,
    private val seenUsersPath: Path
) : AbstractBridge() {
    private val logger = LoggerFactory.getLogger("decent-discord-bridge")
    private val discordRegex: Regex = Regex("([Dd])[Ii]([Ss][Cc][Oo][Rr][Dd])")

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
    private val broadcastLifecycleEvents: Boolean
    private val linkResolver: AbstractLinkResolver

    override val messageRenderer: DiscordMessageToMinecraftRenderer

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

        linkResolver = if (config.resolveLinks) {
            EnabledLinkResolver
        } else {
            DisabledLinkResolver
        }

        messageRenderer = EnabledDiscordMessageToMinecraftRenderer(linkResolver)
        broadcastLifecycleEvents = config.broadcastLifecycleEvents

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
            val rawContent = message.contentRaw

            if (rawContent.startsWith("/")) {
                val server = playerManager.server
                server.submit {
                    val server = playerManager.server
                    val sender = DiscordOutput(message, it.author)
                    server.commandManager.executeWithPrefix(
                        sender.createCommandSource(server),
                        rawContent
                    )
                    sender.complete()
                }

                return@listener
            }

            val content = message.contentDisplay
            if (message.isWebhookMessage || !message.hasContent())
                return@listener

            playerManager.broadcast(messageRenderer.render(message, content), false)
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

    private fun sendSystemInternal(message: Text) = webhook.send(
        WebhookMessageBuilder()
            .setUsername("System")
            .setContent(linkResolver.escapeNotLinks(message.string))
            .setAllowedMentions(emptyMentions)
            .build()
    )


    override fun sendSystem(message: Text) {
        sendSystemInternal(message)
    }

    private fun sendPlayerInternal(
        player: ServerPlayerEntity,
        message: SignedMessage
    ): CompletableFuture<ReadonlyMessage> {
        val filtered = this.filter.renderFilter(message)?.trim()
        if (filtered === null)
            return completelyFiltered // don't send fully filtered messages over the discord bridge

        if (filtered.isEmpty())
            return emptyMessage

        return webhook.send(
            WebhookMessageBuilder()
                .setUsername(player.gameProfile.name.replace(discordRegex, "$1!$2"))
                .setAvatarUrl((player as CachedAvatarUrlDuck).getAvatarUrl())
                .setContent(
                    linkResolver.escapeNotLinks(mentionResolver.resolveMentionsInString(filtered))
                )
                .setAllowedMentions(allowedMentions)
                .build()
        )
    }

    override fun sendPlayer(player: ServerPlayerEntity, message: SignedMessage) {
        sendPlayerInternal(player, message)
    }

    override fun onStartup() {
        if (!broadcastLifecycleEvents)
            return

        sendSystem(Text.literal("Server has started"))
    }

    override fun onShutdown() {
        jda.shutdown()
        jda.awaitShutdown(30, TimeUnit.SECONDS)
        if (broadcastLifecycleEvents) {
            try {
                sendSystemInternal(Text.literal("Server has stopped")).get(15, TimeUnit.SECONDS)
            } catch (ex: Throwable) {
                logger.warn("Failed to send shutdown message", ex)
            }
        }

        webhook.close()

        val writer = Files.newBufferedWriter(seenUsersPath)
        synchronized(seenUsers) {
            gson.toJson(seenUsers, writer)
        }
        writer.flush()
    }

    private companion object {
        private val emptyMentions = AllowedMentions.none()
        private val gson = Gson()

        private val completelyFiltered =
            CompletableFuture.failedFuture<ReadonlyMessage>(IllegalArgumentException("completely filtered message"))
        private val emptyMessage =
            CompletableFuture.failedFuture<ReadonlyMessage>(IllegalArgumentException("empty message"))
    }
}