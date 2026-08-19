package axthrix.world.util.ui;

import arc.Core;
import arc.func.Floatc;
import arc.func.Floatp;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.*;

public class MiniUnitScaleDebug {
    private static BaseDialog dialog;
    private static Table previewsTable;

    public static void open() {
        if (dialog != null) {
            dialog.show();
            rebuildPreviews();
            return;
        }

        dialog = new BaseDialog(Core.bundle.get("menu.mini-scaler-page-header"));
        dialog.addCloseButton();

        dialog.cont.table(root -> {

            root.table(left -> {
                left.top();
                left.add(Core.bundle.get("menu.mini-scaler-page-preview")).padBottom(6f).row();

                previewsTable = new Table();
                left.pane(previewsTable).grow().maxWidth(720f);

                left.row();
                left.table(btns -> {
                    btns.defaults().pad(4f).height(40f);
                    btns.button(Core.bundle.get("menu.mini-scaler-page-copy"), () -> {
                        Core.app.setClipboardText(exportText());
                        ui.showInfoToast(Core.bundle.get("menu.mini-scaler-page-copy-toast"), 2f);
                    }).width(140f).tooltip(Core.bundle.get("menu.mini-scaler-page-copy-tooltip"));
                    btns.button(Core.bundle.get("menu.mini-scaler-page-refresh"), MiniUnitScaleDebug::rebuildPreviews).width(160f).tooltip(Core.bundle.get("menu.mini-scaler-page-refresh-tooltip"));
                }).padTop(8f);
            }).grow().pad(8f);

            root.table(right -> {
                right.top().left();
                right.pane(p -> {
                    p.defaults().left().pad(3f);
                    slider(p, "COMPACT_SCALE_MODIFIER", 0.5f, 2.5f, 0.05f,
                            () -> MiniUnitDisplay.COMPACT_SCALE_MODIFIER, v -> MiniUnitDisplay.COMPACT_SCALE_MODIFIER = v);
                    slider(p, "BASE_SCALE", 0.5f, 4f, 0.05f,
                            () -> MiniUnitDisplay.BASE_SCALE, v -> MiniUnitDisplay.BASE_SCALE = v);
                    slider(p, "TARGET_BOX", 20f, 120f, 1f,
                            () -> MiniUnitDisplay.TARGET_BOX, v -> MiniUnitDisplay.TARGET_BOX = v);
                    slider(p, "TARGET_HIT", 4f, 40f, 0.5f,
                            () -> MiniUnitDisplay.TARGET_HIT, v -> MiniUnitDisplay.TARGET_HIT = v);
                    slider(p, "SIZE_LERP", 0f, 1f, 0.05f,
                            () -> MiniUnitDisplay.SIZE_LERP, v -> MiniUnitDisplay.SIZE_LERP = v);
                    slider(p, "MIN_SIZE_FACTOR", 0.5f, 2f, 0.05f,
                            () -> MiniUnitDisplay.MIN_SIZE_FACTOR, v -> MiniUnitDisplay.MIN_SIZE_FACTOR = v);
                    slider(p, "MAX_SIZE_FACTOR", 0.5f, 2.5f, 0.05f,
                            () -> MiniUnitDisplay.MAX_SIZE_FACTOR, v -> MiniUnitDisplay.MAX_SIZE_FACTOR = v);
                    slider(p, "PLATE_SCALE_RATIO", 0.5f, 3f, 0.05f,
                            () -> MiniUnitDisplay.PLATE_SCALE_RATIO, v -> MiniUnitDisplay.PLATE_SCALE_RATIO = v);
                    slider(p, "DRONE_SCALE_RATIO", 0.5f, 3f, 0.05f,
                            () -> MiniUnitDisplay.DRONE_SCALE_RATIO, v -> MiniUnitDisplay.DRONE_SCALE_RATIO = v);
                    slider(p, "PLATE_SILHOUETTE_SCALE_RATIO", 0.5f, 3f, 0.05f,
                            () -> MiniUnitDisplay.PLATE_SILHOUETTE_SCALE_RATIO, v -> MiniUnitDisplay.PLATE_SILHOUETTE_SCALE_RATIO = v);
                    slider(p, "DRONE_SILHOUETTE_SCALE_RATIO", 0.5f, 3f, 0.05f,
                            () -> MiniUnitDisplay.DRONE_SILHOUETTE_SCALE_RATIO, v -> MiniUnitDisplay.DRONE_SILHOUETTE_SCALE_RATIO = v);
                }).growY().width(420f);
            }).growY().pad(8f);
        }).grow();

        dialog.show();
        rebuildPreviews();
    }

    private static void rebuildPreviews() {
        if (previewsTable == null) return;
        previewsTable.clearChildren();

        Seq<Unit> list = new Seq<>();
        Groups.unit.each(u -> {
            if (player == null || u.team != player.team()) return;
            if (new MiniUnitDisplay(u).hasContent()) list.add(u);
        });

        if (list.isEmpty()) {
            previewsTable.add(Core.bundle.get("menu.mini-scaler-page-empty")).pad(8f);
            return;
        }

        int col = 0;
        for (Unit u : list) {
            previewsTable.table(cell -> {
                cell.background(Tex.pane);
                cell.defaults().center();
                cell.add(u.type != null ? u.type.localizedName : "?").padBottom(6f).row();
                cell.add(new MiniUnitDisplay(u)).size(210f);
            }).size(210f, 210f).pad(20f);

            if (++col % 3 == 0) previewsTable.row();
        }
    }

    private static String exportText() {
        return ""
                + "COMPACT_SCALE_MODIFIER = " + f(MiniUnitDisplay.COMPACT_SCALE_MODIFIER) + "f;\n"
                + "PLATE_SILHOUETTE_SCALE_RATIO = " + f(MiniUnitDisplay.PLATE_SILHOUETTE_SCALE_RATIO) + "f;\n"
                + "DRONE_SILHOUETTE_SCALE_RATIO = " + f(MiniUnitDisplay.DRONE_SILHOUETTE_SCALE_RATIO) + "f;\n"
                + "PLATE_SCALE_RATIO = " + f(MiniUnitDisplay.PLATE_SCALE_RATIO) + "f;\n"
                + "DRONE_SCALE_RATIO = " + f(MiniUnitDisplay.DRONE_SCALE_RATIO) + "f;\n"
                + "TARGET_HIT = " + f(MiniUnitDisplay.TARGET_HIT) + "f;\n"
                + "MIN_SIZE_FACTOR = " + f(MiniUnitDisplay.MIN_SIZE_FACTOR) + "f;\n"
                + "MAX_SIZE_FACTOR = " + f(MiniUnitDisplay.MAX_SIZE_FACTOR) + "f;\n"
                + "TARGET_BOX = " + f(MiniUnitDisplay.TARGET_BOX) + "f;\n"
                + "BASE_SCALE = " + f(MiniUnitDisplay.BASE_SCALE) + "f;\n"
                + "SIZE_LERP = " + f(MiniUnitDisplay.SIZE_LERP) + "f;\n";
    }

    private static String f(float v) {
        return Strings.autoFixed(v, 3);
    }

    private static void slider(Table p, String name, float min, float max, float step,
                               Floatp getter, Floatc setter) {
        p.table(t -> {
            t.add(name).width(210f).left();
            t.slider(min, max, step, getter.get(), setter).growX().padLeft(6f).padRight(6f);
            t.label(() -> Strings.autoFixed(getter.get(), 2)).width(48f).right();
        }).growX().row();
    }
}