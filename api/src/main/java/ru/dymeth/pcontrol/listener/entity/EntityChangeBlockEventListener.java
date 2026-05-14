package ru.dymeth.pcontrol.listener.entity;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import ru.dymeth.pcontrol.PhysicsListener;
import ru.dymeth.pcontrol.data.PControlData;
import ru.dymeth.pcontrol.data.trigger.EventsListenerParser;
import ru.dymeth.pcontrol.data.trigger.PControlTrigger;
import ru.dymeth.pcontrol.rules.pair.EntityMaterialRules;
import ru.dymeth.pcontrol.rules.pair.MaterialMaterialRules;
import ru.dymeth.pcontrol.rules.single.EntityRules;
import ru.dymeth.pcontrol.rules.single.MaterialRules;

import javax.annotation.Nonnull;

public class EntityChangeBlockEventListener extends PhysicsListener {

    private final MaterialMaterialRules rulesEntityChangeBlockEventFromTo = new MaterialMaterialRules(
        this.data, EntityChangeBlockEvent.class, "from", "to");
    private final MaterialRules rulesEntityChangeBlockEventTo = new MaterialRules(
        this.data, EntityChangeBlockEvent.class, "to");
    private final MaterialRules rulesFallingEntityChangeBlockEventBy = new MaterialRules(
        this.data, EntityChangeBlockEvent.class, "falling-by");
    private final MaterialRules rulesFallingEntityChangeBlockEventFrom = new MaterialRules(
        this.data, EntityChangeBlockEvent.class, "falling-from");
    private final EntityMaterialRules rulesNonFallingEntityChangeBlockEventByFrom = new EntityMaterialRules(
        this.data, EntityChangeBlockEvent.class, "non-falling-by", "non-falling-from");
    private final EntityRules rulesNonFallingEntityChangeBlockEventBy = new EntityRules(
        this.data, EntityChangeBlockEvent.class, "non-falling-by");

    public EntityChangeBlockEventListener(@Nonnull PControlData data, @Nonnull EventsListenerParser parser) {
        super(data);
        parser.registerParser(this.rulesEntityChangeBlockEventFromTo);
        parser.registerParser(this.rulesEntityChangeBlockEventTo);
        parser.registerParser(this.rulesFallingEntityChangeBlockEventBy);
        parser.registerParser(this.rulesFallingEntityChangeBlockEventFrom);
        parser.registerParser(this.rulesNonFallingEntityChangeBlockEventByFrom);
        parser.registerParser(this.rulesNonFallingEntityChangeBlockEventBy);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void on(EntityChangeBlockEvent event) {
        Material from = event.getBlock().getType();
        Material to = event.getTo();

        PControlTrigger trigger = this.rulesEntityChangeBlockEventFromTo.findTrigger(from, to);
        if (trigger == null) trigger = this.rulesEntityChangeBlockEventTo.findTrigger(to);

        if (trigger == null) {
            if (event.getEntity() instanceof FallingBlock) {
                Material by = this.versionsAdapter.getFallingBlockMaterial((FallingBlock) event.getEntity());

                trigger = this.rulesFallingEntityChangeBlockEventBy.findTrigger(from);
                if (trigger == null) trigger = this.rulesFallingEntityChangeBlockEventFrom.findTrigger(by);

                if (trigger == null) {
                    this.unrecognizedAction(event, event.getBlock().getLocation(), from + " > " + to + " (by falling " + by + ")");
                    return;
                }
            } else {
                EntityType by = event.getEntity().getType();

                trigger = this.rulesNonFallingEntityChangeBlockEventByFrom.findTrigger(by, from);
                if (trigger == null) trigger = this.rulesNonFallingEntityChangeBlockEventBy.findTrigger(by);

                if (trigger == null) {
                    this.unrecognizedAction(event, event.getBlock().getLocation(), from + " > " + to + " (by " + by + ")");
                }
            }
        }

        if (trigger != null) {
            this.data.cancelIfDisabled(event, trigger);
        }
    }
}
