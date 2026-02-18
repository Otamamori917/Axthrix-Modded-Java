package axthrix.world.types.abilities;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static axthrix.content.AxthrixStatus.*;
import static mindustry.Vars.*;

public class NanobotStormAbility extends Ability {
    private static final Seq<Healthc> all = new Seq<>();

    public float damage = 4f, range = 90, reload = 1, buildingDamageReduction = 8;
    public Effect healEffect = Fx.heal, hitEffect = Fx.hitFlamePlasma, damageEffect = Fx.none;
    public StatusEffect status = nanodiverge;
    public Sound shootSound = Sounds.loopFlux;
    public float statusDuration = 60f * 6f;
    public float x, y = 7f;
    public boolean targetGround = true, targetAir = true, hitBuildings = true, hitUnits = true;
    public float healPercent = 0.08f;

    public float layer = Layer.bullet + 4f, blinkScl = 20f, blinkSize = 0.1f;
    public float effectRadius = 5f, sectorRad = 0.14f, rotateSpeed = 10f;
    public int sectors = 4;
    public Color color = Pal.heal;
    public boolean useAmmo = true;

    protected float timer, curStroke;
    protected boolean anyNearby = false;

    public NanobotStormAbility(){}

    public NanobotStormAbility(float damage, float range){
        this.damage = damage;
        this.range = range;
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-nanobot-storm");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + Strings.autoFixed(60f * damage, 2) + " " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.damage.localized() + " to buildings: [white]" + Strings.autoFixed(60f * damage / buildingDamageReduction, 1) + " " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(range / tilesize, 2) + " " + StatUnit.blocks.localized());
        t.row();
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);

        Draw.z(layer);
        Draw.color(color);
        Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
        float rx = Tmp.v1.x, ry = Tmp.v1.y;
        float orbRadius = effectRadius * (1f + Mathf.absin(blinkScl, blinkSize));

        Fill.circle(rx, ry, orbRadius);
        Draw.color();
        Fill.circle(rx, ry, orbRadius / 2f);

        Lines.stroke((0.7f + Mathf.absin(blinkScl, 0.7f)), color);

        for(int i = 0; i < sectors; i++){
            float rot = unit.rotation + i * 360f/sectors - Time.time * rotateSpeed;
            Lines.arc(rx, ry, orbRadius + 3f, sectorRad, rot);
        }

        Lines.stroke(Lines.getStroke() * curStroke);

        if(curStroke > 0){
            for(int i = 0; i < sectors; i++){
                float rot = unit.rotation + i * 360f/sectors + Time.time * rotateSpeed;
                Lines.arc(rx, ry, range, sectorRad, rot);
            }
        }

        Drawf.light(rx, ry, range * 1.5f, color, curStroke * 0.8f);

        Draw.reset();
    }

    @Override
    public void update(Unit unit){

        curStroke = Mathf.lerpDelta(curStroke, anyNearby ? 1 : 0, 0.09f);

        if((timer += Time.delta) >= reload && (!useAmmo || unit.ammo > 0 || !state.rules.unitAmmo)){
            Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
            float rx = Tmp.v1.x, ry = Tmp.v1.y;
            anyNearby = false;

            all.clear();

            if(hitUnits){
                Units.nearby(null, rx, ry, range, other -> {
                    if(other != unit && other.checkTarget(targetAir, targetGround) && other.targetable(unit.team) && (other.team != unit.team || other.damaged())){
                        all.add(other);
                    }
                });
            }

            if(hitBuildings && targetGround){
                Units.nearbyBuildings(rx, ry, range, b -> {
                    if((b.team != Team.derelict || state.rules.coreCapture) && (b.team != unit.team || b.damaged())){
                        all.add(b);
                    }
                });
            }

            all.sort(h -> h.dst2(rx, ry));
            int len = Math.min(all.size, 1000);
            for(int i = 0; i < len; i++){
                Healthc other = all.get(i);


                if(((Teamc)other).team() == unit.team){
                    if(other.damaged()){
                        anyNearby = true;
                        other.heal(healPercent / 100f * other.maxHealth());
                        healEffect.at(other);
                        damageEffect.at(rx, ry, 0f, color, other);
                        hitEffect.at(rx, ry, unit.angleTo(other), color);

                        if(other instanceof Building b){
                            Fx.healBlockFull.at(b.x, b.y, 0f, color, b.block);
                        }
                    }
                }else{
                    anyNearby = true;
                    if(other instanceof Building b){
                        b.damage(unit.team, damage / buildingDamageReduction * state.rules.unitDamage(unit.team));
                    }else{
                        other.damage(damage * state.rules.unitDamage(unit.team));
                    }
                    if(other instanceof Statusc s){
                        s.apply(status, statusDuration);
                    }
                    hitEffect.at(other.x(), other.y(), unit.angleTo(other), color);
                    damageEffect.at(rx, ry, 0f, color, other);
                    hitEffect.at(rx, ry, unit.angleTo(other), color);
                }
            }

            if(anyNearby){
                shootSound.at(unit);

                if(useAmmo && state.rules.unitAmmo){
                    unit.ammo --;
                }
            }

            timer = 0f;
        }
    }
}