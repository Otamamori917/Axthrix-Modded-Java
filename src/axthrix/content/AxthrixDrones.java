package axthrix.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import axthrix.world.types.abilities.DroneControlAbility;
import axthrix.world.types.abilities.NanobotStormAbility;
import axthrix.world.types.abilities.SStatusFieldAbility;
import axthrix.world.types.bulletypes.SonicBulletType;
import axthrix.world.types.bulletypes.SpiralPattern;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.types.unittypes.MountUnitType;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ExplosionBulletType;
import mindustry.entities.bullet.FireBulletType;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.HoverPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.part.ShapePart;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.unit.MissileUnitType;

import static mindustry.Vars.content;
import static mindustry.Vars.tilePayload;

public class AxthrixDrones {
    public static UnitType
    basicFlame
            ;
    public static void load(){
        basicFlame = new DroneUnitType("flame-1")
        {{
            localizedName = "[orange]TEST DRONE";
            health = 2000;
            armor = 2;
            faceTarget = true;
            flying = true;
            hitSize = 2*2;
            engineColor = Color.orange;
            itemCapacity = 0;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = true;
            range = 12*8;
            engineSize = 2;
            weapons.add(new Weapon(){{
                shootSound = Sounds.flame;
                x = 0;
                y = 0;
                shootY = 6;
                mirror = false;
                top = false;
                reload = 1;
                inaccuracy = 10;
                immunities.add(StatusEffects.burning);
                bullet = new FireBulletType(){{
                    speed = 6f;
                    radius = 5;
                    velMin = 0.8f;
                    velMax = 6;
                    hittable = false;
                    keepVelocity = false;
                    collidesAir = false;
                    hitEffect = Fx.hitFlameSmall;
                    despawnEffect = Fx.none;

                    fragRandomSpread = 0f;
                    fragSpread = 5f;
                    fragVelocityMin = 1f;
                    fragBullets = 10;
                    fragBullet = new BulletType(4.2f, 3f){{
                        ammoMultiplier = 3f;
                        hitSize = 7f;
                        lifetime = 13f;
                        pierce = true;
                        pierceBuilding = true;
                        pierceCap = 2;
                        statusDuration = 60f * 4;
                        shootEffect = Fx.shootSmallFlame;
                        hitEffect = Fx.hitFlameSmall;
                        despawnEffect = Fx.none;
                        status = StatusEffects.melting;
                        keepVelocity = false;
                        hittable = false;
                    }};
                }};
            }});
        }};
    }
}        