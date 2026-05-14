package ru.dymeth.pcontrol.versionsadapter;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.dymeth.pcontrol.VersionsAdapter;
import ru.dymeth.pcontrol.data.PControlData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class VersionsAdapterImpl implements VersionsAdapter {

    private final Map<Material, Boolean> underwaterMaterials;

    public VersionsAdapterImpl(@Nonnull PControlData data) {
        this.underwaterMaterials = new HashMap<>();

        for (Material material : data.getCustomTags().getTag("blocks_under_water_only", Material.class)) {
            this.underwaterMaterials.put(material, false);
        }

        for (Material material : Material.values()) {
            if (!material.isBlock()) continue;
            if (!(material.createBlockData() instanceof Waterlogged)) continue;
            this.underwaterMaterials.put(material, true);
        }
    }

    @Nonnull
    @Override
    public Material getFallingBlockMaterial(@Nonnull FallingBlock fallingBlock) {
        return fallingBlock.getBlockData().getMaterial();
    }

    @Override
    public boolean isBoneMealItem(@Nonnull ItemStack stack) {
        return stack.getType() == Material.BONE_MEAL;
    }

    @Override
    public boolean isBlockContainsWater(@Nonnull Block block) {
        Boolean waterlogged = this.underwaterMaterials.get(block.getType());
        if (waterlogged == null) return false;
        return !waterlogged || ((Waterlogged) block.getBlockData()).isWaterlogged();
    }

    @Override
    public boolean isFacingAt(@Nonnull Block block, @Nonnull BlockFace face) {
        BlockData data = block.getBlockData();
        return !(data instanceof Directional) || ((Directional) data).getFacing() == face;
    }

    @Override
    public void setItemMetaGlowing(@Nonnull ItemMeta meta) {
        meta.setEnchantmentGlintOverride(true);
    }
}
