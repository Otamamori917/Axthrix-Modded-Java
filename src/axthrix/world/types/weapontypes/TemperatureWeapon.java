package axthrix.world.types.weapontypes;

import arc.Core;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import axthrix.world.types.abilities.heatbased.PassiveHeatAbility;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.meta.StatUnit;

public class TemperatureWeapon extends PerkWeapon {
    public float tempConsume = 0f;
    public boolean requiresTemp = false;
    public float reloadBonus = 1f;
    public float damageBonus = 1f;
    public float spreadBonus = 1f;

    public TemperatureWeapon(String name) {
        super(name);
        mountType = TemperatureMount::new;
    }

    public TemperatureWeapon() {
        super();
        mountType = TemperatureMount::new;
    }
    @Override
    public void addStats(UnitType unit, Table t) {
        super.addStats(unit, t);

        if (tempConsume != 0f && reload > 0f) {
            float baseShotsPerSec = 60f / reload;
            float baseTempPerSec = tempConsume * baseShotsPerSec;

            t.row();
            String basePrefix = (baseTempPerSec > 0f) ? "[red]+" : "[cyan]";
            String baseValue = TempUnit.formatDelta(baseTempPerSec);
            t.add("[lightgray]" + Core.bundle.get("stat.aj-temp-throughput") + ": " + basePrefix + baseValue + " " + StatUnit.perSecond.localized()).left();

            if (reloadBonus != 1f && reloadBonus > 0.01f) {
                float maxShotsPerSec = 60f / (reload / reloadBonus);
                float maxTempPerSec = tempConsume * maxShotsPerSec;

                t.row();
                String maxPrefix = (maxTempPerSec > 0f) ? "[red]+" : "[cyan]";
                String maxValue = TempUnit.formatDelta(maxTempPerSec);
                t.add("[lightgray]" + Core.bundle.get("stat.aj-max-temp-throughput") + ": " + maxPrefix + maxValue + " " + StatUnit.perSecond.localized()).left();
            }
        }

        if (reloadBonus != 1f) {
            t.row();
            float relPct = (reloadBonus - 1f) * 100f;
            t.add("[lightgray]" + Core.bundle.get("stat.aj-max-reload-bonus") + ": [white]" + (relPct >= 0f ? "+" : "") + Mathf.round(relPct) + "%").left();
        }

        if (damageBonus != 1f) {
            t.row();
            float dmgPct = (damageBonus - 1f) * 100f;
            t.add("[lightgray]" + Core.bundle.get("stat.aj-max-damage-bonus") + ": [white]" + (dmgPct >= 0f ? "+" : "") + Mathf.round(dmgPct) + "%").left();
        }

        if (spreadBonus != 1f) {
            t.row();
            float sprPct = (spreadBonus - 1f) * 100f;
            t.add("[lightgray]" + Core.bundle.get("stat.aj-max-spread-bonus") + ": [white]" + (sprPct >= 0f ? "+" : "") + Mathf.round(sprPct) + "%").left();
        }
    }
    @Override
    public void update(Unit unit, WeaponMount mount) {
        TemperatureMount tMount = (TemperatureMount) mount;

        float currentTemp = TemperatureLogic.getTemperatureUnit(unit);
        float alpha = 0f;

        if (tempConsume >= 0f && currentTemp > 0f) {
            alpha = Mathf.clamp(currentTemp / 100f);
        } else if (tempConsume < 0f && currentTemp < 0f) {
            alpha = Mathf.clamp(-currentTemp / 100f);
        }

        tMount.liveAlpha = alpha;

        if (alpha > 0.001f && reloadBonus != 1f) {
            float liveReloadMult = 1f + (reloadBonus - 1f) * alpha;
            if (liveReloadMult > 0.01f) {
                float r = (liveReloadMult - 1f) * unit.reloadMultiplier * arc.util.Time.delta;

                if (!alternate || otherSide == -1) {
                    mount.reload -= r;
                } else {
                    WeaponMount other = unit.mounts[otherSide];
                    other.reload -= r / 2f;
                    mount.reload -= r / 2f;

                    if (other instanceof TemperatureMount oM) {
                        oM.liveAlpha = Math.max(oM.liveAlpha, tMount.liveAlpha);
                    }
                }
            }
        }

        if (unit.isShooting() && unit.abilities != null) {
            for (int i = 0; i < unit.abilities.length; i++) {
                if (unit.abilities[i] instanceof PassiveHeatAbility passiveSkill) {
                    passiveSkill.isDealingDamage = true;
                }
            }
        }

        super.update(unit, mount);
    }

    @Override
    public void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        TemperatureMount tMount = (TemperatureMount) mount;

        float currentTemp = TemperatureLogic.getTemperatureUnit(unit);
        float cost = Math.abs(tempConsume);

        if (requiresTemp && cost > 0.001f) {
            if (tempConsume >= 0f && currentTemp < cost) return;
            if (tempConsume < 0f && (-currentTemp) < cost) return;
        }

        float baseInaccuracy = inaccuracy;
        if (tMount.liveAlpha > 0.001f && spreadBonus != 1f) {
            inaccuracy = baseInaccuracy * (1f + (spreadBonus - 1f) * tMount.liveAlpha);
        }

        boolean didShoot = mount.reload <= 0f;

        super.shoot(unit, mount, shootX, shootY, rotation);

        inaccuracy = baseInaccuracy;

        if (didShoot && tempConsume != 0f && (unit.isShooting() || mount.shoot)) {
            if (tempConsume > 0f) {
                float change = Math.min(currentTemp, tempConsume);
                TemperatureLogic.applyTemperatureUnit(unit, -change);
            } else {
                float change = Math.max(currentTemp, tempConsume);
                TemperatureLogic.applyTemperatureUnit(unit, -change);
            }
        }
    }


    @Override
    public void draw(Unit unit, WeaponMount mount) {
        super.draw(unit, mount);

        if (parts.size > 0 && mount instanceof TemperatureMount tm) {
            AxPartParms.axparams.set(0, 0, tm.liveAlpha);
        }
    }
    public static class TemperatureMount extends WeaponMount {
        public float liveAlpha = 0f;

        public TemperatureMount(Weapon weapon) {
            super(weapon);
        }
    }
}
