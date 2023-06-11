package com.github.voxxin.clientstorage.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static com.github.voxxin.clientstorage.client.ClientStorageClient.MODID;

public class ClientStorageKeybinds {

    public static final String CATEGORY = "key." +  "category." + MODID;
    public static final String INTERACTION_KEY = "key." + MODID + ".interact";

    public static final String IMPORT_KEY = "key." + MODID + ".import";

    public static KeyBinding interactionKey;
    public static KeyBinding importKey;

    public static void register() {
        interactionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                INTERACTION_KEY,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        ));

        importKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                IMPORT_KEY,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                CATEGORY
        ));
    }
}
