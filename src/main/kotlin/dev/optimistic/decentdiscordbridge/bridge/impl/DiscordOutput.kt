package dev.optimistic.decentdiscordbridge.bridge.impl

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.FileUpload
import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.permissions.LevelBasedPermissionSet
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

const val MAX_LEN = 2000 - (3 * 6)

class DiscordOutput(val message: Message, val user: User) : CommandSource {
    private val feedback = StringBuilder()
    var used = false

    override fun sendSystemMessage(message: Component) {
        if (used) return
        val asString = message.string.trim().replace("§[a-f0-9k-or]", "")
        if (asString.isEmpty()) return
        if (!feedback.isEmpty()) this.feedback.append('\n')
        this.feedback.append(asString)
    }

    override fun acceptsSuccess() = true
    override fun acceptsFailure() = true
    override fun shouldInformAdmins() = true
    override fun alwaysAccepts() = true

    fun createCommandSourceStack(server: MinecraftServer): CommandSourceStack {
        val level: ServerLevel = server.overworld()

        return CommandSourceStack(
            this,
            Vec3.atLowerCornerOf(level.respawnData.pos()),
            Vec2.ZERO,
            level,
            LevelBasedPermissionSet.NO_PERMISSIONS,
            user.effectiveName,
            Component.literal(user.effectiveName),
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