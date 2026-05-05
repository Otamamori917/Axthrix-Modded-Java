package axthrix.world.types.unittypes;

import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import axthrix.content.AxFactions;
import axthrix.world.types.ai.WildAi;
import axthrix.world.types.block.Egg;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;

import java.util.HashMap;

public class IkatusaUnitType extends AxUnitType {

    // Lifecycle
    @Nullable public UnitType nextStage;
    @Nullable public UnitType maleStage;
    @Nullable public UnitType femaleStage;
    public boolean finalStage = false;
    public float growthTime = 600f, maturityTime = 300f, cooldown = 100f;
    public boolean digs = false, asexual = false, checkFloors = true;

    // Reproduction
    @Nullable public Block egg = null;
    public int spawnAmount = 1;
    public int spawnAmountRand = 1;
    public float spread = 50f;
    public boolean faceOutwards = true;

    @Nullable public UnitType oppositeGender = null;
    public boolean ismale = false;
    public float genderChance = 0.5f;

    // Needs / AI features
    public float wanderRadius = 160f;
    public float foodSeekRange = 240f;
    public Seq<Block> foodBlocks = new Seq<>();
    public boolean huntsUnits = false;
    public Seq<UnitType> preyTypes = new Seq<>();

    // Per-unit state
    public final HashMap<Unit, Float> tick = new HashMap<>();
    public final HashMap<Unit, Float> broodingCooldown = new HashMap<>();
    public final HashMap<Unit, Boolean> brooding = new HashMap<>();

    public IkatusaUnitType(String name) {
        super(name);
        factions.add(AxFactions.ikatusa);
        controller = u -> new WildAi();
    }

    @Override
    public void update(Unit unit) {
        super.update(unit);

        tick.putIfAbsent(unit, 0f);
        broodingCooldown.putIfAbsent(unit, cooldown / 2f);
        if (!asexual) brooding.putIfAbsent(unit, false);

        float t = tick.get(unit);

        // Growth
        if (t >= growthTime) {
            if (!finalStage) {
                Unit newUnit;
                if (nextStage != null) {
                    newUnit = nextStage.create(unit.team);
                } else if (asexual) {
                    newUnit = this.create(unit.team);
                } else {
                    newUnit = Mathf.random() < genderChance ?
                            maleStage.create(unit.team) : femaleStage.create(unit.team);
                }
                newUnit.set(unit.x, unit.y);
                newUnit.rotation = unit.rotation();
                if (!Vars.net.client()) newUnit.add();
            } else {
                unit.kill();
            }
            cleanup(unit);
            return;
        }

        // Brooding / Egg laying
        if (t >= maturityTime && egg != null) {
            if (broodingCooldown.get(unit) >= cooldown) {
                if (asexual) {
                    EggCall(unit);
                } else {
                    // Look for opposite gender nearby
                    Groups.unit.each(other -> {
                        if (other.type instanceof IkatusaUnitType iku &&
                                iku.ismale != this.ismale &&
                                unit.within(other, unit.hitSize * 4f)) {
                            EggCall(unit);
                        }
                    });
                }
            }
            broodingCooldown.put(unit, broodingCooldown.get(unit) + 1f);
        }

        tick.put(unit, t + 1f);
    }

    public void EggCall(Unit unit) {
        if (!(egg instanceof Egg eg)) return;

        if (eg.getValidation(eg.attributes, unit.tileX() * 8, unit.tileY() * 8)) {
            Tmp.v1.rnd(Mathf.random(spread));
            for (int i = 0; i < (Mathf.random(0, spawnAmountRand) + spawnAmount); i++) {
                Tile tile = Vars.world.tile((int)(unit.x + Tmp.v1.x) / 8, (int)(unit.y + Tmp.v1.y) / 8);
                if (tile != null) {
                    tile.setNet(egg, unit.team, faceOutwards ? (int)Tmp.v1.angle()/90 : (int)(unit.rotation + Mathf.range(5f))/90);
                }
            }
            broodingCooldown.put(unit, 0f);
        }
    }

    private void cleanup(Unit unit) {
        tick.remove(unit);
        broodingCooldown.remove(unit);
        brooding.remove(unit);
        unit.remove();
    }

    public static boolean onWater(Unit unit) {
        return unit.floorOn().isLiquid;
    }

    public boolean onDeepWater(Unit unit) {
        return onWater(unit) && unit.floorOn().drownTime > 0;
    }
}