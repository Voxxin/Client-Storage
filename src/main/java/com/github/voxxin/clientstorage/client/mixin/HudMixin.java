package com.github.voxxin.clientstorage.client.mixin;

import com.github.voxxin.clientstorage.client.ModConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BarrelBlock;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import static com.github.voxxin.clientstorage.client.ClientHandler.minecraftInstance;
import static com.github.voxxin.clientstorage.client.ClientHandler.lastDrawnPos;
import static com.github.voxxin.clientstorage.client.ClientStorageClient.MAP_IN_SLOT;

@Mixin(InGameHud.class)
public abstract class HudMixin {

    @Invoker("renderHotbarItem")
    public abstract void invokeRenderHotbarItem(
            int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed
    );

    @Inject(at = @At("HEAD"), method = "render")
    private void renderer(MatrixStack matrices, float tickDelta, CallbackInfo ci) throws CommandSyntaxException {
        if (minecraftInstance == null) return;
        assert minecraftInstance.world != null;

        HitResult hitResult = minecraftInstance.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult)) return;

        if (!(minecraftInstance.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock() instanceof BarrelBlock)) return;
        lastDrawnPos = ((BlockHitResult) hitResult).getBlockPos();

        renderHeldItem(matrices, tickDelta);
    }

    private void renderHeldItem(MatrixStack matrices, float tickDelta) throws CommandSyntaxException {
        ArrayList<ItemStack> item = ModConfig.getBlock();
        PlayerEntity playerEntity = minecraftInstance.player;

        assert item != null;
        if (!item.isEmpty()) {
            int screenWidth = minecraftInstance.getWindow().getScaledWidth();
            int screenHeight = minecraftInstance.getWindow().getScaledHeight();

            int xPos = screenWidth / 2 - 8;
            int yPos = screenHeight / 2 - 8;
            yPos = yPos - 16;


            if (item.size() == 1) {
                if (item.get(0).getItem().equals(Items.FILLED_MAP) && MAP_IN_SLOT) {
                    renderMap(item.get(0), xPos, yPos);
                } else {
                    invokeRenderHotbarItem(xPos, yPos, tickDelta, playerEntity, item.get(0), 0);
                }

            } else {

                int xPos0 = xPos - 8;
                int xPos1 = xPos + 8;

                if (item.get(0).getItem().equals(Items.FILLED_MAP) && MAP_IN_SLOT) {
                    renderMap(item.get(0), xPos0, yPos);
                } else {
                    invokeRenderHotbarItem(xPos0, yPos, tickDelta, playerEntity, item.get(0), 0);
                }

                if (item.get(1).getItem().equals(Items.FILLED_MAP) && MAP_IN_SLOT) {
                    renderMap(item.get(1), xPos1, yPos);
                } else {
                    invokeRenderHotbarItem(xPos1, yPos, tickDelta, playerEntity, item.get(1), 0);
                }
            }
        }
    }

    private static void renderMap(ItemStack stack, int x, int y) {
        if (minecraftInstance.player == null) return;

        Integer mapId = FilledMapItem.getMapId(stack);
        MapState saveData = FilledMapItem.getMapState(mapId, minecraftInstance.player.world);

        if (mapId != null && saveData != null) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.translate(x, y, 0);
            matrixStack.scale(1 / 8f, 1 / 8f, 1);

            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

            minecraftInstance.gameRenderer.getMapRenderer().draw(
                    matrixStack, immediate, mapId, saveData, true, 0xF000D2
            );
            immediate.draw();
            matrixStack.pop();

        }
    }
}