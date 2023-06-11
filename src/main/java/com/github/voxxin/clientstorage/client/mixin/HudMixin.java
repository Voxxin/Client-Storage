package com.github.voxxin.clientstorage.client.mixin;

import com.github.voxxin.clientstorage.client.ModConfig;
import net.minecraft.block.BarrelBlock;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.voxxin.clientstorage.client.ClientHandler.minecraftInstance;
import static com.github.voxxin.clientstorage.client.ClientHandler.lastDrawnPos;

@Mixin(InGameHud.class)
public abstract class HudMixin {

    @Invoker("renderHotbarItem")
    public abstract void invokeRenderHotbarItem(
            int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed
    );

    @Inject(at = @At("HEAD"), method = "render")
    private void renderer(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (minecraftInstance == null) return;
        assert minecraftInstance.world != null;

        HitResult hitResult = minecraftInstance.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult)) return;

        if (!(minecraftInstance.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock() instanceof BarrelBlock)) return;
        lastDrawnPos = ((BlockHitResult) hitResult).getBlockPos();

        renderHeldItem(matrices, tickDelta);
    }

    private void renderHeldItem(MatrixStack matrices, float tickDelta) {
        ItemStack item = ModConfig.getBlock();

        if (item != null) {
            int screenWidth = minecraftInstance.getWindow().getScaledWidth();
            int screenHeight = minecraftInstance.getWindow().getScaledHeight();

            int xPos = screenWidth / 2 - 8;
            int yPos = screenHeight / 2 - 8;
            yPos = yPos - 16;

            invokeRenderHotbarItem(xPos, yPos, tickDelta, minecraftInstance.player, item, 1 + 2);
        }
    }
}
