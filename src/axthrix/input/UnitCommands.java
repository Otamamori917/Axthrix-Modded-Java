package axthrix.input;

import mindustry.ai.UnitCommand;
import mindustry.entities.units.AIController;
import axthrix.world.types.ai.*;

public class UnitCommands {

    public static final UnitCommand
        circleCommand = new UnitCommand("1-circle", "commandRally", u-> {
            if(!u.type().flying){
                var ai = new GroundAi();
                ai.shouldCircle = true;
                return ai;
            }else {
                var ai = new AgressiveFlyingAi();
                ai.shouldCircle = true;
                return ai;
            }
        }) {{
            switchToMove = resetTarget = false;
            drawTarget = true;
        }}, healCommand = new UnitCommand("1-heal", "units", u -> new UnitHealerAi()),
        MineCommand = new UnitCommand("mine", "production", u -> new MiningAi()),
        GuardCommand = new UnitCommand ("1-guard", "units", u ->  new ArmDefenderAi()),
        MendCommand = new UnitCommand ("1-mend", "add", u -> {
            //No other better word for this
            var ai = new UnitHealerAi();
            ai.includeBlocks = true;
            return ai;
        }),
        MoveCommand = new UnitCommand("move", "right", u ->new GroundAi()){{
            switchToMove = resetTarget = false;
            drawTarget = true;
        }},
        DeployCommand = new UnitCommand ("nyfalis-deploy", "down", u ->  new AIController()){{
            switchToMove = resetTarget = false;
            drawTarget = true;
        }}
    ;

}
