package axthrix.world.util;

import mindustry.entities.part.DrawPart;

public class AxPartParms {
    public static final AxPartParms.AxPartParams axparams = new AxPartParms.AxPartParams();

    public static final AxPartParms.PerkPartParams perkparams = new AxPartParms.PerkPartParams();

    public static final AxPartParms.PlateShieldsPartParams shieldparams = new AxPartParms.PlateShieldsPartParams();

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
     */
    public static class PerkPartParams {
        public float perkProgress;
        public float perkActivated;

        public PerkPartParams set(float progress, float activated) {
            perkProgress = progress;
            perkActivated = activated;
            return this;
        }
    }

    // UPDATED: Added weapon tracking variables directly inside the shield plate params class!
    public static class PlateShieldsPartParams {
        public float plateDurability;
        public float plateRebuildProgress;
        public float plateSmoothRebuild;

        // New Weapon Progress Fields
        public float weaponWarmup;
        public float weaponReload;
        public float weaponRecoil;
        public float weaponHeat;

        public PlateShieldsPartParams set(float durability, float rebuildProgress, float smoothRebuild, float warmup, float reload, float recoil, float heat) {
            this.plateDurability = durability;
            this.plateRebuildProgress = rebuildProgress;
            this.plateSmoothRebuild = smoothRebuild;
            this.weaponWarmup = warmup;
            this.weaponReload = reload;
            this.weaponRecoil = recoil;
            this.weaponHeat = heat;
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

    public interface PlateShieldsPartProgress {
        DrawPart.PartProgress
                Durability = p -> shieldparams.plateDurability,
                RebuildProgress = p -> shieldparams.plateRebuildProgress,
                SmoothRebuild = p -> shieldparams.plateSmoothRebuild,
                WeaponWarmup = p -> shieldparams.weaponWarmup,
                WeaponReload = p -> shieldparams.weaponReload,
                WeaponRecoil = p -> shieldparams.weaponRecoil,
                WeaponHeat = p -> shieldparams.weaponHeat;
    }
}
