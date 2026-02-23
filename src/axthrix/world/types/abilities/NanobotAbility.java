package axthrix.world.types.abilities;

import arc.*;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.*;
import axthrix.world.util.NanobotLogic;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.StatusEffect;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static axthrix.content.AxthrixStatus.*;
import static mindustry.Vars.*;

public class NanobotAbility extends Ability {

    public float damage = 2f;
    public float healAmount = 2f;
    public float healPercent = 0.5f;
    public float range = 90f;
    public float tickRate = 5f;
    public float buildingDamageMultiplier = 0.25f;

    public float bulletSpeedBonus = 1.5f;
    public float bulletSlowdown = 0.6f;

    public float efficiencyBoost = 1.5f;
    public float stackPenalty = 0.5f;

    public StatusEffect status = nanodiverge;
    public float statusDuration = 60f * 3f;

    public Color color = Pal.heal;
    public int nanobotCount = 15;
    public float nanobotSize = 0.5f;
    public float nanobotSpeed = 2f;

    public boolean useAmmo = false;

    protected float timer = 0f;
    protected float soundTimer = 0f;
    protected Sound ambientSound = Sounds.loopFlux;
    protected float soundVolume = 0.3f;

    public NanobotAbility(){}

    public NanobotAbility(float damage, float healAmount, float range){
        this.damage = damage;
        this.healAmount = healAmount;
        this.range = range;
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-nanobot-");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + Strings.autoFixed(damage * (60f / tickRate), 2) + " " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]Building Damage: [white]" + Strings.autoFixed(damage * buildingDamageMultiplier * (60f / tickRate), 2) + " " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.healing.localized() + ": [white]" + Strings.autoFixed(healAmount * (60f / tickRate), 2) + " + " +healPercent  * (60f / tickRate) + "% " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.range.localized() + ": [white]" + Strings.autoFixed(range / tilesize, 2) + " " + StatUnit.blocks.localized());
        t.row();

        float allySpeedIncrease = Mathf.pow(bulletSpeedBonus, 60f / tickRate) - 1f;
        float enemySpeedDecrease = 1f - Mathf.pow(bulletSlowdown, 60f / tickRate);

        t.add("[lightgray]Ally Bullet Speed: [white]+" + (int)(allySpeedIncrease * 100f) + "% " + StatUnit.blocks.localized());
        t.row();
        t.add("[lightgray]Enemy Bullet Slow: [white]-" + (int)(enemySpeedDecrease * 100f) + "% " + StatUnit.blocks.localized());
        t.row();
        t.add("[lightgray]Block Efficiency: [white]" + (int)(efficiencyBoost * 100f) + "%");
        t.row();
    }

    protected boolean hasNanobot(Unit unit){
        for(Ability ability : unit.abilities){
            if(ability instanceof NanobotAbility) return true;
        }
        return false;
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);

        if(NanobotLogic.getNanobots(unit) == null){
            NanobotLogic.initNanobots(unit, unit.x, unit.y, nanobotCount);
        }

        NanobotLogic.NanobotParams params = new NanobotLogic.NanobotParams();
        params.x = unit.x;
        params.y = unit.y;
        params.range = range;
        params.color = color;
        params.nanobotCount = nanobotCount;
        params.nanobotSize = nanobotSize;
        params.nanobotSpeed = nanobotSpeed;

        NanobotLogic.drawNanobots(unit, params);
    }

    @Override
    public void update(Unit unit){
        if(NanobotLogic.getNanobots(unit) == null){
            NanobotLogic.initNanobots(unit, unit.x, unit.y, nanobotCount);
        }

        NanobotLogic.cleanupInvalid();

        timer += Time.delta;
        soundTimer += Time.delta;

        if(soundTimer >= 30f){
            ambientSound.at(unit.x, unit.y, 1f, soundVolume);
            soundTimer = 0f;
        }

        NanobotLogic.NanobotParams params = new NanobotLogic.NanobotParams();
        params.x = unit.x;
        params.y = unit.y;
        params.damage = damage;
        params.healAmount = healAmount;
        params.healPercent = healPercent;
        params.range = range;
        params.tickRate = tickRate;
        params.buildingDamageMultiplier = buildingDamageMultiplier;
        params.bulletSpeedBonus = bulletSpeedBonus;
        params.bulletSlowdown = bulletSlowdown;
        params.efficiencyBoost = efficiencyBoost;
        params.stackPenalty = stackPenalty;
        params.status = status;
        params.statusDuration = statusDuration;
        params.color = color;
        params.nanobotCount = nanobotCount;
        params.nanobotSize = nanobotSize;
        params.nanobotSpeed = nanobotSpeed;
        params.team = unit.team;
        params.hasNanobot = hasNanobot(unit);
        NanobotLogic.updateBoost(unit,params);

        NanobotLogic.updateNanobots(unit, params, timer, useAmmo, () -> {
            if(useAmmo && state.rules.unitAmmo){
                unit.ammo--;
            }
            timer = 0f;
        });
    }

    @Override
    public void death(Unit unit){
        NanobotLogic.removeNanobots(unit);
    }
}