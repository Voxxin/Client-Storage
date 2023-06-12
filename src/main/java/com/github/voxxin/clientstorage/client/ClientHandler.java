package com.github.voxxin.clientstorage.client;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

import static com.github.voxxin.clientstorage.client.ClientStorageKeybinds.importKey;
import static com.github.voxxin.clientstorage.client.ClientStorageKeybinds.interactionKey;
import static com.github.voxxin.clientstorage.client.ClientStorageClient.SERVER_DIMENSION;
import static net.minecraft.util.Hand.MAIN_HAND;

public class ClientHandler {
    private static MinecraftClient minecraftInstance = null;
    public static BlockPos lastDrawnPos = null;
    public static void onClientTick(MinecraftClient minecraft) {
        minecraftInstance = minecraft;
        if (minecraft.world == null) return;
        SERVER_DIMENSION = String.valueOf(minecraftInstance.world.getDimensionKey().getValue());

        if (importKey.wasPressed()) {
            ModConfig.importScreen();
        }

        // Setting Which block it is!

        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            if (hand == MAIN_HAND && hitResult.getType() == HitResult.Type.BLOCK) {
                if (world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.BARREL)) {
                    if (interactionKey.isPressed()) {
                        if (!player.getInventory().getMainHandStack().isEmpty()) {

                            ArrayList<ItemStack> itemsArray = new ArrayList<>();
                            itemsArray.add(minecraft.player.getMainHandStack());

                            if (!minecraft.player.getOffHandStack().isEmpty()) itemsArray.add(minecraft.player.getOffHandStack());

                            ModConfig.addBlock(hitResult.getBlockPos(), world.getBlockState(hitResult.getBlockPos()).getBlock(), itemsArray);
                        } else {
                            ModConfig.removeBlock(hitResult.getBlockPos());
                        }
                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        }));
    }

    public static void onHudRenderer(DrawContext drawContext, float v) {
        if (minecraftInstance == null) return;
        assert minecraftInstance.world != null;

        HitResult hitResult = minecraftInstance.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult)) return;

        if (!(minecraftInstance.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock() instanceof BarrelBlock)) return;
        lastDrawnPos = ((BlockHitResult) hitResult).getBlockPos();

        renderHeldItem(drawContext);
    }


    private static void renderHeldItem(DrawContext drawContext) {
        ArrayList<ItemStack> item = ModConfig.getBlock();

        assert item != null;
        if (!item.isEmpty()) {
            int screenWidth = minecraftInstance.getWindow().getScaledWidth();
            int screenHeight = minecraftInstance.getWindow().getScaledHeight();

            int xPos = screenWidth / 2 - 8;
            int yPos = screenHeight / 2 - 8;
            yPos = yPos - 16;

            if (item.size() == 1) {
                drawContext.drawItem(item.get(0), xPos, yPos);
            } else {
                int xPos0 = xPos - 8;
                int xPos1 = xPos + 8;
                drawContext.drawItem(item.get(0), xPos0, yPos);
                drawContext.drawItem(item.get(1), xPos1, yPos);
            }
        }
    }
}

