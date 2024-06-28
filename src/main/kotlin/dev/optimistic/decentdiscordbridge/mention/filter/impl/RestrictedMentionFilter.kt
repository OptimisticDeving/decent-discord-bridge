package dev.optimistic.decentdiscordbridge.mention.filter.impl

import dev.optimistic.decentdiscordbridge.mention.filter.MentionFilter

class RestrictedMentionFilter(private val ids: Set<Long>) : MentionFilter {
    override fun isAllowed(id: Long): Boolean = this.ids.contains(id)
}