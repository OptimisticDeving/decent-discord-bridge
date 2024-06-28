package dev.optimistic.decentdiscordbridge.mention.impl

import dev.optimistic.decentdiscordbridge.mention.AbstractMentionResolver

object DisabledMentionResolver :
    AbstractMentionResolver() {
    override fun resolveMentionsInString(input: String) = input
}