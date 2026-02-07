package axthrix.world.types.block;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.LiquidBlock;

public class LiquidDeposit extends LiquidBlock {
    public float liquidPadding = 0.0F;
    public Seq<AxFaction> faction = new Seq<>();


    public LiquidDeposit(String name) {
        super(name);
        solid = true;
        noUpdateDisabled = true;
        canOverdrive = false;
        floating = true;
    }

    @Override
    public void setStats() {
        super.setStats();

        if(faction.any()){
            stats.add(AxStats.faction, Core.bundle.get("team." +  faction.peek().name));
        }

    }

    public TextureRegion[] icons() {
        return new TextureRegion[]{bottomRegion, region};
    }

    public class LiquidDepositBuild extends LiquidBlock.LiquidBuild {
        public LiquidDepositBuild() {
            super();
        }

        public void updateTile() {
            if (liquids.currentAmount() > 0.01F) {
                dumpLiquid(liquids.current());
            }

        }

        public void draw() {
            Draw.rect(bottomRegion, x, y);
            if (liquids.currentAmount() > 0.001F) {
                LiquidBlock.drawTiledFrames(size, x, y, liquidPadding, liquids.current(), liquids.currentAmount() / liquidCapacity);
            }

            Draw.rect(region, x, y);
        }

        public boolean acceptLiquid(Building source, Liquid liquid) {
            return liquids.current() == liquid || liquids.currentAmount() < 0.2F;
        }
    }
}

