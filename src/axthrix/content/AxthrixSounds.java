package axthrix.content;

import arc.audio.Sound;
import arc.struct.Seq;

import static mindustry.Vars.*;

public class AxthrixSounds {
     static Sound
             PandemoniumMoving = new Sound(),
             PandemoniumScreaming = new Sound(),
             PandemoniumMinigameTheme = new Sound()

    ;

    public static void  LoadSounds(){
        //Note: Vars.tree.loadSound only works with .mp3 and .ogg
        PandemoniumMoving = tree.loadSound("pandemonium-moving");
        PandemoniumScreaming = tree.loadSound("pandemonium-screaming");
        PandemoniumMinigameTheme = tree.loadSound("pandemonium-minigame-theme");
    }
}
