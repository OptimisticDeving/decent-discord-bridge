package dev.optimistic.decentdiscordbridge.avatars.impl

import com.mojang.authlib.GameProfile
import dev.optimistic.decentdiscordbridge.avatars.AbstractAvatarUrlGenerator

class UndashedUuidArgumentUrlGenerator(template: String) : AbstractAvatarUrlGenerator(template) {
    override fun generateAvatarUrl(profile: GameProfile): String =
        String.format(
            template, profile.id
                .toString()
                .replace("-", "")
        )
}