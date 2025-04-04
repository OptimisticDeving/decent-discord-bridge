package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import dev.optimistic.decentdiscordbridge.bridge.AbstractBridge;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "stopServer", at = @At("TAIL"))
    private void stopServer(CallbackInfo ci) {
        if (!DecentDiscordBridge.Companion.isBridgeInitialized()) return;
        final AbstractBridge bridge = DecentDiscordBridge.Companion.getBridge();
        bridge.onShutdown();
    }
}
