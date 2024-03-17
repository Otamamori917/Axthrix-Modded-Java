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
import axthrix.entities.AxDamage;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.bullet.*;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import arc.math.*;
import arc.math.geom.*;

import mindustry.gen.*;


import static mindustry.Vars.tilesize;

public class AttractionFieldAbility extends Ability{
    static Seq<Class<?>> immuneTypes = Seq.with(
            ContinuousBulletType.class,
            LaserBulletType.class,
            SapBulletType.class,
            ShrapnelBulletType.class
    );
	protected static final Seq<Unit> all = new Seq<>();
	
	public ObjectFloatMap<StatusEffect> status = new ObjectFloatMap<>();

	public float x, y;

	public float range;
	public float damage;
    public Color color = Color.black;
    public float suctionRadius = 160f, size = 6f, damageRadius = 17f;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, bulletScaledForce = 1f;
    public float bulletDamage = 10f;
    public boolean repel;
    public AttractionFieldAbility(float damage){
        this.damage = damage;
    }
    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-attraction-field");
    }
    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + damage);
        t.row();
    }

	@Override
	public void update(Unit Unit){

			AxDamage.completeDamage(Unit.team, Unit.x, Unit.y, damageRadius);

			float sR = suctionRadius;
			Units.nearbyEnemies(Unit.team, Unit.x - sR, Unit.y - sR, sR * 2f, sR * 2f, unit -> {
				if(unit.within(Unit.x, Unit.y, sR)){
					Vec2 impulse = Tmp.v1.trns(unit.angleTo(Unit), force + (1f - unit.dst(Unit) / sR) * scaledForce);
					if(repel) impulse.rotate(180f);
					unit.impulseNet(impulse);
				}
			});

			Groups.bullet.intersect(Unit.x - sR, Unit.y - sR, sR * 2f, sR * 2f, other -> {
				if(other != null && !checkType(other.type) && Mathf.within(Unit.x, Unit.y, other.x, other.y, sR)){
					Vec2 impulse = Tmp.v1.trns(other.angleTo(Unit), bulletForce + (1f - other.dst(Unit) / sR) * bulletScaledForce);
					if(repel) impulse.rotate(180f);
					other.vel().add(impulse);

					//manually move bullets to simulate velocity for remote players
					if(other.isRemote()){
						other.move(impulse.x, impulse.y);
					}

					if(other.type.hittable && Mathf.within(Unit.x, Unit.y, other.x, other.y, size * 2f)){
						float realDamage = bulletDamage;
						if(other.damage > realDamage){
							other.damage(other.damage - realDamage);
						}else{
							other.remove();
						}
					}
				}
			});


		super.update(Unit);
	}


	public static boolean checkType(BulletType type){ //Returns true for bullets immune to suction.
		return immuneTypes.contains(c -> c.isAssignableFrom(type.getClass()));
	}
	
	@Override
	public void draw(Unit unit){
		super.draw(unit);
	}
	

}
