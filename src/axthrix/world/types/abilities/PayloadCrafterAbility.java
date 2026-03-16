package axthrix.world.types.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.world.util.AxDrawf;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Payloadc;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
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

    protected static ObjectMap<Integer, UnlockableContent> selectedCraft = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> constructionProgress = new ObjectMap<>();
    protected static ObjectMap<Integer, UnlockableContent> constructingContent = new ObjectMap<>();

    protected transient Table selectionTable = null;
    protected transient int lastControlledUnitId = -1;
    protected transient boolean uiInitialized = false;
    protected transient boolean showCraftOptions = false;

    public PayloadCrafterAbility(){}

    public void addBlock(Block block, int cost, boolean requirePower){
        craftOptions.put(block, new CraftOption(cost, requirePower));
    }

    public void addUnit(UnitType unit, int cost, boolean requirePower){
        craftOptions.put(unit, new CraftOption(cost, requirePower));
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

        Events.on(EventType.ResearchEvent.class, event -> {
            if(headless || player == null || player.unit() == null) return;

            Unit controlled = player.unit();
            if(controlled.abilities != null){
                for(var ab : controlled.abilities){
                    if(ab instanceof PayloadCrafterAbility pca){
                        if(pca.selectionTable != null){
                            Core.app.post(() -> pca.buildSelectionTable(controlled));
                        }
                        break;
                    }
                }
            }
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

    protected PowerNode.PowerNodeBuild getNearestPowerNode(Unit unit){
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

    protected boolean isNotPowered(Unit unit){
        return getNearestPowerNode(unit) == null;
    }

    protected boolean canCraft(Unit unit, UnlockableContent content){
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

    public void setSelectedCraft(Unit unit, UnlockableContent content){
        UnlockableContent previousSelection = selectedCraft.get(unit.id);

        if(previousSelection != content){
            constructionProgress.remove(unit.id);
            constructingContent.remove(unit.id);
        }

        if(content == null){
            selectedCraft.remove(unit.id);
        }else{
            selectedCraft.put(unit.id, content);
        }

        if(headless) return;
        if(selectionTable != null && player != null && player.unit() == unit){
            Core.app.post(() -> buildSelectionTable(unit));
        }
    }

    @Override
    public void update(Unit unit){
        UnlockableContent selected = getSelectedCraft(unit);

        if(selected != null && craftOptions.containsKey(selected) && isUnlocked(selected)){
            CraftOption option = craftOptions.get(selected);
            float buildTime = option.cost * ticksPerCost;

            if(option.requirePower && isNotPowered(unit)){
                constructionProgress.remove(unit.id);
                constructingContent.remove(unit.id);
                return;
            }

            if(unit.stack.amount >= option.cost && unit.stack.item == itemType && unit instanceof Payloadc p){
                if(hasPayloadSpace(p, selected)){
                    UnlockableContent currentlyConstructing = constructingContent.get(unit.id);

                    if(currentlyConstructing != selected){
                        constructionProgress.put(unit.id, 0f);
                        constructingContent.put(unit.id, selected);
                    }

                    float progress = constructionProgress.get(unit.id, 0f);
                    progress += arc.util.Time.delta;
                    constructionProgress.put(unit.id, progress);

                    if(progress >= buildTime){
                        if(selected instanceof Block block){
                            BuildPayload payload = new BuildPayload(block, unit.team);
                            p.addPayload(payload);
                        }else if(selected instanceof UnitType unitType){
                            Unit spawned = unitType.create(unit.team);
                            UnitPayload payload = new UnitPayload(spawned);
                            p.addPayload(payload);
                        }

                        unit.stack.amount -= option.cost;
                        mindustry.content.Fx.spawn.at(unit.x, unit.y);

                        constructionProgress.remove(unit.id);
                        constructingContent.remove(unit.id);
                    }
                }else{
                    constructionProgress.remove(unit.id);
                    constructingContent.remove(unit.id);
                }
            }else{
                constructionProgress.remove(unit.id);
                constructingContent.remove(unit.id);
            }
        }else{
            constructionProgress.remove(unit.id);
            constructingContent.remove(unit.id);
        }

        if(headless) return;

        if(!uiInitialized){
            initializeUI();
            uiInitialized = true;
        }
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

    protected void initializeUI(){
        if(headless) return;

        Events.run(EventType.Trigger.draw, () -> {
            if(player == null || player.unit() == null){
                hideSelectionUI();
                return;
            }

            Unit controlled = player.unit();

            if(controlled.id != lastControlledUnitId){
                hideSelectionUI();
                lastControlledUnitId = controlled.id;

                boolean hasAbility = false;

                if(controlled.abilities != null){
                    for(var ab : controlled.abilities){
                        if(ab instanceof PayloadCrafterAbility pca && pca == this){
                            hasAbility = true;
                            break;
                        }
                    }
                }

                if(hasAbility){
                    showSelectionUI(controlled);
                }
            }
        });
    }

    protected void showSelectionUI(Unit unit){
        if(selectionTable != null) return;

        selectionTable = new Table(Tex.pane);
        buildSelectionTable(unit);

        ui.hudGroup.addChild(selectionTable);
        selectionTable.update(() -> {
            selectionTable.setPosition(
                    Core.graphics.getWidth() - selectionTable.getWidth() - 4f + AxthrixLoader.payloadMenuOffsetX,
                    Core.graphics.getHeight() / 2f - selectionTable.getHeight() / 2f + AxthrixLoader.payloadMenuOffsetY
            );

            if(player != null && player.unit() != null && AxthrixLoader.showPayloadCrafterIndicators){
                updateIndicators(player.unit());
            }
        });
    }

    protected void hideSelectionUI(){
        if(selectionTable != null){
            selectionTable.remove();
            selectionTable = null;
        }
    }

    protected void updateIndicators(Unit unit){
        if(selectionTable == null) return;

        int row = 1;
        Seq<UnlockableContent> sorted = getSortedCraftOptions();

        for(UnlockableContent content : sorted){
            boolean unlocked = isUnlocked(content);

            if(!unlocked){
                row++;
                continue;
            }

            boolean canCraftNow = canCraft(unit, content);

            try {
                var cells = selectionTable.getCells();
                if(row * 2 + 1 < cells.size){
                    var indicatorCell = cells.get(row * 2 + 1);
                    if(indicatorCell.get() instanceof arc.scene.ui.Image img){
                        img.setColor(canCraftNow ? Color.green : Color.red);
                    }
                }
            } catch(Exception ignored){}

            row++;
        }
    }

    protected void buildSelectionTable(Unit unit){
        if(selectionTable == null) return;

        selectionTable.clearChildren();
        selectionTable.background(Tex.pane);
        selectionTable.margin(4f);

        UnlockableContent selected = getSelectedCraft(unit);

        selectionTable.button(Icon.cancel, Styles.clearNoneTogglei, 40f, () -> setSelectedCraft(unit, null)).tooltip(Core.bundle.format("stat.aj-disable")).checked(b -> selected == null);

        if(AxthrixLoader.showPayloadCrafterIndicators){
            selectionTable.image().size(12f).color(Color.green).pad(2f);
        }
        selectionTable.row();

        Seq<UnlockableContent> sorted = getSortedCraftOptions();

        for(UnlockableContent content : sorted){
            CraftOption option = craftOptions.get(content);
            float constructionTime = option.cost * ticksPerCost / 60f;

            boolean unlocked = isUnlocked(content);

            if(!unlocked){
                selectionTable.button(Icon.cancel, Styles.clearNoneTogglei, 40f, () -> {})
                        .disabled(true)
                        .tooltip("[red]"+ Core.bundle.format("stat.aj-locked") +" - "+ Core.bundle.format("stat.aj-missing-research"));

                if(AxthrixLoader.showPayloadCrafterIndicators){
                    selectionTable.image().size(12f).color(Color.red).pad(2f);
                }
                selectionTable.row();
                continue;
            }

            boolean canCraftNow = canCraft(unit, content);

            selectionTable.button(
                    new arc.scene.style.TextureRegionDrawable(content.uiIcon),
                    Styles.clearNoneTogglei, 40f, () -> setSelectedCraft(unit, content)).tooltip(
                    content.localizedName +
                            "\n[lightgray]"+ Core.bundle.format("stat.aj-cost") +" [white]" + option.cost + " " +
                            "\n[lightgray]"+ Core.bundle.format("stat.aj-time") +" [white]" + Strings.autoFixed(constructionTime, 1) +" "+ StatUnit.seconds.localized() +
                            (option.requirePower ? "\n[stat]⚡ [lightgray]"+ Core.bundle.format("stat.aj-power") : "")
            ).checked(b -> selected == content);

            if(AxthrixLoader.showPayloadCrafterIndicators){
                selectionTable.image()
                        .size(12f)
                        .color(canCraftNow ? Color.green : Color.red)
                        .pad(2f);
            }

            selectionTable.row();
        }

        selectionTable.pack();
    }

    public static class CraftOption {
        public int cost;
        public boolean requirePower;

        public CraftOption(int cost, boolean requirePower){
            this.cost = cost;
            this.requirePower = requirePower;
        }
    }
}
