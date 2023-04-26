package mindustry.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.type.unit.*;
import mindustry.type.weapons.*;
import mindustry.world.meta.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;

public class UnitTypes{
    //region standard

    //mech
    public static @EntityDef({Unitc.class, Mechc.class}) UnitType barrier, blockade, pailsade, parapet, impediment;

    //legs
    public static @EntityDef({Unitc.class, Legsc.class}) UnitType anwir, azalea;

    //hover unused
    //public static @EntityDef({Unitc.class, ElevationMovec.class}) UnitType ;

    //air
    public static @EntityDef({Unitc.class}) UnitType spark, bolt, crack, thunder, lightning, da, da2, dh, dh2, bijou, bijoux, ambrosia;

    //payload unused
    //public static @EntityDef({Unitc.class, Payloadc.class}) UnitType ;

    //naval
    public static @EntityDef({Unitc.class, WaterMovec.class}) UnitType ace, adept, maestro, doyen, demon;

    //tank
    public static @EntityDef({Unitc.class, Tankc.class}) UnitType anagh, akshaj, amitojas, agnitejas, ayustejas

    //region neoplasm

    public static @EntityDef({Unitc.class, Crawlc.class}) UnitType latum, renale;

    //endregion

    public static void load(){

    }
}