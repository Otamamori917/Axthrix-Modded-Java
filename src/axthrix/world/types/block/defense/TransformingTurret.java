package axthrix.world.types.block.defense;

import arc.*;
import arc.func.Floatf;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;

public class TransformingTurret extends PowerTurret {

    public Form[] forms;
    public Form baseForm = new Form("base", "Base", "None", null, 0f, 0f, 0f, 0f);

    public float transitionDuration = 60f;

    public TransformingTurret(String name) {
        super(name);
        requirements(Category.turret, ItemStack.with(Items.copper, 80, Items.lead, 60, Items.silicon, 40));
        size = 3;
        rotateSpeed = 6f;
        coolantMultiplier = 1.3f;
        range = 160f;
        configurable = true;
    }

    @Override
    public void load() {
        super.load();

        baseForm.region = Core.atlas.find(name + "-base-form");

        if (forms != null) {
            for (Form f : forms) {
                f.region = Core.atlas.find(name + "-" + f.spriteName);
                f.frames = new TextureRegion[5];

                for (int i = 0; i < f.frames.length; i++) {
                    f.frames[i] = Core.atlas.find(name + "-" + f.spriteName + "-" + (i + 1), f.region);
                }

                if (f.parts != null) {
                    for (FormPart p : f.parts) {
                        p.region = Core.atlas.find(name + "-" + f.spriteName + "-" + p.name);
                    }
                }
            }
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.ammo);

        if (forms == null || forms.length == 0) return;

        stats.add(Stat.input, "Transforming Forms");

        for (Form f : forms) {
            stats.add(Stat.output, "[accent]" + f.displayName + "[]\n" +
                    "• Bullet: " + f.bulletDisplayName + "\n" +
                    "• Range: " + (int) f.range + "\n" +
                    "• Reload: " + Strings.fixed(f.reload / 60f, 1) + " s\n" +
                    "• Spread: ±" + f.spread + "°\n" +
                    "• Power Use: " + f.powerUse + "/s");
        }
    }

    // ====================== FORM & PART CLASSES ======================
    public static class Form {
        public final String spriteName;
        public final String displayName;
        public final String bulletDisplayName;

        public BulletType bullet;
        public float range, spread, reload, powerUse;

        public TextureRegion region;
        public TextureRegion[] frames;
        public FormPart[] parts;

        public Form(String spriteName, String displayName, String bulletDisplayName,
                    BulletType bullet, float range, float spread, float reload, float powerUse) {
            this.spriteName = spriteName;
            this.displayName = displayName;
            this.bulletDisplayName = bulletDisplayName;
            this.bullet = bullet != null ? bullet : Bullets.placeholder;
            this.range = range;
            this.spread = spread;
            this.reload = reload;
            this.powerUse = powerUse;
        }
    }

    public static class FormPart {
        public String name;
        public TextureRegion region;

        public float x = 0f, y = 0f;
        public float rotationOffset = 0f;
        public boolean mirror = false;

        public float heatLerp = 0f;
        public float pulseSpeed = 0f;
        public float recoilAmount = 0f;

        public Floatf<Float> progressFunc = t -> t;

        public FormPart(String name) {
            this.name = name;
        }
    }

    public class TransformingTurretBuild extends PowerTurretBuild {
        public int currentForm = 0;
        public int targetForm = 0;
        public float transitionTime = 0f;
        public float reloadCounter = 0f;
        public float heat = 0f;

        private float switchCooldown = 0f;

        @Override
        public boolean isShooting() {
            return transitionTime <= 0f && super.isShooting();
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (forms == null || forms.length == 0) return;

            if (!isControlled() && target == null) {
                chooseHighestRangeForm();
            }
            else if (!isControlled() && target != null) {
                chooseBestFormForTarget();
            }

            if (currentForm != targetForm) {
                transitionTime += Time.delta;
                switchCooldown = 30f;

                if (transitionTime >= transitionDuration) {
                    currentForm = targetForm;
                    transitionTime = 0f;
                }
            } else {
                transitionTime = 0f;
            }

            if (switchCooldown > 0) switchCooldown -= Time.delta;

            Form form = forms[currentForm];
            boolean isTransforming = transitionTime > 0f;

            if (!isTransforming && hasAmmo() && isShooting() && reloadCounter <= 0f) {
                shootPublic(form.bullet);
                reloadCounter = form.reload;
                heat = 1f;
            }

            reloadCounter -= edelta() * coolantMultiplier;
            heat = Mathf.lerpDelta(heat, 0f, 0.12f);
        }

        private void chooseHighestRangeForm() {
            if (switchCooldown > 0) return;

            float bestRange = -1f;
            int bestIndex = currentForm;

            for (int i = 0; i < forms.length; i++) {
                Form f = forms[i];
                if (f.range > bestRange) {
                    bestRange = f.range;
                    bestIndex = i;
                }
            }

            if (bestIndex != targetForm) {
                targetForm = bestIndex;
                transitionTime = 0f;
            }
        }

        private void chooseBestFormForTarget() {
            if (switchCooldown > 0) return;

            float bestRange = Float.MAX_VALUE;
            int best = currentForm;

            for (int i = 0; i < forms.length; i++) {
                Form f = forms[i];
                if (f.range >= dst(target) && f.range < bestRange) {
                    bestRange = f.range;
                    best = i;
                }
            }

            if (best != targetForm) {
                targetForm = best;
                transitionTime = 0f;
            }
        }

        public void shootPublic(BulletType type){
            shoot(type);
        }

        @Override
        protected void shoot(BulletType type) {
            if (forms == null) return;
            if (transitionTime > 0f) return;

            Form form = forms[currentForm];

            BulletType originalShootType = shootType;
            float originalInaccuracy = inaccuracy;

            shootType = form.bullet;
            inaccuracy = form.spread;

            super.shoot(type);

            shootType = originalShootType;
            inaccuracy = originalInaccuracy;
        }

        @Override
        public BulletType peekAmmo() {
            if (forms == null || currentForm >= forms.length) return Bullets.placeholder;
            return forms[currentForm].bullet;
        }

        @Override
        public float range() {
            if (forms == null || currentForm >= forms.length) return 100f;
            return forms[currentForm].range;
        }

        @Override
        public void draw() {
            Draw.rect(baseForm.region, x, y, rotation - 90);

            if (forms == null || currentForm >= forms.length) return;

            Form form = forms[currentForm];
            float transitionProgress = transitionTime / transitionDuration;

            if (transitionTime <= 0.01f) {
                Draw.rect(form.region, x, y, rotation - 90);
            } else {
                if (transitionProgress < 0.5f) {
                    int frameIndex = (int) ((1f - transitionProgress * 2f) * (form.frames.length - 1));
                    Draw.rect(form.frames[Math.max(0, frameIndex)], x, y, rotation - 90);
                } else {
                    int newIdx = targetForm;
                    TextureRegion[] newFrames = forms[newIdx].frames;
                    int frameIndex = (int) ((transitionProgress - 0.5f) * 2f * (newFrames.length - 1));
                    Draw.rect(newFrames[Math.min(newFrames.length - 1, frameIndex)], x, y, rotation - 90);
                }
            }

            if (form.parts != null) {
                float formAlpha = 1f;
                if (transitionTime > 0f) {
                    formAlpha = (transitionProgress < 0.5f) ? (1f - transitionProgress * 2f) : (transitionProgress - 0.5f) * 2f;
                }

                for (FormPart part : form.parts) {
                    if (part.region == null) continue;

                    float rx = x + Angles.trnsx(rotation, part.x, part.y);
                    float ry = y + Angles.trnsy(rotation, part.x, part.y);
                    float rrot = rotation - 90 + part.rotationOffset;

                    float alpha = formAlpha;
                    float scale = 1f;
                    float extraY = 0f;

                    if (part.heatLerp > 0) {
                        alpha *= Mathf.lerp(0.4f, 1f, heat * part.heatLerp);
                    }

                    if (part.pulseSpeed > 0) {
                        scale = 1f + Mathf.absin(Time.time, part.pulseSpeed, 0.12f);
                    }

                    if (part.recoilAmount > 0) {
                        extraY = -heat * part.recoilAmount;
                    }

                    float customProgress = part.progressFunc.get(Time.time / 60f);
                    alpha *= Mathf.clamp(customProgress);

                    // Rotate extraY offset into world space along the turret's backward axis
                    float wx = rx + Angles.trnsx(rotation + 180f, extraY);
                    float wy = ry + Angles.trnsy(rotation + 180f, extraY);

                    Draw.alpha(alpha);
                    Draw.rect(part.region, wx, wy, rrot);

                    if (part.mirror) {
                        Draw.rect(part.region, wx, wy, -rrot);
                    }

                    Draw.alpha(1f);
                }
            }
        }

        @Override
        public boolean configTapped() {
            return forms != null && forms.length > 0;
        }

        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            if (forms == null) return;

            for (int i = 0; i < forms.length; i++) {
                final int idx = i;
                table.button(forms[i].displayName, () -> configure(idx))
                        .size(70).pad(5)
                        .tooltip("Switch to " + forms[i].displayName);
            }
        }

        @Override
        public void configure(Object value) {
            if (value instanceof Integer i && forms != null && i >= 0 && i < forms.length) {
                targetForm = i;
                transitionTime = 0f;
            }
        }
    }
}