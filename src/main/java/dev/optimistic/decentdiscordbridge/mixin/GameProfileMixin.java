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
            /*
             this might technically be possible with mods that create fake players *improperly*,
             as we mixin to PlayerManager's createPlayer to call the init func on the player's GameProfile,
             so mods that don't call that function could cause the field to never be initialized, thus null

             although JDA does allow for null avatar urls, we'd rather users not get confused between player
             chat and system chat, as a null avatar url (i.e. the webhook's configured avatar)
             is what is used by system chat messages
             */
            DecentDiscordBridge.Companion.getLogger()
                    .error("avatar url SHOULD be initialized before this call, but it wasn't, so initializing now!!!");
            this.calculateAvatarUrl();
        }

        return this.avatarUrl;
    }
}
