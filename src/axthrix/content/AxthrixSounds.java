package axthrix.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.*;
import arc.audio.*;
import mindustry.*;

public class AxthrixSounds {
    public static Sound lightningStrike;

    public static void load() {

        //credits to Betamindy mod
        lightningStrike = l();

    }

    protected static Sound l() {
        return loadSound(soundFiles[num++]);
    }


    protected static Sound loadSound(String soundName) {
        if(!Vars.headless) {
            String name = "sounds/" + soundName;
            String path = name + ".ogg";

            Sound sound = new Sound();

            AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundLoader.SoundParameter(sound));
            desc.errored = Throwable::printStackTrace;

            return sound;
        } else {
            return new Sound();
        }
    }
}
