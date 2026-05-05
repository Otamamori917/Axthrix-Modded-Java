package axthrix.world.types.bulletypes;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.types.block.defense.TransformingTurret;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class HarpoonBulletType extends BulletType {

    private static final ObjectMap<Integer, HarpoonData> activeHarpoons = new ObjectMap<>();
    private static final Seq<RetractData> activeRetracts = new Seq<>();

    public float grabDuration = 200f;
    public float holdDamage = 8f;
    public float holdDamageInterval = 15f;
    public float reelSpeed = 7.5f;
    public float switchToJawsDistance = 35f;
    public float resistancePerHitSize = 0.15f;

    public Color chainColor = Color.darkGray.cpy().add(Color.violet);
    public float chainWidthScale = 0.6f;
    public float chainHeightScale = 0.6f;
    public Color harpoonColor = Color.darkGray.cpy().add(Color.purple);

    public HarpoonBulletType() {
        speed = 5.8f;
        lifetime = 45f;
        damage = 45f;
        collides = collidesAir = collidesGround = true;
        keepVelocity = false;
        hittable = false;
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);

        if (!(entity instanceof Unit target)) return;
        if (!(b.owner instanceof Posc owner)) return;

        if (target.team == b.team) {
            Fx.hitBulletSmall.at(target.x, target.y);
            return;
        }
        float worldHitAngle = Mathf.angle(b.x - target.x, b.y - target.y);
        float localAngle = worldHitAngle - target.rotation;
        float hitDistance = Mathf.dst(b.x, b.y, target.x, target.y);

        if (isHarpooned(target)) {
            HarpoonData existing = activeHarpoons.get(target.id);
            if (existing != null) {
                startRetract(
                        existing.owner.x(), existing.owner.y(),
                        target.x + Tmp.v1.trns(existing.localAngle + target.rotation, existing.hitDistance).x,
                        target.y + Tmp.v1.y,
                        existing.reelSpeed, existing.chainColor,
                        existing.chainWidthScale, existing.chainHeightScale, existing.harpoonColor
                );
                activeHarpoons.remove(target.id);
            }
        }

        harpoonUnit(target, owner, b.team, localAngle, hitDistance);
    }

    private void harpoonUnit(Unit target, Posc owner, mindustry.game.Team team,
                             float localAngle, float hitDistance) {
        HarpoonData data = new HarpoonData();
        data.target = target;
        data.owner = owner;
        data.team = team;
        data.grabTimer = grabDuration;
        data.damageTimer = 0f;
        data.localAngle = localAngle;
        data.hitDistance = hitDistance;

        data.reelSpeed = reelSpeed;
        data.switchToJawsDistance = switchToJawsDistance;
        data.holdDamage = holdDamage;
        data.holdDamageInterval = holdDamageInterval;
        data.resistancePerHitSize = resistancePerHitSize;
        data.chainColor = chainColor.cpy();
        data.chainWidthScale = chainWidthScale;
        data.chainHeightScale = chainHeightScale;
        data.harpoonColor = harpoonColor.cpy();

        activeHarpoons.put(target.id, data);

        target.damage(damage);
        hitEffect.at(target.x, target.y, harpoonColor);
    }


    @Override
    public void draw(Bullet b) {
        if (!(b.owner instanceof Posc pos)) return;

        Draw.z(Layer.flyingUnit + 0.5f);
        drawAnimatedChain(pos.x(), pos.y(), b.x, b.y,
                chainColor, chainWidthScale, chainHeightScale);

        float angle = Mathf.angle(b.x - pos.x(), b.y - pos.y());
        Draw.color(harpoonColor);
        Draw.rect("aj-bullet-harpoon", b.x, b.y, angle + 270f);
        Draw.reset();
    }


    @Override
    public void despawned(Bullet b) {
        if (!(b.owner instanceof Posc owner)) return;

        startRetract(
                owner.x(), owner.y(),
                b.x, b.y,
                reelSpeed, chainColor,
                chainWidthScale, chainHeightScale, harpoonColor
        );
    }


    public static void update() {
        updateHarpoons();
        updateRetracts();
    }

    public static void draw() {
        drawHarpoons();
        drawRetracts();
    }


    private static void updateHarpoons() {
        for (var entry : activeHarpoons) {
            HarpoonData data = entry.value;
            Unit target = data.target;
            Posc owner = data.owner;

            if (!target.isValid() || owner == null || (owner instanceof mindustry.gen.Building b && !b.isValid())) {
                activeHarpoons.remove(entry.key);
                continue;
            }

            if (!Vars.state.isPaused()) {
                data.grabTimer -= Time.delta;
                data.damageTimer += Time.delta;

                float ownerX = owner.x();
                float ownerY = owner.y();
                float dist = Mathf.dst(ownerX, ownerY, target.x, target.y);

                if (dist > 5f) {
                    float resistance = Mathf.log(2f, target.hitSize) * data.resistancePerHitSize;
                    if (!target.isFlying()) resistance += 0.15f;
                    resistance = Mathf.clamp(resistance, 0f, 0.999f);

                    float effectiveSpeed = data.reelSpeed * (1f - resistance) * Time.delta;
                    float dx = ownerX - target.x;
                    float dy = ownerY - target.y;
                    target.x += dx / dist * effectiveSpeed;
                    target.y += dy / dist * effectiveSpeed;
                }

                if (dist < data.switchToJawsDistance) {
                    if (owner instanceof TransformingTurret.TransformingTurretBuild turret) {
                        int jawsIndex = findJawsFormIndex(turret);
                        if (jawsIndex >= 0) {
                            turret.targetForm = jawsIndex;
                            turret.transitionTime = 0f;
                            if (turret.block instanceof TransformingTurret tt
                                    && tt.forms != null && jawsIndex < tt.forms.length) {
                                turret.shootPublic(tt.forms[jawsIndex].bullet);
                            }
                        }
                    }

                    releaseHarpoon(target);
                    activeHarpoons.remove(entry.key);
                    continue;
                }

                if (!Vars.net.client() && data.damageTimer >= data.holdDamageInterval) {
                    target.damage(data.holdDamage);
                    data.damageTimer = 0f;
                }

                if (data.grabTimer <= 0) {
                    Tmp.v1.trns(data.localAngle + target.rotation, data.hitDistance);
                    startRetract(
                            ownerX, ownerY,
                            target.x + Tmp.v1.x, target.y + Tmp.v1.y,
                            data.reelSpeed, data.chainColor,
                            data.chainWidthScale, data.chainHeightScale, data.harpoonColor
                    );
                    releaseHarpoon(target);
                    activeHarpoons.remove(entry.key);
                }
            }
        }
    }

    private static void drawHarpoons() {
        for (var data : activeHarpoons.values()) {
            Unit target = data.target;
            Posc owner = data.owner;

            if (!target.isValid() || owner == null) continue;

            Tmp.v1.trns(data.localAngle + target.rotation, data.hitDistance);
            float headX = target.x + Tmp.v1.x;
            float headY = target.y + Tmp.v1.y;

            Draw.z(Layer.flyingUnit + 0.5f);
            drawAnimatedChain(owner.x(), owner.y(), headX, headY,
                    data.chainColor,
                    data.chainWidthScale, data.chainHeightScale);

            float angle = Mathf.angle(headX - owner.x(), headY - owner.y());
            Draw.color(data.harpoonColor);
            Draw.rect("aj-bullet-harpoon-attached", headX, headY, angle - 90f);
            Draw.reset();
        }
    }

    private static void startRetract(float anchorX, float anchorY,
                                     float headX, float headY,
                                     float reelSpeed,
                                     Color chainColor,
                                     float chainWidthScale, float chainHeightScale,
                                     Color harpoonColor) {
        RetractData r = new RetractData();
        r.anchorX = anchorX;
        r.anchorY = anchorY;
        r.headX = headX;
        r.headY = headY;
        r.reelSpeed = reelSpeed;
        r.chainColor = chainColor.cpy();
        r.chainWidthScale = chainWidthScale;
        r.chainHeightScale = chainHeightScale;
        r.harpoonColor = harpoonColor.cpy();
        activeRetracts.add(r);
    }

    private static void updateRetracts() {
        activeRetracts.removeAll(r -> {
            float dist = Mathf.dst(r.headX, r.headY, r.anchorX, r.anchorY);

            if (dist <= r.reelSpeed * Time.delta + 2f) {
                return true;
            }
            if (!Vars.state.isPaused()) {
                float dx = r.anchorX - r.headX;
                float dy = r.anchorY - r.headY;
                float step = r.reelSpeed * Time.delta;
                r.headX += dx / dist * step;
                r.headY += dy / dist * step;
            }


            return false;
        });
    }

    private static void drawRetracts() {
        for (RetractData r : activeRetracts) {
            Draw.z(Layer.flyingUnit + 0.5f);
            drawAnimatedChain(r.anchorX, r.anchorY, r.headX, r.headY,
                    r.chainColor,
                    r.chainWidthScale, r.chainHeightScale);

            float angle = Mathf.angle(r.headX - r.anchorX, r.headY - r.anchorY);
            Draw.color(r.harpoonColor);
            Draw.rect("aj-bullet-harpoon", r.headX, r.headY, angle + 270f);
            Draw.reset();
        }
    }

    private static void drawAnimatedChain(float x, float y, float endX, float endY,
                                          Color cColor,
                                          float wScale, float hScale) {
        float angleToEnd = Mathf.angle(endX - x, endY - y);
        float distance = Mathf.dst(x, y, endX, endY);

        TextureRegion chainRegion = Core.atlas.find("aj-chain");

        if (chainRegion.found()) {
            float linkHeight = chainRegion.height / 4f * hScale;
            int numLinks = (int) Math.floor(distance / linkHeight);

            for (int i = 0; i < numLinks; i++) {
                Tmp.v1.trns(angleToEnd, distance - i * linkHeight - chainRegion.height / 8f).add(x, y);

                Draw.color(cColor);
                Draw.alpha(0.7f);
                Draw.rect(chainRegion, Tmp.v1.x, Tmp.v1.y,
                        chainRegion.width / 4f * wScale, linkHeight, angleToEnd - 90f);
            }
        } else {
            Draw.color(cColor);
            Lines.stroke(3f * wScale);
            Lines.line(x, y, endX, endY);
        }
    }
    private static int findJawsFormIndex(TransformingTurret.TransformingTurretBuild turret) {
        if (turret == null) return -1;
        if (!(turret.block instanceof TransformingTurret tt)) return -1;
        if (tt.forms == null) return -1;

        for (int i = 0; i < tt.forms.length; i++) {
            if (tt.forms[i].bullet instanceof GrabBulletType) return i;
        }
        return -1;
    }

    public static boolean isHarpooned(Unit unit) {
        return activeHarpoons.containsKey(unit.id);
    }

    public static void releaseHarpoon(Unit unit) {
        activeHarpoons.remove(unit.id);
    }


    public static class HarpoonData {
        public Unit target;
        public Posc owner;
        public Team team;
        public float grabTimer;
        public float damageTimer;
        public float localAngle;
        public float hitDistance;

        public float reelSpeed;
        public float switchToJawsDistance;
        public float holdDamage;
        public float holdDamageInterval;
        public float resistancePerHitSize;

        public Color chainColor;
        public float chainWidthScale;
        public float chainHeightScale;
        public Color harpoonColor;
    }

    public static class RetractData {
        public float anchorX, anchorY;
        public float headX, headY;
        public float reelSpeed;

        public Color chainColor;
        public float chainWidthScale;
        public float chainHeightScale;
        public Color harpoonColor;
    }
}