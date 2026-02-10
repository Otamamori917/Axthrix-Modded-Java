package axthrix.world.types.block.defense;

import axthrix.world.util.AcceleratedLogic;

public class PowerAcceleratedTurret extends AxPowerTurret implements AcceleratedLogic {
    public float acceleratedDelay = 120, acceleratedBonus = 1.5f;
    public int acceleratedSteps = 1;
    public float burnoutDelay = 240, cooldownDelay = 120;
    public boolean burnsOut = true;
    public boolean heatOnShoot = false;

    public PowerAcceleratedTurret(String name){
        super(name);
    }

    // Interface implementations
    @Override public float getAcceleratedDelay() { return acceleratedDelay; }
    @Override public float getAcceleratedBonus() { return acceleratedBonus; }
    @Override public int getAcceleratedSteps() { return acceleratedSteps; }
    @Override public float getBurnoutDelay() { return burnoutDelay; }
    @Override public float getCooldownDelay() { return cooldownDelay; }
    @Override public boolean burnsOut() { return burnsOut; }
    @Override public boolean heatOnShoot() { return heatOnShoot; }
    @Override public float getReload() { return reload; }
    @Override public int getShootShots() { return shoot.shots; }
    @Override public Object getCoolant() { return coolant; }
    @Override public float getCoolantMultiplier() { return coolantMultiplier; }
    @Override public boolean consumesLiquid(Object l) { return this.consumesLiquid((mindustry.type.Liquid)l); }

    @Override
    public void setBars(){
        super.setBars();
        boolean isHeatMode = acceleratedBonus == 1 && burnsOut;
        if(acceleratedBonus == 1 && burnsOut || acceleratedBonus != 1){
            addBar(isHeatMode ? "aj-heat" : "aj-phases",
                    (PowerAcceleratedTurretBuild entity) -> createAcceleratedBar(entity, isHeatMode));
        }
    }

    @Override
    public void setStats(){
        super.setStats();
        addAcceleratedStats(stats); // stats is already of type Stats, this should work now
    }

    public class PowerAcceleratedTurretBuild extends PowerTurretBuild implements AcceleratedLogic.AcceleratedTurretBuild {
        public float accelBoost = 1, accelCounter, coolBonus;
        public int accelCount;
        public float lastReloadCounter = 0;

        // Getters and setters for interface
        @Override public float getAccelBoost() { return accelBoost; }
        @Override public void setAccelBoost(float value) { accelBoost = value; }
        @Override public float getAccelCounter() { return accelCounter; }
        @Override public void setAccelCounter(float value) { accelCounter = value; }
        @Override public float getCoolBonus() { return coolBonus; }
        @Override public void setCoolBonus(float value) { coolBonus = value; }
        @Override public int getAccelCount() { return accelCount; }
        @Override public void setAccelCount(int value) { accelCount = value; }
        @Override public float getLastReloadCounter() { return lastReloadCounter; }
        @Override public void setLastReloadCounter(float value) { lastReloadCounter = value; }
        @Override public float getReloadCounter() { return reloadCounter; }
        @Override public void setReloadCounter(float value) { reloadCounter = value; }
        @Override public float getReload() { return reload; }
        @Override public float getCoolantMultiplier() { return coolantMultiplier; }
        @Override public void setCoolantMultiplier(float value) { coolantMultiplier = value; }

        // Override updateCooling as public to match interface
        @Override
        public void updateCooling() {
            super.updateCooling();
        }

        // Acceleration parameters from parent
        @Override public float getAcceleratedDelay() { return acceleratedDelay; }
        @Override public float getAcceleratedBonus() { return acceleratedBonus; }
        @Override public int getAcceleratedSteps() { return acceleratedSteps; }
        @Override public float getBurnoutDelay() { return burnoutDelay; }
        @Override public float getCooldownDelay() { return cooldownDelay; }
        @Override public boolean burnsOut() { return burnsOut; }
        @Override public boolean heatOnShoot() { return heatOnShoot; }

        @Override
        public void updateTile(){
            super.updateTile();
            updateAcceleration();
        }

        @Override
        protected void updateReload(){
            float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            reloadCounter += delta() * multiplier * accelBoost * baseReloadSpeed();
            reloadCounter = Math.min(reloadCounter, reload);
        }
    }
}