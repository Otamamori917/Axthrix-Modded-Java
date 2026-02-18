package axthrix.world.types.abilities;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.FX.AxthrixFfx;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class HeatWaveAbility extends Ability{
	protected static final Seq<Unit> all = new Seq<>();
	
	public ObjectFloatMap<StatusEffect> status = new ObjectFloatMap<>();
	
	public boolean targetGround = true, targetAir = true;
	public float x, y;
	
	public float reload;
	public float range;
	public float damage;
	
	public float knockback = 20f;

    public Color hitColor;
	
	public Sound shootSound = Sounds.shootEnergyField;
	public Effect hitEffect = Fx.pointHit;
	
	public float maxSpeed = -1;

    public HeatWaveAbility(float reload, float range, float damage, Color hitColor){
        this.reload = reload;
        this.range = range;
        this.damage = damage;
        this.hitColor = hitColor;
    }
    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-heat-wave");
    }
    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + damage);
        t.row();
        t.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(range / tilesize, 2) + " " + StatUnit.blocks.localized());
        t.row();
        t.add("[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60f / reload, 2) + " " + StatUnit.perSecond.localized());
        t.row();
    }

    @Override
	public void init(UnitType type){
		super.init(type);
		if(maxSpeed > 0)maxSpeed = maxSpeed * maxSpeed;
	}
	
	protected float timer = 0;
	
	@Override
	public void update(Unit unit){
		if(unit.disarmed)return;
		
		timer += Time.delta * unit.reloadMultiplier;
		
		if(maxSpeed > 0 && unit.vel().len2() > maxSpeed){
			timer = 0;
		}else if(timer > reload){
			all.clear();
			
			Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
			float rx = Tmp.v1.x, ry = Tmp.v1.y;
			
			Units.nearby(null, rx, ry, range, other -> {
				if(other.team != unit.team && other.checkTarget(targetAir, targetGround) && other.targetable(unit.team)){
					all.add(other);
				}
			});
			
			if(all.any()){
				timer = 0;
				shootSound.at(rx, ry, 1 + Mathf.range(0.15f), 3);

                AxthrixFfx.circleOut(16,range,damage/40, Layer.blockOver,hitColor).at(rx, ry, range);
				for(Unit u : all){
					for(ObjectFloatMap.Entry<StatusEffect> s : status.entries()){
						u.apply(s.key, s.value);
					}
					
					Tmp.v3.set(unit).sub(Tmp.v1).nor().scl(knockback * 80f);
					u.impulse(Tmp.v3);
					u.damage(damage);
					hitEffect.at(u.x, u.y, hitColor);
				}
			}
		}
	}
	
	@Override
	public void draw(Unit unit){
		super.draw(unit);
	}
	

}
