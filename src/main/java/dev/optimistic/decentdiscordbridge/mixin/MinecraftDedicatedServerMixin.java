package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {
    @Inject(method = "setupServer", at = @At("TAIL"))
    private void setupServer(CallbackInfoReturnable<Boolean> cir) {
        // this means the server failed to start
        if (!cir.getReturnValue())
            return;

        DecentDiscordBridge.Companion.getBridge()
                .onStartup();
    }
}