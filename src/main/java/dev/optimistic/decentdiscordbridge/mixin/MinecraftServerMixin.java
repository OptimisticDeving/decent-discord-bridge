package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "shutdown", at = @At("TAIL"))
    private void shutdown(CallbackInfo ci) {
        final DecentDiscordBridge bridge = DecentDiscordBridge.Companion.getBridge();
        if (bridge == null)
            return;

        bridge.shutdown();
    }
}
