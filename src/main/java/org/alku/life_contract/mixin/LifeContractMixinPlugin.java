package org.alku.life_contract.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class LifeContractMixinPlugin implements IMixinConfigPlugin {
    private static final String XAERO_MIXIN = "org.alku.life_contract.mixin.XaeroMinimapRendererMixin";
    private static final String XAERO_RADAR_MIXIN = "org.alku.life_contract.mixin.XaeroRadarStateUpdaterMixin";
    private static final String ULTIMINE_MIXIN = "org.alku.life_contract.mixin.FTBUltimineMixin";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!XAERO_MIXIN.equals(mixinClassName)
                && !XAERO_RADAR_MIXIN.equals(mixinClassName)
                && !ULTIMINE_MIXIN.equals(mixinClassName)) {
            return true;
        }
        try {
            Class.forName(targetClassName, false, LifeContractMixinPlugin.class.getClassLoader());
            return true;
        } catch (LinkageError | ClassNotFoundException ignored) {
            return false;
        }
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
