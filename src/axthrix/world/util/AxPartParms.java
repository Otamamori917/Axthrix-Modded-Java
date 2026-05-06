package axthrix.world.util;

import mindustry.entities.part.DrawPart;

public class AxPartParms {
    public static final AxPartParms.AxPartParams axparams = new AxPartParms.AxPartParams();

    public static final AxPartParms.PerkPartParams perkparams = new AxPartParms.PerkPartParams();

    public static class AxPartParams {
        public float SecondaryReload, SecondarySmoothReload, SecondaryHeat;

        public AxPartParams set(float sr, float ssr, float sh) {
            SecondaryReload = sr;
            SecondarySmoothReload = ssr;
            SecondaryHeat = sh;
            return this;
        }
    }

    /**
     * Custom params object for PerkWeaponType and PerkTurret part progress.
     *
     * @perkProgress : How charged the primary perk is (0.0 = no progress, 1.0 = max stacks).
     * @perkActivated : Whether the primary perk is fully activated (1.0 = yes, 0.0 = no).
     */
    public static class PerkPartParams {
        /** Perk charge progress. Range: 0.0 to 1.0. */
        public float perkProgress;
        /** Perk activated state. 1.0 if at max stacks, 0.0 otherwise. */
        public float perkActivated;

        public PerkPartParams set(float progress, float activated) {
            perkProgress = progress;
            perkActivated = activated;
            return this;
        }
    }

    public interface AxPartProgress {
        DrawPart.PartProgress
                secondaryReload = p -> axparams.SecondaryReload,
                secondarySmoothReload = p -> axparams.SecondarySmoothReload,
                secondaryHeat = p -> axparams.SecondaryHeat;

        float get(AxPartParams p);
    }

    /** PartProgress accessors for perk weapon/turret parts. */
    public interface PerkPartProgress {
        DrawPart.PartProgress
                perkProgress = p -> perkparams.perkProgress,
                perkActivated = p -> perkparams.perkActivated;
    }
}