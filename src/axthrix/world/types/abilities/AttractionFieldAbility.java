package axthrix.world.types.abilities;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import blackhole.entities.abilities.BlackHoleAbility;
import mindustry.content.Fx;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class AttractionFieldAbility extends BlackHoleAbility {
    public AttractionFieldAbility(){
		drawBlackHole = false;
		swirlEffect = Fx.none;
    }
    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-attraction-field");
    }
    @Override
    public void addStats(Table t){
		t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + Strings.autoFixed(30f * damage, 2) + " " + StatUnit.perSecond.localized());
        t.row();
		t.add("[lightgray]Attack " + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(damageRadius / tilesize, 2) + " " + StatUnit.blocks.localized());
		t.row();
		t.add("[lightgray]Attraction " + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(suctionRadius / tilesize, 2) + " " + StatUnit.blocks.localized());
		t.row();
    }

}
