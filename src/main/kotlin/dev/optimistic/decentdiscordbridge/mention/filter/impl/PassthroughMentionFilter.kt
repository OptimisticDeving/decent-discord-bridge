package dev.optimistic.decentdiscordbridge.mention.filter.impl

import dev.optimistic.decentdiscordbridge.mention.filter.MentionFilter

object PassthroughMentionFilter : MentionFilter {
    override fun isAllowed(id: Long): Boolean = true
}