package axthrix.world.types.ai;

import arc.math.Mathf;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.util.*;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.block.ObeliskBlock;
import axthrix.world.types.block.drill.WallBreakerSpawner;
import axthrix.world.types.unittypes.WallBreakerUnitType;
import mindustry.*;
import mindustry.content.Blocks;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import arc.math.geom.*;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.entities.units.AIController;

public class WallBreakerAI extends AIController {
    public enum State { mining, returning }
    public State state = State.mining;
    public Tile targetTile;
    public boolean noWallsLeft = false;
    public float itemTickCounter = 0f;

    @Override
    public void updateUnit() {
        if (!(unit.type instanceof WallBreakerUnitType u) || u.tetherBuilding.get(unit) == null) return;
        Building home = u.tetherBuilding.get(unit);
        WallBreakerSpawner spawner = (WallBreakerSpawner) home.block;

        if (!u.drillTime.containsKey(unit)) u.drillTime.put(unit, 0f);

        if (unit.health <= 1 || unit.dead) {
            unit.kill();
            return;
        }

        if (state == State.mining) {
            if (targetTile == null || !isValidWall(targetTile, spawner)) {
                targetTile = findWallInLine(home, spawner);
            }

            if (targetTile == null) {
                noWallsLeft = true;
                state = State.returning;
                return;
            }

            noWallsLeft = false;
            unit.lookAt(targetTile);

            if (unit.within(targetTile.worldx(), targetTile.worldy(), 8f)) {
                unit.vel.approach(Vec2.ZERO, 0.4f * Time.delta);

                if (!Vars.state.isPaused()) {
                    WallBreakerSpawner.WallData data = spawner.wallToData.get(targetTile.block());

                    if (data != null) {
                        if (unit.stack.item != data.yield) {
                            unit.stack.set(data.yield, 0);
                        }

                        itemTickCounter += Time.delta;
                        if (itemTickCounter >= 10f && unit.stack.amount < unit.type.itemCapacity) {
                            unit.stack.amount++;
                            itemTickCounter = 0f;
                        }

                        float speedMult = (data.drillSpeed > 0) ? data.drillSpeed : 1f;
                        u.drillTime.put(unit, u.drillTime.get(unit) + (Time.delta * speedMult));

                        unit.damage(0.1f * Time.delta);
                    }
                }

                if (u.drillTime.get(unit) >= u.maxDrillTime) {
                    breakWall(targetTile, spawner);
                    u.drillTime.put(unit, 0f);
                    itemTickCounter = 0f;
                    targetTile = null;
                }
            } else {
                moveTo(targetTile, 0);
            }

            if (unit.stack.amount >= unit.type.itemCapacity) {
                state = State.returning;
            }

        } else if (state == State.returning) {
            moveTo(home, 0f);
            unit.lookAt(home);

            if (unit.within(home.x, home.y, 8f)) {
                unit.vel.approach(Vec2.ZERO, 0.4f * Time.delta);

                Units.nearbyBuildings(home.x, home.y, home.hitSize() * 2.5f, container -> {
                    if (container instanceof StorageBlock.StorageBuild sb) {
                        int amountToTake = Math.min(unit.stack.amount, sb.block.itemCapacity - sb.items.get(unit.stack.item));
                        if (amountToTake > 0) {
                            sb.items.add(unit.stack.item, amountToTake);
                            unit.stack.amount -= amountToTake;
                        }
                    }
                });

                if (unit.stack.amount == 0 || unit.within(home.x, home.y, 2f)) {
                    unit.stack.amount = 0;
                    if (!noWallsLeft) state = State.mining;
                }
            }
        }
    }


    public Tile findWallInLine(Building homeBase, WallBreakerSpawner spawner) {
        if (homeBase == null) return null;

        int dx = Geometry.d4x(homeBase.rotation);
        int dy = Geometry.d4y(homeBase.rotation);
        int sideX = -dy;
        int sideY = dx;

        int gapTiles = Mathf.ceil(spawner.snappedUnitGap() / Vars.tilesize);
        int startX = homeBase.tileX() + (dx * gapTiles);
        int startY = homeBase.tileY() + (dy * gapTiles);

        for (int i = 0; i < spawner.scanLength; i++) {
            int halfWidth = spawner.scanWidth / 2;
            for (int j = -halfWidth; j <= halfWidth; j++) {
                int targetX = startX + (dx * i) + (sideX * j);
                int targetY = startY + (dy * i) + (sideY * j);

                Tile other = Vars.world.tile(targetX, targetY);
                if (other != null && isValidWall(other, spawner)) return other;
            }
        }
        return null;
    }

    public boolean isValidWall(Tile tile, WallBreakerSpawner spawner) {
        return tile != null && spawner.wallToData.containsKey(tile.block());
    }

    public void breakWall(Tile tile, WallBreakerSpawner spawner) {
        WallBreakerSpawner.WallData data = spawner.wallToData.get(tile.block());
        if (data == null) return;

        tile.setNet(Blocks.air);

        if (Mathf.chance(0.01)) {
            tile.setNet(AxthrixEnvironment.obelisk);
            if (tile.build instanceof ObeliskBlock.ObeliskBuild ob) {
                int rand = Mathf.random(2);
                if (rand == 0) {
                    ob.hiddenMessage = "The core yearns for the void";
                    ob.techName = "large-plasma-drill";
                } else if (rand == 1) {
                    ob.hiddenMessage = "Silicon is the soul of the machine";
                    ob.techName = "multi-press";
                } else {
                    ob.hiddenMessage = "Beware the shifting sands";
                    ob.techName = "scepter";
                }
                ob.sentenceMode = true;
            }
        } else if (data.ore != null) {
            tile.setOverlayNet(data.ore);
        }
    }
}