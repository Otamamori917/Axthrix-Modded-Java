package axthrix.world.types.unittypes;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Tmp;
import axthrix.content.AxFactions;
import axthrix.world.types.ai.WildAi;
import axthrix.world.types.block.Egg;
import axthrix.world.types.entities.comp.ExtensionUnit;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

import java.util.HashMap;

import static mindustry.Vars.*;

public class IkatusaUnitType extends AxUnitType{
    @Nullable
    public UnitType nextStage;
    @Nullable
    public UnitType maleStage;
    @Nullable
    public UnitType femaleStage;
    public boolean finalStage = false;
    public float growthTime = 600, maturityTime = 300, cooldown = 100;
    public boolean digs = false, asexual = false, checkFloors = true;

    @Nullable
    public Block egg = null;
    public int spawnAmount = 1;
    public int spawnAmountRand = 1;
    public float spread = 50F;
    public boolean faceOutwards = true;


    @Nullable
    public UnitType oppositeGender = null;
    public boolean ismale = false;
    public float genderChance = 0.5f;
    public HashMap<Unit, Float> tick = new HashMap<>();
    public HashMap<Unit, Float> broodingCooldown = new HashMap<>();
    public HashMap<Unit, Boolean> brooding = new HashMap<>();

    //extension types
    public TextureRegion[] tentacleRegions;
    public TextureRegion tentacleEndRegion;
    public int extensionsNum = 3;
    public float bDamage = 6, bLength = 6,eReload = 20;

    public IkatusaUnitType(String name){
        super(name);
        factions.add(AxFactions.ikatusa);
        aiController = WildAi::new;
    }



    @Override
    public void load(){
        super.load();
        if (constructor instanceof ExtensionUnit){
            tentacleRegions = new TextureRegion[extensionsNum];

            for(int i = 0; i < extensionsNum; i++){
                tentacleRegions[i] = Core.atlas.find(name + "-extension-" + i);
            }
            tentacleEndRegion = Core.atlas.find(name + "-extension-end");
        }
    }

    @Override
    public void update(Unit unit){
        super.update(unit);
        if (!tick.containsKey(unit)){
            tick.put(unit,0f);
        }
        if (!broodingCooldown.containsKey(unit)){
            broodingCooldown.put(unit,cooldown/2);
        }
        if (!brooding.containsKey(unit) && !asexual){
            brooding.put(unit,false);
        }

        if(tick.get(unit) >= growthTime){
            if(!finalStage){
                if (nextStage != null){
                    Unit u = nextStage.create(unit.team);
                    u.set(unit.x,unit.y);
                    u.rotation = unit.rotation();
                    if (!Vars.net.client()) {
                        u.add();
                    }
                } else {
                    float createChanceRan = Mathf.random(0.01f,0.99f);
                    float set;

                    if(genderChance > 0.99f) set = 0.99f;
                    else if (genderChance < 0.01f) set = 0.01f;
                    else set = genderChance;

                    if(createChanceRan <= set){
                        Unit u = maleStage.create(unit.team);
                        u.set(unit.x,unit.y);
                        u.rotation = unit.rotation();
                        if (!Vars.net.client()) {
                            u.add();
                        }
                    }else{
                        Unit u = femaleStage.create(unit.team);
                        u.set(unit.x,unit.y);
                        u.rotation = unit.rotation();
                        if (!Vars.net.client()) {
                            u.add();
                        }
                    }
                }
                tick.replace(unit,0f);
                brooding.remove(unit);
                tick.remove(unit);
                broodingCooldown.remove(unit);
                unit.remove();
            }else{
                tick.replace(unit,0f);
                brooding.remove(unit);
                tick.remove(unit);
                broodingCooldown.remove(unit);
                unit.kill();
            }

        }
        if(tick.containsKey(unit) && tick.get(unit) >= maturityTime && egg != null){
            if(broodingCooldown.get(unit) >= cooldown){
                Groups.unit.each(U -> {
                    if (U.type instanceof IkatusaUnitType iku) {
                        float d = Mathf.dst(U.x, U.y, unit.x, unit.y);
                        if (d > unit.hitSize*2) {
                            if(!ismale && iku.ismale){
                                EggCall(unit);
                            }
                        }else{
                            if(ismale && !iku.ismale){

                            }
                        }
                    }
                });
                if(asexual){
                    EggCall(unit);
                }
            }
            if(broodingCooldown.containsKey(unit)){
                broodingCooldown.replace(unit,broodingCooldown.get(unit)+1);
            }
        }
        if(tick.containsKey(unit)){
            tick.replace(unit,tick.get(unit)+1);
        }
    }

    public static boolean onWater(Unit unit){
        return unit.floorOn().isLiquid;
    }

    public boolean onDeepWater(Unit unit){
        return onWater(unit) && unit.floorOn().drownTime > 0;
    }

    public void Move(Position posc, Float circleLength){
        if(aiController instanceof WildAi wa){
            wa.Move(posc,circleLength);
        }
    }

    public void EggCall(Unit unit){
        if(egg instanceof Egg eg){

            if(eg.getValidation(eg.attributes,unit.tileX()*8,unit.tileY()*8)){
                Tmp.v1.rnd(Mathf.random(spread));
                for (int i = 0; i <(Mathf.random(0,spawnAmountRand)+spawnAmount); i++) {

                    Tile tile = world.tile((int)(unit.x+ Tmp.v1.x)/8, (int)(unit.y+ Tmp.v1.y)/8);
                    tile.setNet(egg, unit.team, faceOutwards ? (int)Tmp.v1.angle()/90 : ((int)unit.rotation + (int)Mathf.range(5.0F))/90);
                }
                broodingCooldown.replace(unit,0f);
            }

        }else{
            throw new ClassCastException("Its Supposed to be an egg doofus");
        }
    }
}
