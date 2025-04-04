package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {
    @Inject(method = "initServer", at = @At("TAIL"))
    private void initServer(CallbackInfoReturnable<Boolean> cir) {
        // this means the server failed to start
        if (!cir.getReturnValue())
            return;

        DecentDiscordBridge.Companion.getBridge()
                .onStartup();
    }
}