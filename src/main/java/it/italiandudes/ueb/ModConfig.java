package it.italiandudes.ueb;

import net.minecraftforge.common.config.Config;

@Config(modid = UpgradableEnchantedBooks.MODID, name = UpgradableEnchantedBooks.NAME)
public final class ModConfig {

    @Config.Comment("Specify the cost multiplier of the upgrade. Lower values means lower upgrade cost. 0 to cancel costs. [DEFAULT: 1]")
    public static int costMultiplier = 1;
}
