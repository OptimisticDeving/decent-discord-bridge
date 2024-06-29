package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements CachedAvatarUrlDuck {
    @Override
    public String getAvatarUrl() {
        return ((CachedAvatarUrlDuck)
                (((ServerPlayerEntity) (Object) this)
                        .getGameProfile()))
                .getAvatarUrl();
    }
}
