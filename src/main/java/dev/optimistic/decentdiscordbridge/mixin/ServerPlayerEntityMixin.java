package dev.optimistic.decentdiscordbridge.mixin;

import com.mojang.authlib.GameProfile;
import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements CachedAvatarUrlDuck {
    @Unique
    private String avatarUrl;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
        this.avatarUrl = DecentDiscordBridge.Companion.expectBridge()
                .getUrlGenerator()
                .generateAvatarUrl(profile);
    }

    @Override
    public String getAvatarUrl() {
        return this.avatarUrl;
    }
}
