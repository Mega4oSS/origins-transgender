package ru.alaverdyan.artem.originstransgender.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RitualDataManager {
    private static final File FILE = new File("config/originstransgender/rituals.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<UUID, Integer> data = new HashMap<>();
    private static Map<UUID, boolean[]> dataLoralor = new HashMap<>();


    public static void load() {
        if (!FILE.exists()) return;
        try (Reader r = new FileReader(FILE)) {
            Type type = new TypeToken<Map<UUID, Integer>>(){}.getType();
            data = GSON.fromJson(r, type);
            type = new TypeToken<Map<UUID, boolean[]>>(){}.getType();
            dataLoralor = GSON.fromJson(r, type);
            if (data == null) data = new HashMap<>();
            if (dataLoralor == null) dataLoralor = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (Writer w = new FileWriter(FILE)) {
                GSON.toJson(data, w);
                GSON.toJson(dataLoralor, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getRituals(UUID player) {
        return data.getOrDefault(player, 0);
    }

    public static void addRitual(UUID player) {
        data.put(player, getRituals(player) + 1);
        save();
    }

    public static void setRituals(UUID player, int rituals) {
        data.put(player, rituals);
        save();
    }

    public static boolean[] getLoralorAll(UUID player) {
        return dataLoralor.getOrDefault(player, new boolean[4]);
    }

    public static boolean getLoralor(UUID player, int id) {
        return dataLoralor.getOrDefault(player, new boolean[4])[id];
    }

    public static void setDataLoralor(UUID player, int value, boolean value2) {
        boolean[] boobs = dataLoralor.getOrDefault(player, new boolean[4]);
        boobs[value] = value2;
        dataLoralor.put(player, boobs);
        save();
    }
}
