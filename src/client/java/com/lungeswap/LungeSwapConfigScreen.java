package com.lungeswap;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class LungeSwapConfigScreen extends Screen {
    private final Screen parent;

    public LungeSwapConfigScreen(Screen parent) {
        super(Component.translatable("lungeswap.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        LungeSwapConfig cfg = LungeSwapConfig.INSTANCE;
        int w = 220, x = this.width / 2 - w / 2, y = this.height / 2 - 50;

        addRenderableWidget(new StringWidget(x, y - 24, w, 9, this.title, this.font));

        addRenderableWidget(new TickSlider(x, y, w, 20, cfg.delayTicks));

        addRenderableWidget(Button.builder(toggleText(), b -> {
            cfg.toggleMode = !cfg.toggleMode;
            b.setMessage(toggleText());
        }).bounds(x, y + 26, w, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(x, y + 58, w, 20).build());
    }

    private Component toggleText() {
        return Component.literal("Activation: " + (LungeSwapConfig.INSTANCE.toggleMode ? "Toggle" : "Hold"));
    }

    @Override
    public void onClose() {
        LungeSwapConfig.save();
        // 26.2: setScreen lives on Minecraft.gui
        this.minecraft.gui.setScreen(parent);
    }

    private static class TickSlider extends AbstractSliderButton {
        TickSlider(int x, int y, int w, int h, int ticks) {
            super(x, y, w, h, Component.empty(),
                    (ticks - LungeSwapConfig.MIN_TICKS) / (double) (LungeSwapConfig.MAX_TICKS - LungeSwapConfig.MIN_TICKS));
            updateMessage();
        }

        private int ticks() {
            return LungeSwapConfig.MIN_TICKS
                    + (int) Math.round(this.value * (LungeSwapConfig.MAX_TICKS - LungeSwapConfig.MIN_TICKS));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Delay: " + ticks() + " ticks"));
        }

        @Override
        protected void applyValue() {
            LungeSwapConfig.INSTANCE.delayTicks = ticks();
        }
    }
}
