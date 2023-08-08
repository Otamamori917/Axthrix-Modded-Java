package axthrix.world.types.unittypes;

import axthrix.world.types.ai.AttackDroneAI;
import axthrix.world.types.ai.ControlledDroneAI;
import mindustry.ai.types.MissileAI;
import mindustry.gen.Sounds;
import mindustry.gen.TimedKillUnit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.world.meta.Env;

public class DroneUnitType extends AxUnitType{

    public DroneUnitType(String name){
        super(name);

        playerControllable = false;
        createWreck = false;
        createScorch = false;
        logicControllable = false;
        isEnemy = true;
        useUnitCap = false;
        allowedInPayloads = false;
        flying = true;
        constructor = TimedKillUnit::create;
        trailLength = 7;
        hidden = true;
        hoverable = false;
        speed = 4f;
        lifetime = 6000f;
        rotateSpeed = 2.5f;
        range = 6f;
        targetPriority = -1f;
        outlineColor = Pal.darkOutline;
        fogRadius = 2f;
    }
}