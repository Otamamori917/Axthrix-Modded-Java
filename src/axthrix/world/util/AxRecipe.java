package axthrix.world.util;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import multicraft.IOEntry;

public class AxRecipe {
    public IOEntry input;
    public IOEntry output;
    public float craftTime = 0.0F;
    @Nullable
    public Prov<TextureRegion> icon;
    @Nullable
    public Color iconColor;
    public Effect craftEffect;

    public AxRecipe(IOEntry input, IOEntry output, float craftTime) {
        this.craftEffect = Fx.none;
        this.input = input;
        this.output = output;
        this.craftTime = craftTime;
    }

    public AxRecipe() {
        this.craftEffect = Fx.none;
    }

    public void cacheUnique() {
        this.input.cacheUnique();
        this.output.cacheUnique();
    }

    public boolean isAnyEmpty() {
        if (this.input != null && this.output != null) {
            return this.input.isEmpty() || this.output.isEmpty();
        } else {
            return true;
        }
    }

    public void shrinkSize() {
        this.input.shrinkSize();
        this.output.shrinkSize();
    }

    public boolean isOutputFluid() {
        return !this.output.fluids.isEmpty();
    }

    public boolean isOutputItem() {
        return !this.output.items.isEmpty();
    }

    public boolean isConsumeFluid() {
        return !this.input.fluids.isEmpty();
    }

    public boolean isConsumeItem() {
        return !this.input.items.isEmpty();
    }

    public boolean isConsumeHeat() {
        return this.input.heat > 0.0F;
    }

    public boolean isOutputHeat() {
        return this.output.heat > 0.0F;
    }

    public boolean hasHeat() {
        return this.isConsumeHeat() || this.isOutputHeat();
    }

    public boolean hasItem() {
        return this.isConsumeItem() || this.isOutputItem();
    }

    public boolean hasFluid() {
        return this.isOutputFluid() || this.isOutputFluid();
    }

    public int maxItemAmount() {
        return Math.max(this.input.maxItemAmount(), this.output.maxItemAmount());
    }

    public float maxFluidAmount() {
        return Math.max(this.input.maxFluidAmount(), this.output.maxFluidAmount());
    }

    public float maxPower() {
        return Math.max(this.input.power, this.output.power);
    }

    public float maxHeat() {
        return Math.max(this.input.heat, this.output.heat);
    }

    public String toString() {
        return "Recipe{input=" + this.input + "output=" + this.output + "craftTime" + this.craftTime + "}";
    }
}
