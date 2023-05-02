package ajmain;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

import ajmain.content.BlocksA;
import ajmain.content.UnitsAJava;
import ajmain.content.StatusA;

public class starter extends Mod{

    public starter(){
        Log.info("Loaded  constructor.");

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("welcome");
                dialog.cont.add("welcome to Axthrix this mod is still in development so expect bugs").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(Core.atlas.find("aj-icon")).pad(20f).row();
                dialog.cont.button("okay", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
        StatusA.load();
        UnitsAJava.load();
        BlocksA.load();
        Log.info("Axthrix Content Loaded. :)");
    }

}