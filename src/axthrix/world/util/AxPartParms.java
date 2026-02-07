package axthrix.world.util;

import mindustry.entities.part.DrawPart;

public class AxPartParms {
    public static final AxPartParms.AxPartParams axparams = new AxPartParms.AxPartParams();

    public static class AxPartParams{
        public float SecondaryReload,SecondarySmoothReload;

        public AxPartParams set(float sr,float ssr){
            SecondaryReload = sr;
            SecondarySmoothReload = ssr;

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
                secondarySmoothReload = p -> axparams.SecondarySmoothReload
                        ;

        float get(AxPartParams p);
    }

}

