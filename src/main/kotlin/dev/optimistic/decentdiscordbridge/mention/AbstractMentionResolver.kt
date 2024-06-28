package dev.optimistic.decentdiscordbridge.mention

interface AbstractMentionResolver {
    fun resolveMentionsInString(input: String): String
}