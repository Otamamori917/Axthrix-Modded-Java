package axthrix.world.types.sea.block;

import arc.util.*;
import axthrix.world.types.block.AxBlock;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.type.*;

public class SeaCoreSpawner extends AxBlock {
    public UnitType unitType;
    public float buildTime = 60f * 10f;

    public SeaCoreSpawner(String name) {
        super(name);
        update = true;
        solid = true;
        waterBlock = false;
    }

    public class SeaCoreSpawnerBuild extends AxBlockBuild {
        public float progress;
        public @Nullable Unit constructorUnit;

        @Override
        public void updateTile() {
            super.updateTile();
            boolean coreActive = Groups.build.contains(b -> b instanceof SeaCore.SeaCoreBuild && b.team == team);

            if (!coreActive && (constructorUnit == null || !constructorUnit.isValid())) {
                progress += edelta();
                if (progress >= buildTime) {
                    constructorUnit = unitType.spawn(team, x, y);
                    progress = 0;
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (progress > 0) {
                Drawf.target(x, y, 20f * (progress / buildTime), team.color);
            }
        }
    }
}
