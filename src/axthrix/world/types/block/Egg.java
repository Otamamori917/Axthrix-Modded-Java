package axthrix.world.types.block;

import arc.Core;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
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

public class Egg extends AxBlock{
    public UnitType nextStage;
    public int spawnAmount = 1;
    public int spawnAmountRand = 1;
    public float spread = 8.0F;
    public boolean faceOutwards = true;
    public float growthTime = 600;
    public Seq<Block> attributes = new Seq<>();
    public HashMap<Building, Float> tick = new HashMap<>();


    public Egg(String name){
        super(name);
        faction.add(AxFactions.ikatusa);
        update = true;
        floating = true;
        solid = false;
        placeableLiquid = true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if(attributes.size > 0){
            drawPlaceText(Core.bundle.format(getValidation(attributes,Core.input.mouseWorldX(),Core.input.mouseWorldY()) ? "text.validt" : "text.validf"), x, y, valid);
        }
    }

    @Override
    public void load() {
        super.load();
    }

    public void setStats() {
        super.setStats();
        stats.add(AxStats.tiles, AxStatValues.blocks(attributes,floating,true));
    }

    public class EggBuild extends Building {

        @Override
        public void updateTile(){
            super.updateTile();
            if (!tick.containsKey(this)){
                tick.put(this,0f);
            }
            if(attributes != null){
                if(tick.get(this) >= growthTime/4){
                   if(!getValidation(attributes,x,y)){
                       kill();
                   }
                }

            }





            if(tick.containsValue(growthTime)){
                Tmp.v1.rnd(Mathf.random(spread));
                for (int i = 0; i <(Mathf.random(0,spawnAmountRand)+spawnAmount); i++) {
                    Unit u = nextStage.create(team);
                    u.set(x + Tmp.v1.x, y + Tmp.v1.y);
                    u.rotation = faceOutwards ? Tmp.v1.angle() : rotation + Mathf.range(5.0F);
                    if (!Vars.net.client()) {
                        u.add();
                    }
                }
                tick.replace(this,0f);
                tick.remove(this);
                kill();
            }
            if(tick.containsKey(this)){
                tick.replace(this,tick.get(this)+1);
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(tick.get(this));
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            tick.put(this,read.f());
        }
    }

    public boolean getValidation(Seq<Block> tile,float x, float y){
        for (int i = 0; i < tile.size; i++) {
            if(tile.get(i) != null && tile.get(i) instanceof Floor fl){
                Tile tIle = Vars.world.tileWorld(x,y);
                 if (fl == tIle.floor()){
                     return true;
                 }
            }else if(tile.get(i) != null){
                throw new ClassCastException("Only Floors can be put in egg attributes \""+tile.get(i)+"\" isnt a floor type");
            }
        }
        return false;
    }


}
