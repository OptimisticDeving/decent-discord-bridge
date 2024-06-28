package dev.optimistic.decentdiscordbridge.mention

abstract class AbstractMentionResolver {
    abstract fun resolveMentionsInString(input: String): String
}