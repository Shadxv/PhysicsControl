package ru.dymeth.pcontrol.inventory;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.dymeth.pcontrol.PControlDataBukkit;
import ru.dymeth.pcontrol.data.category.PControlCategory;
import ru.dymeth.pcontrol.data.trigger.PControlTrigger;
import ru.dymeth.pcontrol.text.CommonColor;
import ru.dymeth.pcontrol.text.CommonDecoration;
import ru.dymeth.pcontrol.text.Text;
import ru.dymeth.pcontrol.text.TextHelper;
import ru.dymeth.pcontrol.util.PCMaterial;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PControlTriggerInventory extends PControlInventory {

    private static final boolean WARN_ON_SET_ICONS_FOR_UNAVAILABLE_TRIGGERS = false;
    private static final ItemStack DISALLOWED_TRIGGER = new ItemStack(Material.BARRIER);
    private static final ItemStack WRONG_ICON_TRIGGER = new ItemStack(Material.PAPER);
    private static final PCMaterial BACK_ITEM_MATERIAL = PCMaterial.valueOf("RED_WOOL");

    private final PControlDataBukkit data;
    private final Map<PControlTrigger, Short> slotByTrigger;

    public PControlTriggerInventory(@Nonnull PControlDataBukkit data, @Nonnull PControlCategory category, @Nonnull World world) {
        super(
            data,
            world,
            4,
            data.getMessage("inventory-title",
                "%category%", data.getCategoryName(category),
                "%world%", world.getName())
        );
        this.data = data;

        this.slotByTrigger = new HashMap<>();
        this.findTriggersSlots(category);

        this.setItem((short) (3 * 9 + 4), this.createBackStack(), player ->
            player.openInventory(new PControlCategoryInventory(data, world).getInventory()));
    }

    private void findTriggersSlots(@Nonnull PControlCategory category) {
        List<Short> slots = TriggersSlots.getSlots(category);
        List<PControlTrigger> triggers = category.getTriggers();
        for (int i = 0; i < triggers.size(); i++) {
            PControlTrigger trigger = triggers.get(i);
            short slot = slots.get(i);
            this.slotByTrigger.put(trigger, slot);
        }
    }

    @Nonnull
    private ItemStack createBackStack() {
        ItemStack back = BACK_ITEM_MATERIAL.createStack(1);
        ItemMeta meta = back.getItemMeta();
        if (meta == null) throw new IllegalStateException();
        this.data.getTextHelper().setStackName(meta, this.data.getMessage("select-another-category-item"));
        back.setItemMeta(meta);
        return back;
    }

    public void updateTriggerStack(@Nonnull PControlTrigger trigger) {
        if (trigger == this.data.getTriggersRegisty().getIgnoredState()) return;
        boolean available = trigger.isAvailable();
        boolean enabled = this.data.isActionAllowed(this.world, trigger);

        ItemStack icon = trigger.getIcon();
        if (available) {
            if (icon == null) {
                this.data.log().warning(
                    "Unable to find icon of trigger " + trigger);
                icon = WRONG_ICON_TRIGGER;
            }
        } else {
            if (icon != null && WARN_ON_SET_ICONS_FOR_UNAVAILABLE_TRIGGERS) {
                this.data.log().warning(
                    "Set icon for unavailable trigger " + trigger + ": " + icon.getType());
            }
            icon = DISALLOWED_TRIGGER;
        }
        icon = icon.clone();

        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Item meta could not be null");
        }

        TextHelper helper = this.data.getTextHelper();

        Text name;
        if (available) {
            name = helper.create(this.data.getTriggerName(trigger), (enabled ? CommonColor.GREEN : CommonColor.RED));
        } else {
            name = helper.create(this.data.getTriggerName(trigger), CommonColor.RED, CommonDecoration.STRIKETHROUGH);
        }
        helper.setStackName(meta, name);

        List<Text> lore = new ArrayList<>();
        if (available) {
            lore.add(this.data.getMessage(enabled
                ? "trigger-enabled-state"
                : "trigger-disabled-state"
            ));
            lore.addAll(this.data.getMessage(trigger.isRealtime()
                ? "trigger-realtime"
                : "trigger-on-update"
            ).split("\n"));
        } else {
            lore.add(this.data.getMessage("trigger-unsupported-state"));
        }
        helper.setStackLore(meta, lore);

        if (available && enabled != trigger.getDefaultValue()) {
            this.data.getVersionsAdapter().setItemMetaGlowing(meta);
        }
        meta.addItemFlags(ItemFlag.values());
        icon.setItemMeta(meta);
        short slot = this.slotByTrigger.get(trigger);
        this.setItem(slot, icon, player -> this.switchTrigger(player, trigger));
    }

    public void switchTrigger(@Nonnull CommandSender sender, @Nonnull PControlTrigger trigger) {
        if (trigger == this.data.getTriggersRegisty().getIgnoredState()) return;
        if (!trigger.isAvailable()) {
            return;
        }
        if (!sender.isOp()
            && !sender.hasPermission("physicscontrol.trigger.*")
            && !sender.hasPermission("physicscontrol.trigger." + trigger.name().toLowerCase())) {
            this.data.getMessage("bad-perms-trigger", "%trigger%", this.data.getTriggerName(trigger)).send(sender);
            return;
        }
        this.data.switchTrigger(this.world, trigger);
        this.updateTriggerStack(trigger);

        Text msg = this.data.getMessage(
            this.data.isActionAllowed(this.world, trigger) ? "trigger-enabled" : "trigger-disabled",
            "%player%", sender.getName(),
            "%trigger%", this.data.getTriggerName(trigger),
            "%world%", this.world.getName());

        this.data.announce(this.world, msg);
    }
}
