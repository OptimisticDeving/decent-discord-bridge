package dev.optimistic.decentdiscordbridge.avatars

import com.mojang.authlib.GameProfile

abstract class AbstractAvatarUrlGenerator {
    protected abstract val template: String

    abstract fun generateAvatarUrl(profile: GameProfile): String
}