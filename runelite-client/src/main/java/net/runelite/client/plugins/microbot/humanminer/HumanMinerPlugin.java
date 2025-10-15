package net.runelite.client.plugins.microbot.humanminer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;

@PluginDescriptor(
    name = PluginDescriptor.Mocrosoft + "Human Miner",
    description = "A human-like mining bot with advanced antiban features",
    tags = {"microbot", "mining", "skilling", "antiban"},
    enabledByDefault = false
)
@Slf4j
public class HumanMinerPlugin extends Plugin {

    @Inject
    private HumanMinerConfig config;

    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private HumanMinerOverlay overlay;

    private HumanMinerScript script;

    @Provides
    HumanMinerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HumanMinerConfig.class);
    }

    private final HotkeyListener startHotkeyListener = new HotkeyListener(() -> config.startHotkey()) {
        @Override
        public void hotkeyPressed() {
            toggleScript();
        }
    };

    @Override
    protected void startUp() {
        log.info("Human Miner plugin started");

        if (overlayManager != null) {
            overlayManager.add(overlay);
        }

        keyManager.registerKeyListener(startHotkeyListener);

        // Initialize script
        script = new HumanMinerScript();

        log.info("Human Miner ready! Press {} to start/stop mining", config.startHotkey());
    }

    @Override
    protected void shutDown() {
        log.info("Human Miner plugin stopped");

        if (script != null && script.isRunning()) {
            script.shutdown();
        }

        keyManager.unregisterKeyListener(startHotkeyListener);

        if (overlayManager != null) {
            overlayManager.remove(overlay);
        }

        // Reset antiban settings
        Rs2Antiban.resetAntibanSettings();
    }

    private void toggleScript() {
        if (script == null) {
            script = new HumanMinerScript();
        }

        if (!script.isRunning()) {
            // Configure antiban before starting
            configureAntiban();

            // Start the script
            Microbot.getClientThread().runOnClientThread(() -> {
                if (script.run(config)) {
                    log.info("Human Miner started mining {}", config.rockType().getName());
                    Microbot.showMessage("Human Miner started!");
                } else {
                    log.error("Failed to start Human Miner");
                    Microbot.showMessage("Failed to start Human Miner");
                }
                return true;
            });
        } else {
            // Stop the script
            script.shutdown();
            log.info("Human Miner stopped");
            Microbot.showMessage("Human Miner stopped!");
        }
    }

    private void configureAntiban() {
        if (!config.useAntiban()) {
            Rs2AntibanSettings.antibanEnabled = false;
            return;
        }

        // Enable antiban
        Rs2AntibanSettings.antibanEnabled = true;
        Rs2AntibanSettings.usePlayStyle = true;

        // Configure based on user settings
        Rs2AntibanSettings.naturalMouse = config.naturalMouse();
        Rs2AntibanSettings.randomIntervals = config.randomIntervals();
        Rs2AntibanSettings.moveMouseOffScreen = config.moveMouseOffScreen();
        Rs2AntibanSettings.moveMouseRandomly = config.moveMouseRandomly();
        Rs2AntibanSettings.takeMicroBreaks = config.takeMicroBreaks();

        // Set micro break durations
        Rs2AntibanSettings.microBreakDurationLow = config.microBreakDurationLow();
        Rs2AntibanSettings.microBreakDurationHigh = config.microBreakDurationHigh();

        // Enable behavioral features
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;

        // Set chances for random behaviors
        Rs2AntibanSettings.actionCooldownChance = 0.15; // 15% chance
        Rs2AntibanSettings.microBreakChance = config.takeMicroBreaks() ? 0.08 : 0.0; // 8% chance if enabled
        Rs2AntibanSettings.moveMouseRandomlyChance = config.moveMouseRandomly() ? 0.12 : 0.0; // 12% chance if enabled
        Rs2AntibanSettings.moveMouseOffScreenChance = config.moveMouseOffScreen() ? 0.10 : 0.0; // 10% chance if enabled

        log.info("Antiban configured: naturalMouse={}, randomIntervals={}, microBreaks={}",
                 config.naturalMouse(), config.randomIntervals(), config.takeMicroBreaks());
    }

    public HumanMinerScript getScript() {
        return script;
    }
}
