package com.grinderwolf.swm.plugin.config;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MinioFakeMap implements Map<String, WorldData> {

    private final MinioClient minioClient;
    private final String HOST = "";
    private final String USER = "";
    private final String PASSWORD = "";

    public MinioFakeMap() {
        minioClient = MinioClient.builder()
                .endpoint(HOST, 9000, false)
                .credentials(USER, PASSWORD)
                .build();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String string) {
            CompletableFuture<Boolean> promise = CompletableFuture.supplyAsync(() -> {
                try {
                    minioClient.statObject(StatObjectArgs.builder()
                            .bucket("isles")
                            .object(string + ".slime")
                            .build());
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            });
            try {
                return promise.get();
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public WorldData get(Object key) {
        if (containsKey(key)) {
            return getGenericWorldData();
        }
        return null;
    }

    private WorldData getGenericWorldData() {
        WorldData worldData = new WorldData();
        worldData.setDataSource("minio");
        worldData.setDifficulty("normal");
        worldData.setLoadOnStartup(false);
        return worldData;
    }

    @Nullable
    @Override
    public WorldData put(String key, WorldData value) {
        return null;
    }

    @Override
    public WorldData remove(Object key) {
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends WorldData> m) {

    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public Set<String> keySet() {
        CompletableFuture<Set<String>> promise = CompletableFuture.supplyAsync(() -> {
            Set<String> stringSet = new HashSet<>();
            Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket("isles")
                    .recursive(true)
                    .build());
            for (Result<Item> item : result) {
                String world;
                try {
                    world = item.get().objectName();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return new HashSet<>();
                }
                if (world.endsWith(".slime")) {
                    stringSet.add(world.substring(0, world.length() - 6));
                }
            }
            return stringSet;
        });

        try {
            return promise.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new HashSet<>();
    }

    @NotNull
    @Override
    public Collection<WorldData> values() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Set<Entry<String, WorldData>> entrySet() {
        CompletableFuture<Set<Entry<String, WorldData>>> promise = CompletableFuture.supplyAsync(() -> {
            Set<Entry<String, WorldData>> entrySet = new HashSet<>();
            Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                            .bucket("isles")
                            .recursive(true)
                            .build());
            for (Result<Item> item : result) {
                String world;
                try {
                    world = item.get().objectName();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return new HashSet<>();
                }
                WorldData worldData = getGenericWorldData();
                if (world.endsWith(".slime")) {

                    Entry<String, WorldData> entry = new AbstractMap.SimpleEntry<>(world.substring(0, world.length() - 6), worldData);
                    entrySet.add(entry);
                }
            }
                return entrySet;
        });

        try {
            return promise.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new HashSet<>();
    }
}
