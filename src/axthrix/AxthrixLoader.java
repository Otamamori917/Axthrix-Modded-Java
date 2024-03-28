package axthrix;

import arc.*;
import arc.util.*;
import axthrix.content.blocks.axthrix.AxthrixCrafters;
import axthrix.content.blocks.axthrix.AxthrixTurrets;
import axthrix.content.units.*;
import axthrix.world.util.StackWorldState;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

import axthrix.content.*;

public class AxthrixLoader extends Mod{

    public AxthrixLoader(){
        Log.info("Loaded constructor.");
    }

    @Override
    public void loadContent(){
        Log.info("Loading Axthrix content");
        StackWorldState.load();
        AxFactions.load();
        AxStats.load();
        //AxItems.load();
        AxthrixStatus.load();
        //AxLiquids.load();
        AxthrixDrones.load();
        AxthrixUnits.load();
        LegendUnits.load();
        RaodonUnits.load();
        //AxthrixBlocks.load();
        AxthrixCrafters.load();
        //AxthrixPower.load();
        AxthrixTurrets.load();
        AxPlanets.load();
        //AxSectorPresets.load();
        AxthrixTechTree.load();
        Log.info("Axthrix Content Loaded. :)");


        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog(Core.bundle.get("menu.aj-menu.title"));
                dialog.cont.add(Core.bundle.get("menu.aj-menu.message")).row();
                dialog.cont.image(Core.atlas.find("aj-icon")).pad(20f).row();
                dialog.cont.button("okay", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }
}