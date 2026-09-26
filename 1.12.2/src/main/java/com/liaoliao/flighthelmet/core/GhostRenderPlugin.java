package com.liaoliao.flighthelmet.core;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("PigThingsGhostTerrain")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("com.liaoliao.flighthelmet.core.")
public final class GhostRenderPlugin implements IFMLLoadingPlugin {
    @Override public String[] getASMTransformerClass() { return new String[]{GhostRenderTransformer.class.getName()}; }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String, Object> data) { }
    @Override public String getAccessTransformerClass() { return null; }
}
