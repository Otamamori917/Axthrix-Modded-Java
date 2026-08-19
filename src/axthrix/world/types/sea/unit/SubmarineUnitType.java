package axthrix.world.types.sea.unit;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.sea.managers.LayerManager;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.world.Tile;

public class SubmarineUnitType extends UnitType {
    public SubmarineUnitType(String name) {
        super(name);
        canBoost = false;

        playerControllable = true;
        logicControllable = true;
    }

    @Override
    public void init() {
        super.init();

        aiController = () -> new SubmarineAi();

        commands = arc.struct.Seq.with(
                UnitCommand.moveCommand,
                UnitCommand.rebuildCommand,
                UnitCommand.assistCommand,
                seaDiveCommand
        );
    }

    public static final UnitCommand seaDiveCommand = new UnitCommand("sea-dive", "down", u -> new DiveAI()) {{
        switchToMove = false;
        drawTarget = true;
        resetTarget = false;
    }};


    @Override
    public void update(Unit unit) {
        super.update(unit);


        boolean isSub = LayerManager.isSubmerged(unit);

        if (isSub && !unit.isPlayer()) {
            Tile currentTile = unit.tileOn();
            boolean forceSurface = false;

            if (!LayerManager.isDeep(currentTile)) {
                forceSurface = true;
            }
            else {
                Tmp.v1.trns(unit.rotation, 12f).add(unit.x, unit.y);
                Tile nextTile = Vars.world.tileWorld(Tmp.v1.x, Tmp.v1.y);

                if (nextTile != null && !LayerManager.isDeep(nextTile)) {
                    forceSurface = true;
                }
            }

            if (forceSurface) {
                handleDiveToggle(unit);
            }
        }

        if (unit.isPlayer() && AxthrixLoader.activateTapped()) {
            handleDiveToggle(unit);
        }
    }

    @Override
    public boolean targetable(Unit unit, Team from) {
        return unit.team == from || !LayerManager.isSubmerged(unit);
    }

    @Override
    public void applyColor(Unit unit) {
        float intensity = LayerManager.shaderIntensity;
        boolean isSub = LayerManager.isSubmerged(unit);

        if (isSub) {
            Color floorCol = Vars.world.floorWorld(unit.x, unit.y).mapColor;

            Tmp.c1.set(Color.white).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);

            Draw.color(Tmp.c1);

            Draw.mixcol(Tmp.c2.set(floorCol).mul(0.83f), 0.6f * intensity);
        } else {
            super.applyColor(unit);
        }
    }

    @Override
    public void draw(Unit unit) {
        float intensity = LayerManager.shaderIntensity;
        boolean playerSub = LayerManager.isPlayerSubmerged();
        boolean unitSub = LayerManager.isSubmerged(unit);

        if (unitSub && !playerSub) {
            float shadowWeight = 1f - intensity;
            float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
            float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

            Draw.z(Layer.groundUnit-1);
            Draw.color(Color.black);
            Draw.alpha(0.3f * shadowWeight);

            Draw.rect(unit.type.fullIcon, unit.x + ox, unit.y + oy, unit.rotation - 90);
            Draw.reset();

            if (shadowWeight > 0.9f) return;
        }

        super.draw(unit);
    }


    public void handleDiveToggle(Unit unit) {
        Tile tile = unit.tileOn();
        boolean submerged = LayerManager.submergedUnits.contains(unit);

        if (!submerged) {
            if (tile != null && tile.build instanceof AxBlock.AxBlockBuild b) {
                if (!((AxBlock) b.block).waterBlock) {
                    if (unit.isPlayer()) Vars.ui.showInfoToast("Cannot dive through solid ground!", 2f);
                    return;
                }
            }

            if (tile == null || (!tile.floor().name.contains("deep") && tile.floor() != Blocks.deepwater && tile.floor() != AxthrixEnvironment.tharaxianDeep)) {
                if (unit.isPlayer()) Vars.ui.showInfoToast("Water too shallow!", 2f);
                return;
            }
        }

        LayerManager.setSubmerged(unit, !submerged);
        Fx.bubble.at(unit.x, unit.y);
    }

    @Override
    public void display(Unit unit, Table table) {
        super.display(unit, table);
        if (LayerManager.submergedUnits.contains(unit)) {
            table.row();
            table.add(new Bar("SUBMERGED", Pal.accent, () -> 1f)).size(100f, 18f).pad(2);
        }
    }
}
