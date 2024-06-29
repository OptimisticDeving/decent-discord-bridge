package dev.optimistic.decentdiscordbridge.mixin;

import com.mojang.authlib.GameProfile;
import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameProfile.class)
public abstract class GameProfileMixin implements CachedAvatarUrlDuck {
    @Unique
    private @Nullable String avatarUrl;

    @Override
    public void calculateAvatarUrl() {
        if (this.avatarUrl != null)
            return;

        this.avatarUrl = DecentDiscordBridge.Companion.getBridge()
                .generateAvatarUrl((GameProfile) (Object) this);
    }

    @Override
    public String getAvatarUrl() {
        if (this.avatarUrl == null) {
            DecentDiscordBridge.Companion.getLogger()
                    .error("avatar url SHOULD be initialized before this call, but it wasn't, so initializing now!!!");
            this.calculateAvatarUrl();
        }

        return this.avatarUrl;
    }
}
