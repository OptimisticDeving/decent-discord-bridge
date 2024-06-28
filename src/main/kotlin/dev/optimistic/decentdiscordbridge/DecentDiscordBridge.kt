package dev.optimistic.decentdiscordbridge

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import dev.optimistic.decentdiscordbridge.Configuration.AvatarConfiguration.TemplateType.*
import dev.optimistic.decentdiscordbridge.avatars.AbstractAvatarUrlGenerator
import dev.optimistic.decentdiscordbridge.avatars.impl.DashedUuidArgumentUrlGenerator
import dev.optimistic.decentdiscordbridge.avatars.impl.UndashedUuidArgumentUrlGenerator
import dev.optimistic.decentdiscordbridge.avatars.impl.UsernameArgumentUrlGenerator
import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck
import dev.optimistic.decentdiscordbridge.filter.FilterRenderer
import dev.optimistic.decentdiscordbridge.filter.impl.AppliedFilterRenderer
import dev.optimistic.decentdiscordbridge.filter.impl.NoOpFilterRenderer
import dev.optimistic.decentdiscordbridge.mention.AbstractMentionResolver
import dev.optimistic.decentdiscordbridge.mention.impl.DisabledMentionResolver
import dev.optimistic.decentdiscordbridge.mention.impl.EnabledMentionResolver
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer
import dev.optimistic.decentdiscordbridge.util.StringExtensions.escapeDiscordSpecial
import me.lucko.configurate.toml.TOMLConfigurationLoader
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.nio.file.Files

class DecentDiscordBridge(private val playerManager: PlayerManager) {
    val logger = LoggerFactory.getLogger("decent-discord-bridge")
    private val filter: FilterRenderer
    val urlGenerator: AbstractAvatarUrlGenerator
    private val allowedMentions: AllowedMentions
    private val webhook: JDAWebhookClient
    private val jda: JDA
    private val mentionResolver: AbstractMentionResolver

    init {
        val loader = FabricLoader.getInstance()
        val configPath = loader.configDir.resolve("decent-discord-bridge").resolve("config.toml")
        if (Files.notExists(configPath)) {
            Files.createDirectories(configPath.parent)
        }

        val configLoader = TOMLConfigurationLoader.builder()
            .path(configPath)
            .defaultOptions { options ->
                options
                    .header("Configuration file for Decent Discord Bridge")
                    .serializers {
                        it.registerAnnotatedObjects(
                            objectMapperFactory()
                        )
                    }
            }
            .build()
        val rootNode = configLoader.load()
        val config = rootNode.get<Configuration>() ?: throw IllegalArgumentException("Failed to load config")
        rootNode.set(config)
        configLoader.save(rootNode) // toml config loader doesn't really do proper equal checks, so this would happen anyway if we compared states

        this.urlGenerator = when (config.playerAvatars.templateType) {
            UNDASHED_UUID -> UndashedUuidArgumentUrlGenerator(config.playerAvatars.template)
            DASHED_UUID -> DashedUuidArgumentUrlGenerator(config.playerAvatars.template)
            USERNAME -> UsernameArgumentUrlGenerator(config.playerAvatars.template)
        }

        this.filter = when (config.applyFilterToWebhookMessages) {
            true -> AppliedFilterRenderer
            false -> NoOpFilterRenderer
        }

        this.allowedMentions = config.mentions.intoJda()

        logger.info("Config loaded.")
        logger.info("Building webhook...")
        webhook = WebhookClientBuilder(config.webhookId, config.webhookToken)
            .setDaemon(true) // prevents blocking shutdown
            .buildJDA()
        logger.info("Webhook built!")
        logger.info("Logging into Discord...")
        val (jda, guild) = startClient(token = config.token, channelId = config.channelId)
        this.jda = jda

        mentionResolver = if (config.mentions.users.allowed) {
            EnabledMentionResolver(guild, mentionFilter = config.mentions.users.asMentionFilter())
        } else {
            DisabledMentionResolver
        }

        logger.info("Logged into Discord!")
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
            if (it.channel.idLong != channelId)
                return@listener

            if (it.author.isBot || it.author.isSystem)
                return@listener

            val message = it.message
            if (message.isWebhookMessage)
                return@listener

            playerManager.broadcast(DiscordMessageToMinecraftRenderer.render(message), false)
        }

        jda.awaitReady()
        return Pair(jda, jda.getGuildChannelById(channelId)!!.guild)
    }

    fun shutdown() {
        this.webhook.close()
        this.jda.shutdownNow()
    }

    fun sendSystem(message: Text) {
        webhook.send(
            WebhookMessageBuilder()
                .setUsername("System")
                .setContent(message.string.escapeDiscordSpecial())
                .setAllowedMentions(emptyMentions)
                .build()
        )
    }

    fun sendPlayer(player: ServerPlayerEntity, message: SignedMessage) {
        val filtered = this.filter.renderFilter(message)
        if (filtered === null)
            return // don't send fully filtered messages over the discord bridge

        webhook.send(
            WebhookMessageBuilder()
                .setUsername(player.gameProfile.name)
                .setAvatarUrl((player as CachedAvatarUrlDuck).getAvatarUrl())
                .setContent(
                    mentionResolver.resolveMentionsInString(filtered)
                        .escapeDiscordSpecial()
                )
                .setAllowedMentions(this.allowedMentions)
                .build()
        )
    }

    companion object {
        var bridge: DecentDiscordBridge? = null
        private val emptyMentions = AllowedMentions.none()

        fun expectBridge() = bridge!!
    }
}