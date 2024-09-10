package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
import blackhole.entities.abilities.BlackHoleAbility;
import blackhole.entities.effect.SwirlEffect;
import blackhole.graphics.BHDrawf;
import blackhole.graphics.BlackHoleRenderer;
import blackhole.utils.BlackHoleUtils;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.tilesize;

public class AttractionFieldAbility extends Ability {
    protected static Vec2 vec = new Vec2();
    public float x;
    public float y;
    public float damageInterval = 2.0F;
    public boolean whenShooting = false;
    public boolean whenNotShooting = false;
    public float warmupSpeed = 0.06F;
    public boolean drawBlackHole = true;
    public float horizonRadius = -1.0F;
    public float lensingRadius = -1.0F;
    public float damageRadius = 6.0F;
    public float suctionRadius = 160.0F;
    public boolean repel = false;
    public float force = 10.0F;
    public float scaledForce = 800.0F;
    public float bulletForce = 0.1F;
    public float scaledBulletForce = 1.0F;
    public float damage = 30.0F;
    public float bulletDamage = 10.0F;
    @Nullable
    public Color color = null;
    public float starWidth = -1.0F;
    public float starHeight = -1.0F;
    public float starAngle;
    @Nullable
    public Color starIn;
    @Nullable
    public Color starOut;
    public Effect swirlEffect;
    public float swirlInterval;
    public int swirlEffects;
    public boolean counterClockwise;
    protected float effectTimer;
    protected float suctionTimer;
    protected float scl;

    public AttractionFieldAbility(){
        drawBlackHole = false;
        swirlEffect = Fx.none;
    }
    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-attraction-field");
    }
    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + Strings.autoFixed(30f * damage, 2) + " " + StatUnit.perSecond.localized());
        t.row();
        if (damageRadius != suctionRadius) {
            t.add("[lightgray]Attack " + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(damageRadius / tilesize, 2) + " " + StatUnit.blocks.localized());
            t.row();
            t.add("[lightgray]Attraction " + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(suctionRadius / tilesize, 2) + " " + StatUnit.blocks.localized());
            t.row();
        }else{
            t.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(suctionRadius / tilesize, 2) + " " + StatUnit.blocks.localized());
            t.row();
        }
        if (!repel){
            t.add("[lightgray] Attracting Force");
            t.row();
        } else {
            t.add("[lightgray] Repelling Force");
            t.row();
        }
        if (whenShooting){
            t.add("[lightgray] Active only While Shooting");
            t.row();
        }
        if (whenNotShooting){
            t.add("[lightgray] Active only While Not Shooting");
            t.row();
        }
        t.add("[#800000] Other units with the same ability will cancel out each-others Forces but take the Damage");
        t.row();
    }

    public void init(UnitType type) {
        if (this.horizonRadius < 0.0F) {
            this.horizonRadius = this.damageRadius;
        }

        if (this.lensingRadius < 0.0F) {
            this.lensingRadius = this.suctionRadius;
        }

        if (!this.whenShooting) {
            this.scl = 1.0F;
        }

        if (this.starWidth > 0.0F && this.starHeight < 0.0F) {
            this.starHeight = this.starWidth / 2.0F;
        }

        BlackHoleUtils.immuneUnits.add(type);
    }

    public void draw(Unit unit) {
        if (this.drawBlackHole && !(this.scl < 0.01F)) {
            vec.set(this.x, this.y).rotate(unit.rotation - 90.0F).add(unit);
            BlackHoleRenderer.addBlackHole(vec.x, vec.y, this.horizonRadius * this.scl, this.lensingRadius * this.scl, BHDrawf.teamColor(unit, this.color));
            if (this.starWidth > 0.0F) {
                BlackHoleRenderer.addStar(vec.x, vec.y, this.starWidth * this.scl, this.starHeight * this.scl, this.starAngle, BHDrawf.teamColor(unit, this.starIn), BHDrawf.teamColor(unit, this.starOut));
            }

        }
    }

    public void update(Unit unit) {
        if (!whenNotShooting && unit.isShooting || !whenShooting && !unit.isShooting) {
            this.scl = Mathf.lerpDelta(this.scl, 1.0F, this.warmupSpeed);
        } else {
            this.scl = Mathf.lerpDelta(this.scl, 0.0F, this.warmupSpeed);
        }

        if (!(this.scl < 0.01F)) {
            vec.set(this.x, this.y).rotate(unit.rotation - 90.0F);
            if ((this.suctionTimer += Time.delta) >= this.damageInterval) {
                BlackHoleUtils.blackHoleUpdate(unit.team, unit, vec.x, vec.y, this.damageRadius * this.scl, this.suctionRadius * this.scl, this.damage, this.bulletDamage, this.repel, this.force, this.scaledForce, this.bulletForce, this.scaledBulletForce);
                this.suctionTimer %= this.damageInterval;
            }

            if ((this.effectTimer += Time.delta) >= this.swirlInterval) {
                vec.add(unit);

                for(int i = 0; i < this.swirlEffects; ++i) {
                    this.swirlEffect.at(vec.x, vec.y, this.suctionRadius * (this.counterClockwise ? -1.0F : 1.0F), BHDrawf.teamColor(unit, this.color), unit);
                }

                this.effectTimer %= this.swirlInterval;
            }

        }
    }
}
