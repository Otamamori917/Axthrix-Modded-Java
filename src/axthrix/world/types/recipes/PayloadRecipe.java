package axthrix.world.types.recipes;

import mindustry.type.*;
import mindustry.world.*;
import axthrix.world.types.block.*;

public class PayloadRecipe {
    public float craftTime;
    public boolean requiresUnlock = true;
    public boolean blockBuild = true, centerBuild;

    public ItemStack[] itemRequirements;
    public LiquidStack liquidRequirements;
    public float powerUse;
    public Block inputBlock;

    public Block outputBlock;

    public PayloadRecipe(Block block){
        outputBlock = block;
        itemRequirements = block.requirements;

        if(block instanceof PayloadAmmoBlock m){
            craftTime = m.constructTime;
            powerUse = m.powerUse;
            inputBlock = m.prev;
        }
    }

    public PayloadRecipe(Block block, float powerUse, float craftTime){
        this(block);
        this.craftTime = craftTime;
        this.powerUse = powerUse;
    }

    public PayloadRecipe(Block outputBlock, Block inputBlock, float powerUse, float craftTime){
        this(outputBlock, powerUse, craftTime);
        this.inputBlock = inputBlock;
    }

    public Liquid getLiquidInput(){
        return liquidRequirements != null ? liquidRequirements.liquid : null;
    }

    public boolean hasLiquidInput(Liquid liquid){
        return liquidRequirements != null && liquidRequirements.liquid == liquid;
    }

    public boolean hasInputBlock(){
        return inputBlock != null;
    }

    public boolean showReqList(){
        return itemRequirements.length > 0 || liquidRequirements != null;
    }

    public boolean unlocked(){
        return !requiresUnlock || outputBlock.unlockedNow();
    }
}
