package axthrix.content;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.Seq;
import arc.util.noise.*;
import axthrix.content.blocks.AxthrixEnvironment;
import mindustry.content.Blocks;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;

public class ThraxiaPlanetGenerator extends SerpuloPlanetGenerator {

    // Sea level — 48% of planet is ocean
    static final float seaLevel = 0.48f;
    static final float shallowLevel = 0.42f;
    static final float beachLevel = 0.53f;
    static final float midLevel = 0.65f;
    static final float highLevel = 0.75f;

    // Noise seeds
    static final int continentSeed = 1;
    static final int islandSeed = 2;
    static final int detailSeed = 3;
    static final int riverSeed = 4;
    static final int wildlifeSeed = 5;
    static final int ferricSeed = 6;

    @Override
    public float getHeight(Vec3 position) {
        // Continental shelf — large low-frequency blob for the main ocean
        float continent = Simplex.noise3d(continentSeed, 1, 0.5f, 1f / 1.8f, position.x, position.y, position.z);

        // Island bumps inside the ocean zone
        float islands = Simplex.noise3d(islandSeed, 3, 0.6f, 1f / 0.6f, position.x, position.y, position.z) * 0.25f;

        // Land detail noise
        float detail = Simplex.noise3d(detailSeed, 4, 0.5f, 1f / 0.4f, position.x, position.y, position.z) * 0.2f;

        // River carving — thin low-frequency valleys
        float river = Simplex.noise3d(riverSeed, 2, 0.5f, 1f / 0.5f, position.x, position.y, position.z);
        float riverCarve = Math.max(0f, 0.03f - Math.abs(river - 0.5f) * 0.4f);

        float h;
        if (continent < seaLevel) {
            // Ocean zone — add islands
            h = continent + islands * 0.3f;
        } else {
            // Land zone — add detail and carve rivers
            h = continent + detail - riverCarve;
        }

        return Mathf.clamp(h);
    }

    @Override
    public void genTile(Vec3 position, TileGen tile) {
        float h = getHeight(position);

        tile.floor = getFloor(h, position);
        tile.block = getWall(h, position);
    }

    private Block getFloor(float h, Vec3 position) {
        // Wildlife noise — scattered patches anywhere on land
        float wildlife = Simplex.noise3d(wildlifeSeed, 2, 0.5f, 1f / 0.3f, position.x, position.y, position.z);

        // Ferric streak noise — short orange veins
        float ferric = Simplex.noise3d(ferricSeed, 3, 0.7f, 1f / 0.2f, position.x, position.y, position.z);

        if (h < shallowLevel) {
            return AxthrixEnvironment.tharaxianDeep;
        } else if (h < seaLevel) {
            return AxthrixEnvironment.tharaxianShallows;
        } else if (h < beachLevel) {
            // Beach/coast — crimson sand with slope variants
            if (h < seaLevel + 0.01f) return AxthrixEnvironment.crimsonSandDarkSlope;
            if (h > beachLevel - 0.01f) return AxthrixEnvironment.crimsonSandLightSlope;
            return AxthrixEnvironment.crimsonSandFloor;
        } else if (h < midLevel) {
            // Low terrain — dark basalt with wildlife and ferric patches
            if (wildlife > 0.72f) return AxthrixEnvironment.tharaxianMoss;
            if (wildlife < 0.18f) return AxthrixEnvironment.voidBloom;
            if (ferric > 0.78f) return AxthrixEnvironment.ferricStreak;
            return AxthrixEnvironment.darkBasalt;
        } else if (h < highLevel) {
            // Mid terrain — tharaxian stone with patches
            if (wildlife > 0.75f) return AxthrixEnvironment.tharaxianMoss;
            if (wildlife < 0.15f) return AxthrixEnvironment.voidBloom;
            if (ferric > 0.80f) return AxthrixEnvironment.ferricStreak;
            return AxthrixEnvironment.tharaxianStone;
        } else {
            // High terrain/peaks — deep slate
            return AxthrixEnvironment.deepSlate;
        }
    }

    private Block getWall(float h, Vec3 position) {
        if (h < seaLevel) return Blocks.air;

        // Wildlife patches don't get walls
        float wildlife = Simplex.noise3d(wildlifeSeed, 2, 0.5f, 1f / 0.3f, position.x, position.y, position.z);
        if (wildlife > 0.72f || wildlife < 0.18f) return Blocks.air;

        if (h < beachLevel) return Blocks.air; // No walls on beach
        if (h < midLevel) {
            // Sparse walls on low terrain
            float wallNoise = Simplex.noise3d(detailSeed, 2, 0.5f, 1f / 0.2f, position.x, position.y, position.z);
            return wallNoise > 0.82f ? AxthrixEnvironment.darkBasaltWall : Blocks.air;
        } else if (h < highLevel) {
            float wallNoise = Simplex.noise3d(detailSeed, 2, 0.5f, 1f / 0.2f, position.x, position.y, position.z);
            return wallNoise > 0.75f ? AxthrixEnvironment.tharaxianStoneWall : Blocks.air;
        } else {
            float wallNoise = Simplex.noise3d(detailSeed, 2, 0.5f, 1f / 0.2f, position.x, position.y, position.z);
            return wallNoise > 0.68f ? AxthrixEnvironment.deepSlateWall : Blocks.air;
        }
    }
}
