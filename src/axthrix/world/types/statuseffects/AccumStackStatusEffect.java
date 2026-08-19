package axthrix.world.types.statuseffects;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.AxResources;
import axthrix.content.FX.FieldEffects;
import axthrix.world.types.abilities.*;
import axthrix.world.util.AxStatValues;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.NanobotLogic;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.StatusEntry;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

public class AccumStackStatusEffect extends StackStatusEffect {
    public StatusResult result = new NoneResult();
    public boolean clearOnMaxStacks = true;

    public AccumStackStatusEffect(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(new Stat("aj-max-result"), table -> {
            table.left();
            result.display(table);
        });
    }

    @Override
    public void init(){
        super.init();
        Events.run(EventType.Trigger.update, () -> {
            AccumStackStatusEffect.FieldPulse.updateAll();
            AccumStackStatusEffect.TriggerLock.update();
            AccumStackStatusEffect.PendingExplosion.update();
        });
    }

    @Override
    protected void updateDisplayName() {
        Unit u = Vars.player.unit();
        if (u == null) {
            localizedName = localName;
            return;
        }

        boolean has = unitCharges.containsKey(u) && u.hasEffect(this);
        if (!has) {
            localizedName = localName;
            return;
        }

        int stacks = unitCharges.get(u);
        StringBuilder sb = new StringBuilder(localName);
        sb.append(" [accent]x").append(stacks).append(" | ").append(charges);

        float delay = PendingExplosion.remaining(u, this);
        float field = FieldPulse.remainingFor(u, this);
        if (delay > 0f) {
            sb.append(" [gray]|[] ").append(Strings.autoFixed(delay / 60f, 1)).append("s");
        }
        if (field > 0f) {
            sb.append(" [cyan]|[] ").append(Strings.autoFixed(field / 60f, 1)).append("s");
        }

        localizedName = sb.toString();
    }

    @Override
    public void start(Unit unit, float time) {
        boolean first = !unitCharges.containsKey(unit);

        if (result.triggerCooldown > 0f
                && !result.allowStackDuringCooldown
                && TriggerLock.isLocked(unit, this)) {
            return;
        }

        super.start(unit, time);

        if (first && unitCharges.containsKey(unit)) {
            result.applied(unit, this);
        }

        if (unitCharges.containsKey(unit)) {
            checkMax(unit);
        }
    }

    @Override
    public void update(Unit unit, StatusEntry entry) {
        super.update(unit, entry);
        if (unitCharges.containsKey(unit)) {
            result.update(unit, this, entry);
            checkMax(unit);
        }
    }

    @Override
    public void draw(Unit unit, float intensity) {
        super.draw(unit, intensity);
        if (unitCharges.containsKey(unit)) {
            result.draw(unit, this);
        }
    }

    @Override
    public void end(Unit unit) {
        result.removed(unit, this);
        super.end(unit);
    }

    protected void checkMax(Unit unit) {
        Integer c = unitCharges.get(unit);
        if (c == null || c < charges) return;

        if (result.triggerCooldown > 0f && TriggerLock.isLocked(unit, this)) return;

        if (result.triggerCooldown > 0f) {
            TriggerLock.lock(unit, this, result.triggerCooldown);
        }

        result.trigger(unit, this);

        if (clearOnMaxStacks) {
            end(unit);
            unit.unapply(this);
        } else {
            unitCharges.put(unit, 1);
        }
    }


    public abstract static class StatusResult {
        public float triggerCooldown = 0f;
        public boolean allowStackDuringCooldown = true;

        public void trigger(Unit unit, AccumStackStatusEffect parent) {}
        public void applied(Unit unit, AccumStackStatusEffect parent) {}
        public void removed(Unit unit, AccumStackStatusEffect parent) {}
        public void update(Unit unit, AccumStackStatusEffect parent, StatusEntry entry) {}
        public void draw(Unit unit, AccumStackStatusEffect parent) {}

        public void display(Table t) {
            if (triggerCooldown > 0f) {
                t.add(Core.bundle.format("stat.aj-result-cooldown",
                        Strings.autoFixed(triggerCooldown / 60f, 1) + " " + StatUnit.seconds.localized())).left().row();
                t.add(Core.bundle.get(allowStackDuringCooldown
                        ? "stat.aj-result-stack-allowed"
                        : "stat.aj-result-stack-blocked")).left().row();
            }
        }
    }

    public static class NoneResult extends StatusResult {
        @Override
        public void display(Table t) {
            t.add(Core.bundle.get("stat.aj-result-none")).left().row();
        }
    }

    public static class MultiResult extends StatusResult {
        public StatusResult[] results = {};

        @Override
        public void trigger(Unit unit, AccumStackStatusEffect parent) {
            for (StatusResult r : results) if (r != null) r.trigger(unit, parent);
        }

        @Override
        public void applied(Unit unit, AccumStackStatusEffect parent) {
            for (StatusResult r : results) if (r != null) r.applied(unit, parent);
        }

        @Override
        public void removed(Unit unit, AccumStackStatusEffect parent) {
            for (StatusResult r : results) if (r != null) r.removed(unit, parent);
        }

        @Override
        public void update(Unit unit, AccumStackStatusEffect parent, StatusEntry entry) {
            for (StatusResult r : results) if (r != null) r.update(unit, parent, entry);
        }

        @Override
        public void draw(Unit unit, AccumStackStatusEffect parent) {
            for (StatusResult r : results) if (r != null) r.draw(unit, parent);
        }

        @Override
        public void display(Table t) {
            t.add(Core.bundle.get("stat.aj-result-multi")).left().row();
            super.display(t);
            for (StatusResult r : results) if (r != null) r.display(t);
        }
    }

    public static class ExplosionResult extends StatusResult {
        public float damage = 40f;
        public float radius = 80f;
        public float splashDamageMultiplier = 0.55f;
        public float buildingDamageMultiplier = 0.55f;
        public boolean heal = false;
        public float healAmount = 20f;
        public boolean reapplySelf = false;
        public float reapplyTime = 120f;
        public float triggerDelay = 0f;
        public float knockback = 0f;
        public float temp = 0f;
        public StatusEffect status = null;
        public float statusDuration = 40f;
        public Effect effect = Fx.explosion;

        @Override
        public void trigger(Unit unit, AccumStackStatusEffect parent) {
            if (triggerDelay > 0f) {
                PendingExplosion.add(unit, parent, this, triggerDelay);
                return;
            }
            fire(unit, parent);
        }

        public void fire(Unit unit, AccumStackStatusEffect parent) {
            if (unit == null || unit.dead) return;

            effect.at(unit.x, unit.y);

            if (damage != 0f) unit.damage(damage);
            if (temp != 0f) TemperatureLogic.applyTemperatureUnit(unit, temp);
            if (status != null) unit.apply(status, statusDuration);

            if (radius <= 0f) return;

            Units.nearby(unit.team, unit.x, unit.y, radius, other -> {
                if (other == unit || other.dead) return;

                float f = 1f - Mathf.clamp(other.dst(unit) / radius);

                if (heal) {
                    other.heal(healAmount * f);
                } else {
                    other.damage(damage * splashDamageMultiplier * f);
                }

                if (temp != 0f) TemperatureLogic.applyTemperatureUnit(other, temp * f);
                if (status != null) other.apply(status, statusDuration);

                if (knockback != 0f) {
                    Tmp.v1.set(other).sub(unit.x, unit.y).nor().scl(knockback * f * 8f);
                    other.impulse(Tmp.v1);
                }

                if (reapplySelf && !other.dead) {
                    other.apply(parent, reapplyTime);
                    if (!unitCharges.containsKey(other)) {
                        unitCharges.put(other, 1);
                    } else {
                        unitCharges.put(other, unitCharges.get(other) + 1);
                    }
                    unitTime.put(other, reapplyTime);
                }
            });

            Units.nearbyBuildings(unit.x, unit.y, radius, building -> {
                if (building.dead || building.team != unit.team) return;
                if (!building.block.destructible) return;

                float f = 1f - Mathf.clamp(building.dst(unit) / radius);

                if (heal) {
                    building.heal(healAmount * f);
                } else {
                    building.damage(damage * buildingDamageMultiplier * f);
                }

                if (temp != 0f) TemperatureLogic.applyTemperatureBuilding(building, temp * f);
            });
        }

        @Override
        public void display(Table t) {
            t.add(Core.bundle.get("stat.aj-result-explosion")).left().row();
            super.display(t);
            if (damage != 0f)
                t.add(Core.bundle.format("stat.aj-result-damage", Strings.autoFixed(damage, 1))).left().row();
            if (radius > 0f)
                t.add(Core.bundle.format("stat.aj-result-radius",
                        Strings.autoFixed(radius / 8f, 1) + " " + StatUnit.blocks.localized())).left().row();
            if (triggerDelay > 0f)
                t.add(Core.bundle.format("stat.aj-result-delay",
                        Strings.autoFixed(triggerDelay / 60f, 1) + " " + StatUnit.seconds.localized())).left().row();
            if (knockback != 0f)
                t.add(Core.bundle.format("stat.aj-result-knockback", Strings.autoFixed(knockback, 1))).left().row();
            if (temp != 0f)
                t.add(Core.bundle.format("stat.aj-result-temp", TempUnit.formatDelta(temp))).left().row();
            if (status != null)
                t.add(Core.bundle.format("stat.aj-result-status", status.emoji() + " " + status.localizedName)).left().row();
            if (reapplySelf)
                t.add(Core.bundle.get("stat.aj-result-chain")).left().row();
            if (heal)
                t.add(Core.bundle.format("stat.aj-result-heal", Strings.autoFixed(healAmount, 1))).left().row();
        }
    }

    public static class FieldResult extends StatusResult {
        public float radius = 80f;
        public float duration = 240f;
        public float damagePerPulse = 12f;
        public float pulseInterval = 20f;
        public float tempPerPulse = 0f;
        public float minHealthFraction = 0f;
        public StatusEffect status = null;
        public float statusDuration = 40f;
        public Color color = Color.cyan;
        public Effect visual = FieldEffects.cryo;

        @Override
        public void trigger(Unit unit, AccumStackStatusEffect parent) {
            if (unit == null || unit.dead) return;

            visual.at(unit.x, unit.y, radius, color, new FieldData(
                    unit.team, duration, radius, damagePerPulse, pulseInterval, tempPerPulse,
                    minHealthFraction, status, statusDuration, color
            ));

            FieldPulse.active.add(new FieldPulse(
                    unit.x, unit.y, unit.team, duration, radius,
                    damagePerPulse, pulseInterval, tempPerPulse,
                    minHealthFraction, status, statusDuration,
                    unit.id, parent.id
            ));
        }

        @Override
        public void display(Table t) {
            t.add(Core.bundle.get("stat.aj-result-field")).left().row();
            super.display(t);
            if (radius > 0f)
                t.add(Core.bundle.format("stat.aj-result-radius",
                        Strings.autoFixed(radius / 8f, 1) + " " + StatUnit.blocks.localized())).left().row();
            if (duration > 0f)
                t.add(Core.bundle.format("stat.aj-result-duration",
                        Strings.autoFixed(duration / 60f, 1) + " " + StatUnit.seconds.localized())).left().row();

            float pps = pulseInterval > 0f ? 60f / pulseInterval : 0f;
            if (damagePerPulse != 0f)
                t.add(Core.bundle.format("stat.aj-result-damage",
                        Strings.autoFixed(damagePerPulse * pps, 1) + " " + StatUnit.perSecond.localized())).left().row();
            if (tempPerPulse != 0f)
                t.add(Core.bundle.format("stat.aj-result-temp",
                        TempUnit.formatDelta(tempPerPulse * pps) + " " + StatUnit.perSecond.localized())).left().row();
            if (status != null)
                t.add(Core.bundle.format("stat.aj-result-status", status.emoji() + " " + status.localizedName)).left().row();
            if (minHealthFraction > 0f)
                t.add(Core.bundle.format("stat.aj-result-min-hp",
                        Strings.autoFixed(minHealthFraction * 100f, 0) + StatUnit.percent.localized())).left().row();
        }
    }

    public static class AbilityResult extends StatusResult {
        public Ability ability;
        public boolean scaleAbility = false;
        public boolean requireMaxStacks = true;
        /** Nanobot: damage host team instead of healing. */
        public boolean inverted = false;

        private static final HashMap<Unit, Ability> map = new HashMap<>();
        private static final ObjectMap<Unit, AccumStackStatusEffect> owners = new ObjectMap<>();

        public static boolean isActive(Unit unit, AccumStackStatusEffect parent) {
            return map.containsKey(unit) && owners.get(unit) == parent;
        }

        private void grant(Unit unit, AccumStackStatusEffect parent) {
            if (ability == null || map.containsKey(unit)) return;

            Ability cpy = ability.copy();
            configure(unit, cpy);
            map.put(unit, cpy);
            owners.put(unit, parent);
        }

        private void configure(Unit unit, Ability a) {
            if (a instanceof ShieldArcAbility sa) {
                if (scaleAbility && ability instanceof ShieldArcAbility src) {
                    sa.max = (src.max / 100f) * unit.maxHealth;
                    sa.x = src.x * unit.hitSize / 8f;
                    sa.y = src.y * unit.hitSize / 8f;
                    sa.radius = src.radius * unit.hitSize / 8f;
                }
                sa.init(unit.type);
                return;
            }

            if (a instanceof NanobotAbility n) {
                n.inverted = inverted;
                if (scaleAbility) {
                    float s = unit.hitSize / 8f;
                    n.range *= s;
                    n.nanobotCount = Math.max(4, Mathf.round(n.nanobotCount * s));
                    n.damage *= Math.max(0.75f, s);
                    n.healAmount *= Math.max(0.75f, s);
                }
                return;
            }

            if (a instanceof AfterBurnAbility ab) {
                if (ability instanceof AfterBurnAbility src) {
                    ab.fuelCap = src.fuelCap;
                    ab.fuelUsedPerTick = src.fuelUsedPerTick;
                    ab.fuelRegenPerTick = src.fuelRegenPerTick;
                    ab.boostForce = src.boostForce;
                    ab.forceDirection = src.forceDirection;
                    ab.setX = src.setX;
                    ab.setY = src.setY;
                    ab.setRot = src.setRot;
                    ab.length = src.length;
                    ab.width = src.width;
                    ab.mirror = src.mirror;
                    ab.colors = src.colors;
                    ab.lengthWidthPans = src.lengthWidthPans;
                }
                if (scaleAbility) {
                    float s = unit.hitSize / 8f;
                    ab.fuelCap = Math.max(50, Mathf.round(ab.fuelCap * s));
                    ab.boostForce *= Math.max(0.75f, s);
                    ab.length *= s;
                    ab.width *= Math.max(0.75f, s);
                    ab.setX *= s;
                    ab.setY *= s;
                }
            }
        }

        @Override
        public void applied(Unit unit, AccumStackStatusEffect parent) {
            if (!requireMaxStacks) grant(unit, parent);
        }

        @Override
        public void trigger(Unit unit, AccumStackStatusEffect parent) {
            if (requireMaxStacks) grant(unit, parent);
        }

        @Override
        public void update(Unit unit, AccumStackStatusEffect parent, StatusEntry entry) {
            Ability a = map.get(unit);
            if (a != null) a.update(unit);
        }

        @Override
        public void draw(Unit unit, AccumStackStatusEffect parent) {
            Ability a = map.get(unit);
            if (a != null) a.draw(unit);
        }

        @Override
        public void removed(Unit unit, AccumStackStatusEffect parent) {
            Ability a = map.remove(unit);
            owners.remove(unit);
            if (a instanceof NanobotAbility) {
                NanobotLogic.removeNanobots(unit);
            }
        }

        @Override
        public void display(Table t) {
            t.add(Core.bundle.get("stat.aj-result-ability")).left().row();
            super.display(t);

            if (inverted) {
                t.add(Core.bundle.get("stat.aj-result-ability-inverted")).left().row();
            }

            if (ability != null) {
                t.add(ability.localized()).left().row();
                ability.addStats(t);
                t.row();
            }

            t.add(Core.bundle.get(requireMaxStacks
                    ? "stat.aj-result-on-max"
                    : "stat.aj-result-while-active")).left().row();
        }
    }

    public static class FragResult extends StatusResult {
        public BulletType fragBullet;
        public int fragBullets = 1;
        public float fragRandomSpread = 360f;
        public boolean requireMaxStacks = true;

        private static final ObjectMap<Unit, Boolean> armed = new ObjectMap<>();
        private static final ObjectMap<Unit, AccumStackStatusEffect> owners = new ObjectMap<>();
        private static final Seq<Bullet> tagged = new Seq<>();
        private static boolean listener;

        public static boolean isArmed(Unit unit, AccumStackStatusEffect parent) {
            return armed.get(unit, false) && owners.get(unit) == parent;
        }

        private static void ensureListener() {
            if (listener) return;
            listener = true;
            Events.run(EventType.Trigger.update, () -> {
                if (Vars.state.isPaused()) return;
                Seq<Bullet> remove = new Seq<>();
                tagged.each(b -> {
                    if (!b.isAdded()) {
                        if (b.data instanceof FragConfig cfg && cfg.fragBullet != null && !Vars.net.client()) {
                            for (int i = 0; i < cfg.fragBullets; i++) {
                                cfg.fragBullet.createNet(
                                        b.team, b.x, b.y,
                                        Mathf.random(cfg.fragRandomSpread) - cfg.fragRandomSpread / 2f + b.rotation(),
                                        1f, 1f, 1f
                                );
                            }
                        }
                        remove.add(b);
                    }
                });
                remove.each(tagged::remove);
            });
        }

        static class FragConfig {
            BulletType fragBullet;
            int fragBullets;
            float fragRandomSpread;
        }

        private void arm(Unit unit, AccumStackStatusEffect parent) {
            armed.put(unit, true);
            owners.put(unit, parent);
            ensureListener();
        }

        @Override
        public void applied(Unit unit, AccumStackStatusEffect parent) {
            if (!requireMaxStacks) arm(unit, parent);
        }

        @Override
        public void trigger(Unit unit, AccumStackStatusEffect parent) {
            if (requireMaxStacks) arm(unit, parent);
        }

        @Override
        public void update(Unit unit, AccumStackStatusEffect parent, StatusEntry entry) {
            if (fragBullet == null || Vars.net.client()) return;
            if (requireMaxStacks && !armed.get(unit, false)) return;

            FragConfig cfg = new FragConfig();
            cfg.fragBullet = fragBullet;
            cfg.fragBullets = fragBullets;
            cfg.fragRandomSpread = fragRandomSpread;

            Groups.bullet.intersect(
                    unit.x - unit.hitSize * 2f, unit.y - unit.hitSize * 2f,
                    unit.hitSize * 4f, unit.hitSize * 4f,
                    b -> {
                        if (b.owner == unit && !tagged.contains(b)) {
                            b.data = cfg;
                            tagged.add(b);
                        }
                    }
            );
        }

        @Override
        public void removed(Unit unit, AccumStackStatusEffect parent) {
            armed.remove(unit);
            owners.remove(unit);
        }

        @Override
        public void display(Table t) {
            t.add(Core.bundle.get("stat.aj-result-frag")).left().row();
            super.display(t);
            if (fragBullet != null) {
                t.add(Core.bundle.format("stat.aj-result-frag-count", fragBullets)).left().row();
                t.table(bt -> {
                    bt.left().defaults().left();
                    AxStatValues.appendCustomStats(bt, fragBullet, AxResources.nothingness, true);
                }).left().padLeft(8f).row();
            }
            t.add(Core.bundle.get(requireMaxStacks
                    ? "stat.aj-result-on-max"
                    : "stat.aj-result-while-active")).left().row();
        }
    }

    public static class HealthTriggerResult extends StatusResult {
        public float activationThreshold = 0.5f;
        public boolean activationRepair = false;
        public float activationRepairAmount = 0.5f;
        public boolean kills = false;
        public float activationResistanceTime = 60f;
        public Effect activationFx = Fx.none;
        public StatusResult onActivate = null;
        public boolean requireMaxStacks = true;

        private static final ObjectMap<Unit, HealthTriggerResult> active = new ObjectMap<>();
        private static final ObjectMap<Unit, AccumStackStatusEffect> owners = new ObjectMap<>();
        private static boolean listener;

        public static boolean isArmed(Unit unit, AccumStackStatusEffect parent) {
            return active.get(unit) != null && owners.get(unit) == parent;
        }

        private static void ensureListener() {
            if (listener) return;
            listener = true;
            Events.run(EventType.Trigger.update, () -> {
                Seq<Unit> done = new Seq<>();
                active.each((unit, ht) -> {
                    if (unit == null || !unit.isValid() || unit.dead) {
                        done.add(unit);
                        return;
                    }
                    AccumStackStatusEffect parent = owners.get(unit);
                    if (parent == null) {
                        done.add(unit);
                        return;
                    }
                    if (unit.health >= unit.maxHealth * ht.activationThreshold) return;

                    if (ht.activationFx != null) ht.activationFx.at(unit.x, unit.y);

                    if (ht.kills) {
                        unit.kill();
                    } else {
                        if (ht.activationRepair) unit.heal(unit.maxHealth * ht.activationRepairAmount);
                        unit.dead = false;
                        unit.apply(StatusEffects.invincible, ht.activationResistanceTime);
                    }

                    if (ht.onActivate != null) ht.onActivate.trigger(unit, parent);

                    done.add(unit);
                    parent.end(unit);
                    unit.unapply(parent);
                });
                done.each(u -> {
                    active.remove(u);
                    owners.remove(u);
                });
            });
        }

        private void arm(Unit unit, AccumStackStatusEffect parent) {
            active.put(unit, this);
            owners.put(unit, parent);
            ensureListener();
        }

        @Override
        public void applied(Unit unit, AccumStackStatusEffect parent) {
            if (!requireMaxStacks) arm(unit, parent);
        }

        @Override
        public void trigger(Unit unit, AccumStackStatusEffect parent) {
            if (requireMaxStacks) arm(unit, parent);
        }

        @Override
        public void removed(Unit unit, AccumStackStatusEffect parent) {
            if (!isArmed(unit, parent)) {
                active.remove(unit);
                owners.remove(unit);
            }
        }

        @Override
        public void display(Table t) {
            t.add(Core.bundle.get("stat.aj-result-health")).left().row();
            super.display(t);
            t.add(Core.bundle.format("stat.aj-result-health-threshold",
                    Strings.autoFixed(activationThreshold * 100f, 0) + StatUnit.percent.localized())).left().row();
            if (kills) {
                t.add(Core.bundle.get("stat.aj-result-health-kill")).left().row();
            } else if (activationRepair) {
                t.add(Core.bundle.format("stat.aj-result-health-repair",
                        Strings.autoFixed(activationRepairAmount * 100f, 0) + StatUnit.percent.localized())).left().row();
            }
            t.add(Core.bundle.get(requireMaxStacks
                    ? "stat.aj-result-on-max"
                    : "stat.aj-result-while-active")).left().row();
            if (onActivate != null) {
                t.add(Core.bundle.get("stat.aj-result-health-also")).left().row();
                onActivate.display(t);
            }
        }
    }


    public static class FieldPulse {
        public static final Seq<FieldPulse> active = new Seq<>();

        float x, y, timeLeft, radius, damage, pulseInterval, pulseTimer, temp;
        float minHp, statusDuration;
        StatusEffect status;
        boolean done;
        Team team;
        int hostUnitId, statusId;

        public FieldPulse(float x, float y, Team team, float dur, float r, float dmg, float pulse,
                          float temp, float minHp, StatusEffect status, float statusDur,
                          int hostUnitId, int statusId) {
            this.x = x;
            this.y = y;
            this.team = team;
            this.timeLeft = dur;
            this.radius = r;
            this.damage = dmg;
            this.pulseInterval = pulse;
            this.temp = temp;
            this.minHp = minHp;
            this.status = status;
            this.statusDuration = statusDur;
            this.hostUnitId = hostUnitId;
            this.statusId = statusId;
        }

        void update() {
            timeLeft -= Time.delta;
            if (timeLeft <= 0f) {
                done = true;
                return;
            }

            pulseTimer += Time.delta;
            if (pulseTimer < pulseInterval) return;
            pulseTimer = 0f;

            Units.nearby(team, x, y, radius, u -> {
                if (u.dead) return;
                if (minHp > 0f && u.healthf() < minHp) return;
                float f = 1f - Mathf.clamp(u.dst(x, y) / radius);
                u.damage(damage * f);
                if (temp != 0f) TemperatureLogic.applyTemperatureUnit(u, temp * f);
                if (status != null) u.apply(status, statusDuration);
            });

            Units.nearbyBuildings(x, y, radius, building -> {
                if (building.dead || building.team != team) return;
                if (!building.block.destructible) return;
                if (minHp > 0f && building.healthf() < minHp) return;
                float f = 1f - Mathf.clamp(building.dst(x, y) / radius);
                building.damage(damage * f);
                if (temp != 0f) TemperatureLogic.applyTemperatureBuilding(building, temp * f);
            });
        }

        public static void updateAll() {
            active.removeAll(f -> {
                f.update();
                return f.done;
            });
        }

        public static float remainingFor(Unit unit, AccumStackStatusEffect status) {
            float max = 0f;
            for (FieldPulse f : active) {
                if (f.hostUnitId == unit.id && f.statusId == status.id) {
                    max = Math.max(max, f.timeLeft);
                }
            }
            return max;
        }
    }

    public static class FieldData {
        public Team team;
        public float duration, radius, damage, pulse, temp, minHp, statusDur;
        public StatusEffect status;
        public Color color;

        public FieldData(Team team, float duration, float radius, float damage, float pulse, float temp,
                         float minHp, StatusEffect status, float statusDur, Color color) {
            this.team = team;
            this.duration = duration;
            this.radius = radius;
            this.damage = damage;
            this.pulse = pulse;
            this.temp = temp;
            this.minHp = minHp;
            this.status = status;
            this.statusDur = statusDur;
            this.color = color;
        }
    }

    public static class TriggerLock {
        private static final ObjectMap<Long, Float> until = new ObjectMap<>();

        private static long key(Unit unit, AccumStackStatusEffect status) {
            return (((long) unit.id) << 32) ^ (status.id & 0xffffffffL);
        }

        public static void lock(Unit unit, AccumStackStatusEffect status, float duration) {
            until.put(key(unit, status), Time.time + duration);
        }

        public static boolean isLocked(Unit unit, AccumStackStatusEffect status) {
            return remaining(unit, status) > 0f;
        }

        public static float remaining(Unit unit, AccumStackStatusEffect status) {
            Float t = until.get(key(unit, status));
            if (t == null) return 0f;
            float left = t - Time.time;
            if (left <= 0f) {
                until.remove(key(unit, status));
                return 0f;
            }
            return left;
        }

        public static void update() {
            Seq<Long> remove = new Seq<>();
            until.each((k, t) -> {
                if (t == null || Time.time >= t) remove.add(k);
            });
            remove.each(until::remove);
        }
    }


    public static class PendingExplosion {
        private static final Seq<PendingExplosion> list = new Seq<>();

        Unit unit;
        AccumStackStatusEffect parent;
        ExplosionResult result;
        float timer;

        PendingExplosion(Unit unit, AccumStackStatusEffect parent, ExplosionResult result, float delay) {
            this.unit = unit;
            this.parent = parent;
            this.result = result;
            this.timer = delay;
        }

        public static void add(Unit unit, AccumStackStatusEffect parent, ExplosionResult result, float delay) {
            list.add(new PendingExplosion(unit, parent, result, delay));
        }

        public static float remaining(Unit unit, AccumStackStatusEffect status) {
            float max = 0f;
            for (PendingExplosion p : list) {
                if (p.unit == unit && p.parent == status) max = Math.max(max, p.timer);
            }
            return max;
        }

        public static void update() {
            list.removeAll(p -> {
                if (p.unit == null || p.unit.dead) return true;
                p.timer -= Time.delta;
                if (p.timer <= 0f) {
                    p.result.fire(p.unit, p.parent);
                    return true;
                }
                return false;
            });
        }
    }
}