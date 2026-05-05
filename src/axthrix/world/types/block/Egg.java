package axthrix.world.types.block;

import arc.Core;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.content.AxFactions;
import axthrix.world.util.AxStatValues;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.util.HashMap;

public class Egg extends AxBlock {
    public UnitType nextStage;
    public int spawnAmount = 1;
    public int spawnAmountRand = 1;
    public float spread = 8.0F;
    public boolean faceOutwards = true;
    public float growthTime = 600;
    public Seq<Block> attributes = new Seq<>();
    public HashMap<Building, Float> tick = new HashMap<>();

    /**
     * Probability (0–1) that an individual egg building goes bad and never hatches.
     * Default 0 — existing eggs are unaffected unless explicitly set.
     * Set to e.g. 0.30f for a 30% bad-egg chance (Jormungandr eggs).
     */
    public float badEggChance = 0f;

    /**
     * Tick at which the bad-egg roll is made.
     * -1 = auto-compute as 20% of growthTime on first update.
     */
    public float badEggCheckTime = -1f;

    public Egg(String name) {
        super(name);
        faction.add(AxFactions.ikatusa);
        update = true;
        floating = true;
        solid = false;
        placeableLiquid = true;
        accumulationResistanceHeat = 0.5f;
        accumulationResistanceCold = 1.1f;
        effectResistanceHeat = 0.8f;
        effectResistanceCold = 2.5f;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if (attributes.size > 0) {
            drawPlaceText(Core.bundle.format(getValidation(attributes, Core.input.mouseWorldX(), Core.input.mouseWorldY()) ? "text.validt" : "text.validf"), x, y, valid);
        }
    }

    @Override
    public void load() {
        super.load();
    }

    public void setStats() {
        super.setStats();
        stats.add(AxStats.tiles, AxStatValues.blocks(attributes, floating, true));
    }

    public class EggBuild extends AxBlockBuild {

        // ---- bad egg state ----
        boolean badChecked = false;
        boolean isBad = false;

        @Override
        public void updateTile() {
            super.updateTile();

            if (!tick.containsKey(this)) {
                tick.put(this, 0f);
            }

            // ---- bad-egg roll (only when badEggChance > 0) ----
            if (badEggChance > 0f) {
                float checkAt = badEggCheckTime < 0f ? growthTime * 0.20f : badEggCheckTime;

                if (!badChecked && tick.get(this) >= checkAt) {
                    badChecked = true;
                    if (Mathf.random() < badEggChance) {
                        isBad = true;
                    }
                }

                if (isBad) {
                    // Die 30 ticks after the check so it visually "rots"
                    if (tick.get(this) >= checkAt + 30f) {
                        kill();
                        return;
                    }
                    tick.replace(this, tick.get(this) + 1f);
                    return;
                }
            }

            // ---- floor validation kill ----
            if (attributes != null) {
                if (tick.get(this) >= growthTime / 8) {
                    if (!getValidation(attributes, x, y)) {
                        kill();
                    }
                }
            }

            // ---- hatch ----
            if (tick.get(this) == growthTime) {
                Tmp.v1.rnd(Mathf.random(spread));
                int rand = (Mathf.random(0, spawnAmountRand) + spawnAmount);
                for (int i = 0; i < rand; i++) {
                    Unit u = nextStage.create(team);
                    u.set(x + arc.util.Tmp.v1.x, y + arc.util.Tmp.v1.y);
                    u.rotation = faceOutwards ? arc.util.Tmp.v1.angle() : rotation + Mathf.range(5.0F);
                    if (!Vars.net.client()) {
                        u.add();
                    }
                }
                tick.replace(this, 0f);
                tick.remove(this);
                kill();
            }

            if (tick.containsKey(this)) {
                tick.replace(this, tick.get(this) + 1f);
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(tick.get(this));
            write.bool(badChecked);
            write.bool(isBad);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            tick.put(this, read.f());
            badChecked = read.bool();
            isBad = read.bool();
        }
    }

    public boolean getValidation(Seq<Block> tile, float x, float y) {
        for (int i = 0; i < tile.size; i++) {
            if (tile.get(i) != null && tile.get(i) instanceof Floor fl) {
                Tile tIle = Vars.world.tileWorld(x, y);
                if (fl == tIle.floor()) {
                    return true;
                }
            } else if (tile.get(i) != null) {
                throw new ClassCastException("Only Floors can be put in egg attributes \"" + tile.get(i) + "\" isnt a floor type");
            }
        }
        return false;
    }
}