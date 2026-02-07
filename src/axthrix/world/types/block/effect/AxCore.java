package axthrix.world.types.block.effect;

import arc.Core;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Timer;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import axthrix.world.types.AxFaction;

import static mindustry.Vars.state;

public class AxCore extends CoreBlock {
    public Seq<AxFaction> faction = new Seq<>();
    public float healTime = -1;
    public Effect warmupEffect = Fx.none;
    public Effect spawnEffect = Fx.none;
    public boolean blackListFactions = false;

    @Override
    public void setStats() {
        super.setStats();

        if(faction.any()){
            stats.add(AxStats.faction, Core.bundle.get("team." +  faction.peek().name));
        }
    }

    public DrawBlock drawer = new DrawMulti(
            new DrawDefault()
    );

    public AxCore(String name)
    {
        super(name);
        update = true;
        noUpdateDisabled = false;
    }

    @Override
    public void init() {
        super.init();
        drawer.load(this);
    }

    @Override
    public void loadIcon(){
        super.loadIcon();
        fullIcon = Core.atlas.find(name + "-full",fullIcon);
        uiIcon = Core.atlas.find(name + "-ui",fullIcon);
    }

    public class AxCoreBuild extends CoreBuild {
        float charge = 0;
        Seq<Player> playerQue = new Seq<>();

        @Override
        public void requestSpawn(Player player){
            //do not try to respawn in unsupported environments at all
            if(!unitType.supportsEnv(state.rules.env)) return;
            if(warmupEffect.lifetime == 0) {
                Call.playerSpawn(tile,player);
                spawnEffect.at(tile);
                return;
            }
            if(playerQue.contains(player)) return;
            warmupEffect.at(tile);
            playerQue.add(player);
            Timer.schedule(()->{
                Call.playerSpawn(tile, player);
                spawnEffect.at(tile);
                playerQue.remove(player);
                },
                    warmupEffect.lifetime/60);
        }

        @Override
        public void draw() {
            drawer.draw(this);
            drawTeamTop();
        }

        @Override
        public void updateTile()
        {
            super.updateTile();
            if (healTime < 1) return;
            if (wasRecentlyDamaged()) return;
            if (health != maxHealth) {
                charge += Time.delta;
                if (charge > 60) {
                    charge %= 60;
                    heal(maxHealth/healTime);
                    Fx.healBlockFull.at(x, y, block.size, Pal.heal, block);
                }
            }
        }
    }
}
