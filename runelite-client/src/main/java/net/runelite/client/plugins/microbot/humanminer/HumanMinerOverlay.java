package net.runelite.client.plugins.microbot.humanminer;

import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.humanminer.enums.Rocks;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.util.Arrays;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class HumanMinerOverlay extends OverlayPanel {

    private final HumanMinerPlugin plugin;
    private final HumanMinerConfig config;

    @Inject
    public HumanMinerOverlay(HumanMinerPlugin plugin, HumanMinerConfig config) {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Human Miner overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            HumanMinerScript script = plugin.getScript();

            if (script == null || !script.isRunning()) {
                return null;
            }

            if (!config.showOverlay()) {
                // Still render rock highlights even if overlay is off
                renderRockHighlights(graphics);
                return null;
            }

            // Render the panel
            panelComponent.setPreferredSize(new Dimension(250, 300));

            panelComponent.getChildren().add(TitleComponent.builder()
                .text("Human Miner")
                .color(Color.CYAN)
                .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            // Mining info
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Rock Type:")
                .right(config.rockType().getName())
                .build());

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Ores Mined:")
                .right(String.valueOf(script.getOresMined()))
                .build());

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Ores/Hour:")
                .right(String.valueOf(script.getOresPerHour()))
                .build());

            // Runtime
            Duration runtime = script.getRuntime();
            String runtimeStr = String.format("%02d:%02d:%02d",
                runtime.toHours(),
                runtime.toMinutesPart(),
                runtime.toSecondsPart());

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Runtime:")
                .right(runtimeStr)
                .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            // Status
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(Microbot.status)
                .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            // Antiban info (if enabled)
            if (config.useAntiban()) {
                Rs2Antiban.renderAntibanOverlayComponents(panelComponent);
            }

            // Render rock highlights
            renderRockHighlights(graphics);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return super.render(graphics);
    }

    private void renderRockHighlights(Graphics2D graphics) {
        if (!config.highlightRocks()) {
            return;
        }

        try {
            Rocks selectedRock = config.rockType();
            int[] rockIds = selectedRock.getObjectIds();

            // Find all rocks of the selected type
            for (int rockId : rockIds) {
                GameObject rock = Rs2GameObject.findObjectById(rockId);

                if (rock != null) {
                    drawRockHighlight(graphics, rock);
                }
            }
        } catch (Exception ex) {
            // Silently ignore rendering errors
        }
    }

    private void drawRockHighlight(Graphics2D graphics, GameObject rock) {
        try {
            LocalPoint localPoint = rock.getLocalLocation();
            if (localPoint == null) {
                return;
            }

            Polygon polygon = Perspective.getCanvasTilePoly(Microbot.getClient(), localPoint);
            if (polygon == null) {
                return;
            }

            // Draw filled polygon
            Color fillColor = config.highlightColor();
            graphics.setColor(fillColor);
            graphics.fillPolygon(polygon);

            // Draw outline
            graphics.setColor(new Color(
                fillColor.getRed(),
                fillColor.getGreen(),
                fillColor.getBlue(),
                255
            ));
            graphics.setStroke(new BasicStroke(2));
            graphics.drawPolygon(polygon);

            // Draw rock name
            Point textPoint = Perspective.getCanvasTextLocation(
                Microbot.getClient(),
                graphics,
                localPoint,
                config.rockType().getGameName(),
                0
            );

            if (textPoint != null) {
                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font("Arial", Font.BOLD, 12));
                graphics.drawString(config.rockType().getGameName(), textPoint.getX(), textPoint.getY());
            }
        } catch (Exception ex) {
            // Silently ignore individual rock rendering errors
        }
    }
}
