package axthrix.world.types.abilities.heatbased;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class HeatPipeNetworkAbility extends PassiveHeatAbility {
    public float networkRadius = 120f;
    public float damagePerSecond = 200f;
    public float temperaturePerSecond = 3f;
    public int maxTargets = 6;
    public float pipeSpeed = 40f;
    public float selfHeatMultiplier = 0.01f;

    public float targetHeatDamageBonus = 0.02f;

    public boolean damageBuildings = true;
    public float buildingDamageMultiplier = 0.6f;

    // Chain visuals
    public float chainWidthScale = 1f;
    public float chainHeightScale = 1f;
    public Color chainColor = Color.orange;
    public Color flowColor = Color.red;
    public float flowSpeed = 2f; // How fast the color wave moves

    private static class PipeConnection {
        public Vec2 target = new Vec2();
        public float progress = 0f;
        public boolean isUnit = true;
        public int entityId;
    }

    private ObjectMap<Integer, Seq<PipeConnection>> connections = new ObjectMap<>();
    private static Vec2 tv1 = new Vec2();

    public HeatPipeNetworkAbility(){
        singleTickHeatLoss = 0;
        bytickHeatLoss = 2;

        idleHeatGain = 6;
    }

    @Override
    public void update(Unit unit){
        super.update(unit);

        if(!connections.containsKey(unit.id)){
            connections.put(unit.id, new Seq<>());
        }

        Seq<PipeConnection> pipes = connections.get(unit.id);

        pipes.removeAll(pipe -> {
            if(pipe.isUnit){
                Unit target = mindustry.gen.Groups.unit.getByID(pipe.entityId);
                return target == null || !target.isValid() || !unit.within(target, networkRadius);
            } else {
                Building target = mindustry.Vars.world.build(pipe.entityId);
                return target == null || !unit.within(target, networkRadius);
            }
        });

        if(pipes.size < maxTargets){
            Seq<Unit> unitTargets = new Seq<>();
            Seq<Building> buildingTargets = new Seq<>();

            Units.nearbyEnemies(unit.team, unit.x, unit.y, networkRadius, unitTargets::add);

            if(damageBuildings){
                Units.nearbyBuildings(unit.x, unit.y, networkRadius, building -> {
                    if(building.team != unit.team && building.block.destructible){
                        buildingTargets.add(building);
                    }
                });
            }

            for(Unit target : unitTargets){
                if(pipes.size >= maxTargets) break;

                boolean alreadyTargeted = pipes.contains(p -> p.isUnit && p.entityId == target.id);
                if(!alreadyTargeted){
                    PipeConnection newPipe = new PipeConnection();
                    newPipe.target.set(target.x, target.y);
                    newPipe.isUnit = true;
                    newPipe.entityId = target.id;
                    pipes.add(newPipe);
                }
            }

            for(Building target : buildingTargets){
                if(pipes.size >= maxTargets) break;

                boolean alreadyTargeted = pipes.contains(p -> !p.isUnit && p.entityId == target.pos());
                if(!alreadyTargeted){
                    PipeConnection newPipe = new PipeConnection();
                    newPipe.target.set(target.x, target.y);
                    newPipe.isUnit = false;
                    newPipe.entityId = target.pos();
                    pipes.add(newPipe);
                }
            }
        }

        boolean dealingDamage = false;
        float scaledDamagePerSecond = getScaledDamage(unit, damagePerSecond, selfHeatMultiplier);

        for(PipeConnection pipe : pipes){
            if(pipe.isUnit){
                Unit target = mindustry.gen.Groups.unit.getByID(pipe.entityId);
                if(target != null && target.isValid()){
                    pipe.target.set(target.x, target.y);
                    pipe.progress = Math.min(1f, pipe.progress + (pipeSpeed / networkRadius) * Time.delta / 60f);

                    if(pipe.progress >= 1f){
                        dealingDamage = true;

                        float targetHeat = TemperatureLogic.getHeatUnit(target);
                        float targetEffectResist = TemperatureLogic.getEffectResistanceHeatUnit(target);

                        float finalDamage = applyTargetHeatBonus(targetHeat, targetEffectResist, scaledDamagePerSecond, targetHeatDamageBonus);

                        target.damage(finalDamage * Time.delta / 60f);
                        TemperatureLogic.applyTemperatureUnit(target, temperaturePerSecond * Time.delta / 60f);
                    }
                }
            } else {
                Building target = mindustry.Vars.world.build(pipe.entityId);
                if(target != null){
                    pipe.target.set(target.x, target.y);
                    pipe.progress = Math.min(1f, pipe.progress + (pipeSpeed / networkRadius) * Time.delta / 60f);

                    if(pipe.progress >= 1f){
                        dealingDamage = true;

                        float buildingHeat = TemperatureLogic.getHeatBuilding(target);
                        float buildingEffectResist = TemperatureLogic.getEffectResistanceHeatBuilding(target);

                        float finalDamage = applyTargetHeatBonus(buildingHeat, buildingEffectResist, scaledDamagePerSecond, targetHeatDamageBonus);

                        target.damage(finalDamage * buildingDamageMultiplier * Time.delta / 60f);
                        TemperatureLogic.applyTemperatureBuilding(target, temperaturePerSecond * Time.delta / 60f);
                    }
                }
            }
        }

        isDealingDamage = dealingDamage;
    }

    @Override
    public void draw(Unit unit){
        if(!connections.containsKey(unit.id)) return;

        Draw.z(29); // Under units

        Seq<PipeConnection> pipes = connections.get(unit.id);

        // Get chain texture (fallback to square if not available)
        TextureRegion chainRegion = Core.atlas.find("aj-chain");
        boolean hasChainTexture = chainRegion.found();

        for(PipeConnection pipe : pipes){
            // Calculate current end position based on progress
            Tmp.v1.set(pipe.target).sub(unit.x, unit.y).scl(pipe.progress).add(unit.x, unit.y);

            if(hasChainTexture){
                // Draw chain with animated color
                drawAnimatedChain(chainRegion, unit.x, unit.y, Tmp.v1.x, Tmp.v1.y, pipe.progress);
            } else {
                // Fallback: draw tiled squares
                drawFallbackChain(unit.x, unit.y, Tmp.v1.x, Tmp.v1.y, pipe.progress);
            }
        }

        Draw.reset();
    }

    private void drawAnimatedChain(TextureRegion region, float x, float y, float endx, float endy, float connectionProgress){
        float angleToEnd = Mathf.angle(endx - x, endy - y);
        float distance = Mathf.dst(x, y, endx, endy);

        // Chain dimensions in world units
        float height = region.height / 4f * chainHeightScale;
        float width = region.width / 4f * chainWidthScale;

        // Number of full chain links
        int numLinks = (int)Math.floor(distance / height);
        float remainder = ((distance * 4) % (region.height * chainHeightScale)) / 8;

        // Flow animation progress (0-1 cycling)
        float flowProgress = (Time.time * flowSpeed % 60f) / 60f;

        // Draw full links
        for(int i = 0; i < numLinks; i++){
            tv1.trns(angleToEnd, distance - i * height - region.height / 8f * chainHeightScale).add(x, y);

            // Calculate link color based on flow
            float linkProgress = (float)i / Math.max(1, numLinks);
            float colorPhase = (linkProgress + flowProgress) % 1f;

            // Only show flow color if chain is fully connected
            if(connectionProgress >= 1f){
                Draw.color(chainColor, flowColor, Mathf.curve(colorPhase, 0f, 0.3f));
            } else {
                Draw.color(chainColor);
            }

            Draw.alpha(0.6f);
            Draw.rect(region, tv1.x, tv1.y, width, height, angleToEnd - 90);
        }

        // Draw remainder link
        if(remainder > 0.5f){
            tv1.trns(angleToEnd, remainder);

            if(connectionProgress >= 1f){
                Draw.color(chainColor, flowColor, flowProgress);
            } else {
                Draw.color(chainColor);
            }

            Draw.alpha(0.6f);
            Draw.rect(region, x + tv1.x, y + tv1.y, width, remainder * 2, angleToEnd - 90);
        }
    }

    private void drawFallbackChain(float x, float y, float endx, float endy, float connectionProgress){
        float dst = Mathf.dst(x, y, endx, endy);
        int segments = Math.max(1, (int)(dst / 8f));

        float flowProgress = (Time.time * flowSpeed % 60f) / 60f;

        for(int i = 0; i < segments; i++){
            float segProgress = (float)i / segments;
            Tmp.v2.set(endx, endy).sub(x, y).scl(segProgress).add(x, y);

            // Calculate color for this segment
            float colorPhase = (segProgress + flowProgress) % 1f;

            if(connectionProgress >= 1f){
                Draw.color(chainColor, flowColor, Mathf.curve(colorPhase, 0f, 0.3f));
            } else {
                Draw.color(chainColor);
            }

            Draw.alpha(0.6f);
            Lines.stroke(2.5f);
            Lines.square(Tmp.v2.x, Tmp.v2.y, 4f, 45f);
        }
    }

    @Override
    public String localized(){
        return "Heat Chains";
    }
}