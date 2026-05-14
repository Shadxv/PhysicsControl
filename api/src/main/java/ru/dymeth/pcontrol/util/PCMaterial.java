package ru.dymeth.pcontrol.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PCMaterial {

    @Nullable
    public static PCMaterial getMaterial(@Nonnull String name) {
        Material result = Material.getMaterial(name);
        if (result == null) return null;
        return new PCMaterial(result);
    }

    @Nonnull
    public static PCMaterial valueOf(@Nonnull String name) {
        PCMaterial result = getMaterial(name);
        if (result == null) throw new IllegalArgumentException(name);
        return result;
    }

    private final Material material;
    private final int hashCode;

    public PCMaterial(@Nonnull Material material) {
        this.material = material;
        this.hashCode = this.material.ordinal();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(@Nonnull Object other) {
        if (other instanceof PCMaterial) return equals((PCMaterial) other);
        if (other instanceof Material) return equals((Material) other);
        if (other instanceof Block) return equals((Block) other);
        if (other instanceof ItemStack) return equals((ItemStack) other);
        return false;
    }

    public boolean equals(@Nonnull PCMaterial other) {
        return other.material == this.material;
    }

    public boolean equals(@Nonnull Material material) {
        return this.material == material;
    }

    public boolean equals(@Nonnull Block block) {
        return block.getType() == this.material;
    }

    public boolean equals(@Nonnull ItemStack stack) {
        return stack.getType() == this.material;
    }

    @Override
    public String toString() {
        return this.material.name();
    }

    @Nonnull
    public ItemStack createStack(int amount) {
        return new ItemStack(this.material, amount);
    }

    public boolean isItemMaterial(boolean allowAir) {
        return MaterialUtils.isItemMaterial(this.material, allowAir);
    }

    public boolean isBlockMaterial(boolean allowAir) {
        return MaterialUtils.isBlockMaterial(this.material, allowAir);
    }

    public boolean isValidMaterial(boolean allowAir) {
        return MaterialUtils.isValidMaterial(this.material, allowAir);
    }

    public boolean isAirMaterial() {
        return MaterialUtils.isAirMaterial(this.material);
    }

    public boolean isLegacyMaterial() {
        return MaterialUtils.isLegacyMaterial(this.material);
    }

    @Nonnull
    public Material getType() {
        return this.material;
    }
}
