package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Shaders;
import mindustry.world.meta.Stat;

public class SilveringWeakness extends Ability {
    public TextureRegion plateRegion;
    public boolean whenShooting = false;
    public Color color = Color.valueOf("d1efff");
    public float healthReduction = 0.2F;
    public float maxPentalyTime = 60;
    public float z = 110.0F;
    protected float warmup;

    public SilveringWeakness() {
    }

    public void update(Unit unit) {
        super.update(unit);
        boolean active = (unit.isShooting || !whenShooting);
        warmup = Mathf.lerpDelta(warmup, active ? 1.0F : 0.0F, 1F / maxPentalyTime);
        unit.healthMultiplier -= warmup * healthReduction;
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-sliverweakness");
    }

    public void addStats(Table t) {
        t.add("[lightgray]" + Stat.healthMultiplier.localized() + ": [white]" + Math.round(healthReduction * 100.0F) + 100 + "%");
    }

    public void draw(Unit unit) {
        if (warmup > 0.001F) {
            if (plateRegion == null) {
                plateRegion = Core.atlas.find(unit.type.name + "-armor", unit.type.fullIcon);
            }

            Draw.draw(z <= 0.0F ? Draw.z() : z, () -> {
                Shaders.armor.region = plateRegion;
                Shaders.armor.progress = warmup;
                Shaders.armor.time = -Time.time / Mathf.lerpDelta(40,2,-1);
                Draw.rect(Shaders.armor.region, unit.x, unit.y, unit.rotation - 90.0F);
                Draw.color(color);
                Draw.shader(Shaders.armor);
                Draw.rect(Shaders.armor.region, unit.x, unit.y, unit.rotation - 90.0F);
                Draw.shader();
                Draw.reset();
            });
        }

    }
}
