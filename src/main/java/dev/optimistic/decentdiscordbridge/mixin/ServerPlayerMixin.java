package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements CachedAvatarUrlDuck {
    @Override
    public String getAvatarUrl() {
        return ((CachedAvatarUrlDuck)
                (((ServerPlayer) (Object) this)
                        .getGameProfile()))
                .getAvatarUrl();
    }
}
