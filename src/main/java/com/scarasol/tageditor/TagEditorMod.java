package com.scarasol.tageditor;

import com.mojang.logging.LogUtils;
import com.scarasol.tageditor.configuration.CommonConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.scarasol.tageditor.network.NetworkHandler;


/**
 * @author Scarasol
 */
@Mod(TagEditorMod.MODID)
public class TagEditorMod
{

    public static final String MODID = "tag_editor";

    public static final Logger LOGGER = LogUtils.getLogger();
    public TagEditorMod()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "tag-editor-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.addNetworkMessage();
    }


}
