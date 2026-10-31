package com.lungeswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class LungeSwapConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("lungeswap.json");

    public static final int MIN_TICKS = 5;
    public static final int MAX_TICKS = 100;

    public int delayTicks = 8;          // full cycle length in ticks (5-100)
    public boolean toggleMode = false;  // false = hold key, true = press to toggle on/off
    public boolean attackWithSpear = true; // also left-click once with the spear after swapping

    public static LungeSwapConfig INSTANCE = new LungeSwapConfig();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE)) {
                    LungeSwapConfig c = GSON.fromJson(r, LungeSwapConfig.class);
                    if (c != null) INSTANCE = c;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        INSTANCE.delayTicks = Math.max(MIN_TICKS, Math.min(MAX_TICKS, INSTANCE.delayTicks));
        save();
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
