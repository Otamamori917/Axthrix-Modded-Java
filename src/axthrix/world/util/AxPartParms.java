package axthrix.world.util;

import mindustry.entities.part.DrawPart;

public class AxPartParms {
    public static final AxPartParms.AxPartParams axparams = new AxPartParms.AxPartParams();

    public static class AxPartParams{
        public float SecondaryReload,SecondarySmoothReload,SecondaryHeat;

        public AxPartParams set(float sr,float ssr,float sh){
            SecondaryReload = sr;
            SecondarySmoothReload = ssr;
            SecondaryHeat = sh;

            return this;
        }

        /*public AxPartParams set(float health, int team, float elevation, float ammo){
            return set(float sr,float ssr);
        }*/
    }

    public interface AxPartProgress {
        /*AxPartProgress
                secondaryReload = p -> axparams.SecondaryReload,
                secondarySmoothReload = p -> axparams.SecondarySmoothReload
                        ;*/

        DrawPart.PartProgress
                secondaryReload = p -> axparams.SecondaryReload,
                secondarySmoothReload = p -> axparams.SecondarySmoothReload,
                secondaryHeat = p -> axparams.SecondaryHeat
                        ;

        float get(AxPartParams p);
    }

}

