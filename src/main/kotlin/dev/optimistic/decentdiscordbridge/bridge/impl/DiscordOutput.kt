package dev.optimistic.decentdiscordbridge.bridge.impl

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.FileUpload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

const val MAX_LEN = 2000 - (3 * 6)

class DiscordOutput(val message: Message, val user: User) : CommandOutput {
    private val feedback = StringBuilder()
    var used = false

    override fun sendMessage(message: Text) {
        if (used) return
        val asString = message.string.trim().replace("§[a-f0-9k-or]", "")
        if (asString.isEmpty()) return
        if (!feedback.isEmpty()) this.feedback.append('\n')
        this.feedback.append(asString)
    }

    override fun shouldReceiveFeedback() = true
    override fun shouldTrackOutput() = true
    override fun shouldBroadcastConsoleToOps() = true
    override fun cannotBeSilenced() = true

    fun createCommandSource(server: MinecraftServer): ServerCommandSource {
        val world: ServerWorld = server.overworld

        return ServerCommandSource(
            this,
            Vec3d.of(world.getSpawnPos()),
            Vec2f.ZERO,
            world,
            0,
            user.effectiveName,
            Text.literal(user.effectiveName),
            server,
            null
        )
    }

    fun complete() {
        used = true

        val feedback = feedback.toString()
        if (feedback.isEmpty()) return
        if (feedback.length > MAX_LEN) {
            message.replyFiles(FileUpload.fromData(feedback.toByteArray(Charsets.UTF_8), "message.txt"))
        } else {
            message.reply("```${feedback.replace("`", "")}```")
        }.complete(true)
    }
}