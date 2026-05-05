package axthrix.world.types.sea.managers;

import arc.Core;
import arc.math.Mathf;
import arc.struct.ObjectSet;
import arc.util.Log;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.sea.block.SeaCore;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.gen.Entityc;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;

import java.util.concurrent.atomic.AtomicBoolean;

public class LayerManager {

    /** All units currently in the submerged layer. */
    public static final ObjectSet<Unit> submergedUnits = new ObjectSet<>();
    /** All buildings currently in the submerged layer. */
    public static final ObjectSet<Building> submergedBuilds = new ObjectSet<>();

    /** Current shader fade intensity, lerped smoothly. */
    public static float shaderIntensity = 0f;
    private static final float fadeSpeed = 1f / 60f;

    /** Returns true if the local player's unit is submerged. */
    public static boolean isPlayerSubmerged() {
        AtomicBoolean valid = new AtomicBoolean(false);
        valid.set(Vars.player != null
                && Vars.player.unit() != null
                && submergedUnits.contains(Vars.player.unit()));
        if (!valid.get()){
            Vars.indexer.eachBlock(null, Core.camera.position.x, Core.camera.position.y, Core.camera.width,
                    b -> isSubmerged(b),
                    b -> {
                        if(b instanceof Turret.TurretBuild tb){
                            valid.set(tb.unit.isPlayer());
                        }
                    }
            );
        }
        return valid.get();
    }

    /** Sets a unit's submerged state. */
    public static void setSubmerged(Unit unit, boolean submerged) {
        if (submerged) {
            submergedUnits.add(unit);
        } else {
            submergedUnits.remove(unit);
        }
    }
    public static boolean isWater(Tile tile){
        return isDeep(tile) || isShallow(tile);
    }
    public static boolean isDeep(Tile tile) {
        return tile != null && (
                tile.floor() == Blocks.deepwater ||
                tile.floor() == Blocks.deepTaintedWater ||
                tile.floor() == AxthrixEnvironment.tharaxianDeep
        );
    }

    public static boolean isShallow(Tile tile) {
        return tile != null && (
                tile.floor() == Blocks.water ||
                tile.floor() == Blocks.taintedWater ||
                tile.floor() == Blocks.sandWater ||
                tile.floor() == Blocks.darksandWater ||
                tile.floor() == Blocks.darksandTaintedWater ||
                tile.floor() == AxthrixEnvironment.tharaxianShallows
        );
    }


    /** Sets a building's submerged state. */
    public static void setSubmerged(Building build, boolean submerged) {
        if (submerged) submergedBuilds.add(build);
        else submergedBuilds.remove(build);
    }

    public static boolean canInteract(Entityc a, Entityc b) {
        return isSubmerged(a) == isSubmerged(b);
    }


    /** Checks an entity's submerged state */
    public static boolean isSubmerged(Object entity) {
        /// Unit variant. just checks if its in the submergedUnits list.
        if (entity instanceof Unit u) return submergedUnits.contains(u);

        /// Block variant. checks multiple instances
        if (entity instanceof Building b){
            if (b.block instanceof SeaCore){
                return true;
            }
            //never used but there if i ever need it lmao
            boolean check = submergedBuilds.contains(b);

            if(!check){
                if(b.block instanceof AxBlock ab){
                    check = ab.waterBlock;
                }else{
                    try {
                        var blockClass = b.block.getClass();
                        var waterBlockField = blockClass.getField("waterBlock");
                        check = waterBlockField.getBoolean(b.block);
                    } catch(Exception ignored){
                    }
                }
            }
            return check;
        }

        return false;
    }

    /** Returns true if the target is visible to the viewer.
     *  Units on different layers cannot see each other. */
    public static boolean isVisible(Unit viewer, Unit target) {
        boolean viewerSub = submergedUnits.contains(viewer);
        boolean targetSub = submergedUnits.contains(target);
        return viewerSub == targetSub;
    }

    /** Called every frame to smoothly fade the shader intensity. */
    public static void update() {
        float target = isPlayerSubmerged() ? 1f : 0f;
        shaderIntensity = Mathf.lerp(shaderIntensity, target, fadeSpeed);
    }

    /** Clears all submerged units — call on world unload. */
    public static void clear() {
        submergedUnits.clear();
        submergedBuilds.clear();
        shaderIntensity = 0f;
    }
}