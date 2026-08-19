package axthrix.world.types.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.world.util.draw.AxDrawf;
import axthrix.world.util.net.AxPayloadCraft;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.net.Packet;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.*;

public class PayloadCrafterAbility extends Ability {
    public Item itemType;
    public float ticksPerCost = 2f;
    public boolean drawPowerLines = true;
    public float powerLineDetectionRange = 120f;

    public ObjectMap<UnlockableContent, CraftOption> craftOptions = new ObjectMap<>();

    public static ObjectMap<Integer, UnlockableContent> selectedCraft = new ObjectMap<>();
    public static ObjectMap<Integer, Float> constructionProgress = new ObjectMap<>();
    public static ObjectMap<Integer, UnlockableContent> constructingContent = new ObjectMap<>();

    protected transient boolean showCraftOptions = false;
    public static ObjectMap<Integer, UnlockableContent> lastCraft = new ObjectMap<>();

    public PayloadCrafterAbility(){
        AxPayloadCraft.register();
    }

    public void addBlock(Block block, int cost, float  poweredSpeedBonus){
        craftOptions.put(block, new CraftOption(cost, false, poweredSpeedBonus));
    }

    public void addBlockpwr(Block block, int cost){
        craftOptions.put(block, new CraftOption(cost, true, 1));
    }

    public void addUnit(UnitType unit, int cost, float poweredSpeedBonus){
        craftOptions.put(unit, new CraftOption(cost, false, poweredSpeedBonus));
    }

    public void addUnitpwr(UnitType unit, int cost){
        craftOptions.put(unit, new CraftOption(cost, true, 1));
    }

    public UnlockableContent getPreviewCraft(Unit unit) {
        UnlockableContent on = getSelectedCraft(unit);
        if (on != null) return on;
        UnlockableContent last = lastCraft.get(unit.id);
        if (last != null && craftOptions.containsKey(last) && isUnlocked(last)) return last;
        Seq<UnlockableContent> sorted = getSortedCraftOptions();
        return sorted.isEmpty() ? null : sorted.first();
    }

    public void cycleCraft(Unit unit) {
        if (unit == null) return;
        if (getSelectedCraft(unit) != null) return;
        Seq<UnlockableContent> sorted = getSortedCraftOptions();
        if (sorted.isEmpty()) return;

        UnlockableContent cur = lastCraft.get(unit.id);
        int idx = cur == null ? -1 : sorted.indexOf(cur);
        if (idx < 0) idx = 0;
        else idx = (idx + 1) % sorted.size;

        lastCraft.put(unit.id, sorted.get(idx));
    }

    public void toggleCrafting(Unit unit) {
        if (unit == null) return;

        UnlockableContent cur = getSelectedCraft(unit);
        if (cur != null) {
            lastCraft.put(unit.id, cur);
            setSelectedCraft(unit, null);
            return;
        }

        UnlockableContent restore = lastCraft.get(unit.id);
        if (restore == null || !craftOptions.containsKey(restore) || !isUnlocked(restore)) {
            Seq<UnlockableContent> sorted = getSortedCraftOptions();
            restore = sorted.isEmpty() ? null : sorted.first();
        }
        if (restore != null) lastCraft.put(unit.id, restore);
        setSelectedCraft(unit, restore);
    }

    protected boolean isUnlocked(UnlockableContent content){
        if(!Vars.state.isCampaign()) return true;
        return content.unlocked();
    }

    protected Seq<UnlockableContent> getSortedCraftOptions(){
        Seq<UnlockableContent> sorted = new Seq<>();

        Seq<UnlockableContent> blocks = new Seq<>();
        for(var entry : craftOptions){
            if(entry.key instanceof Block){
                blocks.add(entry.key);
            }
        }
        blocks.sort(c -> craftOptions.get(c).cost);
        sorted.addAll(blocks);

        Seq<UnlockableContent> units = new Seq<>();
        for(var entry : craftOptions){
            if(entry.key instanceof UnitType){
                units.add(entry.key);
            }
        }
        units.sort(c -> craftOptions.get(c).cost);
        sorted.addAll(units);

        return sorted;
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-payload-crafter");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]"+ Core.bundle.format("stat.aj-powerline") +" [white]" + (int)(powerLineDetectionRange / 8f) +" "+ StatUnit.blocks.localized());
        t.row();

        if(craftOptions.size > 0){
            Table optionsContainer = new Table();

            t.button(b -> b.add(Core.bundle.format("stat.aj-show-options",craftOptions.size+1)), Styles.flatt, () -> {
                showCraftOptions = !showCraftOptions;
                rebuildCraftOptions(optionsContainer);
            }).growX().height(40f);
            t.row();

            t.add(optionsContainer).growX();
            t.row();

            rebuildCraftOptions(optionsContainer);
        }
    }

    protected void rebuildCraftOptions(Table container){
        container.clearChildren();

        if(!showCraftOptions) return;

        Seq<UnlockableContent> sorted = getSortedCraftOptions();

        for(UnlockableContent content : sorted){
            CraftOption option = craftOptions.get(content);
            float constructionTime = option.cost * ticksPerCost / 60f;

            boolean unlocked = isUnlocked(content);

            container.table(bt -> {
                if(unlocked){
                    bt.image(content.uiIcon).size(32f).padRight(8f);
                }else{
                    bt.image(Icon.cancel).size(32f).color(Color.red).padRight(8f);
                }

                bt.table(info -> {
                    if(unlocked){
                        info.add(content.localizedName).row();
                        info.table(row -> {
                            row.add("[lightgray]"+ Core.bundle.format("stat.aj-cost"));
                            row.add(StatValues.displayItem(itemType, option.cost, true)).padLeft(4f);
                        }).left();
                        info.row();
                        info.add("[lightgray]"+ Core.bundle.format("stat.aj-time") +" [white]" + Strings.autoFixed(constructionTime, 1) +" "+ StatUnit.seconds.localized());
                        if(option.requirePower){
                            info.row();
                            info.add("[stat]⚡ [lightgray]"+ Core.bundle.format("stat.aj-power"));
                        }else if (option.poweredSpeedBonus != 1){
                            info.add("[lightgray]" + Core.bundle.format("stat.aj-powered-bonus",
                                    Strings.autoFixed(option.poweredSpeedBonus, 2) + "x")).row();
                        }
                    }else{
                        info.add("[red]???").row();
                        info.add("[lightgray]"+ Core.bundle.format("stat.aj-locked"));
                    }
                }).center().pad(4f).growX();

                bt.button("?", Styles.flatBordert, () -> ui.content.show(content)).right().growY().visible(() -> unlocked && content.unlockedNow()).size(40f);
            }).left();
            container.row();
        }
    }

    @Override
    public void init(UnitType type){
        Events.on(EventType.UnitDestroyEvent.class, event -> {
            constructionProgress.remove(event.unit.id);
            constructingContent.remove(event.unit.id);
        });
    }

    protected boolean hasPayloadSpace(Payloadc p, UnlockableContent content){
        float maxCapacity = ((Unit)p).type.payloadCapacity;
        float currentUsage = 0f;
        Seq<Payload> payloads = p.payloads();

        for(int i = 0; i < payloads.size; i++){
            Payload payload = payloads.get(i);
            if(payload instanceof BuildPayload bp){
                currentUsage += bp.block().size * bp.block().size * tilePayload;
            }else if(payload instanceof UnitPayload up){
                currentUsage += up.unit.hitSize * up.unit.hitSize;
            }
        }

        float neededSpace;
        if(content instanceof Block block){
            neededSpace = block.size * block.size * tilePayload;
        }else if(content instanceof UnitType unitType){
            neededSpace = unitType.hitSize * unitType.hitSize;
        }else{
            return false;
        }

        return (currentUsage + neededSpace) <= maxCapacity;
    }

    public PowerNode.PowerNodeBuild getNearestPowerNode(Unit unit){
        final PowerNode.PowerNodeBuild[] nearest = {null};
        final float[] closestDist = {Float.MAX_VALUE};

        Units.nearbyBuildings(unit.x, unit.y, powerLineDetectionRange, b -> {
            if(b.team == unit.team && b.block instanceof PowerNode){
                if(b.power != null && b.power.graph.getLastPowerProduced() > 0.0f){
                    if(b instanceof PowerNode.PowerNodeBuild node){
                        float dst = unit.dst(b);
                        if(dst < closestDist[0]){
                            nearest[0] = node;
                            closestDist[0] = dst;
                        }
                    }
                }
            }
        });

        return nearest[0];
    }

    public boolean isNotPowered(Unit unit){
        return getNearestPowerNode(unit) == null;
    }

    public boolean canCraft(Unit unit, UnlockableContent content){
        if(!craftOptions.containsKey(content)) return false;
        if(!isUnlocked(content)) return false;

        CraftOption option = craftOptions.get(content);

        if(unit.stack.amount < option.cost || unit.stack.item != itemType) return false;
        if(option.requirePower && isNotPowered(unit)) return false;

        if(unit instanceof Payloadc p){
            return hasPayloadSpace(p, content);
        }else{
            return false;
        }
    }

    public UnlockableContent getSelectedCraft(Unit unit){
        return selectedCraft.get(unit.id);
    }

    public void setSelectedCraft(Unit unit, UnlockableContent content) {
        if (unit == null) return;

        if (net.client()) {
            AxPayloadCraft.setSelected(unit.id, content == null ? -1 : content.id);
            applySelectedCraftLocal(unit, content);
            return;
        }

        applySelectedCraftLocal(unit, content);
    }

    private void applySelectedCraftLocal(Unit unit, UnlockableContent content) {
        UnlockableContent previous = selectedCraft.get(unit.id);
        if (previous != content) {
            constructionProgress.remove(unit.id);
            constructingContent.remove(unit.id);
        }
        if (content == null) {
            selectedCraft.remove(unit.id);
        } else {
            selectedCraft.put(unit.id, content);
        }
    }

    @Override
    public void update(Unit unit) {
        if ((player != null && player.unit() == unit) && !net.client()) {
            if (AxthrixLoader.toggleTapped()) toggleCrafting(unit);
            if (AxthrixLoader.activateTapped()) cycleCraft(unit);
        } else if ((player != null && player.unit() == unit) && net.client()) {
            if (AxthrixLoader.toggleTapped()) toggleCrafting(unit);
            if (AxthrixLoader.activateTapped()) cycleCraft(unit);
        }

        UnlockableContent selected = getSelectedCraft(unit);

        if (selected == null || !craftOptions.containsKey(selected) || !isUnlocked(selected)) {
            constructionProgress.remove(unit.id);
            constructingContent.remove(unit.id);
            return;
        }

        CraftOption option = craftOptions.get(selected);
        float buildTime = option.cost * ticksPerCost;

        if (option.requirePower && isNotPowered(unit)) {
            constructionProgress.remove(unit.id);
            constructingContent.remove(unit.id);
            return;
        }

        if (unit.stack.amount < option.cost || unit.stack.item != itemType || !(unit instanceof Payloadc p)) {
            constructionProgress.remove(unit.id);
            constructingContent.remove(unit.id);
            return;
        }

        if (!hasPayloadSpace(p, selected)) {
            constructionProgress.remove(unit.id);
            constructingContent.remove(unit.id);
            return;
        }

        UnlockableContent currentlyConstructing = constructingContent.get(unit.id);
        if (currentlyConstructing != selected) {
            constructionProgress.put(unit.id, 0f);
            constructingContent.put(unit.id, selected);
        }

        float speed = 1f;
        if (!option.requirePower && !isNotPowered(unit)) {
            speed = option.poweredSpeedBonus;
        }

        float progress = constructionProgress.get(unit.id, 0f);
        progress += arc.util.Time.delta * speed;
        constructionProgress.put(unit.id, progress);

        if (progress < buildTime) return;

        if (!net.client()) {
            if (selected instanceof Block block) {
                p.addPayload(new BuildPayload(block, unit.team));
            } else if (selected instanceof UnitType unitType) {
                Unit spawned = unitType.create(unit.team);
                p.addPayload(new UnitPayload(spawned));
            }
            unit.stack.amount -= option.cost;
            mindustry.content.Fx.spawn.at(unit.x, unit.y);
        }

        constructionProgress.remove(unit.id);
        constructingContent.remove(unit.id);
    }

    @Override
    public void draw(Unit unit){
        if(drawPowerLines){
            PowerNode.PowerNodeBuild node = getNearestPowerNode(unit);
            if(node != null){
                if(node.block instanceof PowerNode nodeb) {
                    Draw.z(nodeb.powerLayer);
                    Draw.color(Tmp.c1.set(nodeb.laserColor1).lerp(nodeb.laserColor2, (1f - node.power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f)).a(Renderer.laserOpacity));
                    nodeb.drawLaser(unit.x,unit.y, node.x, node.y, 0, node.block.size);
                    Draw.reset();
                }
            }
        }

        UnlockableContent constructing = constructingContent.get(unit.id);

        if(constructing != null){
            float progress = constructionProgress.get(unit.id, 0f);
            CraftOption option = craftOptions.get(constructing);
            if(option == null) return;

            float buildTime = option.cost * ticksPerCost;

            Draw.draw(Layer.flyingUnitLow, () -> AxDrawf.materialize(
                    unit.x,
                    unit.y,
                    constructing.fullIcon,
                    unit.team.color,
                    constructing instanceof Block ? 90 : unit.rotation - 90,
                    0.05f,
                    progress / buildTime,
                    -arc.util.Time.time / 4f
            ));
        }
    }

    public void buildEmbeddedSelectionTable(Unit unit, Table targetRow) {
        targetRow.clearChildren();
        targetRow.margin(4f);

        boolean on = getSelectedCraft(unit) != null;
        String toggleTip = Core.bundle.format("ui.ax-info.craft-toggle-key", AxthrixLoader.bindToggle)
                + "\n" + Core.bundle.get(on ? "ui.ax-info.craft-on" : "ui.ax-info.craft-off");

        targetRow.button(on ? Icon.ok : Icon.cancel, Styles.clearNonei, 32f, () -> toggleCrafting(unit))
                .tooltip(toggleTip)
                .padRight(6f);

        Seq<UnlockableContent> sorted = getSortedCraftOptions();
        UnlockableContent preview = getPreviewCraft(unit);

        for (UnlockableContent content : sorted) {
            CraftOption option = craftOptions.get(content);
            float constructionTime = option.cost * ticksPerCost / 60f;
            boolean unlocked = isUnlocked(content);

            if (!unlocked) {
                targetRow.button(Icon.cancel, Styles.clearNonei, 32f, () -> {})
                        .disabled(true)
                        .tooltip("[red]" + Core.bundle.format("stat.aj-locked") + " - "
                                + Core.bundle.format("stat.aj-missing-research"))
                        .padRight(4f);
                continue;
            }

            boolean canCraftNow = canCraft(unit, content);
            boolean marked = preview == content; // shows selection even when off

            var btn = targetRow.button(
                    new arc.scene.style.TextureRegionDrawable(content.uiIcon),
                    Styles.clearNoneTogglei, 32f,
                    () -> {
                        lastCraft.put(unit.id, content);
                        if (getSelectedCraft(unit) != null) {
                            setSelectedCraft(unit, content); // only switch while on
                        }
                    }
            ).tooltip(
                    content.localizedName
                            + "\n[lightgray]" + Core.bundle.format("stat.aj-cost") + " [white]" + option.cost
                            + "\n[lightgray]" + Core.bundle.format("stat.aj-time") + " [white]"
                            + Strings.autoFixed(constructionTime, 1) + " " + StatUnit.seconds.localized()
                            + (option.requirePower
                            ? "\n[stat]⚡ [lightgray]" + Core.bundle.format("stat.aj-power")
                            : option.poweredSpeedBonus != 1f
                            ? "\n[accent]" + Strings.autoFixed(option.poweredSpeedBonus, 2) + "x"
                            : "")
            ).checked(b -> getPreviewCraft(unit) == content).padRight(4f).get();

            if (AxthrixLoader.showAdvanced) {
                btn.setColor(canCraftNow ? Color.white : Color.valueOf("8B4444"));
            }
        }
    }

    public void applySelectedFromServer(Unit unit, UnlockableContent content) {
        applySelectedCraftLocal(unit, content);
    }

    public static class CraftOption {
        public int cost;
        public boolean requirePower;
        public float poweredSpeedBonus;

        public CraftOption(int cost, boolean requirePower,float bonus){
            this.cost = cost;
            this.requirePower = requirePower;
            this.poweredSpeedBonus = bonus;
        }
    }
}
