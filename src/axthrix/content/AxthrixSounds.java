package axthrix.content;

import arc.audio.Sound;
import arc.struct.Seq;

import static mindustry.Vars.*;

public class AxthrixSounds {
     public static Sound
             PandemoniumMoving = new Sound(),
             PandemoniumScreaming = new Sound(),
             PandemoniumMinigameTheme = new Sound(),
             MetalCrash = new Sound(),
             RevolverReload = new Sound(),
             RevolverEmpty = new Sound(),
             Swings = new Sound(),
             Stank = new Sound(),
             Death = new Sound(),
             SolarEmbrace = new Sound(),
             laserwoosh = new Sound(),
             laserememit = new Sound(),
             catchit = new Sound(),
             payitback = new Sound(),
             slam = new Sound(),
             nadeshoot = new Sound(),
             nadeblast = new Sound(),
             parserShoot = new Sound(),
             parserCharge = new Sound(),
             pop = new Sound()



                     ;

    public static void  LoadSounds(){
        //Note: Vars.tree.loadSound only works with .mp3 and .ogg
        PandemoniumMoving = tree.loadSound("pandemonium-moving");
        PandemoniumScreaming = tree.loadSound("pandemonium-screaming");
        PandemoniumMinigameTheme = tree.loadSound("pandemonium-minigame-theme");
        MetalCrash = tree.loadSound("crash-sound");
        RevolverReload = tree.loadSound("revolver-reload");
        RevolverEmpty = tree.loadSound("revolver-empty");
        Swings = tree.loadSound("swings");
        Stank = tree.loadSound("stank-noise");
        Death = tree.loadSound("Boom");
        SolarEmbrace = tree.loadSound("solar-embrace");
        laserwoosh = tree.loadSound("woooshhh");
        laserememit = tree.loadSound("laser2");
        catchit = tree.loadSound("catch");
        payitback = tree.loadSound("payback");
        slam = tree.loadSound("slam");
        nadeshoot = tree.loadSound("grenade");
        nadeblast = tree.loadSound("grenade-blast");
        parserShoot = tree.loadSound("shoot-parser");
        parserCharge = tree.loadSound("charge-parser");
        pop = tree.loadSound("pop");

    }
}
