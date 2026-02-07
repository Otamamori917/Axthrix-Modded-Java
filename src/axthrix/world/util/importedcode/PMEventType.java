package axthrix.world.util.importedcode;

import mindustry.entities.Effect;
import mindustry.gen.Bullet;

public class PMEventType{
    public static class BallisticMissileLand{
        public final Bullet bullet;
        public final Effect blockEffect;

        public BallisticMissileLand(Bullet bullet, Effect blockEffect){
            this.bullet = bullet;
            this.blockEffect = blockEffect;
        }
    }
}

