package axthrix.world.types.block.defense;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.util.AxStatValues;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.part.DrawPart;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.tilesize;

public class TransformingTurret extends Turret {
    public Form[] forms = {};
    public float transformTime = 60f;
    public TextureRegion baseRegion;
    /** How fast smoothReloadProgress eases toward the instant reload progress each tick, used for
     * DrawPart's "smoothReload" param. Higher = snappier, lower = more eased/laggy. */
    public float smoothReloadSpeed = 0.15f;

    public TransformingTurret(String name) {
        super(name);
        configurable = true;

        config(Integer.class, (TransformingTurretBuild build, Integer value) -> {
            build.changeForm(value);
        });
    }

    public static class Form {
        public String name;
        public String displayName;
        public String attackName;
        public BulletType bullet;
        public float range;
        public float reload;
        public float damage;
        public float recoil;
        public TextureRegion region;
        public TextureRegion outline;
        /** Dedicated icon shown in the stats dropdown - separate from the in-world region since
         * that one's often a turret-mounted part not meant to stand alone as an icon. Needs a
         * "<prefix>-<name>-display" sprite; falls back to the normal region if that's missing. */
        public TextureRegion displayRegion;
        public Seq<DrawPart> parts = new Seq<>();

        public Form(String name, String displayName, String attackName, BulletType bullet, float range, float reload, float damage, float recoil) {
            this.name = name;
            this.displayName = displayName;
            this.attackName = attackName;
            this.bullet = bullet;
            this.range = range;
            this.reload = reload;
            this.damage = damage;
            this.recoil = recoil;
        }

        public void load(String prefix) {
            this.region = Core.atlas.find(prefix + "-" + name, Core.atlas.find(name));
            this.outline = Core.atlas.find(prefix + "-" + name + "-outline");
            this.displayRegion = Core.atlas.find(prefix + "-" + name + "-display", this.region);
            for (DrawPart part : parts) {
                part.turretShading = true;
                part.load(prefix);
            }
        }

    }

    @Override
    public void init() {
        super.init();
        if (forms.length > 0) {
            this.range = forms[0].range;
            this.reload = Float.MAX_VALUE;
        }
    }



    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find(name + "-base", Core.atlas.find("base"+size));
        for (Form f : forms) {
            f.load(name);
        }
    }


    @Override
    public void setBars() {
        super.setBars();
        addBar("formProgress", (TransformingTurretBuild b) -> new Bar(
                () -> b.currentFormIndex == b.targetFormIndex ?
                        forms[b.currentFormIndex].displayName :
                        Core.bundle.get("ui.transforming", "Transforming..."),
                () -> Pal.accent,
                () -> b.formTime / transformTime
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.ammo);

        stats.add(Stat.ammo, table -> {
            table.row();

            for(Form form : forms) {
                table.table(Styles.grayPanel, ft -> {
                    ft.left().top().defaults().padRight(3).left();

                    ft.table(title -> {
                        if(form.displayRegion != null && form.displayRegion.found()) {
                            title.image(form.displayRegion).size(40f).padRight(4).right().top().scaling(Scaling.fit);
                        }
                        title.table(n -> {
                            n.add(form.displayName);
                            n.row();
                            n.add("[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60f / form.reload, 2) + " " + StatUnit.perSecond.localized());
                            n.row();
                            n.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.autoFixed(form.range / tilesize, 1) + " " + StatUnit.blocks.localized());
                        }).padLeft(8f).left().top();
                    });
                    ft.row();

                    Table bulletBody = new Table();
                    StatValues.ammo(ObjectMap.of(this, form.bullet), true, false).display(bulletBody);
                    AxStatValues.appendCustomStats(bulletBody, form.bullet);

                    Collapser coll = new Collapser(bulletBody, true);
                    coll.setDuration(0.1f);

                    ft.table(dt -> {
                        dt.left().defaults().left();
                        dt.add(Core.bundle.get("stat.aj-transform-bulletstats", "Bullet Stats"));
                        dt.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false))
                                .update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen))
                                .size(8).padLeft(16f).expandX();
                    });
                    ft.row();
                    ft.add(coll);
                }).growX().pad(5).margin(10);
                table.row();
            }
        });
    }

    public class TransformingTurretBuild extends TurretBuild {
        public int currentFormIndex = 0;
        public int targetFormIndex = 0;
        public float formTime = transformTime;
        public float customReloadTimer = 0f;
        public float smoothReloadProgress = 0f;

        public void changeForm(int index) {
            if (index < 0 || index >= forms.length || index == currentFormIndex) return;
            targetFormIndex = index;
            formTime = 0f;
        }
        @Override
        public float progress(){
            Form activeForm = forms[currentFormIndex];
            return Mathf.clamp(customReloadTimer / activeForm.reload);
        }

        @Override
        public void updateTile() {
            if (!isControlled() && hasAmmo() && target != null) {
                float dist = dst(target);
                int bestForm = 0;

                for (int i = 1; i < forms.length; i++) {
                    if (dist <= forms[i].range) {
                        bestForm = i;
                        break;
                    }
                }

                if (bestForm != targetFormIndex) {
                    configure(bestForm);
                }
            } else if (!isControlled() && target == null && targetFormIndex != 0) {
                configure(0);
            }

            Form activeForm = forms[currentFormIndex];
            TransformingTurret.this.range = activeForm.range;

            super.updateTile();

            if (currentFormIndex == targetFormIndex) {
                if (customReloadTimer < activeForm.reload) {
                    customReloadTimer += Time.delta * edelta();
                }

                if (isShooting() && isActive() && customReloadTimer >= activeForm.reload && hasAmmo()) {
                    BulletType type = useAmmo();
                    shoot(type);
                    customReloadTimer = 0f;
                }
            }

            smoothReloadProgress = Mathf.lerpDelta(smoothReloadProgress, progress(), smoothReloadSpeed);

            if (formTime < transformTime) {
                formTime += Time.delta;
                if (formTime >= transformTime) {
                    currentFormIndex = targetFormIndex;
                }
            }
        }

        @Override
        public BulletType useAmmo() {
            return forms[currentFormIndex].bullet;
        }

        @Override
        public BulletType peekAmmo() {
            return forms[currentFormIndex].bullet;
        }

        @Override
        public boolean hasAmmo() {
            return true;
        }

        @Override
        public void draw() {
            Draw.z(Layer.block);
            Draw.rect(baseRegion, x, y);
            Draw.z(Layer.turret);
            Drawf.shadow(x, y, size * 4f);

            Form currentForm = forms[currentFormIndex];
            Form targetForm = forms[targetFormIndex];

            if (formTime >= transformTime) {
                if (currentForm.outline != null && currentForm.outline.found()) {
                    Draw.rect(currentForm.outline, x + recoilOffset.x, y + recoilOffset.y, drawrot());
                }
                if (currentForm.region != null && currentForm.region.found()) {
                    Draw.rect(currentForm.region, x + recoilOffset.x, y + recoilOffset.y, drawrot());
                }
                drawNativeParts(currentForm, 1f);
            } else {
                float progress = formTime / transformTime;

                Draw.color(Color.white, 1f - progress);
                if (currentForm.outline != null && currentForm.outline.found()) {
                    Draw.rect(currentForm.outline, x + recoilOffset.x, y + recoilOffset.y, drawrot());
                }
                if (currentForm.region != null && currentForm.region.found()) {
                    Draw.rect(currentForm.region, x + recoilOffset.x, y + recoilOffset.y, drawrot());
                }
                drawNativeParts(currentForm, 1f - progress);

                Draw.color(Color.white, progress);
                if (targetForm.outline != null && targetForm.outline.found()) {
                    Draw.rect(targetForm.outline, x + recoilOffset.x, y + recoilOffset.y, drawrot());
                }
                if (targetForm.region != null && targetForm.region.found()) {
                    Draw.rect(targetForm.region, x + recoilOffset.x, y + recoilOffset.y, drawrot());
                }
                drawNativeParts(targetForm, progress);
            }
            Draw.reset();
        }

        private void drawNativeParts(Form form, float alphaModifier) {
            if (form.parts.isEmpty()) return;

            float prevProgress = progress();
            var params = DrawPart.params.set(
                    warmup(),
                    1f - prevProgress,
                    1f - smoothReloadProgress,
                    heat,
                    curRecoil,
                    charge,
                    x + recoilOffset.x,
                    y + recoilOffset.y,
                    rotation
            );

            for (DrawPart part : form.parts) {
                params.setRecoil(part.recoilIndex >= 0 && curRecoils != null ? curRecoils[part.recoilIndex] : curRecoil);
                Draw.color(Color.white, alphaModifier);
                part.draw(params);
            }
            Draw.reset();
        }

        @Override
        public void buildConfiguration(Table table) {
            table.background(Tex.buttonEdge3);
            for (int i = 0; i < forms.length; i++) {
                final int index = i;
                table.button(forms[i].displayName, () -> {
                    configure(index);
                }).size(120f, 40f).disabled(currentFormIndex != targetFormIndex).pad(4);
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(currentFormIndex);
            write.i(targetFormIndex);
            write.f(formTime);
            write.f(customReloadTimer);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            currentFormIndex = read.i();
            targetFormIndex = read.i();
            formTime = read.f();
            customReloadTimer = read.f();
        }
    }
}
