package axthrix.world.types.unittypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.util.Strings;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.content.AxthrixSounds;
import axthrix.world.util.AxStats;
import axthrix.content.FX.AxthrixFfx;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.type.ItemStack;
import mindustry.world.meta.Stat;
import axthrix.world.types.entities.comp.StealthUnit;
import axthrix.world.types.recipes.RecipeGeneric;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

public class CnSUnitType extends AxUnitType {
  // Stealth stuff
  public boolean cloaks = false;
  public float vulnerabilityTime = 60f;

  // Crafter unit stuff
  public RecipeGeneric recipe;

  public CnSUnitType(String name) {
    super(name);
  }

  public CnSUnitType(String name,float vulnerabilitytime) {
    super(name);
    vulnerabilityTime = vulnerabilitytime;
    cloaks = true;
    if(AxthrixLoader.screwStealthFlyers){
      deathSound = AxthrixSounds.Death;
      loopSound = AxthrixSounds.Stank;
      loopSoundVolume = 0.5f;
    }

  }

  @Override
  public void setStats() {
    super.setStats();
    if (cloaks){
      stats.add(AxStats.timeToCloak, vulnerabilityTime / 60f, StatUnit.seconds);
    }
    if (recipe != null) {
      stats.add(Stat.input, t -> {
        for (ItemStack stack : recipe.consumeItems) t.add(StatValues.displayItem(stack.item)).pad(3f);
      });
      stats.add(Stat.output, t -> {
        for (ItemStack stack : recipe.outputItems) t.add(StatValues.displayItem(stack.item)).pad(3f);
      });
    }
  }

  @Override
  public boolean targetable(Unit unit, Team from) {
    if (cloaks && unit instanceof StealthUnit u) return !u.cloaked();
    return super.targetable(unit, from);
  }

  @Override
  public void applyColor(Unit unit) {
    if (cloaks && unit instanceof StealthUnit u) {
      Draw.mixcol(Tmp.c1.set(Vars.world.floorWorld(unit.x, unit.y).mapColor).mul(0.83f), 0.6f * (1f - u.vulnerabilityFrame/vulnerabilityTime));
    } else {
      super.applyColor(unit);
    }
  }
}
