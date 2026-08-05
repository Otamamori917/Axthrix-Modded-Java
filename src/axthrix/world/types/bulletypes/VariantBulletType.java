package axthrix.world.types.bulletypes;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Teamc;

public class VariantBulletType extends BulletType {
    public Seq<BulletType> variants = new Seq<>();
    public Seq<Float> variantWeights = new Seq<>();
    public boolean useWeights = false;

    public VariantBulletType(BulletType... bulletTypes) {
        variants.add(bulletTypes);
    }

    private BulletType selectBullet() {
        if (variants.isEmpty()) return this;

        if (!useWeights || variantWeights.isEmpty()) {
            return variants.random();
        }

        float totalWeight = 0f;
        int limit = Math.min(variants.size, variantWeights.size);

        for (int i = 0; i < limit; i++) {
            totalWeight += variantWeights.get(i);
        }

        if (totalWeight <= 0f) return variants.random();

        float roll = Mathf.random(totalWeight);
        float currentSum = 0f;

        for (int i = 0; i < limit; i++) {
            currentSum += variantWeights.get(i);
            if (roll <= currentSum) {
                return variants.get(i);
            }
        }

        return variants.random();
    }
    @Override
    public @Nullable Bullet create(@Nullable Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY) {
        return selectBullet().create(owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY);
    }

    @Override
    public @Nullable Bullet create(@Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY) {
        return selectBullet().create(owner, shooter, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY);
    }

    @Override
    public @Nullable Bullet create(@Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target) {
        return selectBullet().create(owner, shooter, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }
}
