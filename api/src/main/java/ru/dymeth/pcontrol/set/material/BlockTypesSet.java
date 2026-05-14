package ru.dymeth.pcontrol.set.material;

import org.bukkit.Material;
import ru.dymeth.pcontrol.set.CustomEnumSet;
import ru.dymeth.pcontrol.set.CustomSet;
import ru.dymeth.pcontrol.set.KeyedEnumSet;
import ru.dymeth.pcontrol.util.MaterialUtils;
import ru.dymeth.pcontrol.util.PCMaterial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class BlockTypesSet extends KeyedEnumSet<Material, PCMaterial> {

    @Nonnull
    public static Set<Material> createPrimitive(boolean allowAir,
                                                @Nonnull String setName,
                                                @Nonnull Logger logger,
                                                @Nonnull Consumer<BlockTypesSet> consumer
    ) {
        return create(allowAir, setName, logger, consumer)
            .stream()
            .map(PCMaterial::getType)
            .collect(Collectors.toSet());
    }

    @Nonnull
    public static Set<PCMaterial> create(boolean allowAir,
                                         @Nonnull String setName,
                                         @Nonnull Logger logger,
                                         @Nonnull Consumer<BlockTypesSet> consumer
    ) {
        BlockTypesSet result = new BlockTypesSet(allowAir);
        try {
            consumer.accept(result);
        } catch (NoSuchFieldError e) {
            String materialName = e.getMessage();
            logger.warning("Unable to fill set " + setName + ". " +
                "Block " + materialName + " not found. Plugin may not work correctly");
        } catch (NullPointerException e) {
            String tagName = e.getMessage();
            if (tagName != null && tagName.startsWith("Cannot invoke \"org.bukkit.Tag.getValues()\" because \"org.bukkit.Tag.")) {
                tagName = tagName.substring(
                    "Cannot invoke \"org.bukkit.Tag.getValues()\" because \"org.bukkit.Tag.".length(),
                    tagName.length() - "\" is null".length()
                );
            } else {
                tagName = null;
            }
            if (tagName == null) {
                logger.log(Level.WARNING, "Unable to fill set " + setName + ". " +
                    "Unknown error occurred. Plugin may not work correctly", e);
            } else {
                logger.warning("Unable to fill set " + setName + ". " +
                    "Tag " + tagName + " not found. Plugin may not work correctly");
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Unable to fill set " + setName + ". " +
                "Unknown error occurred. Plugin may not work correctly", t);
        }
        return Collections.unmodifiableSet(result.getValues());
    }

    private final boolean allowAir;

    private BlockTypesSet(boolean allowAir) {
        super(Material.class);
        this.allowAir = allowAir;
    }

    @Nonnull
    @Override
    public CustomEnumSet<Material, PCMaterial> add(@Nonnull Predicate<Material> filter) {
        return super.add(material -> MaterialUtils.isBlockMaterial(material, this.allowAir) && filter.test(material));
    }

    @Nonnull
    public CustomSet<PCMaterial> add(@Nonnull Collection<PCMaterial> elements) {
        for (PCMaterial element : elements) {
            if (element.isBlockMaterial(this.allowAir)) continue;
            throw new IllegalArgumentException("Unable to add non-block material to blocks set (" + element + ")");
        }
        return super.add(elements);
    }

    @Nonnull
    @Override
    public BlockTypesSet add(@Nonnull String... elementNames) {
        this.add(MaterialUtils.getBlockMaterials(this.allowAir, null, elementNames));
        return this;
    }

    @Nonnull
    @Override
    public PCMaterial enumToElement(@Nonnull Material enumValue, @Nullable String elementName) {
        if (!MaterialUtils.isBlockMaterial(enumValue, this.allowAir)) {
            throw new IllegalArgumentException("Unable to add non-block material to blocks set (" + enumValue + ")");
        }
        return elementName == null ? new PCMaterial(enumValue) : PCMaterial.valueOf(elementName);
    }
}
