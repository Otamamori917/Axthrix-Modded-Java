/*
package axthrix.world.types.block.production;

import java.lang.Math;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class DoubleConveyor extends Block {
    private static final float itemSpace = 0.4f;
    private static int perBeltCap = 6;

    public @Load(value = "conveyor-#1-#2", lengths = { 7, 8 }) TextureRegion[][] regions;

    public float speed = 0f;
    public float displayedSpeed = 0f;

    public @Nullable Block junctionReplacement, bridgeReplacement;

    public DoubleConveyor(String name) {
        super(name);
        rotate = true;
        update = true;
        group = BlockGroup.transportation;
        hasItems = true;
        itemCapacity = perBeltCap;
        priority = TargetPriority.transport;
        conveyorPlacement = true;
        underBullets = true;

        ambientSound = Sounds.loopConveyor;
        ambientSoundVolume = 0.0022f;
        unloadable = false;
        noUpdateDisabled = false;
    }

    public void setPerBeltCap(int cap) {
        perBeltCap = cap;
        itemCapacity = cap;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.itemsMoved, displayedSpeed, StatUnit.itemsSecond);
    }

    @Override
    public void init() {
        super.init();

        if (junctionReplacement == null)
            junctionReplacement = Blocks.junction;
        if (bridgeReplacement == null || !(bridgeReplacement instanceof ItemBridge))
            bridgeReplacement = Blocks.itemBridge;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        int[] bits = new int[] { 0, 1, 1, 0, 0 };

        if (bits == null)
            return;

        TextureRegion region = regions[bits[0]][0];
        Draw.rect(region, plan.drawx(), plan.drawy(), region.width * bits[1] * region.scl(),
                region.height * bits[2] * region.scl(), plan.rotation * 90);
    }

    // stack conveyors should be bridged over, not replaced
    @Override
    public boolean canReplace(Block other) {
        return super.canReplace(other) && !(other instanceof StackConveyor);
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans) {
        if (bridgeReplacement == null)
            return;

        Placement.calculateBridges(plans, (ItemBridge) bridgeReplacement, b -> b instanceof Conveyor);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { regions[0][0] };
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans) {
        if (junctionReplacement == null)
            return this;

        Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y
                && (req.block instanceof Conveyor || req.block instanceof Junction));
        return cont.get(Geometry.d4(req.rotation)) &&
                cont.get(Geometry.d4(req.rotation - 2)) &&
                req.tile() != null &&
                req.tile().block() instanceof Conveyor &&
                Mathf.mod(req.tile().build.rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
    }

    public class ConveyorBuild extends Building implements ChainedBuilding {
        public class ConveyorItem {
            public Item item;
            public float x = 0;
            public float y = 0;

            public ConveyorItem(Item item, float x, float y) {
                this.item = item;
                this.x = x;
                this.y = y;
            }

            public ConveyorItem(Item item) {
                this.item = item;
            }
        }

        public class ConveyorBelt {
            public ConveyorItem[] beltItems = new ConveyorItem[perBeltCap];
            public float clogHeat = 0;
            public int len = 0;
            public float minitem = 2f;
            public int side;

            public void update(Building inFront) {
                boolean rotating = blendbits == 1;
                boolean rotin = rotating && side == innerBelt;
                boolean rotout = rotating && side != innerBelt;

                boolean sidein = false;
                if (inFront instanceof ConveyorBuild cb) {
                    sidein = cb.blendbits == 2 || cb.blendbits == 3;
                }

                float convEnd = 2f;
                var actualSpace = itemSpace * (rotout ? 1.5f : 1);

                if (inFront != null && inFront.rotation == rotation)
                    label1: {
                        if (nextc != null) {
                            if (sidein && nextc.rotation != rotation) {
                                convEnd = 2f - actualSpace;
                            } else {
                                convEnd = 2f - Math.max(actualSpace - nextc.belts[side].minitem, 0);
                            }
                            break label1;
                        }

                        if (inFront instanceof ConveyorBuild nc) {
                            if (sidein && nextc.rotation != rotation) {
                                convEnd = 2f - actualSpace;
                            } else {
                                convEnd = 2f
                                        - Math.max(
                                                actualSpace
                                                        - (nc.belts[side == 0 ? 1 : 0].minitem),
                                                0);

                            }
                        }
                    }

                var actualEnd = convEnd - (rotin ? 1.5f : 0) - (rotout ? 0.5f : 0);

                var lastItem = true;
                minitem = actualEnd;

                float maxMove = speed * edelta() * (rotout ? 1.42f : 1f);

                for (int i = len - 1; i >= 0; i--) {
                    var curItem = beltItems[i];

                    float maxPos;
                    if (lastItem) {
                        maxPos = 2f;
                        lastItem = false;
                    } else {
                        var nextItem = beltItems[i + 1].y;
                        maxPos = nextItem - actualSpace;
                    }

                    float moveAmount = Mathf.clamp(maxPos - curItem.y, 0, maxMove);

                    curItem.y += moveAmount;

                    var oldY = curItem.y;

                    if (curItem.y > actualEnd) {
                        curItem.y = actualEnd;
                    }

                    curItem.x = Mathf.approach(curItem.x, 0,
                            moveAmount * 2f);

                    var skipMin = false;

                    if (oldY >= actualEnd) {
                        if (pass(curItem.item, side)) {
                            skipMin = true;
                            len++;
                            if (inFront instanceof ConveyorBuild) {
                                skipMin = true;

                                if (nextc != null) {
                                    @SuppressWarnings("unused")
                                    var targetSide = 1 - side * 2;
                                }
                            }
                            items.remove(curItem.item, 1);
                            len = Math.min(i, len);
                        }
                    }

                    if (!skipMin && curItem.y < minitem) {
                        minitem = curItem.y;
                    }
                }

                if (minitem < actualSpace) {
                    clogHeat = Mathf.approachDelta(clogHeat, 1f, 1f / 60f);
                } else {
                    clogHeat = 0f;
                }
            }

            public void addItemToStart(Item item) {
                for (int i = len; i > 0; i--) {
                    beltItems[i] = beltItems[i - 1];
                }

                beltItems[0] = new ConveyorItem(item);
                items.add(item, 1);
                noSleep();
                len++;
            }

            public int insertAt(Item item, float location) {
                int insertLoc = -1;

                if (len > 1) {
                    for (int i = 0; i < len - 1; i++) {
                        var thisItem = beltItems[i];
                        var nextItem = beltItems[i + 1];

                        var afterThis = location > (thisItem.y + itemSpace);
                        var beforNext = location < (nextItem.y - itemSpace);

                        if (afterThis && beforNext) {
                            insertLoc = i + 1;
                        }
                    }

                    if (insertLoc == -1) {
                        if (location < beltItems[0].y - itemSpace) {
                            insertLoc = 0;
                        }
                        if (location > beltItems[len - 1].y + itemSpace) {
                            insertLoc = len;
                        }
                    }
                } else if (len == 1) {
                    insertLoc = location < beltItems[0].y ? 0 : 1;
                } else {
                    insertLoc = 0;
                }

                if (insertLoc == -1) {
                    Log.err("beans");
                }

                for (int i = len; i > insertLoc; i--) {
                    beltItems[i] = beltItems[i - 1];
                }

                beltItems[insertLoc] = new ConveyorItem(item, 0, location);
                items.add(item, 1);
                noSleep();
                len++;

                return insertLoc;
            }

            public void subLastItem() {
                beltItems[len - 1] = null;
                len--;
            }

            public void removeAt(int index) {
                for (int i = index; i < len; i++) {
                    if (i + 1 == len) {
                        beltItems[i] = null;
                        len--;
                        return;
                    } else {
                        beltItems[i] = beltItems[i + 1];
                    }
                }
                noSleep();
            }

            public void draw(float x, float y, int rotation) {
                Draw.z(Layer.block - 0.1f);
                float layer = Layer.block - 0.1f, wwidth = world.unitWidth(), wheight = world.unitHeight(),
                        scaling = 0.01f;

                boolean rotating = blendbits == 1;
                boolean rotin = rotating && side == innerBelt;
                boolean rotout = rotating && side != innerBelt;

                for (int i = 0; i < len; i++) {
                    var curitem = beltItems[i];

                    Item item = curitem.item;

                    var sideOffset = (side - 0.5f) * 1.5f;

                    float afterY = curitem.y;

                    float drawX = curitem.x;
                    if (rotating) {
                        // I have no idea how or why this works but it works
                        var r = ((rotin) ? 0.5f : 1.33f);
                        var startPos = (rotout ? 0.625f : 1.375f);
                        var toMove = 2f - startPos;
                        var passed = (float) (curitem.y / r);

                        if (blendscly == -1) {
                            afterY = (1 - Mathf.cos(Mathf.pi / 2 * passed)) * toMove + startPos;
                            drawX = Mathf.approach(-2f, -sideOffset,
                                    (-sideOffset + 2f) * (Mathf.sin(Mathf.pi / 2 * passed)));
                        } else {
                            afterY = (1 - Mathf.cos(Mathf.pi / 2 * passed)) * toMove + startPos;
                            drawX = -Mathf.approach(-2f, sideOffset,
                                    (sideOffset + 2f) * (Mathf.sin(Mathf.pi / 2 * passed)));
                        }
                        sideOffset = 0;
                    }
                    float realY = afterY - 0.5f;

                    Tmp.v1.trns(rotation * 90, tilesize);
                    Tmp.v2.trns(rotation * 90, -tilesize / 2f, (drawX - sideOffset) * tilesize / 2f);

                    float ix = (x + Tmp.v1.x * realY + Tmp.v2.x),
                            iy = (y + Tmp.v1.y * realY + Tmp.v2.y);

                    Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                    Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
                }
            }

            public int howManyCanFit() {
                return Mathf.floor(minitem / itemSpace);
            }

            public boolean hasSpace() {
                if (blendbits == 0 || blendbits == 2 || blendbits == 3) {
                    return (len < perBeltCap) && minitem >= itemSpace;
                } else if (blendbits == 1) {
                    if (side == innerBelt) {
                        return minitem >= itemSpace;
                    } else {
                        return minitem >= itemSpace * 1.5f;
                    }
                } else {
                    return false;
                }
            }

            public boolean hasSpace(float wherey) {
                var ret = true;

                for (int i = 0; i < len; i++) {
                    var item = beltItems[i];

                    if (item.y - itemSpace < wherey && wherey < item.y + itemSpace) {
                        ret = false;
                    }
                }
                return ret;
            }

            public ConveyorBelt(int side) {
                this.side = side;
            }
        }

        public int blendbits, blending;
        public int blendsclx = 1, blendscly = 1;

        public int dumpNo = 0;
        public short innerBelt = 0;

        public ConveyorBuild nextc;
        Tile front1;
        Tile front2;

        public ConveyorBelt[] belts = new ConveyorBelt[] { new ConveyorBelt(0), new ConveyorBelt(1) };

        public void draw() {
            var running = enabled && !clogged();

            int frame = running ? (int) (((Time.time * speed * 16f * timeScale * efficiency)) % 8)
                    : 0;

            Draw.z(Layer.block - 0.2f);

            Draw.rect(regions[blendbits][frame], x, y, tilesize * blendsclx * size, tilesize * blendscly * size,
                    rotation * 90);

            Draw.z(Layer.block - 0.1f);

            for (ConveyorBelt belt : belts) {
                belt.draw(x, y, rotation);
            }
        }

        @Override
        public void payloadDraw() {
            Draw.rect(block.fullIcon, x, y);
        }

        @Override
        public void drawCracks() {
            Draw.z(Layer.block - 0.15f);
            super.drawCracks();
        }

        @Override
        public void overwrote(Seq<Building> builds) {
        }

        @Override
        public boolean shouldAmbientSound() {
            return !clogged();
        }

        public int[] buildBlending() {
            int[] blendresult = new int[5];

            blendresult[0] = 0;
            blendresult[1] = blendresult[2] = 1;
            blendresult[3] = 0;

            var fromSides = new int[] { -1, -1, -1 };

            var i = 0;
            for (Building near : proximity) {
                Tmp.v1.set(this.getRelOffset(near)).rotate(rotation * -90);
                var angle = Mathf.round((Tmp.v1.angle() / 90f));
                angle = Mathf.mod(angle, 4);

                if (angle != 0) {
                    if (near instanceof ConveyorBuild) {
                        var nearc = (ConveyorBuild) near;
                        if (nearc.front1.build == this || nearc.front2.build == this)
                            label2: {

                                for (int side : fromSides) {
                                    if (side == angle) {
                                        break label2;
                                    }
                                }
                                fromSides[i++] = angle;
                            }
                    }
                }
            }

            if (i == 0) {
                return blendresult;
            }

            if (i == 1) {
                if (fromSides[0] != 2) {
                    var side = fromSides[0];
                    blendresult[0] = 1;
                    blendresult[2] = (side == 1 ? 1 : -1);
                }
            }

            if (i == 2) {
                var s1 = fromSides[0];
                var s2 = fromSides[1];

                if (s1 == 2 || s2 == 2) {
                    var side = s1 == 2 ? s2 : s1;
                    blendresult[0] = 2;
                    blendresult[2] = (side == 1 ? -1 : 1);
                } else {
                    blendresult[0] = 4;
                }
            }

            if (i == 3) {
                blendresult[0] = 3;
            }

            return blendresult;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();

            int[] bits = buildBlending();
            blendbits = bits[0];
            blendsclx = bits[1];
            blendscly = bits[2];
            innerBelt = (short) Mathf.round((-blendscly / 2f) + 0.5f);

            // -1 -> 0
            // 1 -> 1

            var thisX = x / tilesize;
            var thisY = y / tilesize;

            Tmp.v3.trns(rotation * 90, 1.5f);
            Tmp.v1.trns(rotation * 90, 0f, 0.5f);
            Tmp.v2.trns(rotation * 90, 0f, -0.5f);

            Tmp.v1.add(Tmp.v3).add(thisX, thisY);
            Tmp.v2.add(Tmp.v3).add(thisX, thisY);

            front1 = Vars.world.tile((int) Tmp.v1.x, (int) Tmp.v1.y);
            front2 = Vars.world.tile((int) Tmp.v2.x, (int) Tmp.v2.y);

            if (front1.build == null || front2.build == null) {
                nextc = null;
            }

            if (front1.build == front2.build && (front1.build instanceof ConveyorBuild)) {
                nextc = (ConveyorBuild) front1.build;
            } else {
                nextc = null;
            }
        }

        @Override
        public void unitOn(Unit unit) {
            if (clogged() || !enabled)
                return;

            noSleep();

            float mspeed = speed * tilesize * 55f;
            float centerSpeed = 0.1f;
            float centerDstScl = 3f;
            float tx = Geometry.d4x(rotation), ty = Geometry.d4y(rotation);

            float centerx = 0f, centery = 0f;

            if (Math.abs(tx) > Math.abs(ty)) {
                centery = Mathf.clamp((y - unit.y()) / centerDstScl, -centerSpeed, centerSpeed);
                if (Math.abs(y - unit.y()) < 1f)
                    centery = 0f;
            } else {
                centerx = Mathf.clamp((x - unit.x()) / centerDstScl, -centerSpeed, centerSpeed);
                if (Math.abs(x - unit.x()) < 1f)
                    centerx = 0f;
            }

            unit.impulse((tx * mspeed + centerx) * delta(), (ty * mspeed + centery) * delta());
        }

        @Override
        public void updateTile() {
            if (belts[0].len == 0 && belts[1].len == 0) {
                belts[0].clogHeat = belts[1].clogHeat = 0f;
                belts[0].minitem = belts[1].minitem = 2f;
                sleep();
                return;
            }

            belts[0].update(front1.build);
            belts[1].update(front2.build);

            noSleep();
        }

        public boolean clogged() {
            return belts[0].clogHeat >= 0.5f && belts[1].clogHeat >= 0.5f;
        }

        public boolean pass(Item item, int side) {
            Building next = side == 0 ? front1.build : front2.build;

            if (item != null && next != null && next.team == team && next.acceptItem(this, item)) {
                if (nextc != null) {
                    if (nextc.blendbits <= 1
                            || ((nextc.blendbits == 2 || nextc.blendbits == 3) && nextc.rotation == rotation)) {
                        if (nextc.belts[side].hasSpace()) {
                            nextc.handleItem(side, item);
                        } else {
                            return false;
                        }
                        return true;
                    } else if (nextc.blendbits >= 2) {
                        var targetSide = nextc.getRelOffset(this).rotate(nextc.rotation * -90).y > 0 ? 0 : 1;
                        var theX = ((targetSide == 0 ? 1 - side : side) + 0.5f);
                        theX = Mathf.approach(theX, 1f, 0.125f);
                        if (nextc.belts[targetSide].hasSpace(theX)) {
                            nextc.belts[targetSide].beltItems[nextc.belts[targetSide].insertAt(item,
                                    theX)].x = (1f * (targetSide == 0 ? 1f : -1f));
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (next instanceof ConveyorBuild bnext) {
                        if (next.rotation == rotation) {
                            // If next belt isnt not aligned
                            var targetSide = 1 - side;
                            var offset = side == 0 ? -0.5f : 0.5f;

                            if (bnext.belts[targetSide].hasSpace()) {
                                bnext.handleItem(targetSide, item);
                                bnext.belts[targetSide].beltItems[0].x = offset;
                                return true;
                            }
                        } else {
                            if (bnext.blendbits == 1) {
                                var targetSide = 1 - side;
                                if (bnext.belts[targetSide].hasSpace()) {
                                    bnext.handleItem(targetSide, item);
                                    if (bnext.blendbits == 1) {
                                        bnext.belts[targetSide].beltItems[0].x = (targetSide == bnext.innerBelt ? -1.25f
                                                : -2.5f) * -bnext.blendscly;
                                    }
                                    return true;
                                } else {
                                    return false;
                                }
                            } else if (bnext.blendbits >= 2) {
                                var targetSide = bnext.getRelOffset(this).rotate(bnext.rotation * -90).y > 0 ? 0 : 1;
                                var theX = 2f - ((targetSide == 0 ? 1 - side : side) + 0.5f);
                                theX = Mathf.approach(theX, 1f, 0.125f);
                                if (bnext.belts[targetSide].hasSpace(theX)) {
                                    bnext.belts[targetSide].beltItems[bnext.belts[targetSide].insertAt(item,
                                            theX)].x = (1f * (targetSide == 0 ? 1f : -1f));
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                    } else {
                        // If building is not a conveyor
                        if (next.block.allowConv) {
                            next.handleItem(this, item);
                        } else {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public int removeStack(Item item, int amount) {
            noSleep();

            int removed = 0;

            var curBelt = 0;
            for (int j = 0; j < amount; j++) {
                var belt = belts[curBelt];
                var otherbelt = belts[curBelt == 0 ? 1 : 0];

                var r = false;
                for (int i = 0; i < belt.len; i++) {
                    if (belt.beltItems[i].item == item) {
                        belt.removeAt(i);
                        removed++;
                        r = true;
                        break;
                    }
                }

                if (!r) {
                    for (int i = 0; i < otherbelt.len; i++) {
                        if (otherbelt.beltItems[i].item == item) {
                            otherbelt.removeAt(i);
                            removed++;
                            break;
                        }
                    }
                }

                curBelt = curBelt == 0 ? 1 : 0;
            }

            items.remove(item, removed);
            return removed;
        }

        @Override
        public void getStackOffset(Item item, Vec2 trns) {
            trns.trns(rotdeg() + 180f, tilesize / 2f);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            return 0;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            Tile facing = Edges.getFacingEdge(source.tile, tile);

            if (facing == null) {
                return false;
            }

            var relCoords = this.getRelCoords(facing).rotate(rotation * -90);
            var angle = Mathf.angle(relCoords.x, relCoords.y);
            var direction = (Math.floor((angle - 135f) / 90f) + 40) % 4;

            if (!(source instanceof ConveyorBuild)) {
                return direction == 0 && source.block.allowConv;
            } else {
                if (direction == 2) {
                    return false;
                } else {
                    if (direction == 0 || blendbits <= 1) {
                        return checkCapacity(source);
                    } else {
                        if (blendbits >= 2) {
                            return checkCapacitySide(source);
                        }
                    }
                }
            }

            Log.info("brainrot v2");
            return false;
        }

        public boolean checkCapacitySide(Building source) {
            return belts[this.getRelOffset(source).rotate(rotation * -90).y < 0 ? 1 : 0].len < perBeltCap;
        }

        public boolean checkCapacity(Building source) {
            var touching = this.getTouching(source);

            int belt;
            if (source.block.size == 1) {
                belt = this.getRelOffset(source).rotate(rotation * -90).y < 0 ? 1
                        : 0;
            } else if (touching.size == 2) {
                return (belts[0].len < perBeltCap && belts[0].hasSpace())
                        || (belts[1].len < perBeltCap && belts[1].hasSpace());
            } else {
                belt = getRelOffset(source).rotate(rotation * -90).y > 0 ? 0
                        : 1;
            }
            return belts[belt].len < perBeltCap && belts[belt].hasSpace();
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (!checkCapacity(source)) {
                return;
            }

            Draw.z(Layer.block + 1);

            var touching = this.getTouching(source);

            Vec2[] touchCoords = new Vec2[touching.size];

            for (int i = 0; i < touching.size; i++) {
                touchCoords[i] = this.getRelCoords(touching.get(i)).rotate(rotation * -90);
            }

            int side;
            boolean inOrder = true;

            if (touching.size > 1) {
                inOrder = touchCoords[0].y > touchCoords[1].y;
            }

            @SuppressWarnings("unused")
            Tile sourceTile;

            if (touching.size == 1) {
                side = (touchCoords[0].y > 0) ? 0 : 1;
                sourceTile = touching.get(0);
            } else {
                var accept1 = belts[0].hasSpace();
                var accept2 = belts[1].hasSpace();

                if (accept1 && accept2) {
                    side = (dumpNo + (inOrder ? 0 : 1)) % 2;
                    sourceTile = touching.get(dumpNo % 2);
                } else if (accept1 && !accept2) {
                    side = 0;
                    sourceTile = touching.get(inOrder ? 0 : 1);
                } else if (!accept1 && accept2) {
                    side = 1;
                    sourceTile = touching.get(inOrder ? 1 : 0);
                } else {
                    side = -1;
                    Log.err("Brainrot");
                }

                dumpNo++;
            }

            belts[side].addItemToStart(item);
        }

        public void handleItem(int side, Item item) {
            Draw.z(Layer.block + 1);

            dumpNo++;

            belts[side].addItemToStart(item);
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            for (int i = 0; i < 2; i++) {
                write.i(belts[i].len);
                for (int j = 0; j < belts[i].len; j++) {
                    var curItem = belts[i].beltItems[j];
                    write.i(Pack.intBytes((byte) curItem.item.id, (byte) (curItem.x * 127),
                            (byte) (curItem.y * 127 - 128), (byte) 0));
                }
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            for (int i = 0; i < 2; i++) {
                belts[i].len = read.i();
                for (int j = 0; j < belts[i].len; j++) {
                    int val = read.i();

                    short id = (short) (((byte) (val >> 24)) & 0xff);
                    float x = (float) ((byte) (val >> 16)) / 127f;
                    float y = ((float) ((byte) (val >> 8)) + 128f) / 127f;

                    var curBelt = belts[i];

                    if (j < perBeltCap) {
                        curBelt.beltItems[j] = new ConveyorItem(content.item(id), x, y);
                    }
                }
            }
            onProximityUpdate();
            updateTile();
        }

        @Override
        public Object senseObject(LAccess sensor) {
            if (sensor == LAccess.firstItem) {
                int beltNo;

                if (belts[0].len > 0)
                    beltNo = 0;
                else if (belts[1].len > 0)
                    beltNo = 1;
                else {
                    return null;
                }

                return belts[beltNo].beltItems[belts[beltNo].len - 1].item;
            }
            return super.senseObject(sensor);
        }

        @Nullable
        @Override
        public Building next() {
            return nextc;
        }
    }
}
 */