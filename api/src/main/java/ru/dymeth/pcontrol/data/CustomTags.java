package ru.dymeth.pcontrol.data;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.dymeth.pcontrol.set.material.BlockTypesSet;
import ru.dymeth.pcontrol.util.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class CustomTags {

    private final PControlData data;
    private final Map<String, Set<Material>> materialTagsByName = new HashMap<>();

    public CustomTags(@Nonnull PControlData data) {
        this.data = data;
    }

    public void parseTags() {
        this.materialTagsByName.clear();

        this.initSpecificTags();

        File configFile = FileUtils.createConfigFileIfNotExist(this.data.getPlugin(),
            "logics/tags.yml", "logics/tags.yml");
        YamlConfiguration rootSection = YamlConfiguration.loadConfiguration(configFile);

        for (String tagName : rootSection.getKeys(false)) {
            this.registerMaterialTag(tagName, BlockTypesSet.createPrimitive(
                false,
                tagName,
                this.data.log(),
                this.data.getTypesSetsParser().createBlockTypesParser(rootSection.getStringList(tagName), true)
            ));
        }
    }

    private void initSpecificTags() {
        this.initBlocksSet("world_air", set -> {
            set.addPrimitive(Material.AIR);
            set.addPrimitive(Material.CAVE_AIR);
        });
        this.initBlocksSet("gravity_blocks", set -> {
            set.add(Material::hasGravity);
            set.addPrimitive(Material.SCAFFOLDING);
        });
    }

    private void initBlocksSet(@Nonnull String tagName, @Nonnull Consumer<BlockTypesSet> consumer) {
        this.registerMaterialTag(tagName, BlockTypesSet.createPrimitive(
            true,
            tagName,
            this.data.log(),
            consumer
        ));
    }

    private void registerMaterialTag(@Nonnull String tagName, @Nonnull Set<Material> materials) {
        tagName = tagName.toLowerCase();
        if (this.materialTagsByName.put(tagName, materials) != null) {
            throw new IllegalArgumentException("Duplicate tag: " + tagName);
        }
    }

    @Nonnull
    public <T> Set<T> getTag(@Nonnull String tagName, @Nonnull Class<T> clazz) {
        Set<T> result = this.getTagOrNull(tagName, clazz);
        if (result != null) return result;
        throw new IllegalArgumentException("Plugin tag not found: " + tagName);
    }

    @Nullable
    public <T> Set<T> getTagOrNull(@Nonnull String tagName, @Nonnull Class<T> clazz) {
        if (clazz == Material.class) {
            //noinspection unchecked
            return (Set<T>) this.materialTagsByName.get(tagName.toLowerCase());
        } else {
            throw new IllegalArgumentException("Unsupported tag type");
        }
    }
}
