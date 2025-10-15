package net.runelite.client.plugins.microbot.humanminer;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.humanminer.enums.Rocks;

import java.awt.*;

@ConfigGroup("humanminer")
public interface HumanMinerConfig extends Config {

    @ConfigSection(
        name = "General Settings",
        description = "General mining settings",
        position = 0,
        closedByDefault = false
    )
    String generalSection = "general";

    @ConfigSection(
        name = "Antiban Settings",
        description = "Human-like behavior settings",
        position = 1,
        closedByDefault = false
    )
    String antibanSection = "antiban";

    @ConfigSection(
        name = "Visual Settings",
        description = "Overlay and highlight settings",
        position = 2,
        closedByDefault = false
    )
    String visualSection = "visual";

    // =============== GENERAL SETTINGS ===============

    @ConfigItem(
        keyName = "startHotkey",
        name = "Start/Stop Hotkey",
        description = "Hotkey to start/stop mining",
        position = 0,
        section = generalSection
    )
    default Keybind startHotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
        keyName = "rockType",
        name = "Rock Type",
        description = "Select the type of rock to mine",
        position = 1,
        section = generalSection
    )
    default Rocks rockType() {
        return Rocks.IRON;
    }

    @ConfigItem(
        keyName = "dropOres",
        name = "Drop Ores",
        description = "Drop ores when inventory is full",
        position = 2,
        section = generalSection
    )
    default boolean dropOres() {
        return false;
    }

    @ConfigItem(
        keyName = "bankOres",
        name = "Bank Ores",
        description = "Bank ores when inventory is full (overrides drop)",
        position = 3,
        section = generalSection
    )
    default boolean bankOres() {
        return false;
    }

    @ConfigItem(
        keyName = "stopWhenFull",
        name = "Stop When Full",
        description = "Stop mining when inventory is full",
        position = 4,
        section = generalSection
    )
    default boolean stopWhenFull() {
        return true;
    }

    // =============== ANTIBAN SETTINGS ===============

    @ConfigItem(
        keyName = "useAntiban",
        name = "Enable Antiban",
        description = "Enable human-like behavior patterns",
        position = 0,
        section = antibanSection
    )
    default boolean useAntiban() {
        return true;
    }

    @ConfigItem(
        keyName = "naturalMouse",
        name = "Natural Mouse Movement",
        description = "Use natural-looking mouse movements",
        position = 1,
        section = antibanSection
    )
    default boolean naturalMouse() {
        return true;
    }

    @ConfigItem(
        keyName = "randomIntervals",
        name = "Random Intervals",
        description = "Add randomness to action timing",
        position = 2,
        section = antibanSection
    )
    default boolean randomIntervals() {
        return true;
    }

    @ConfigItem(
        keyName = "moveMouseOffScreen",
        name = "Move Mouse Off Screen",
        description = "Occasionally move mouse off screen",
        position = 3,
        section = antibanSection
    )
    default boolean moveMouseOffScreen() {
        return true;
    }

    @ConfigItem(
        keyName = "moveMouseRandomly",
        name = "Move Mouse Randomly",
        description = "Occasionally move mouse to random locations",
        position = 4,
        section = antibanSection
    )
    default boolean moveMouseRandomly() {
        return true;
    }

    @ConfigItem(
        keyName = "takeMicroBreaks",
        name = "Take Micro Breaks",
        description = "Take short random breaks",
        position = 5,
        section = antibanSection
    )
    default boolean takeMicroBreaks() {
        return false;
    }

    @Range(
        min = 1,
        max = 30
    )
    @ConfigItem(
        keyName = "microBreakDurationLow",
        name = "Min Break Duration (min)",
        description = "Minimum micro break duration in minutes",
        position = 6,
        section = antibanSection
    )
    default int microBreakDurationLow() {
        return 3;
    }

    @Range(
        min = 1,
        max = 60
    )
    @ConfigItem(
        keyName = "microBreakDurationHigh",
        name = "Max Break Duration (min)",
        description = "Maximum micro break duration in minutes",
        position = 7,
        section = antibanSection
    )
    default int microBreakDurationHigh() {
        return 8;
    }

    // =============== VISUAL SETTINGS ===============

    @ConfigItem(
        keyName = "highlightRocks",
        name = "Highlight Rocks",
        description = "Highlight the selected rock type",
        position = 0,
        section = visualSection
    )
    default boolean highlightRocks() {
        return true;
    }

    @ConfigItem(
        keyName = "highlightColor",
        name = "Highlight Color",
        description = "Color to highlight rocks",
        position = 1,
        section = visualSection
    )
    default Color highlightColor() {
        return new Color(0, 255, 255, 50);
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show Overlay",
        description = "Show mining statistics overlay",
        position = 2,
        section = visualSection
    )
    default boolean showOverlay() {
        return true;
    }
}
