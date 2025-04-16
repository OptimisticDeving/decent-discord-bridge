package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLifecycleHooks.class)
public abstract class ServerLifecycleHooksMixin {
    @Inject(method = "handleServerStarted", at = @At("TAIL"))
    private static void handleServerStarted(MinecraftServer server, CallbackInfo ci) {
        DecentDiscordBridge.Companion.getBridge()
                .onStartup();
    }
}