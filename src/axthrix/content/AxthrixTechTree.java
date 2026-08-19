package axthrix.content;

import arc.Events;
import axthrix.content.blocks.AxthrixCrafters;
import axthrix.content.blocks.AxthrixTurrets;
import axthrix.content.blocks.PayloadAmmoBlocks;
import axthrix.content.units.AxthrixUnits;
import axthrix.content.units.IkatusaUnits;
import mindustry.Vars;
import mindustry.game.EventType;
import axthrix.world.teamResearch;


import static mindustry.content.TechTree.node;
import static mindustry.content.TechTree.nodeRoot;


public class AxthrixTechTree {
    public static void load()
    {
        AxPlanets.Tharaxia.techTree = nodeRoot("Tharaxia",AxPlanets.Tharaxia,() -> {
            nodeRoot("Axthrix",AxFactions.axthrix,() -> {


            });
            nodeRoot("Ikatusa",AxFactions.ikatusa,() -> {


            });
            nodeRoot("Raodon",AxFactions.raodon,() -> {


            });
        });


        Events.on(EventType.ResearchEvent.class,(res) -> {
             if (res.content instanceof teamResearch t && t.refTeam.techTree != null)
             {
                 t.refTeam.techTree.planet = AxPlanets.Tharaxia;
                 Vars.ui.research.rebuildTree(t.refTeam.techTree);
             }
        });
    }
}
