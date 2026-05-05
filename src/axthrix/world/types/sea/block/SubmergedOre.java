package axthrix.world.types.sea.block;

import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import axthrix.world.types.sea.managers.LayerManager;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.type.Item;

public class SubmergedOre extends OreBlock {

    public SubmergedOre(String name, Item item) {
        super(name, item);
        hasShadow = false;
    }

    @Override
    public void drawBase(Tile tile) {
        // Leave empty to prevent baking into the floor cache
    }


}

