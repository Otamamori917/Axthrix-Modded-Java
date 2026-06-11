package axthrix.world.util;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import axthrix.world.types.perks.*;
import mindustry.ui.Styles;
import mindustry.world.meta.StatValues;

/**
 * Static helper that renders a perk-system stat table into a parent {@link Table}.
 * <p>
 * Call {@link #addPerkStats(Table, Seq)} from any {@code setStats()} override that
 * owns a {@code Seq<Perk>} — PerkTurretType or AxUnitType (for PerkWeapon instances).
 */
public class PerkStats {

    /**
     * Adds a labelled perk-system section to {@code parent}, one sub-table per perk.
     * Rows with default/zero values are omitted automatically.
     *
     * @param parent the stat value table passed by the engine
     * @param perks  the seq of perks to describe
     */
    public static void addPerkStats(Table parent, Seq<Perk> perks) {
        if (perks.isEmpty()) return;

        // Section header
        parent.add("[stat]" + Core.bundle.get("stat.aj-perk-system")).left().padTop(4f).colspan(2).row();

        for (Perk perk : perks) {
            parent.table(Styles.grayPanel, t -> {
                t.defaults().left().padLeft(4f).padTop(2f);

                // ---- Perk type name ----
                String typeName = perkTypeName(perk);
                t.add("[accent]" + typeName).row();

                // ---- Trigger mode ----
                String triggerKey = triggerLocKey(perk);
                t.add("[stat]" + Core.bundle.get("stat.aj-perk-trigger") + " [white]"
                        + Core.bundle.get(triggerKey)).row();

                // ---- Type-specific primary config ----
                addTypeSpecificRows(t, perk);

                // ---- Stack / hit config (not shown for continuous perks) ----
                boolean continuous = perk instanceof RangePerk || perk instanceof SpeedPerk;
                if (!continuous) {
                    if (perk.hitsPerStack > 1) {
                        t.add("[stat]" + Core.bundle.get("stat.aj-perk-hits-per-stack")
                                + " [white]" + perk.hitsPerStack).row();
                    }
                    if (perk.maxStacks > 1) {
                        t.add("[stat]" + Core.bundle.get("stat.aj-perk-max-stacks")
                                + " [white]" + perk.maxStacks).row();
                    }
                    if (perk.decaysOnMiss) {
                        t.add("[stat]" + Core.bundle.get("stat.aj-perk-decay-miss")).row();
                    }
                    if (perk.decaysOverTime) {
                        t.add("[stat]" + Core.bundle.get("stat.aj-perk-decay-time")
                                + " [white]" + Strings.fixed(perk.decayTime / 60f, 1)
                                + " " + Core.bundle.get("stat.aj-perk-seconds")).row();
                    }
                }

                // ---- Buff rows (only non-zero) ----
                addBuffRow(t, "stat.aj-perk-reload-buff",    perk.reloadBuff,    true,  "%");
                addBuffRow(t, "stat.aj-perk-damage-buff",    perk.damageBuff,    false, "%");
                addBuffRow(t, "stat.aj-perk-range-buff",     perk.rangeBuff,     false, " " + Core.bundle.get("stat.aj-perk-units"));
                addBuffRow(t, "stat.aj-perk-resist-buff",    perk.resistanceBuff,false, "");
                addBuffRow(t, "stat.aj-perk-speed-buff",     perk.speedBuff,     false, "%");

            }).left().padTop(3f).padLeft(2f).growX().row();
        }
    }

    // ---- Private helpers ----

    /** Appends type-specific rows (rows unique to one Perk subclass). */
    private static void addTypeSpecificRows(Table t, Perk perk) {
        if (perk instanceof BulletPerk bp) {
            // post-perk reload multiplier — only show if not 1.0
            if (bp.postPerkReloadMultiplier != 1f) {
                String dir = bp.postPerkReloadMultiplier < 1f
                        ? "[green]" : "[scarlet]";
                t.add("[stat]" + Core.bundle.get("stat.aj-perk-post-reload")
                        + " " + dir
                        + Strings.fixed(bp.postPerkReloadMultiplier * 100f, 0) + "%").row();
            }

        } else if (perk instanceof DurationPerk dp) {
            t.add("[stat]" + Core.bundle.get("stat.aj-perk-duration")
                    + " [white]" + Strings.fixed(dp.duration / 60f, 1)
                    + " " + Core.bundle.get("stat.aj-perk-seconds")).row();

        } else if (perk instanceof RangePerk rp) {
            t.add("[stat]" + Core.bundle.get("stat.aj-perk-req-distance")
                    + " [white]" + Strings.fixed(rp.requiredDistance / 8f, 1)
                    + " " + Core.bundle.get("stat.aj-perk-tiles")).row();
            if (rp.invertRange) {
                t.add("[stat]" + Core.bundle.get("stat.aj-perk-invert-range")).row();
            }

        } else if (perk instanceof SpeedPerk sp) {
            t.add("[stat]" + Core.bundle.get("stat.aj-perk-req-speed")
                    + " [white]" + Strings.fixed(sp.requiredSpeed, 2)
                    + " " + Core.bundle.get("stat.aj-perk-units-per-tick")).row();

        } else if (perk instanceof ShotBuffPerk sb) {
            String resetKey = sb.resetOnShot
                    ? "stat.aj-perk-reset-on-shot-yes"
                    : "stat.aj-perk-reset-on-shot-no";
            t.add("[stat]" + Core.bundle.get("stat.aj-perk-reset-on-shot-label")
                    + " [white]" + Core.bundle.get(resetKey)).row();

        }
    }

    /**
     * Adds a single buff row, omitting it when the value is zero.
     *
     * @param isReduction if true, the buff reduces a stat (reload) so the value is
     *                    displayed as a reduction percentage rather than a bonus.
     */
    private static void addBuffRow(Table t, String labelKey, float value, boolean isReduction, String suffix) {
        if (value <= 0f) return;
        String color = isReduction ? "[green]" : "[accent]";
        String sign  = isReduction ? "-" : "+";
        String display = isReduction
                ? Strings.fixed(value * 100f, 0)
                : Strings.fixed(value * 100f, 0);
        t.add("[stat]" + Core.bundle.get(labelKey) + " " + color + sign + display + suffix).row();
    }

    /** Human-readable perk type name derived from class. */
    private static String perkTypeName(Perk perk) {
        if (perk instanceof BulletPerk)   return Core.bundle.get("stat.aj-perk-type-bullet");
        if (perk instanceof DurationPerk) return Core.bundle.get("stat.aj-perk-type-duration");
        if (perk instanceof RangePerk)    return Core.bundle.get("stat.aj-perk-type-range");
        if (perk instanceof SpeedPerk)    return Core.bundle.get("stat.aj-perk-type-speed");
        if (perk instanceof ShotBuffPerk) return Core.bundle.get("stat.aj-perk-type-shotbuff");
        return perk.getClass().getSimpleName();
    }

    /** Returns the bundle key for a perk's trigger mode display string. */
    private static String triggerLocKey(Perk perk) {
        if (perk instanceof RangePerk || perk instanceof SpeedPerk) {
            return "stat.aj-perk-trigger-continuous";
        }
        return switch (perk.triggerMode) {
            case HITS         -> "stat.aj-perk-trigger-hits";
            case RECEIVE_HITS -> "stat.aj-perk-trigger-receive";
            case DISTANCE     -> "stat.aj-perk-trigger-distance";
        };
    }
}