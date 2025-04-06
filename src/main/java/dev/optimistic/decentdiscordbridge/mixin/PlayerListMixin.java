package dev.optimistic.decentdiscordbridge.mixin;

import com.mojang.authlib.GameProfile;
import dev.optimistic.decentdiscordbridge.DecentDiscordBridge;
import dev.optimistic.decentdiscordbridge.bridge.AbstractBridge;
import dev.optimistic.decentdiscordbridge.ducks.CachedAvatarUrlDuck;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void init(CallbackInfo ci) {
        new DecentDiscordBridge((PlayerList) (Object) this);
    }

    @Inject(method = "getPlayerForLogin", at = @At("HEAD"))
    private void onPlayerLogin(GameProfile profile, ClientInformation clientInformation,
                               CallbackInfoReturnable<ServerPlayer> cir) {
        ((CachedAvatarUrlDuck) profile).calculateAvatarUrl();
    }

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("TAIL"))
    private void broadcastChatMessage(PlayerChatMessage message, Predicate<ServerPlayer> shouldSendFiltered, @Nullable ServerPlayer sender, ChatType.Bound params, CallbackInfo ci) {
        final AbstractBridge bridge = DecentDiscordBridge.Companion.getBridge();

        if (sender == null) {
            bridge.sendSystem(params.decorate(message.decoratedContent()));
        } else {
            bridge.sendPlayer(sender, message);
        }
    }

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("TAIL"))
    private void broadcastSystemMessage(Component message, boolean bypassHiddenChat, CallbackInfo ci) {
        if (bypassHiddenChat)
            return; // again, who cares?

        if (DecentDiscordBridge.Companion
                .getBridge()
                .getMessageRenderer()
                .isRenderedAndRemoveIfSo(message))
            return;

        DecentDiscordBridge.Companion.getBridge().sendSystem(message);
    }
}
