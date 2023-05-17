package axthrix;

import arc.*;
import arc.util.*;
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
        AxthrixStatus.load();
        AxthrixUnits.load();
        AxthrixBlocks.load();
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