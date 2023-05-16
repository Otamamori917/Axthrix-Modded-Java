package ajmain;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

import ajmain.content.*;

public class starter extends Mod{

    public starter(){
        Log.info("Loaded  constructor.");
    }

    @Override
    public void loadContent(){
        StatusA.load();
        UnitsAJava.load();
        BlocksA.load();
        Log.info("Axthrix Content Loaded. :)");

        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog(Core.bundle.get("menu.aj-menu.title"));
                dialog.cont.add(Core.bundle.get("menu.aj-menu.message")).row();
                dialog.cont.image(Core.atlas.find("aj-icon")).pad(20f).row();
                dialog.cont.button("@okay", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }
}