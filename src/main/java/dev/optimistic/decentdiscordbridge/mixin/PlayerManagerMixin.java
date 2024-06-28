package dev.optimistic.decentdiscordbridge.mixin;

import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import dev.optimistic.decentdiscordbridge.discord.AbstractBridge;
import dev.optimistic.decentdiscordbridge.message.DiscordMessageToMinecraftRenderer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void init(CallbackInfo ci) {
        new DecentDiscordBridge((PlayerManager) (Object) this);
    }

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("TAIL"))
    private void chatBroadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
        final AbstractBridge bridge = DecentDiscordBridge.Companion.getBridge();

        if (sender == null) {
            bridge.sendSystem(params.applyChatDecoration(message.getContent()));
        } else {
            bridge.sendPlayer(sender, message);
        }
    }

    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Z)V", at = @At("TAIL"))
    private void serverBroadcast(Text message, boolean overlay, CallbackInfo ci) {
        if (overlay)
            return; // again, who cares?

        if (DiscordMessageToMinecraftRenderer.INSTANCE.isRenderedAndRemoveIfSo(message))
            return;

        DecentDiscordBridge.Companion.getBridge().sendSystem(message);
    }
}
