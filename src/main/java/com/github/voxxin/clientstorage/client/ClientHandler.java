package com.github.voxxin.clientstorage.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static com.github.voxxin.clientstorage.client.ClientStorageKeybinds.importKey;
import static com.github.voxxin.clientstorage.client.ClientStorageKeybinds.interactionKey;
import static com.github.voxxin.clientstorage.client.ClientStorageClient.SERVER_DIMENSION;
import static net.minecraft.util.Hand.MAIN_HAND;

public class ClientHandler {
    public static MinecraftClient minecraftInstance = null;
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
                            ModConfig.addBlock(hitResult.getBlockPos(), world.getBlockState(hitResult.getBlockPos()).getBlock(), minecraft.player.getMainHandStack());
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
}

