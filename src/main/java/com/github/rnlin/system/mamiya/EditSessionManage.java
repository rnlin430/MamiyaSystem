package com.github.rnlin.system.mamiya;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditSessionManage {

    private Map<String, EditSession> editSessionList = new HashMap<>();
    private WorldEditPlugin we;
    private String originWorldName = "origin";

    public EditSessionManage(WorldEditPlugin we) {
        this.we = we;
    }

    @Nullable
    public EditSession getEditSession(@NotNull Player player) {
        if (!player.isOnline()) return null;
        String pn = player.getName();
        if (!this.editSessionList.containsKey(pn)) {
            creatEditSession(pn);
        }
        return this.editSessionList.get(pn);
    }

    private void creatEditSession(String playerName) {
        if (editSessionList.containsKey(playerName)) {
            try {
                throw new Exception("This editSession already exists.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Player player = Objects.requireNonNull(Bukkit.getPlayer(playerName),
                playerName + "is offline.\nFailed to create EditSession.");

        LocalSession session = we.getSession(player);
        com.sk89q.worldedit.world.World presentWorld = session.getSelectionWorld();
//        RegionSelector rs = session.getRegionSelector(presentWorld);
//
//        World originWorld = Objects.requireNonNull(Bukkit.getWorld(this.originWorldName),
//                this.originWorldName + " is not found.");
//        session.setRegionSelector(BukkitAdapter.adapt(originWorld), rs);
//
//        Region region = session.getRegionSelector(BukkitAdapter.adapt(originWorld)).getIncompleteRegion();
//
//        EditSession editSession = WorldEdit
//                .getInstance()
//                .getEditSessionFactory()
//                .getEditSession(BukkitAdapter.adapt(originWorld), -1);
//
//        CuboidRegion region2 = new CuboidRegion(BukkitAdapter.adapt(originWorld), region.getMinimumPoint(), region.getMaximumPoint());
//        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
//
//        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
//                editSession, region2, clipboard, region2.getMinimumPoint()
//        );
//        // configure here
//        try {
//            Operations.complete(forwardExtentCopy);
//            forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(player)::print);
//        } catch (WorldEditException ex) {
//            ex.printStackTrace();
//        }
//
        editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(presentWorld, -1);
//        Operation operation = new ClipboardHolder(clipboard)
//                .createPaste(editSession)
//                .to(region2.getMinimumPoint())
//                .copyEntities(true)
//                // configure here
//                .build();
//
//        try {
//            Operations.complete(operation);
//        } catch (WorldEditException e) {
//            e.printStackTrace();
//        }
//        editSession.close();
        this.editSessionList.put(playerName, )

    }
}
