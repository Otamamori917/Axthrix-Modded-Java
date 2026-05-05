package axthrix.world.types.block.defense;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.content.AxFactions;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.unittypes.JormungandrUnitType;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;

public class JormungandrNest extends AxBlock {

    public float nestDuration = 3600f;
    public float attackRange = 100f;
    public float reload = 60f;
    public float attackDamage = 40f;

    @Nullable public BulletType bullet = null;
    @Nullable public UnitType abandonSpawn = null;

    public boolean isHatchlingNest = false;

    public TextureRegion coiledRegion;

    // -----------------------------------------------------------------------

    public JormungandrNest(String name) {
        super(name);
        faction.add(AxFactions.ikatusa);
        update = true;
        solid = false;
        destructible = true;
        floating = false;
        placeablePlayer = false;
        health = 400;
    }

    @Override
    public void load() {
        super.load();
        coiledRegion = arc.Core.atlas.find(name + "-coiled", region);
    }

    // -----------------------------------------------------------------------

    public class JormungandrNestBuild extends AxBlockBuild {

        public boolean drawCoiled = false;
        public float coilTick = 0f;
        public float reloadCounter = 0f;

        @Nullable public JormungandrUnitType snakeType = null;

        public void onSnakeNested(JormungandrUnitType type, Unit unit) {
            health = maxHealth;
            drawCoiled = true;
            coilTick = 0f;
            reloadCounter = 0f;
            snakeType = type;
        }

        @Override
        public void CustCollision(Unit unit) {
            if (!(unit.type instanceof JormungandrUnitType)) {
                super.CustCollision(unit);
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (!drawCoiled) return;

            coilTick += 1f;
            if (coilTick >= nestDuration) {
                uncoil();
                return;
            }

            reloadCounter += 1f;
            if (reloadCounter >= reload) {
                Unit target = Units.closestEnemy(team, x, y, attackRange, u -> !u.dead());
                if (target != null) {
                    reloadCounter = 0f;
                    fireAt(target.x, target.y, target);
                } else {
                    mindustry.gen.Building b = Units.findEnemyTile(team, x, y, attackRange, bl -> true);
                    if (b != null) {
                        reloadCounter = 0f;
                        fireAt(b.x, b.y, null);
                    }
                }
            }
        }

        protected void fireAt(float tx, float ty, @Nullable Unit unitTarget) {
            if (bullet != null) {
                bullet.create(this, team, x, y, Mathf.angle(tx - x, ty - y), 1f, 1f, null);
            } else {
                if (unitTarget != null) {
                    unitTarget.damage(attackDamage);
                } else {
                    mindustry.gen.Building b = Vars.world.buildWorld(tx, ty);
                    if (b != null && b.team != team) b.damage(attackDamage);
                }
            }
        }

        public void uncoil() {
            drawCoiled = false;
            reloadCounter = 0f;
            if (isHatchlingNest && abandonSpawn != null) {
                Unit u = abandonSpawn.create(team);
                u.set(x, y);
                u.rotation = Mathf.random(360f);
                if (!Vars.net.client()) u.add();
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (drawCoiled) {
                Draw.z(Layer.turret - 0.1f);
                Draw.rect(coiledRegion, x, y, rotation * 90f);
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(drawCoiled);
            write.f(coilTick);
            write.f(reloadCounter);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            drawCoiled = read.bool();
            coilTick = read.f();
            reloadCounter = read.f();
        }
    }
    public static class JormungandrNestYoung extends JormungandrNest {
        public JormungandrNestYoung(String name) {
            super(name);
            size = 1;
            health = 200;
            nestDuration = 2400f;
            attackRange = 70f;
            attackDamage = 25f;
            reload = 70f;
            isHatchlingNest = true;
        }
    }
    public static class JormungandrNestJuvenile extends JormungandrNest {
        public JormungandrNestJuvenile(String name) {
            super(name);
            size = 2;
            health = 350;
            nestDuration = 3000f;
            attackRange = 85f;
            attackDamage = 35f;
            reload = 65f;
            isHatchlingNest = true;
        }
    }
    public static class JormungandrNestAdult extends JormungandrNest {
        public JormungandrNestAdult(String name) {
            super(name);
            size = 4;
            health = 600;
            nestDuration = 3600f;
            attackRange = 100f;
            attackDamage = 50f;
            reload = 60f;
        }
    }
    public static class JormungandrNestElder extends JormungandrNest {
        public JormungandrNestElder(String name) {
            super(name);
            size = 6;
            health = 900;
            nestDuration = 7200f;
            attackRange = 140f;
            attackDamage = 80f;
            reload = 50f;
        }
    }
}