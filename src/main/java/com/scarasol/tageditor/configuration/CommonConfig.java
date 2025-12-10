package com.scarasol.tageditor.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * @author Scarasol
 */
public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> OPEN_EDIT;

    static {
        OPEN_EDIT = BUILDER.comment("""
                Whether to enable the tag editor box in the Creative Mode inventory.
                Only players with a permission level higher than 2 can make edits.""")
                .define("Enable Editor", true);
        SPEC = BUILDER.build();
    }
}
