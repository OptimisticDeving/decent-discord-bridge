package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements CachedAvatarUrlDuck {
    @Unique
    private @Nullable String avatarUrl;

    public ServerPlayerMixin() {
        super(null, null, 0, null);
    }

    @Override
    public void calculateAvatarUrl() {
        if (this.avatarUrl != null)
            return;

        this.avatarUrl = DecentDiscordBridge.Companion.getBridge()
                .generateAvatarUrl(this.getGameProfile());
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

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void restoreFrom(ServerPlayer serverPlayer, boolean bl, CallbackInfo ci) {
        this.avatarUrl = ((CachedAvatarUrlDuck) serverPlayer).getAvatarUrl();
    }
}
