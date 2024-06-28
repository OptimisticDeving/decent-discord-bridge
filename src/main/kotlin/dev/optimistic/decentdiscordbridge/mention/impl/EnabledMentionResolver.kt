package dev.optimistic.decentdiscordbridge.mention.impl

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge
import dev.optimistic.decentdiscordbridge.mention.AbstractMentionResolver
import dev.optimistic.decentdiscordbridge.mention.filter.MentionFilter
import net.dv8tion.jda.api.entities.Guild

class EnabledMentionResolver(private val guild: Guild, private val mentionFilter: MentionFilter) :
    AbstractMentionResolver() {
    private fun resolveMention(name: String): String? {
        val lowercaseName = name.lowercase()

        return this.guild.members.find {
            mentionFilter.isAllowed(it.idLong) &&
                    (it.effectiveName.lowercase() == lowercaseName
                            || it.user.globalName?.lowercase() == lowercaseName
                            || it.user.name == lowercaseName)
        }?.asMention
    }

    override fun resolveMentionsInString(input: String) = input.replace(mentionRegex) {
        val originalMatch = it.value
        val mentionable = it.groups[1] ?: it.groups[2]
        DecentDiscordBridge.expectBridge().logger.info("$mentionable")
        return@replace this.resolveMention(mentionable!!.value) ?: originalMatch
    }

    private companion object {
        private val mentionRegex = Regex("<@([^>]+)>|@(\\w+)")
    }
}