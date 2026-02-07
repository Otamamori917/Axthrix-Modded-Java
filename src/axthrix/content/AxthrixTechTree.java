package axthrix.content;

import arc.Events;
import axthrix.content.blocks.AxthrixCrafters;
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
            node(AxFactions.axthrix.icon,() -> {
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
                node(AxthrixCrafters.caliberPress,() -> {
                    node(PayloadAmmoBlocks.empty1mCaliber,() -> {

                    });
                    node(AxthrixCrafters.caliberCrafter,() -> {
                        node(PayloadAmmoBlocks.basic1mCaliber,() -> {
                            node(PayloadAmmoBlocks.frostbite1mCaliber,() -> {

                            });
                            node(PayloadAmmoBlocks.incendiary1mCaliber,() -> {

                            });
                            node(PayloadAmmoBlocks.quicksilver1mCaliber,() -> {

                            });
                        });
                        node(PayloadAmmoBlocks.arcane1mCaliber,() -> {
                            node(PayloadAmmoBlocks.tempest1mCaliber,() -> {

                            });
                            node(PayloadAmmoBlocks.sonicwave1mCaliber,() -> {

                            });
                            node(PayloadAmmoBlocks.void1mCaliber,() -> {

                            });
                        });
                    });
                });
                node(AxthrixCrafters.pCoilPress,() -> {
                    node(AxthrixCrafters.centrifugalAccelerator,() -> {

                    });
                });
                node(AxthrixCrafters.liquidDeposit,() -> {
                    node(AxthrixCrafters.broiler,() -> {
                        node(AxthrixCrafters.chemicalSeparator,() -> {
                            node(AxthrixCrafters.liquidator,() -> {
                                node(AxthrixUnits.sig,() -> {
                                /*node(AxthrixUnits.colt,() -> {
                                    node(AxthrixUnits.caiber,() -> {
                                        node(AxthrixUnits.magnum,() -> {
                                            node(AxthrixUnits.siegfried,() -> {

                                            });
                                        });
                                    });
                                });*/
                                });
                            });
                            node(AxthrixCrafters.componentPrinter,() -> {
                                node(AxthrixCrafters.coreCrafter,() -> {

                                });
                            });
                        });
                    });
                });
            });
            node(AxFactions.raodon.icon,() -> {

            });
            node(AxFactions.ikatusa.icon,() -> {
                node(IkatusaUnits.SpawnJelly,() -> {
                    node(IkatusaUnits.YoungJelly,() -> {
                        node(IkatusaUnits.JuvenileJelly,() -> {
                            node(IkatusaUnits.AdultJelly,() -> {
                                node(IkatusaUnits.ElderJelly,() -> {

                                });
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
