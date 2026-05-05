package axthrix.world.types.sea.managers;

import arc.math.geom.Point2;
import arc.struct.IntQueue;
import arc.struct.IntSet;
import axthrix.content.blocks.AxthrixEnvironment;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Tile;

public class UnderwaterZone {

    /** World position of the center of the largest water body. */
    public static int centerX = -1, centerY = -1;

    /** Size of the largest water body in tiles. */
    public static int largestBodySize = 0;

    /** Whether a valid underwater zone was found on load. */
    public static boolean valid = false;

    private static final int[] dx = {1, -1, 0, 0};
    private static final int[] dy = {0, 0, 1, -1};

    /** Runs BFS flood fill to find the largest contiguous water body.
     *  Called on WorldLoadEvent. */
    public static void scan() {
        centerX = -1;
        centerY = -1;
        largestBodySize = 0;
        valid = false;

        int w = Vars.world.width();
        int h = Vars.world.height();

        IntSet visited = new IntSet();
        IntQueue queue = new IntQueue();

        int bestSize = 0;
        int bestCX = -1, bestCY = -1;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int key = Point2.pack(x, y);
                if (visited.contains(key)) continue;

                Tile tile = Vars.world.tile(x, y);
                if (tile == null || !isWater(tile)) continue;

                // BFS from this water tile
                int bodySize = 0;
                int sumX = 0, sumY = 0;

                queue.clear();
                queue.addLast(key);
                visited.add(key);

                while (queue.size > 0) {
                    int current = queue.removeFirst();
                    int cx = Point2.x(current);
                    int cy = Point2.y(current);

                    bodySize++;
                    sumX += cx;
                    sumY += cy;

                    for (int d = 0; d < 4; d++) {
                        int nx = cx + dx[d];
                        int ny = cy + dy[d];
                        int nkey = Point2.pack(nx, ny);

                        if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                        if (visited.contains(nkey)) continue;

                        Tile neighbor = Vars.world.tile(nx, ny);
                        if (neighbor == null || !isWater(neighbor)) continue;

                        visited.add(nkey);
                        queue.addLast(nkey);
                    }
                }

                if (bodySize > bestSize) {
                    bestSize = bodySize;
                    bestCX = sumX / bodySize;
                    bestCY = sumY / bodySize;
                }
            }
        }

        if (bestSize > 0) {
            centerX = bestCX;
            centerY = bestCY;
            largestBodySize = bestSize;
            valid = true;
        }
    }

    private static boolean isWater(Tile tile) {
        return tile.floor() == Blocks.water
                || tile.floor() == Blocks.deepwater
                || tile.floor() == AxthrixEnvironment.tharaxianShallows
                || tile.floor() == AxthrixEnvironment.tharaxianDeep;
    }
}