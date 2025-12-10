package com.scarasol.tageditor.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.scarasol.tageditor.TagEditorMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Scarasol
 */
public class JsonHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void writeListToJson(Set<TagTuple<String, Boolean>> set, ResourceLocation resource) {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(TagEditorMod.MODID);
            Path namespaceDir = configDir.resolve(resource.getNamespace());
            if (Files.notExists(namespaceDir)) {
                Files.createDirectories(namespaceDir);
            }

            Path file = namespaceDir.resolve(resource.getPath().replace("/", "+") + ".json");

            JsonObject root = new JsonObject();
            JsonArray array = new JsonArray();

            for (TagTuple<String, Boolean> tuple : set) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", tuple.getA());
                obj.addProperty("add", tuple.getB());
                array.add(obj);
            }

            root.add("value", array);

            String jsonText = GSON.toJson(root);
            Files.writeString(file, jsonText, StandardCharsets.UTF_8);

            TagEditorMod.LOGGER.info("Created JSON: " + file.toAbsolutePath());

        } catch (IOException e) {
            TagEditorMod.LOGGER.error("Failed to write JSON for resource: " + resource, e);
        }
    }



    public static Map<String, Set<TagTuple<String, Boolean>>> readAllJsonValues() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(TagEditorMod.MODID);
        Map<String, Set<TagTuple<String, Boolean>>> result = Maps.newHashMap();

        if (Files.notExists(configDir) || !Files.isDirectory(configDir)) {
            return result;
        }

        try (Stream<Path> stream = Files.walk(configDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .forEach(p -> {
                        try {
                            String content = Files.readString(p, StandardCharsets.UTF_8);
                            JsonObject obj = GSON.fromJson(content, JsonObject.class);
                            if (obj == null || !obj.has("value")) {
                                return;
                            }

                            JsonElement valueElem = obj.get("value");
                            if (!valueElem.isJsonArray()) {
                                return;
                            }

                            JsonArray array = valueElem.getAsJsonArray();
                            Set<TagTuple<String, Boolean>> set = Sets.newHashSet();

                            parseJsonElement(array, set);

                            // 计算 ResourceLocation 风格 key
                            Path relativePath = configDir.relativize(p);
                            Path parent = relativePath.getParent();

                            String namespace;
                            if (parent == null) {
                                namespace = TagEditorMod.MODID;
                            } else {
                                namespace = parent.toString().replace(FileSystems.getDefault().getSeparator(), "/");
                            }

                            String fileName = p.getFileName().toString();
                            if (fileName.toLowerCase(Locale.ROOT).endsWith(".json")) {
                                fileName = fileName.substring(0, fileName.length() - 5);
                            }

                            String key = namespace + ":" + fileName;
                            result.put(key.replace("+", "/"), set);

                        } catch (IOException | JsonParseException ex) {
                            TagEditorMod.LOGGER.error("Failed to read/parse JSON file: " + p + " -> " + ex.getMessage());
                        }
                    });
        } catch (IOException e) {
            TagEditorMod.LOGGER.error("Failed to walk config directory: " + configDir, e);
        }

        return result;
    }

    public static Set<TagTuple<String, Boolean>> readJsonList(ResourceLocation resource) {
        Set<TagTuple<String, Boolean>> result = Sets.newHashSet();
        Path file = FMLPaths.CONFIGDIR.get()
                .resolve(TagEditorMod.MODID)
                .resolve(resource.getNamespace())
                .resolve(resource.getPath() + ".json");

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return result;
        }

        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject obj = GSON.fromJson(content, JsonObject.class);
            if (obj == null || !obj.has("value")) {
                return result;
            }

            JsonElement valueElem = obj.get("value");
            if (!valueElem.isJsonArray()) {
                return result;
            }

            JsonArray array = valueElem.getAsJsonArray();
            parseJsonElement(array, result);

        } catch (IOException | JsonParseException e) {
            TagEditorMod.LOGGER.error("Failed to read JSON file: " + file + " -> " + e.getMessage());
        }

        return result;
    }


    private static void parseJsonElement(JsonArray array, Set<TagTuple<String, Boolean>> set) {
        for (JsonElement el : array) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject o = el.getAsJsonObject();

            // 如果 id 不存在或不是字符串，跳过
            if (!o.has("id") || !o.get("id").isJsonPrimitive() || !o.get("id").getAsJsonPrimitive().isString()) {
                continue;
            }
            String id = o.get("id").getAsString();

            // 默认 add = true
            boolean add = true;
            if (o.has("add") && o.get("add").isJsonPrimitive() && o.get("add").getAsJsonPrimitive().isBoolean()) {
                add = o.get("add").getAsBoolean();
            }

            set.add(new TagTuple<>(id, add));
        }
    }



}
