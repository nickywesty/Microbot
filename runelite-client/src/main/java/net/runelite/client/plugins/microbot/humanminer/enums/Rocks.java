package net.runelite.client.plugins.microbot.humanminer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Rocks {
    CLAY("Clay", "Clay rocks", new int[]{9711, 9712, 9713, 15503, 15504, 15505}),
    COPPER("Copper", "Copper rocks", new int[]{10943, 10944, 10945, 11936, 11937, 11938, 11960, 11961, 11962}),
    TIN("Tin", "Tin rocks", new int[]{10080, 11933, 11934, 11935, 11957, 11958, 11959}),
    IRON("Iron", "Iron rocks", new int[]{11364, 11365, 36203, 36204, 37307, 37308, 37309}),
    SILVER("Silver", "Silver rocks", new int[]{11368, 11369, 37304, 37305}),
    COAL("Coal", "Coal rocks", new int[]{11366, 11367, 36201, 36202, 37310, 37311}),
    GOLD("Gold", "Gold rocks", new int[]{11370, 11371, 37306}),
    MITHRIL("Mithril", "Mithril rocks", new int[]{11372, 11373, 37312, 37313}),
    ADAMANTITE("Adamantite", "Adamantite rocks", new int[]{11374, 11375, 37314, 37315}),
    RUNITE("Runite", "Runite rocks", new int[]{11376, 11377, 37316, 37317}),
    AMETHYST("Amethyst", "Amethyst crystal", new int[]{11388});

    private final String name;
    private final String gameName;
    private final int[] objectIds;
}
