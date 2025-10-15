package net.runelite.client.plugins.microbot.humanminer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.humanminer.enums.Rocks;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HumanMinerScript extends Script {

    public static double version = 1.0;

    @Getter
    private int oresMined = 0;

    @Getter
    private int totalExperience = 0;

    @Getter
    private Instant startTime;

    @Getter
    private GameObject currentRock = null;

    private Rocks selectedRock;
    private HumanMinerConfig config;

    private int startingMiningLevel;
    private int currentMiningLevel;
    private int experienceGained = 0;

    public boolean run(HumanMinerConfig config) {
        this.config = config;
        this.selectedRock = config.rockType();
        this.startTime = Instant.now();
        this.startingMiningLevel = Rs2Player.getRealSkillLevel(Skill.MINING);
        this.currentMiningLevel = startingMiningLevel;

        // Set the antiban activity to mining
        Rs2Antiban.setActivity(Activity.GENERAL_MINING);

        log.info("Starting Human Miner - Mining: {}", selectedRock.getName());

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;

                // Check if we should stop
                if (shouldStop()) {
                    log.info("Stopping conditions met");
                    shutdown();
                    return;
                }

                // Update mining level
                currentMiningLevel = Rs2Player.getRealSkillLevel(Skill.MINING);
                experienceGained = Rs2Player.getBoostedSkillLevel(Skill.MINING) - startingMiningLevel;

                // Handle micro breaks by chance
                if (Rs2AntibanSettings.takeMicroBreaks && Rs2Antiban.takeMicroBreakByChance()) {
                    log.info("Taking a micro break...");
                    return;
                }

                // Main mining logic
                if (!Rs2Player.isAnimating() && !Rs2Player.isMoving()) {
                    // Not currently mining, find and mine a rock

                    // Trigger action cooldown by chance (creates human-like pauses)
                    Rs2Antiban.actionCooldown();

                    if (Rs2AntibanSettings.actionCooldownActive) {
                        // Wait for cooldown to finish
                        sleep(Rs2Antiban.getTIMEOUT());
                        Rs2AntibanSettings.actionCooldownActive = false;
                        return;
                    }

                    // Find nearby rock
                    currentRock = findNearestRock();

                    if (currentRock == null) {
                        log.warn("No {} rocks found nearby", selectedRock.getName());
                        Microbot.status = "No rocks found!";

                        // Move mouse randomly while waiting
                        if (Rs2AntibanSettings.moveMouseRandomly && Rs2Random.diceFractional(0.3)) {
                            Rs2Antiban.moveMouseRandomly();
                        }

                        sleep(2000, 3000);
                        return;
                    }

                    // Mine the rock
                    mineRock(currentRock);

                } else {
                    // Currently mining/moving
                    Microbot.status = "Mining " + selectedRock.getName() + "...";

                    // Occasionally move mouse randomly while mining (human-like behavior)
                    if (Rs2Player.isAnimating() && Rs2Random.diceFractional(0.05)) {
                        if (Rs2AntibanSettings.moveMouseRandomly) {
                            sleep(Rs2Random.between(500, 1500));
                            Rs2Antiban.moveMouseRandomly();
                        }
                    }
                }

            } catch (Exception ex) {
                log.error("Error in Human Miner script", ex);
                Microbot.showMessage("Error: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS); // Run every 600ms for more human-like timing

        return true;
    }

    private boolean shouldStop() {
        // Check if inventory is full
        if (Rs2Inventory.isFull()) {
            if (config.bankOres()) {
                return handleBanking();
            } else if (config.dropOres()) {
                return handleDropping();
            } else if (config.stopWhenFull()) {
                Microbot.showMessage("Inventory full - stopping!");
                return true;
            }
        }

        return false;
    }

    private boolean handleBanking() {
        Microbot.status = "Banking ores...";

        if (!Rs2Bank.isOpen()) {
            // Walk to bank
            if (!Rs2Bank.walkToBank()) {
                log.error("Failed to walk to bank");
                return true; // Stop script
            }

            // Open bank
            if (!Rs2Bank.openBank()) {
                log.error("Failed to open bank");
                return true;
            }

            // Add some human delay after opening bank
            sleep(Rs2Random.between(600, 1200));
        }

        if (Rs2Bank.isOpen()) {
            // Deposit all ores except pickaxe
            Rs2Bank.depositAllExcept("pickaxe");

            // Add human-like delay
            sleep(Rs2Random.between(400, 800));

            // Close bank
            Rs2Bank.closeBank();

            // Add delay after closing
            sleep(Rs2Random.between(300, 600));
        }

        return false; // Continue mining
    }

    private boolean handleDropping() {
        Microbot.status = "Dropping ores...";

        // Drop all items except pickaxe
        Rs2Inventory.dropAllExcept(item ->
            item.getName() != null && item.getName().toLowerCase().contains("pickaxe")
        );

        // Add human-like delay after dropping
        sleep(Rs2Random.between(800, 1600));

        return false; // Continue mining
    }

    private GameObject findNearestRock() {
        // Find the nearest rock of the selected type
        int[] rockIds = selectedRock.getObjectIds();

        return Rs2GameObject.getGameObject(rockIds[0]);
    }

    private void mineRock(GameObject rock) {
        if (rock == null) return;

        // Check if rock has line of sight
        if (!Rs2GameObject.hasLineOfSight(rock)) {
            log.debug("Rock not in line of sight, finding another...");
            return;
        }

        // Human-like delay before clicking
        sleep(Rs2Random.between(100, 300));

        // Mine the rock - Rs2GameObject.interact will use natural mouse if enabled
        boolean clicked = Rs2GameObject.interact(rock, "Mine");

        if (clicked) {
            Microbot.status = "Mining " + selectedRock.getName() + "...";

            // Wait for mining animation to start
            Rs2Player.waitForAnimation();

            // Increment ores mined (approximate)
            oresMined++;

            // Add some variation in how we wait after clicking
            if (Rs2Random.diceFractional(0.7)) {
                // 70% chance to wait for animation
                sleep(Rs2Random.between(200, 600));
            } else {
                // 30% chance to immediately look for next rock (more active play style)
                sleep(Rs2Random.between(50, 150));
            }
        } else {
            log.warn("Failed to click rock");

            // Move mouse randomly on failure (human-like)
            if (Rs2AntibanSettings.moveMouseRandomly && Rs2Random.diceFractional(0.5)) {
                Rs2Antiban.moveMouseRandomly();
            }
        }
    }

    public Duration getRuntime() {
        if (startTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(startTime, Instant.now());
    }

    public int getOresPerHour() {
        if (startTime == null) return 0;

        long seconds = Duration.between(startTime, Instant.now()).getSeconds();
        if (seconds == 0) return 0;

        return (int) ((oresMined * 3600.0) / seconds);
    }

    public int getExperiencePerHour() {
        if (startTime == null) return 0;

        long seconds = Duration.between(startTime, Instant.now()).getSeconds();
        if (seconds == 0) return 0;

        return (int) ((experienceGained * 3600.0) / seconds);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        Rs2Antiban.resetAntibanSettings();
        log.info("Human Miner shutdown - Mined {} ores in {}", oresMined, getRuntime());
    }
}
