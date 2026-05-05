package axthrix.world.types.unittypes;

import axthrix.world.types.entities.comp.WorldUnit;
/**
 * Unit Type with the ability to build on it
 * @code {@code setGridLayout(new byte[][]{
 *     {0,0,0,0,0,0,0,0,0,0},
 *     {1,1,1,1,0,0,0,0,0,0},
 *     {1,1,1,1,1,1,0,0,0,0},
 *     {1,1,1,1,1,1,1,1,1,1},
 *     {1,1,1,1,1,1,1,1,1,1},
 *     {1,1,1,1,1,1,1,1,1,1},
 *     {1,1,1,1,1,1,1,1,1,1},
 *     {1,1,1,1,1,1,1,1,1,1},
 *     {1,1,1,1,1,1,1,1,1,1},
 *     {0,0,0,0,0,0,0,0,0,0}
 *});}
 * @author Siede
 */
public class WorldUnitType extends AxUnitType{
    public int buildSize = 4;
    public boolean[][] buildArea = new boolean[buildSize][buildSize];
    public WorldUnitType(String name)
    {
        super(name);
        constructor = () -> new WorldUnit(this);
    }


    public void setGridLayout(byte[][] layout) {
        buildArea = new boolean[layout.length][layout[0].length];
        for(int i1 = 0;i1 < layout.length;i1++) {
            for (int i2 = 0;i2 < layout[i1].length;i2++) {
                buildArea[i1][i2] = layout[i1][i2] > 0;
            }
        }
        buildSize = Math.max(layout.length,layout[0].length);
    }
}
