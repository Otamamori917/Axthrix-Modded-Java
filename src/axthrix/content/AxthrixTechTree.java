package axthrix.content;

import arc.Events;
import axthrix.content.units.AxthrixUnits;
import mindustry.Vars;
import mindustry.game.EventType;
import axthrix.world.teamResearch;


import static mindustry.content.TechTree.node;
import static mindustry.content.TechTree.nodeRoot;


public class AxthrixTechTree {
    public static void load()
    {
        AxPlanets.Tharaxia.techTree = nodeRoot("Tharaxia",AxPlanets.Tharaxia,() -> {
            node(AxFactions.axthrix.icon);
            node(AxFactions.raodon.icon);
            node(AxFactions.ikatusa.icon);
        });
        AxFactions.axthrix.techTree = nodeRoot ("Axthrix", AxFactions.axthrix.icon,() -> {
            node(AxthrixUnits.barrier,() -> {
                node(AxthrixUnits.blockade,() -> {
                    node(AxthrixUnits.palisade,() -> {
                        node(AxthrixUnits.parapet,() -> {
                            node(AxthrixUnits.impediment,() -> {

                            });
                        });
                    });
                });
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
