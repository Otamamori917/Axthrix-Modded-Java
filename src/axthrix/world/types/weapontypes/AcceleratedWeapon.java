package axthrix.world.types.weapontypes;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import axthrix.world.util.AxPartParms;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public class AcceleratedWeapon extends Weapon {
    public float accelCooldownTime = 120f;
    public float accelCooldownWaitTime = 60f;
    public float accelPerShot = 1.1f;
    public float minReload = 5f;

    // Overheat settings
    public float overheatDuration = 300f; // Time before overheat (in ticks, 5 seconds at 60fps)
    public float overheatCooldown = 240f; // How long to cool down from overheat (4 seconds)
    public boolean canOverheat = true; // Toggle overheat mechanic

    public AcceleratedWeapon(String name){
        super(name);
        mountType = AcceleratedMount::new;
    }

    @Override
    public void addStats(UnitType unit, Table t){
        super.addStats(unit, t);

        if(minReload > 0 && minReload < reload){
            float shotsPerSecond = 60f / reload;
            float accelPerSecond = accelPerShot * shotsPerSecond;
            float bonusPerSecond = (accelPerSecond / reload) * 100f;

            t.row();
            t.add("[lightgray]Fire Rate Boost: [white]+" + Strings.autoFixed(bonusPerSecond, 1) + "%[lightgray]/sec while firing");

            t.row();
            float maxBonus = ((reload / minReload) - 1) * 100f;
            t.add("[lightgray]Max Fire Rate Bonus: [white]+" + Mathf.round(maxBonus) + "%");
        }

        if(canOverheat){
            t.row();
            t.add("[lightgray]Overheat After: [white]" + Strings.autoFixed(overheatDuration / 60f, 1) + " [lightgray]seconds of firing");
            t.row();
            t.add("[lightgray]Overheat Cooldown: [white]" + Strings.autoFixed(overheatCooldown / 60f, 1) + " [lightgray]seconds");
        }
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        AcceleratedMount aMount = (AcceleratedMount)mount;

        // Handle overheat state FIRST
        if(aMount.overheated){
            aMount.overheatCounter -= Time.delta;

            // Force reload to max so weapon can't shoot
            mount.reload = reload;

            if(aMount.overheatCounter <= 0){
                aMount.overheated = false;
                aMount.accel = 0f;
                aMount.waitTime = 0f;
                aMount.overheatCounter = 0f;
                aMount.heatCounter = 0f;
            }
            // Don't process anything else while overheated
            super.update(unit, mount);
            return;
        }

        // Track heat buildup when shooting
        if(unit.isShooting() && canOverheat){
            aMount.heatCounter += Time.delta;

            // Check for overheat
            if(aMount.heatCounter >= overheatDuration){
                aMount.overheated = true;
                aMount.overheatCounter = overheatCooldown;
                aMount.heatCounter = 0f;
                aMount.accel = 0f;
                aMount.waitTime = 0f;
                super.update(unit, mount);
                return;
            }
        }else if(!unit.isShooting() && aMount.heatCounter > 0){
            // Cool down when not shooting
            aMount.heatCounter = Math.max(0f, aMount.heatCounter - Time.delta);
        }

        // Normal acceleration logic
        float r = ((aMount.accel / reload) * unit.reloadMultiplier * Time.delta) * (reload - minReload);
        if(!alternate || otherSide == -1){
            mount.reload -= r;
        }else{
            WeaponMount other = unit.mounts[otherSide];
            other.reload -= r / 2f;
            mount.reload -= r / 2f;
            if(other instanceof AcceleratedMount aM){
                float accel = unit.isShooting() && unit.canShoot() ? Math.max(aM.accel, aMount.accel) : Math.min(aM.accel, aMount.accel);
                float wTime = unit.isShooting() && unit.canShoot() ? Math.max(aM.waitTime, aMount.waitTime) : Math.min(aM.waitTime, aMount.waitTime);
                float heat = unit.isShooting() && unit.canShoot() ? Math.max(aM.heatCounter, aMount.heatCounter) : Math.min(aM.heatCounter, aMount.heatCounter);
                aM.accel = accel;
                aM.waitTime = wTime;
                aM.heatCounter = heat;
                aMount.accel = accel;
                aMount.waitTime = wTime;
                aMount.heatCounter = heat;
            }
        }

        // Cooldown logic
        if(aMount.waitTime <= 0f){
            aMount.accel = Math.max(0f, aMount.accel - (minReload / accelCooldownTime) * Time.delta);
        }else{
            aMount.waitTime -= Time.delta;
        }

        super.update(unit, mount);
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
        AcceleratedMount aMount = (AcceleratedMount)mount;

        // Double-check overheat (shouldn't reach here if overheated, but just in case)
        if(aMount.overheated) return;

        // Add acceleration
        aMount.accel = Mathf.clamp(aMount.accel + accelPerShot, 0f, minReload);
        aMount.waitTime = accelCooldownWaitTime;

        super.shoot(unit, mount, shootX, shootY, rotation);
    }

    @Override
    public void draw(Unit unit, WeaponMount mount) {
        super.draw(unit, mount);

        if(parts.size > 0) {
            if (mount instanceof AcceleratedMount aw) {
                // Calculate heat as a 0-1 value
                float heat = 0f;

                if(aw.overheated){
                    // While overheated, show cooling progress (1 = just overheated, 0 = cooled down)
                    heat = aw.overheatCounter / overheatCooldown;
                }else{
                    // While not overheated, show heat buildup (0 = cold, 1 = about to overheat)
                    heat = canOverheat ? aw.heatCounter / overheatDuration : 0f;
                }

                AxPartParms.axparams.set(
                        0, //used in other always 0
                        0, //used in other always 0
                        heat // Heat value 0-1
                );
            }
        }
    }

    public static class AcceleratedMount extends WeaponMount {
        public float accel = 0f;
        public float waitTime = 0f;
        public boolean overheated = false;
        public float overheatCounter = 0f;
        public float heatCounter = 0f; // Tracks time spent firing

        public AcceleratedMount(Weapon weapon){
            super(weapon);
        }

        public float heatf(){
            AcceleratedWeapon w = (AcceleratedWeapon)weapon;
            if(overheated) return 1f - (overheatCounter / w.overheatCooldown);
            return heatCounter / w.overheatDuration;
        }

        public float accelF(){
            AcceleratedWeapon w = (AcceleratedWeapon)weapon;
            return accel / w.minReload;
        }
    }
}