package com.lungeswap;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LungeSwapClient implements ClientModInitializer {
    public static KeyMapping activateKey;

    private static boolean toggled = false;
    private static int timer = 0;
    private static Method startAttackMethod;
    private static Field missTimeField;
    private static boolean reflectionFailed = false;

    @Override
    public void onInitializeClient() {
        LungeSwapConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("lungeswap", "main"));
        activateKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.lungeswap.activate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, category));

        ClientTickEvents.END_CLIENT_TICK.register(LungeSwapClient::tick);
    }

    private static void tick(Minecraft mc) {
        LungeSwapConfig cfg = LungeSwapConfig.INSTANCE;

        if (mc.player == null || mc.gameMode == null || mc.gui.screen() != null) {
            timer = 0;
            while (activateKey.consumeClick()) { }
            return;
        }

        boolean active;
        if (cfg.toggleMode) {
            while (activateKey.consumeClick()) toggled = !toggled;
            active = toggled;
        } else {
            toggled = false;
            while (activateKey.consumeClick()) { }
            active = activateKey.isDown();
        }

        if (!active) {
            timer = 0;
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        int spearSlot = findSpearInHotbar(mc);
        if (spearSlot < 0) {
            mc.player.sendOverlayMessage(Component.translatable("lungeswap.no_spear"));
            timer = 20;
            return;
        }

        // The whole lunge swap happens inside ONE tick:
        // select spear -> jab (left click) -> select the previous item again.
        int originalSlot = mc.player.getInventory().getSelectedSlot();
        if (originalSlot != spearSlot) {
            mc.player.getInventory().setSelectedSlot(spearSlot);
        }

        clickAttack(mc);

        if (originalSlot != spearSlot) {
            mc.player.getInventory().setSelectedSlot(originalSlot);
        }

        // wait N ticks, then do it again
        timer = Math.max(0, cfg.delayTicks - 1);
    }

    /** Runs the game's own left-click routine (same code a real left click uses). */
    private static void clickAttack(Minecraft mc) {
        if (reflectionFailed) return;
        try {
            if (startAttackMethod == null) {
                startAttackMethod = Minecraft.class.getDeclaredMethod("startAttack");
                startAttackMethod.setAccessible(true);
                try {
                    missTimeField = Minecraft.class.getDeclaredField("missTime");
                    missTimeField.setAccessible(true);
                } catch (Exception ignored) {
                    missTimeField = null;
                }
            }
            if (missTimeField != null) {
                missTimeField.setInt(mc, 0); // make sure the game doesn't block the click
            }
            startAttackMethod.invoke(mc);
        } catch (Exception e) {
            reflectionFailed = true;
            e.printStackTrace();
            if (mc.player != null) {
                mc.player.sendOverlayMessage(Component.literal("Lunge Swap: could not trigger attack (see latest.log)"));
            }
        }
    }

    private static int findSpearInHotbar(Minecraft mc) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getItem(i);
            if (!s.isEmpty() && BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().endsWith("_spear")) {
                return i;
            }
        }
        return -1;
    }
}
