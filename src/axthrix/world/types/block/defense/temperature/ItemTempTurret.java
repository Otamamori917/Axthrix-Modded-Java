package axthrix.world.types.block.defense.temperature;

import arc.math.Mathf;
import arc.util.Time;
import axthrix.world.types.block.defense.parent.AxItemTurret;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.entities.bullet.BulletType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class ItemTempTurret extends AxItemTurret {

    public float temperatureOutput = 3f;
    public float lockOutThreshold = 80f;

    public boolean hardLockout = false;
    public float selfCooldownTime = 60f * 8f;
    public float sinkEffectiveness = 1f;

    public ItemTempTurret(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.output, table -> {
            String prefix = (temperatureOutput > 0) ? "[red]+" : "[cyan]";
            String value = TempUnit.formatDelta(temperatureOutput * (60f / reload));
            table.add(prefix + value + StatUnit.perSecond.localized());
        });

        if (lockOutThreshold != 0) {
            stats.add(new Stat("aj-lockout"), table -> {
                String prefix = (lockOutThreshold > 0) ? "[red]+" : "[cyan]";
                table.add(prefix + TempUnit.format(lockOutThreshold));
            });
        }

        stats.add(Stat.heatCapacity, table -> {
            if (temperatureOutput > 0) {
                table.add("[red]" + TempUnit.format(maxTempStorage));
            } else {
                table.add("[cyan]" + TempUnit.format(-maxTempStorage));
            }
        });
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("temperature", (ItemTempTurretBuild entity) -> new TemperatureBar("Temp", entity));
    }

    public class ItemTempTurretBuild extends AxItemTurret.AxItemTurretBuild {
        public boolean lockedOut = false;

        @Override
        public void updateTile() {
            super.updateTile();

            float directed = TemperatureLogic.getDirectedTemperature(this, temperatureOutput);

            if (hardLockout) {
                if (lockedOut) {
                    if (Math.abs(directed) <= 0.01f) {
                        lockedOut = false;
                    } else if (selfCooldownTime > 0f) {
                        float step = (Math.abs(lockOutThreshold) / selfCooldownTime) * Time.delta;
                        TemperatureLogic.drainTemperatureTowardZero(this, temperatureOutput > 0, step);
                    }
                } else if (lockOutThreshold != 0f && Math.abs(directed) >= Math.abs(lockOutThreshold)) {
                    lockedOut = true;
                }
            }
        }

        @Override
        protected void shoot(BulletType type) {
            float directed = TemperatureLogic.getDirectedTemperature(this, temperatureOutput);

            if (hardLockout) {
                if (lockedOut) return;
            } else if (lockOutThreshold != 0f && directed >= Math.abs(lockOutThreshold)) {
                return;
            }

            super.shoot(type);
            TemperatureLogic.applyTemperatureBuilding(this, temperatureOutput);
        }

        public float getSinkEffectiveness() {
            return sinkEffectiveness;
        }

        @Override
        public void draw() {
            super.draw();
            float directed = TemperatureLogic.getDirectedTemperature(this, temperatureOutput);
            AxPartParms.axparams.set(0, 0, Mathf.clamp(directed / maxTempStorage));
        }
    }
}