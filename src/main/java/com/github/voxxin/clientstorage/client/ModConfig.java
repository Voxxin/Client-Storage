package com.github.voxxin.clientstorage.client;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.github.voxxin.clientstorage.client.ClientHandler.lastDrawnPos;
import static com.github.voxxin.clientstorage.client.ClientStorageClient.*;


public class ModConfig {
    private static final File clientStorageDir = FabricLoader.getInstance().getConfigDir().resolve("client-storage").toFile();
    private static final File importFolder = new File(clientStorageDir, "import");
    private static File locationsFile = null;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static void isOrAddDir() {
        File serverSideDir = new File(clientStorageDir, "multiPlayer");
        if (SINGLEPLAYER) serverSideDir = new File(clientStorageDir, "singlePlayer");

        SERVER_IP = SERVER_IP.replaceAll(" ", "_");
        SERVER_IP = SERVER_IP.replaceAll("\\./([\\d.]+:\\d+)", "");
        SERVER_IP = SERVER_IP.toLowerCase();

        File serverWorldDir = new File(serverSideDir, ClientStorageClient.SERVER_IP);
        locationsFile = new File(serverWorldDir, "locations.json");

        try {
            importFolder.mkdirs();

            serverSideDir.mkdirs();
            serverWorldDir.mkdirs();
            if (!locationsFile.exists()) {
                locationsFile.getParentFile().getParentFile().mkdirs();
                locationsFile.getParentFile().mkdirs();
                locationsFile.createNewFile();
                JsonObject initialLocations = new JsonObject();
                locationFile(initialLocations);
            }

            File[] files = importFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        importLocation(file);
                        if (!file.delete()) {
                            System.out.println("Failed to delete file: " + file.getName());
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject locationFile() {
        try {
            if (locationsFile == null) return null;
            if (!locationsFile.exists()) {
                return new JsonObject();
            }
            FileReader fileReader = new FileReader(locationsFile);
            return gson.fromJson(fileReader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading location file: " + e.getMessage());
        }
    }

    private static void locationFile(JsonObject object) {
        try (FileWriter fileWriter = new FileWriter(locationsFile)) {
            gson.toJson(object, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException("Error saving location file: " + e.getMessage());
        }
    }

    public static void addBlock(BlockPos blockPos, Block block, ItemStack heldItem) {
        isOrAddDir();
        JsonObject locations = locationFile();
        if (locations == null) return;

        JsonArray dimensionArray = new JsonArray();

        if (locations.getAsJsonArray(SERVER_DIMENSION) != null) {
            dimensionArray = locations.getAsJsonArray(SERVER_DIMENSION);
        }
        JsonObject blockLoc = new JsonObject();
        JsonArray blockLocation = new JsonArray();
        blockLocation.add(blockPos.getX());
        blockLocation.add(blockPos.getY());
        blockLocation.add(blockPos.getZ());

        blockLoc.add("location", blockLocation);
        blockLoc.addProperty("type", block.getTranslationKey());
        blockLoc.addProperty("item", String.valueOf(Registry.ITEM.getId(heldItem.getItem())));

        if (dimensionArray.contains(blockLoc)) return;

        for (int i = 0; i < dimensionArray.size(); i++) {
            JsonObject blockPosObj = dimensionArray.get(i).getAsJsonObject();
            if (blockPosObj.size() == 0) break;
            JsonArray locationArray = blockPosObj.get("location").getAsJsonArray();

            if (locationArray.toString().equals(blockLocation.toString())) {
                dimensionArray.remove(i);
                i--;
            }
        }

        dimensionArray.add(blockLoc);

        locations.add(SERVER_DIMENSION, dimensionArray);
        locationFile(locations);
    }

    public static void removeBlock(BlockPos blockPos) {
        isOrAddDir();
        JsonObject locations = locationFile();
        if (locations == null) return;

        JsonArray dimensionArray = new JsonArray();

        if (locations.getAsJsonArray(SERVER_DIMENSION) != null) {
            dimensionArray = locations.getAsJsonArray(SERVER_DIMENSION);
        }

        if (dimensionArray.isEmpty()) return;

        JsonArray blockLocation = new JsonArray();
        blockLocation.add(blockPos.getX());
        blockLocation.add(blockPos.getY());
        blockLocation.add(blockPos.getZ());

        for (int i = 0; i < dimensionArray.size(); i++) {
            JsonObject blockPosObj = dimensionArray.get(i).getAsJsonObject();
            if (blockPosObj.size() == 0) return;
            JsonArray locationArray = blockPosObj.get("location").getAsJsonArray();

            if (locationArray.toString().equals(blockLocation.toString())) {
                dimensionArray.remove(i);
                i--;
            }
        }

        locations.add(SERVER_DIMENSION, dimensionArray);
        locationFile(locations);
    }

    public static ItemStack getBlock() {
        if (lastDrawnPos == null) return null;
        ItemStack itemStack = null;

        isOrAddDir();
        JsonObject locations = locationFile();
        if (locations == null) return null;
        locationFile(locations);

        JsonArray dimensionArray = new JsonArray();

        if (locations.getAsJsonArray(SERVER_DIMENSION) != null) {
            dimensionArray = locations.getAsJsonArray(SERVER_DIMENSION);
        }

        JsonArray blockLocation = new JsonArray();
        blockLocation.add(lastDrawnPos.getX());
        blockLocation.add(lastDrawnPos.getY());
        blockLocation.add(lastDrawnPos.getZ());

        for (JsonElement blockPosObj0 : dimensionArray) {
            JsonObject blockPosObj = blockPosObj0.getAsJsonObject();
            JsonArray locationArray = blockPosObj.get("location").getAsJsonArray();

            if (blockLocation.equals(locationArray) && itemStack == null) {
                Item item = Registry.ITEM.get(Identifier.tryParse(blockPosObj.get("item").getAsString())).asItem();
                itemStack = new ItemStack(item);
            }
        }

        return itemStack;
    }

    public static void importLocation(File file) throws IOException {
        JsonObject locations = locationFile();
        if (locations == null) return;

        Map<String, JsonArray> existingDimensions = new HashMap<>();

        for (String jsonElement : locations.keySet()) {
            existingDimensions.put(jsonElement, locations.getAsJsonArray(jsonElement));
        }

        if (!file.getName().endsWith(".json")) return;

        FileReader fileReader = new FileReader(file);
        JsonObject jsonedFile = gson.fromJson(fileReader, JsonObject.class);
        fileReader.close();

        JsonObject newFile = new JsonObject();

        for (String newArrayDimension : jsonedFile.keySet()) {

            JsonArray newDimensionArray = jsonedFile.getAsJsonArray(newArrayDimension);

            if (existingDimensions.get(newArrayDimension) != null) {
                for (JsonElement oldArrayElement : existingDimensions.get(newArrayDimension)) {
                    for (JsonElement newArrayElement : newDimensionArray) {
                        JsonElement newLoc = newArrayElement.getAsJsonObject().get("location");
                        JsonElement oldLoc = oldArrayElement.getAsJsonObject().get("location");
                        if (newLoc.equals(oldLoc)) return;
                    }

                    if (!newDimensionArray.contains(oldArrayElement)) newDimensionArray.add(oldArrayElement);
                }
            }

            newFile.add(newArrayDimension, newDimensionArray);
        }


        locationFile(newFile);
    }

    public static void importScreen() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                Runtime.getRuntime().exec("explorer.exe " + importFolder.getAbsolutePath());
            } else if (osName.contains("mac")) {
                Runtime.getRuntime().exec("open " + importFolder.getAbsolutePath());
            } else {
                Runtime.getRuntime().exec("xdg-open " + importFolder.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}