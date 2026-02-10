package axthrix.world.util;

import arc.*;
import arc.graphics.Color;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

public interface AcceleratedLogic {
    // Configuration getters (implement these in your turret classes)
    float getAcceleratedDelay();
    float getAcceleratedBonus();
    int getAcceleratedSteps();
    float getBurnoutDelay();
    float getCooldownDelay();
    boolean burnsOut();
    boolean heatOnShoot();

    // Access to turret properties
    float getReload();
    int getShootShots();
    Object getCoolant();
    float getCoolantMultiplier();
    boolean consumesLiquid(Object l);

    // Default implementation for stats - changed from StatProxy to Stats
    default void addAcceleratedStats(Stats stats) {
        if(getAcceleratedBonus() != 1){
            stats.add(AxStats.maxFireRateBonus,
                    60.0F / getReload() * getShootShots() * (getAcceleratedBonus() * getAcceleratedSteps() - 1) + "/sec ~ [stat]" +
                            ((getAcceleratedBonus() - 1) * getAcceleratedSteps()) * 100 + "% []Bonus",
                    StatUnit.none);
            stats.add(AxStats.timeForMaxBonus, (getAcceleratedDelay() * getAcceleratedSteps()) / 60, StatUnit.seconds);
        }
        if (burnsOut()){
            stats.add(AxStats.overheat, ((getAcceleratedDelay() * getAcceleratedSteps()) + getBurnoutDelay()) / 60, StatUnit.seconds);
            stats.add(AxStats.timeToCool, getCooldownDelay() / 60, StatUnit.seconds);
        }
        if (getCoolant() != null) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster, StatValues.boosters(getReload(), ((mindustry.type.LiquidStack)getCoolant()).amount,
                    getCoolantMultiplier(), true, (l) -> ((mindustry.type.Liquid)l).coolant && consumesLiquid(l)));
        }
    }

    // Default implementation for bars - fixed to use getters instead of direct field access
    default <T extends AcceleratedTurretBuild> Bar createAcceleratedBar(T entity, boolean isHeatMode) {
        if(isHeatMode){
            return new Bar(
                    () -> entity.getAccelCount() > getAcceleratedSteps() ?
                            Core.bundle.format("bar.aj-overheated") :
                            Core.bundle.format("bar.aj-heat", Strings.autoFixed((entity.getAccelBoost() - 1) * 100f, 2)),
                    () -> entity.getAccelCount() > getAcceleratedSteps() ? Pal.remove : Color.orange,
                    entity::heatf
            );
        } else {
            return new Bar(
                    () -> entity.getAccelCount() > getAcceleratedSteps() ?
                            Core.bundle.format("bar.aj-overheated") :
                            Core.bundle.format("bar.aj-phases", Strings.autoFixed((entity.getAccelBoost() - 1) * 100f, 2)),
                    () -> entity.getAccelCount() > getAcceleratedSteps() ? Pal.remove : Pal.techBlue,
                    entity::boostf
            );
        }
    }

    // Shared build logic interface
    interface AcceleratedTurretBuild {
        float getAccelBoost();
        void setAccelBoost(float value);
        float getAccelCounter();
        void setAccelCounter(float value);
        float getCoolBonus();
        void setCoolBonus(float value);
        int getAccelCount();
        void setAccelCount(int value);
        float getLastReloadCounter();
        void setLastReloadCounter(float value);

        // From turret
        float getReloadCounter();
        void setReloadCounter(float value);
        float getReload();
        boolean isShooting();
        float edelta();
        void updateCooling(); // Changed to public (was protected in the parent)
        float getCoolantMultiplier();
        void setCoolantMultiplier(float value);

        // Acceleration parameters
        float getAcceleratedDelay();
        float getAcceleratedBonus();
        int getAcceleratedSteps();
        float getBurnoutDelay();
        float getCooldownDelay();
        boolean burnsOut();
        boolean heatOnShoot();

        default void updateAcceleration() {
            if(getCoolantMultiplier() != 0){
                setCoolBonus(getCoolantMultiplier());
            }

            // Detect if a shot was fired
            boolean justFired = heatOnShoot() && getLastReloadCounter() > 0 &&
                    getReloadCounter() < getLastReloadCounter() &&
                    getReloadCounter() < getReload() * 0.5f;
            setLastReloadCounter(getReloadCounter());

            if(getAccelCount() > getAcceleratedSteps()){
                // Cooling down from overheat
                setAccelCounter(getAccelCounter() + edelta());
                if(getAccelCounter() >= getCooldownDelay()){
                    setCoolantMultiplier(getCoolBonus());
                    updateCooling();
                    setAccelCount(0);
                    setAccelBoost(1);
                    setAccelCounter(0);
                }
            }else if(heatOnShoot() && justFired){
                // Heat on shoot mode
                setAccelCounter(getAccelCounter() + getReload());

                if(getAccelCount() < getAcceleratedSteps() && getAccelCounter() >= getAcceleratedDelay()){
                    setAccelBoost(getAccelBoost() + (getAcceleratedBonus() - 1));
                    setAccelCount(getAccelCount() + 1);
                    setAccelCounter(getAccelCounter() % getAcceleratedDelay());
                }else if(burnsOut() && getAccelCounter() >= getBurnoutDelay()){
                    setAccelBoost(0);
                    setCoolantMultiplier(0);
                    setAccelCount(getAccelCount() + 1);
                    setAccelCounter(0);
                }
            }else if(!isShooting() && getAccelCounter() > 0 && getAccelCount() <= getAcceleratedSteps()){
                // Gradual heat dissipation
                float dissipationRate = getBurnoutDelay() / getCooldownDelay();
                setAccelCounter(getAccelCounter() - Time.delta * dissipationRate);

                if(getAccelCounter() <= 0){
                    setAccelCounter(0);
                    setAccelCount(0);
                    setAccelBoost(1);
                    setCoolantMultiplier(getCoolBonus());
                    updateCooling();
                }
            }else if(!heatOnShoot() && isShooting()){
                // Original time-based behavior
                setAccelCounter(getAccelCounter() + edelta());
                if(getAccelCount() < getAcceleratedSteps() && getAccelCounter() >= getAcceleratedDelay()){
                    setAccelBoost(getAccelBoost() + (getAcceleratedBonus() - 1));
                    setAccelCount(getAccelCount() + 1);
                    setAccelCounter(getAccelCounter() % getAcceleratedDelay());
                }else if(burnsOut() && getAccelCounter() >= getBurnoutDelay()){
                    setAccelBoost(0);
                    setCoolantMultiplier(0);
                    setAccelCount(getAccelCount() + 1);
                    setAccelCounter(0);
                }
            }
        }

        default float boostf(){
            if(getAccelCount() > getAcceleratedSteps()) return 1 - (getAccelCounter() / getCooldownDelay());
            return Mathf.clamp((float)getAccelCount() / getAcceleratedSteps());
        }

        default float heatf(){
            if(getAccelCount() > getAcceleratedSteps()) return 1 - (getAccelCounter() / getCooldownDelay());
            return getAccelCounter() / getBurnoutDelay();
        }
    }
}