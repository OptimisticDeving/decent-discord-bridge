package dev.optimistic.decentdiscordbridge.mention.filter

fun interface MentionFilter {
    fun isAllowed(id: Long): Boolean
}