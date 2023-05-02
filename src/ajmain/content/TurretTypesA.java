package ajmain.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class AcceleratedTurret extends ItemTurret{
    public float  acceleratedDelay = 120, acceleratedBonus = 1.5f;

    public AcceleratedTurret(String name){
        super(name);

        @Override
        protected void updateShooting(){
            if(!hasAmmo()) return;

            act = Mathf.wait(act, acceleratedDelay(), peekAmmo().reloadMultiplier = acceleratedBonus);

            BulletType type = peekAmmo();

            shoot(type);
        }
    }
}