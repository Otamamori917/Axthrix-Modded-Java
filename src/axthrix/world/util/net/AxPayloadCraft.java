package axthrix.world.util.net;

import arc.util.Log;
import axthrix.world.types.abilities.PayloadCrafterAbility;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

import static mindustry.Vars.*;

public class AxPayloadCraft {
    public static void register() {
        net.handleServer(PayloadCraftPacket.class, (con, packet) -> {
            Unit unit = Groups.unit.getByID(packet.unitId);
            if (unit == null) return;
            if (con.player != null && con.player.unit() != unit) return;

            UnlockableContent content = packet.contentId < 0 ? null : contentById(packet.contentId);
            for (var a : unit.abilities) {
                if (a instanceof PayloadCrafterAbility pca) {
                    pca.applySelectedFromServer(unit, content);
                    break;
                }
            }
        });
    }

    public static void setSelected(int unitId, int contentId) {
        PayloadCraftPacket p = new PayloadCraftPacket();
        p.unitId = unitId;
        p.contentId = contentId;
        if (net.server()) {
            Unit unit = Groups.unit.getByID(unitId);
            if (unit == null) return;
            UnlockableContent content = contentId < 0 ? null : contentById(contentId);
            for (var a : unit.abilities) {
                if (a instanceof PayloadCrafterAbility pca) {
                    pca.applySelectedFromServer(unit, content);
                    break;
                }
            }
        } else {
            net.send(p, true);
        }
    }

    private static UnlockableContent contentById(int id) {
        var b = content.block(id);
        if (b != null) return b;
        return content.unit(id);
    }

    public static class PayloadCraftPacket extends Packet {
        public int unitId;
        public int contentId;

        @Override
        public void write(arc.util.io.Writes write) {
            write.i(unitId);
            write.i(contentId);
        }

        @Override
        public void read(arc.util.io.Reads read) {
            unitId = read.i();
            contentId = read.i();
        }
    }
}