package dev.optimistic.decentdiscordbridge

import dev.optimistic.decentdiscordbridge.bridge.AbstractBridge
import dev.optimistic.decentdiscordbridge.bridge.impl.DisabledBridge
import dev.optimistic.decentdiscordbridge.bridge.impl.EnabledBridge
import me.lucko.configurate.toml.TOMLConfigurationLoader
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.PlayerManager
import org.slf4j.LoggerFactory
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.nio.file.Files

class DecentDiscordBridge(playerManager: PlayerManager) {
    private val logger = LoggerFactory.getLogger("decent-discord-bridge")

    init {
        val loader = FabricLoader.getInstance()
        val configSubpath = loader.configDir.resolve("decent-discord-bridge")
        val configPath = configSubpath.resolve("config.toml")
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

        val seenUsersPath = configSubpath.resolve("seen_users.json")

        logger.info("Config loaded.")
        if (config.canLoad()) {
            bridge = EnabledBridge(playerManager, config, seenUsersPath)
            logger.info("Bridge successfully loaded!")
        } else {
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            logger.error("! Token not set in the config. Discord bridge not loading. !")
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            bridge = DisabledBridge
        }
    }

    companion object {
        lateinit var bridge: AbstractBridge

        fun isBridgeInitialized() = ::bridge.isInitialized
    }
}