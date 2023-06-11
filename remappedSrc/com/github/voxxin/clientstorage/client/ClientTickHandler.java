package com.github.voxxin.clientstorage.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.w3c.dom.Text;

import javax.print.DocFlavor;

import static net.minecraft.util.Hand.MAIN_HAND;
import static net.minecraft.util.ActionResult.*;

public class ClientTickHandler {
    private static net.minecraft.client.MinecraftClient minecraftInstance = null;
    public static void onClientTick(net.minecraft.client.MinecraftClient minecraft) {
        minecraftInstance = minecraft;
        // Setting Which block it is!

        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            if (hand == MAIN_HAND && hitResult.getType() == HitResult.Type.BLOCK) {
                if (world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.BARREL)) {

                    return FAIL;
                }
            }
            return PASS;
        }));

        if (minecraft.player != null) {
            HitResult hitResult = minecraft.crosshairTarget;

            if (hitResult.getType() != HitResult.Type.BLOCK) return;

            BlockHitResult blockHitResult = (BlockHitResult) hitResult;

            assert minecraft.world != null;
            BlockState blockState = minecraft.world.getBlockState(blockHitResult.getBlockPos());

            if (blockState.getBlock() instanceof BarrelBlock) {
                HudRenderCallback.EVENT.register(((drawContext, tickDelta) -> {
                    ItemStack heldItemStack = minecraft.player.getMainHandStack();
                    renderHeldItem(drawContext, heldItemStack);
                }));
            }
        }
    }

    private static void renderHeldItem(DrawContext drawContext, ItemStack heldItemStack) {
        if (!heldItemStack.isEmpty() && minecraftInstance != null) {
            // Get the current screen width and height
            int screenWidth = minecraftInstance.getWindow().getWidth();
            int screenHeight = minecraftInstance.getWindow().getHeight();

            // Calculate the position to render the item
            int xPos = screenWidth / 2 - 8;
            int yPos = screenHeight / 2 - 8;

            // Render the item on the screen


            drawContext.drawItem(matrixStack, xPos, yPos, itemStack);
        }
    }
}

