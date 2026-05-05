package axthrix.world.types.block;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import axthrix.world.util.importedcode.DrawPseudo3d;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

import static arc.math.Mathf.lerp;

public class ObeliskBlock extends Block {
    public TextureRegion baseRegion, displayBgRegion, sideFrontRegion, sideBackRegion, sideLeftRegion, sideRightRegion, tileRegion, tileBlankRegion, topRegion;
    public TextureRegion[] tileRegions;

    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find(name + "-base", "obelisk-base");
        displayBgRegion = Core.atlas.find(name + "-display-bg", "obelisk-display-bg");

        // Individual side sprites for better blending
        sideFrontRegion = Core.atlas.find(name + "-side-front", "obelisk-side-front");
        sideBackRegion = Core.atlas.find(name + "-side-back", "obelisk-side-back");
        sideLeftRegion = Core.atlas.find(name + "-side-left", "obelisk-side-left");
        sideRightRegion = Core.atlas.find(name + "-side-right", "obelisk-side-right");

        tileRegion = Core.atlas.find(name + "-tile", "obelisk-tile");
        tileBlankRegion = Core.atlas.find(name + "-tile-blank", "obelisk-tile-blank");
        topRegion = Core.atlas.find(name + "-top", "obelisk-top");

        // Load tile variants: obelisk-tile1, obelisk-tile2, etc. until not found
        Seq<TextureRegion> variants = new Seq<>();
        for(int i = 1; i <= 10; i++){
            TextureRegion r = Core.atlas.find(name + "-tile-" + i);
            if(!r.found()) break;
            variants.add(r);
        }
        tileRegions = variants.isEmpty() ? new TextureRegion[]{tileRegion} : variants.toArray(TextureRegion.class);
    }



    public ObeliskBlock(String name) {
        super(name);
        update = true;
        solid = true;
        configurable = true;
        destructible = true;
    }

    public class ObeliskBuild extends Building {
        public String hiddenMessage = "The ancient power awaits";
        public String techName = "large-plasma-drill";
        public boolean solved = false;
        public boolean sentenceMode = false;
        public boolean saveProgress = false;
        public int[] savedBoard = null;
        public int savedEmptyPos = -1;
        public int[] tileVariants = null;

        public int puzzleSize = 3;
        public int tokenCount = 0;
        public Seq<String> tokens;

        public float riseTimer = 0f;
        public static final float riseDuration = 120f;
        public static final float riseDelay = 30f;

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();

            if (this.tile.x == 100 && this.tile.y == 50) {
                this.hiddenMessage = "Secret Location Message";
                this.techName = "large-plasma-drill";
                this.sentenceMode = true;
            }

            updateDisplayStats();
        }


        @Override
        public void placed() {
            super.placed();
            riseTimer = -riseDelay;
            updateDisplayStats();
        }

        private void randomizeTileVariants(int total) {
            tileVariants = new int[total];
            for(int i = 0; i < total; i++){
                tileVariants[i] = Mathf.random(tileRegions.length - 1);
            }
        }

        public void updateDisplayStats() {
            String clean = hiddenMessage.trim();
            int count = sentenceMode ? (clean.isEmpty() ? 0 : clean.split("\\s+").length) : hiddenMessage.replace(" ", "").length();
            puzzleSize = count >= 63 ? 8 : count >= 48 ? 7 : count >= 35 ? 6 : count >= 24 ? 5 : count >= 15 ? 4 : 3;
            tokenCount = count;

            if(sentenceMode){
                tokens = new Seq<>();
                for(String word : clean.split("\\s+")) if(!word.isEmpty()) tokens.add(translateWord(word));
            } else tokens = AncientTranslator.tokenize(hiddenMessage);

            randomizeTileVariants(puzzleSize * puzzleSize);
        }

        @Override
        public void draw() {
            int gSize = Math.max(3, puzzleSize);
            if(baseRegion != null) Draw.rect(baseRegion, x, y, size * Vars.tilesize, size * Vars.tilesize);

            float h = 4.5f * Mathf.pow(Math.max(riseTimer, 0f) / riseDuration, 0.5f);
            float baseW = (size * Vars.tilesize / 1.1f);
            float w = baseW + (gSize - 3) * 2f;
            float l = size * Vars.tilesize / 2.8f;

            float fLx = x-w, fLy = y-l, fRx = x+w, fRy = y-l;
            float bLx = x-w, bLy = y+l, bRx = x+w, bRy = y+l;

            float tfLx = DrawPseudo3d.xHeight(fLx, h), tfLy = DrawPseudo3d.yHeight(fLy, h);
            float tfRx = DrawPseudo3d.xHeight(fRx, h), tfRy = DrawPseudo3d.yHeight(fRy, h);
            float tbLx = DrawPseudo3d.xHeight(bLx, h), tbLy = DrawPseudo3d.yHeight(bLy, h);
            float tbRx = DrawPseudo3d.xHeight(bRx, h), tbRy = DrawPseudo3d.yHeight(bRy, h);

            boolean camAbove = arc.Core.camera.position.y > y;
            boolean camRight = arc.Core.camera.position.x > x;

            Draw.color(Color.white);

            //Draw Furthest Wall
            if(camAbove) drawWall(sideFrontRegion, tfLx, tfLy, fLx, fLy, fRx, fRy, tfRx, tfRy);
            else drawWall(sideBackRegion, tbRx, tbRy, bRx, bRy, bLx, bLy, tbLx, tbLy);

            //Draw Side Walls
            if(camRight) {
                drawWall(sideLeftRegion, tbLx, tbLy, bLx, bLy, fLx, fLy, tfLx, tfLy);
                drawWall(sideRightRegion, tfRx, tfRy, fRx, fRy, bRx, bRy, tbRx, tbRy);
            } else {
                drawWall(sideRightRegion, tfRx, tfRy, fRx, fRy, bRx, bRy, tbRx, tbRy);
                drawWall(sideLeftRegion, tbLx, tbLy, bLx, bLy, fLx, fLy, tfLx, tfLy);
            }

            //Closest Wall
            if(camAbove) drawWall(sideBackRegion, tbRx, tbRy, bRx, bRy, bLx, bLy, tbLx, tbLy);
            else drawWall(sideFrontRegion, tfLx, tfLy, fLx, fLy, fRx, fRy, tfRx, tfRy);

            //Draw Top
            if(topRegion != null) {
                Draw.color(solved ? Pal.accent.cpy().add(Color.white).add(Color.white) : Color.white);
                Fill.quad(topRegion, tfRx, tfRy, tbRx, tbRy, tbLx, tbLy, tfLx, tfLy);
            }

            //Draw Display on the front wall
            if(arc.Core.camera.position.y < (y - l)) {
                drawWallDisplay(x - w, y - l, x + w, y - l, h);
            }
            Draw.reset();
        }

        private void drawWall(TextureRegion region, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
            if(region != null) Fill.quad(region, x1, y1, x2, y2, x3, y3, x4, y4);
        }



        private void drawWallDisplay(float x1, float y1, float x2, float y2, float h) {
            if(puzzleSize <= 0) return;

            int gSize = puzzleSize;
            float startH = h * 0.18f;
            float endH = h * 0.72f;

            float bx1 = DrawPseudo3d.xHeight(x1, startH), by1 = DrawPseudo3d.yHeight(y1, startH);
            float bx2 = DrawPseudo3d.xHeight(x2, startH), by2 = DrawPseudo3d.yHeight(y2, startH);
            float tx1 = DrawPseudo3d.xHeight(x1, endH), ty1 = DrawPseudo3d.yHeight(y1, endH);
            float tx2 = DrawPseudo3d.xHeight(x2, endH), ty2 = DrawPseudo3d.yHeight(y2, endH);

            if(displayBgRegion != null) {
                Draw.color(Color.white);
                Fill.quad(displayBgRegion, tx1, ty1, bx1, by1, bx2, by2, tx2, ty2);
            }

            float pad = 0.05f;
            for(int i = 0; i < gSize * gSize; i++) {
                float gx = (i % gSize) / (float)gSize;
                float gy = 1.0f - ((i / gSize + 1.0f) / (float)gSize);
                float step = 1.0f / (float)gSize;

                float cx1 = lerp(lerp(bx1, bx2, gx), lerp(tx1, tx2, gx), gy);
                float cy1 = lerp(lerp(by1, by2, gx), lerp(ty1, ty2, gx), gy);
                float cx2 = lerp(lerp(bx1, bx2, gx + step), lerp(tx1, tx2, gx + step), gy);
                float cy2 = lerp(lerp(by1, by2, gx + step), lerp(ty1, ty2, gx + step), gy);
                float cx3 = lerp(lerp(bx1, bx2, gx + step), lerp(tx1, tx2, gx + step), gy + step);
                float cy3 = lerp(lerp(by1, by2, gx + step), lerp(ty1, ty2, gx + step), gy + step);
                float cx4 = lerp(lerp(bx1, bx2, gx), lerp(tx1, tx2, gx), gy + step);
                float cy4 = lerp(lerp(by1, by2, gx), lerp(ty1, ty2, gx), gy + step);

                int val = (savedBoard != null && i < savedBoard.length) ? savedBoard[i] : (solved ? i : -1);
                if (val == gSize * gSize - 1 || val == -1) continue;

                TextureRegion sprite;
                if(val >= tokenCount){
                    sprite = tileBlankRegion;
                } else {
                    int variantIdx = (tileVariants != null && val < tileVariants.length) ? tileVariants[val] : 0;
                    sprite = tileRegions[variantIdx];
                }
                boolean isCorrect = solved || (tokens != null && i < tokens.size && val < tokens.size && tokens.get(val).equals(tokens.get(i)));
                Color col = isCorrect ? Pal.accent.cpy().add(Color.white).add(Color.white) : Color.white;

                float midX = (cx1 + cx3) / 2f, midY = (cy1 + cy3) / 2f;
                if(sprite != null) {
                    Draw.color(col);
                    Fill.quad(sprite,
                            lerp(cx4, midX, pad), lerp(cy4, midY, pad),
                            lerp(cx1, midX, pad), lerp(cy1, midY, pad),
                            lerp(cx2, midX, pad), lerp(cy2, midY, pad),
                            lerp(cx3, midX, pad), lerp(cy3, midY, pad)
                    );
                }
            }
        }




        public String translateWord(String word) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (i + 1 < word.length()) {
                    char next = word.charAt(i + 1);
                    if (next == '?' || next == '!' || next == ',') {
                        sb.append(AncientTranslator.getRune(c)).append(next);
                        i++;
                        continue;
                    }
                }
                sb.append(AncientTranslator.getRune(c));
            }
            return sb.toString();
        }

        @Override
        public void updateTile() {
            super.updateTile();

            // 1. Calculate boundaries (Width and Length/Height)
            float baseW = (block.size * Vars.tilesize / 1.1f);
            float w = baseW + (Math.max(3, puzzleSize) - 3) * 2f;
            float l = (block.size * Vars.tilesize / 2.8f);

            // Use a slightly larger range for the search to ensure we catch units on the edges
            float searchRange = Math.max(w, l) + 20f;

            Groups.unit.intersect(x - searchRange, y - searchRange, searchRange * 2, searchRange * 2, unit -> {
                // Only affect flying units (matching your first example)
                if(!unit.isFlying()) return;

                // 2. Square Hitbox Math
                float dx = unit.x - x;
                float dy = unit.y - y;

                // Define the "Collision Box" including the unit's own hitsize
                float hW = w + unit.hitSize / 2f;
                float hL = l + unit.hitSize / 2f;

                // Check if the unit is inside the rectangle
                if(Math.abs(dx) < hW && Math.abs(dy) < hL){

                    // 3. Precise Overlap Correction (Nudging)
                    // Determine which side they are closer to so we push them out the shortest way
                    float overlapX = hW - Math.abs(dx);
                    float overlapY = hL - Math.abs(dy);

                    if(overlapX < overlapY){
                        // Push out Left or Right
                        unit.x += (dx > 0 ? 1 : -1) * overlapX;

                        // 4. Frictionless Bounce (Zero out velocity heading INTO the block)
                        float signX = (dx > 0 ? 1 : -1);
                        if((unit.vel.x * signX) < 0) unit.vel.x = 0;
                    } else {
                        // Push out Up or Down
                        unit.y += (dy > 0 ? 1 : -1) * overlapY;

                        // Frictionless Bounce
                        float signY = (dy > 0 ? 1 : -1);
                        if((unit.vel.y * signY) < 0) unit.vel.y = 0;
                    }

                    // 5. Speed-based Effects (Puff & Kill logic from example 1)
                    float speed = unit.vel.len();
                    if(speed > 4.5f){
                        unit.kill();
                    }

                    Fx.smokePuff.at(unit.x, unit.y);
                }
            });
        }


        @Override
        public void buildConfiguration(Table table) {
            table.button(Icon.logic, () -> {
                if(!solved) new DecipherDialog(this).show();
                else Vars.ui.showInfo("The stone has already been translated.");
            }).size(40f).tooltip("Decipher");

            if(Vars.state.isEditor() || Vars.state.rules.infiniteResources){
                table.button(Icon.settings, () -> {
                    final String oldMessage = hiddenMessage;
                    final String oldTech = techName;
                    final boolean oldMode = sentenceMode;
                    final boolean[] wasSaved = {false};

                    BaseDialog dialog = new BaseDialog("Edit Obelisk Settings");
                    dialog.hidden(() -> {
                        if(!wasSaved[0]) {
                            hiddenMessage = oldMessage;
                            techName = oldTech;
                            sentenceMode = oldMode;
                        }
                    });


                    final int maxTiles = 63, maxWordLen = 12, letterHardLimit = 100, sentenceHardLimit = 800;

                    dialog.cont.table(t -> {
                        t.add("Message: ").left();
                        TextField field = t.field(hiddenMessage, val -> hiddenMessage = val).width(300f).get();
                        field.setMaxLength(sentenceMode ? sentenceHardLimit : letterHardLimit);
                        t.row();

                        t.add(new Label(() -> {
                            String clean = hiddenMessage.trim();
                            String[] words = clean.split("\\s+");
                            boolean wordTooLong = false;
                            if(sentenceMode) {
                                for(String w : words) if (w.length() > maxWordLen) { wordTooLong = true; break; }
                            }
                            int count = sentenceMode ? (clean.isEmpty() ? 0 : words.length) : hiddenMessage.replace(" ", "").length();
                            boolean invalid = count > maxTiles || count == 0 || wordTooLong;
                            String color = invalid ? "[red]" : "[accent]";
                            String warning = wordTooLong ? " [red](Word too long!)[]" : "";
                            return "Tokens: " + color + count + "[] / " + maxTiles + warning;
                        })).left().padLeft(10).row();

                        if(Vars.state.isCampaign()){
                            t.add("Unlock ID: ").left();
                            t.field(techName, val -> techName = val).width(300f).row();
                        }

                        t.check("Sentence Mode", sentenceMode, val -> {
                            sentenceMode = val;
                            field.setMaxLength(sentenceMode ? sentenceHardLimit : letterHardLimit);
                        }).left().row();
                    }).pad(10).row();

                    dialog.cont.button("Reset Puzzle", Icon.refresh, () -> {
                        solved = false;
                        savedBoard = null;
                        savedEmptyPos = -1;
                        updateDisplayStats();
                        Vars.ui.showInfo("Reset complete.");
                    }).size(240f, 50f).pad(10).row();

                    dialog.buttons.button("@back", Icon.left, dialog::hide).size(120f, 64f);
                    TextButton okBtn = dialog.buttons.button("@ok", () -> {
                        wasSaved[0] = true;
                        updateDisplayStats();
                        this.configure(hiddenMessage);
                        dialog.hide();
                    }).size(120f, 64f).get();

                    okBtn.update(() -> {
                        String clean = hiddenMessage.trim();
                        String[] words = clean.split("\\s+");
                        boolean wordTooLong = false;
                        if(sentenceMode) for(String w : words) if (w.length() > maxWordLen) { wordTooLong = true; break; }
                        int count = sentenceMode ? (clean.isEmpty() ? 0 : words.length) : hiddenMessage.replace(" ", "").length();
                        okBtn.setDisabled(count > maxTiles || count == 0 || wordTooLong);
                    });
                    dialog.show();
                }).size(40f).tooltip("Customize");
            }
        }


        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(solved);
            write.bool(saveProgress);
            write.str(hiddenMessage);
            write.str(techName);
            write.bool(sentenceMode);
            write.bool(savedBoard != null && saveProgress);
            if(savedBoard != null && saveProgress){
                write.s(savedBoard.length);
                for(int i : savedBoard) write.s(i);
                write.s(savedEmptyPos);
            }
            write.bool(tileVariants != null);
            if(tileVariants != null){
                write.s(tileVariants.length);
                for(int v : tileVariants) write.s(v);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            solved = read.bool();
            saveProgress = read.bool();
            hiddenMessage = read.str();
            techName = read.str();
            sentenceMode = read.bool();
            riseTimer = riseDuration;
            if(read.bool()){
                int len = read.s();
                savedBoard = new int[len];
                for(int i = 0; i < len; i++) savedBoard[i] = read.s();
                savedEmptyPos = read.s();
            }
            if(read.bool()){
                int len = read.s();
                tileVariants = new int[len];
                for(int i = 0; i < len; i++) tileVariants[i] = read.s();
            } else {
                // First time loading, randomize
                updateDisplayStats();
            }
        }

        public void finish() {
            solved = true;
            Fx.instBomb.at(this);
            UnlockableContent content = Vars.content.getByName(ContentType.block, techName);
            if(Vars.state.isCampaign() && content != null && !content.unlocked()){
                content.unlock();
                Vars.ui.announce("[gold]New Research Available: " + content.localizedName + "[]");
            } else Vars.ui.announce("[accent]Puzzle Solved![]");
        }
    }

    public static class DecipherDialog extends BaseDialog {
        ObeliskBuild build;
        int size, emptyPos;
        int[] board;
        Seq<String> tokens;
        boolean isWinning = false;
        float tileSize = 80f;
        TextButton[] tileButtons;

        public DecipherDialog(ObeliskBuild build) {
            super("Decipher Ancient Text");
            this.build = build;
            update(() -> { if(!build.isValid()) hide(); });

            if(build.sentenceMode){
                tokens = new Seq<>();
                for(String word : build.hiddenMessage.split("\\s+")) {
                    if(!word.isEmpty()) tokens.add(build.translateWord(word));
                }
            } else tokens = AncientTranslator.tokenize(build.hiddenMessage);

            int count = tokens.size;

            // FIXED SIZE LOGIC: Starts at 3x3 and expands until all tokens + 1 hole fit
            size = 3;
            while(size * size - 1 < count && size < 8) {
                size++;
            }

            // Sync with build for rendering
            build.puzzleSize = size;
            build.tokenCount = tokens.size;

            for(String s : tokens) tileSize = Math.max(tileSize, s.length() * 11f + 35f);
            int totalSlots = size * size;
            tileButtons = new TextButton[totalSlots];

            if(build.savedBoard != null && build.savedBoard.length == totalSlots){
                board = build.savedBoard.clone();
                emptyPos = build.savedEmptyPos;
            } else {
                board = new int[totalSlots];
                for(int i = 0; i < totalSlots; i++) board[i] = i;
                emptyPos = totalSlots - 1;
                shuffle();
            }
            setup();
        }

        void shuffle(){
            for(int i = 0; i < 400; i++){
                int[] adj = getAdjacent(emptyPos);
                int move = adj[Mathf.random(adj.length - 1)];
                board[emptyPos] = board[move];
                board[move] = size * size - 1;
                emptyPos = move;
            }
        }

        void setup() {
            cont.clear();
            buttons.clear();
            addCloseButton();

            buttons.button("Reshuffle", Icon.refresh, () -> {
                int total = size * size;
                for(int i = 0; i < total; i++) board[i] = i;
                emptyPos = total - 1;
                shuffle();

                build.savedBoard = board;
                build.savedEmptyPos = emptyPos;
                build.saveProgress = true;

                refreshTiles();
                build.updateDisplayStats();
            }).size(210f, 64f);

            Table main = cont.table().get();
            Table puzzleSide = main.table().get();
            puzzleSide.add(new Label(() -> isWinning ? "[green]Translated: [white]" + build.hiddenMessage : "Decipher Tablet")).padBottom(10).row();

            Table gridTable = new Table();
            int total = size * size;
            for(int i = 0; i < total; i++) {
                int pos = i;
                tileButtons[i] = new TextButton("");
                tileButtons[i].clicked(() -> {
                    if(isWinning) return;
                    if(isAdjacent(pos, emptyPos)) {
                        board[emptyPos] = board[pos];
                        board[pos] = total - 1;
                        emptyPos = pos;

                        build.savedBoard = board;
                        build.savedEmptyPos = emptyPos;
                        build.saveProgress = true;

                        refreshTiles();
                        checkWin();
                    }
                });
                gridTable.add(tileButtons[i]).size(tileSize).pad(2);
                if((i + 1) % size == 0) gridTable.row();
            }
            puzzleSide.add(gridTable);
            refreshTiles();

            main.image().color(Pal.accent).width(3f).fillY().pad(20);
            Table legendSide = main.table().get();
            legendSide.add("[lightgray]Legend").row();
            legendSide.pane(t -> {
                for(char c = 'A'; c <= 'Z'; c++){
                    t.add(c + ": [accent]" + AncientTranslator.getRune(c) + "[] / [lightgray]" + AncientTranslator.getRune(Character.toLowerCase(c)) + "[]").left().padRight(10).row();
                }
            }).maxHeight(450f);
        }

        void refreshTiles() {
            int total = size * size;
            for (int i = 0; i < total; i++) {
                int val = board[i];
                TextButton btn = tileButtons[i];
                if (btn == null) continue;
                if (val == total - 1) {
                    btn.setText("");
                    btn.setDisabled(true);
                    btn.color.a = 0f;
                } else {
                    btn.setDisabled(false);
                    btn.color.a = 1f;
                    boolean hasText = val < tokens.size;
                    btn.setText(hasText ? tokens.get(val) : "");
                    btn.setColor(hasText && i < tokens.size && tokens.get(val).equals(tokens.get(i)) ? Color.valueOf("84f491").mul(0.85f) : Pal.accent);
                }
            }
        }

        void checkWin() {
            for (int i = 0; i < tokens.size; i++) {
                int val = board[i];
                if (val == size * size - 1) return;
                if (val >= tokens.size) return;
                // Matches based on the text/rune content (handles duplicate letters)
                if (!tokens.get(val).equals(tokens.get(i))) return;
            }
            isWinning = true;
            build.savedBoard = null;
            refreshTiles();
            Time.run(120f, () -> { hide(); build.finish(); });
        }

        int[] getAdjacent(int pos){
            IntSeq adj = new IntSeq();
            int x = pos % size, y = pos / size;
            if(x > 0) adj.add(pos - 1);
            if(x < size - 1) adj.add(pos + 1);
            if(y > 0) adj.add(pos - size);
            if(y < size - 1) adj.add(pos + size);
            return adj.toArray();
        }

        boolean isAdjacent(int p1, int p2) {
            return Math.abs(p1 % size - p2 % size) + Math.abs(p1 / size - p2 / size) == 1;
        }
    }



    public static class AncientTranslator {
        private static final ObjectMap<Character, String[]> map = new ObjectMap<>(){{
            put('A', new String[]{"Qm", "lq"});
            put('B', new String[]{"Kvo", "ov"});
            put('C', new String[]{"Wba", "ab"});
            put('D', new String[]{"Wzm", "wm"});
            put('E', new String[]{"Sha", "hs"});
            put('F', new String[]{"Sup", "ps"});
            put('G', new String[]{"Ivcn", "cln"});
            put('H', new String[]{"Shi", "mi"});
            put('I', new String[]{"Huv", "vu"});
            put('J', new String[]{"Cykl", "klo"});
            put('K', new String[]{"Xste", "sce"});
            put('L', new String[]{"Kay", "yl"});
            put('M', new String[]{"Ktuh", "htu"});
            put('N', new String[]{"Czv", "vz"});
            put('O', new String[]{"Vixu", "jiu"});
            put('P', new String[]{"Tius", "tus"});
            put('Q', new String[]{"Ogiu", "yui"});
            put('R', new String[]{"Kxac", "xec"});
            put('S', new String[]{"Lhc", "ch"});
            put('T', new String[]{"Ko", "ku"});
            put('U', new String[]{"Sfv", "va"});
            put('V', new String[]{"Ojyv", "wyv"});
            put('W', new String[]{"Cso", "ol"});
            put('X', new String[]{"Yzav", "azv"});
            put('Y', new String[]{"Shli", "lui"});
            put('Z', new String[]{"Xio", "xo"});
        }};

        public static Seq<String> tokenize(String input) {
            Seq<String> tokens = new Seq<>();
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if(c == ' ') continue;
                if (i + 2 < input.length() && input.charAt(i + 1) == '\'') {
                    tokens.add(getRune(c) + "'" + getRune(input.charAt(i + 2)));
                    i += 2; continue;
                }
                String rune = getRune(c);
                if (i + 1 < input.length()) {
                    char next = input.charAt(i + 1);
                    if (next == '?' || next == '!' || next == ',') { rune += next; i++; }
                }
                tokens.add(rune);
            }
            return tokens;
        }

        public static String getRune(char c) {
            String[] opts = map.get(Character.toUpperCase(c));
            if(opts == null) return String.valueOf(c);
            return (opts.length > 1 && Character.isLowerCase(c)) ? opts[1] : opts[0];
        }
    }
}
