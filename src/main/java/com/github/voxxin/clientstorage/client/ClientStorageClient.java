package com.github.voxxin.clientstorage.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ClientStorageClient implements ClientModInitializer {

    public static String MODID = "client-storage";
    public static String SERVER_IP = null;
    public static boolean SINGLEPLAYER = false;
    public static String SERVER_DIMENSION = null;
    @Override
    public void onInitializeClient() {
        ClientStorageKeybinds.register();

        ClientTickEvents.END_CLIENT_TICK.register(ClientHandler::onClientTick);
        HudRenderCallback.EVENT.register(ClientHandler::onHudRenderer);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (client.isInSingleplayer()) {
                SERVER_IP = client.getServer().getSaveProperties().getLevelName();
                SINGLEPLAYER = true;
            } else SERVER_IP = handler.getConnection().getAddress().toString();
        }));
    }
}

