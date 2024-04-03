package axthrix.world.types.block.defense;

import arc.math.geom.*;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.*;
import axthrix.world.types.AxFaction;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;
import axthrix.world.types.block.*;
import axthrix.world.util.*;

import static mindustry.Vars.*;

public class PayloadTurretType extends PayloadAmmoTurret{
    public float payloadSpeed = 0.7f;
    public float minLoadWarmup = 1f;

    public Seq<AxFaction> faction = new Seq<>();
    public boolean blackListFactions = false;

    public boolean partOfPlayerFaction()
    {
        if (blackListFactions)
            return faction.count(f -> f.partOf(Vars.player.team())) == 0;
        return faction.size == 0 || faction.count(f -> f.partOf(Vars.player.team())) > 0;
    }

    @Override
    public boolean isVisible(){
        return state.rules.editor || (partOfPlayerFaction() && !isHidden() && (!state.rules.hideBannedBlocks || !state.rules.isBanned(this)));
    }

    @Override
    public boolean isPlaceable(){
        return Vars.net.server() || (!state.rules.isBanned(this) || state.rules.editor) && supportsEnv(state.rules.env);
    }

    public PayloadTurretType(String name){
        super(name);

        maxAmmo = 1;
        shootEffect = smokeEffect = Fx.none;
        outlinedIcon = 3;

        drawer = new DrawIPayloadTurret(true);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, AxStatValues.ammo(ammoTypes, true));
    }

    public void setWarmupTime(float seconds){
        minLoadWarmup = 0f;
        minWarmup = 1f;
        linearWarmup = true;
        shootWarmupSpeed = 1 / (seconds * 60f);
    }

    public void setUsers(){
        for(var entry : ammoTypes.copy().entries()){
            if(entry.key instanceof PayloadAmmoBlock m){
                m.user = this;
                m.bullet = entry.value;
            }
        }
    }

    public class SinglePayloadAmmoTurretBuild extends PayloadTurretBuild{
        public Payload payload;
        public float payLen;
        public Vec2 payVector = new Vec2();

        @Override
        public void updateTile(){
            if(moveInPayload()){
                payloads.add(payload.content());
                payload = null;
            }

            super.updateTile();
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return this.payload == null && shootWarmup <= minLoadWarmup && super.acceptPayload(source, payload);
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            this.payload = payload;
            this.payVector.set(source).sub(this).clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
            payLen = payVector.len();
        }

        public void updatePayload(){
            if(payload != null){
                payload.set(x + payVector.x, y + payVector.y, payload.rotation());
            }
        }

        public boolean moveInPayload(){
            if(payload == null) return false;

            updatePayload();

            payVector.approach(Vec2.ZERO, payloadSpeed * delta());

            return hasArrived();
        }

        public boolean hasArrived(){
            return payVector.isZero(0.01f);
        }

        public float payloadf(){
            return payVector.len() / payLen;
        }

        @Override
        protected void shoot(BulletType type){
            super.shoot(type);

            if(minLoadWarmup < 0.999f){ //Ensure that it doesn't attempt to load before resetting.
                shootWarmup -= shootWarmupSpeed * Time.delta;
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(payVector.x);
            write.f(payVector.y);
            write.f(payLen);
            Payload.write(payload, write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                payVector.set(read.f(), read.f());
                payLen = read.f();
                payload = Payload.read(read);
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}
