package com.terraformersmc.modmenu.api;

/** Compile-only stub. Real Mod Menu supplies this at runtime; excluded from the jar. */
public interface ModMenuApi {
    default ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> null;
    }
}
