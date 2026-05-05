package axthrix.world.types.block.drill;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.struct.*;
import axthrix.world.util.draw.AxDrawf;
import axthrix.world.types.unittypes.WallBreakerUnitType;
import axthrix.world.util.ui.WallBreakerBar;
import mindustry.*;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;

public class WallBreakerSpawner extends Block {
    public UnitType unitType;
    public float buildTime = 60f * 5f;
    public int scanLength = 10;
    public int scanWidth = 3;

    public ObjectMap<Block, WallData> wallToData = new ObjectMap<>();

    public static class WallData {
        public Block ore;
        public Item yield;
        public float drillSpeed;

        public WallData(Block ore, Item yield, float drillSpeed) {
            this.ore = ore;
            this.yield = yield;
            this.drillSpeed = drillSpeed;
        }
    }

    public WallBreakerSpawner(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        rotate = true;
        configurable = true;
    }

    public void setWallData(Block wall, Block ore, Item yield, float drillSpeed) {
        wallToData.put(wall, new WallData(ore, yield, drillSpeed));
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("unit-system", WallBreakerBar::new);
    }

    /** Returns the snapped world-unit gap between the block face and the scan area start. */
    public float snappedUnitGap() {
        float unitHitSize = ((WallBreakerUnitType) unitType).hitSize;
        float blockFaceOffset = (size / 2f) * Vars.tilesize;
        // Ceil hitSize to nearest tile boundary
        float snappedUnitRadius = Mathf.ceil(unitHitSize / Vars.tilesize) * Vars.tilesize;
        return blockFaceOffset + snappedUnitRadius;
    }

    private void drawRangeIndicator(float x, float y, int rotation, Color teamColor) {
        int dx = Geometry.d4x(rotation);
        int dy = Geometry.d4y(rotation);

        float unitHitSize = ((WallBreakerUnitType) unitType).hitSize;
        float snappedUnitRadius = Mathf.ceil(unitHitSize / Vars.tilesize) * Vars.tilesize;
        float gap = snappedUnitGap();

        float startX = x + dx * gap;
        float startY = y + dy * gap;

        float rectW = scanWidth * Vars.tilesize;
        float rectH = scanLength * Vars.tilesize;

        boolean horizontal = (dx != 0);
        float drawW = horizontal ? rectH : rectW;
        float drawH = horizontal ? rectW : rectH;

        float centerX = startX + dx * (rectH / 2f);
        float centerY = startY + dy * (rectH / 2f);

        float left   = centerX - drawW / 2f;
        float right  = centerX + drawW / 2f;
        float bottom = centerY - drawH / 2f;
        float top    = centerY + drawH / 2f;

        int dashesW = Math.round(drawW / 4f);
        int dashesH = Math.round(drawH / 4f);

        Lines.stroke(3f, Pal.gray);
        Lines.dashLine(left,  bottom, right, bottom, dashesW);
        Lines.dashLine(right, bottom, right, top,    dashesH);
        Lines.dashLine(right, top,    left,  top,    dashesW);
        Lines.dashLine(left,  top,    left,  bottom, dashesH);

        Lines.stroke(1f, teamColor);
        Lines.dashLine(left,  bottom, right, bottom, dashesW);
        Lines.dashLine(right, bottom, right, top,    dashesH);
        Lines.dashLine(right, top,    left,  top,    dashesW);
        Lines.dashLine(left,  top,    left,  bottom, dashesH);

        Draw.color();
        Lines.stroke(1f);

        // Icon drawn at snapped spawn position, sized to snapped radius * 2
        float spawnX = x + dx * ((size / 2f * Vars.tilesize) + snappedUnitRadius / 2f);
        float spawnY = y + dy * ((size / 2f * Vars.tilesize) + snappedUnitRadius / 2f);
        float iconSize = snappedUnitRadius;

        Draw.color(teamColor, 0.6f);
        Draw.rect(unitType.fullIcon, spawnX, spawnY, iconSize, iconSize, 0f);
        Draw.color();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawRangeIndicator(
                x * Vars.tilesize,
                y * Vars.tilesize,
                rotation,
                Vars.player.team().color
        );
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (tile == null) return false;

        int dx = Geometry.d4x(rotation);
        int dy = Geometry.d4y(rotation);
        int sideX = -dy;

        float unitHitSize = ((WallBreakerUnitType) unitType).hitSize;
        int snappedRadiusTiles = Mathf.ceil(unitHitSize / Vars.tilesize);
        int blockHalfSize = size / 2;

        // Spawn area starts immediately in front of the block face
        int spawnStartX = tile.x + dx * (blockHalfSize + 1);
        int spawnStartY = tile.y + dy * (blockHalfSize + 1);
        int sideY = dx;

        // Check every tile in the spawn area is clear
        int halfWidth = scanWidth / 2;
        for (int depth = 0; depth < snappedRadiusTiles; depth++) {
            for (int j = -halfWidth; j <= halfWidth; j++) {
                int checkX = spawnStartX + dx * depth + sideX * j;
                int checkY = spawnStartY + dy * depth + sideY * j;
                Tile check = Vars.world.tile(checkX, checkY);
                if (check == null || check.solid()) return false;
            }
        }

        // Scan starts after the spawn area
        int scanStartX = spawnStartX + dx * snappedRadiusTiles;
        int scanStartY = spawnStartY + dy * snappedRadiusTiles;

        for (int i = 0; i < scanLength; i++) {
            for (int j = -halfWidth; j <= halfWidth; j++) {
                int targetX = scanStartX + dx * i + sideX * j;
                int targetY = scanStartY + dy * i;
                Tile other = Vars.world.tile(targetX, targetY);
                if (other != null && wallToData.containsKey(other.block())) return true;
            }
        }

        return false;
    }

    public class WallBreakerBuilding extends Building {
        public float progress = 0;
        public Unit spawnedUnit = null;

        @Override
        public void updateTile() {
            if (spawnedUnit != null && !spawnedUnit.isValid()) {
                spawnedUnit = null;
            }

            if ((spawnedUnit == null || !spawnedUnit.isValid()) && power.status > 0) {
                progress += edelta();
                if (progress >= buildTime) {
                    spawnUnit();
                    if (spawnedUnit != null && spawnedUnit.isValid()) {
                        consume();
                    }
                    progress = 0;
                }
            }
        }

        @Override
        public void draw() {
            super.draw();

            if ((spawnedUnit == null || !spawnedUnit.isValid()) && progress > 0) {
                Draw.draw(Draw.z(), () -> {
                    float spawnX = x + Geometry.d4x(rotation) * Vars.tilesize;
                    float spawnY = y + Geometry.d4y(rotation) * Vars.tilesize;
                    float prog = progress / buildTime;

                    AxDrawf.materialize(
                            spawnX,
                            spawnY,
                            unitType.fullIcon,
                            team.color,
                            0f,
                            0.1f,
                            prog
                    );
                });
            }
        }

        @Override
        public void drawSelect() {
            drawRangeIndicator(x, y, rotation, team.color);
        }

        public void spawnUnit() {
            float spawnX = x + Geometry.d4x(rotation) * Vars.tilesize;
            float spawnY = y + Geometry.d4y(rotation) * Vars.tilesize;

            spawnedUnit = unitType.spawn(team, spawnX, spawnY);
            Events.fire(new EventType.UnitCreateEvent(spawnedUnit, this));
            if (spawnedUnit.type instanceof WallBreakerUnitType unit) {
                unit.tetherBuilding.put(spawnedUnit, this);
            }
        }
    }
}