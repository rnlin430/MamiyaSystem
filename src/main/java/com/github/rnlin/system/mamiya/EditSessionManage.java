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

import java.util.*;

public class EditSessionManage {

    private WorldEditPlugin we;
    private int maxLimitblock = -1;
    private HistoryManage historyManage;


    public EditSessionManage(WorldEditPlugin we) {
        this.we = we;
        historyManage = this.new HistoryManage();
    }

    @Nullable
    public EditSession getEditSession(@NotNull Player player) {
        if (!player.isOnline()) return null;
        String pn = player.getName();
        if (historyManage.getNextEditSession(pn) == null) {
            creatEditSession(pn);
        }
        return historyManage.getNextEditSession(pn);
    }

    private void creatEditSession(String playerName) {
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
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(presentWorld, maxLimitblock);
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
        historyManage.addEditSessionToHistory(playerName, editSession, presentWorld);

    }

    private class HistoryManage {

        // Connects(Manage) the EditSession instance of each world of the player.
        private Map<String, List<EditSession>> editSessionMap = new HashMap<>();

        // Save edit history for all world.
        private Map<String, List<String>> editHist = new HashMap<>();

        private void addEditSessionToHistory(
                @NotNull String playerName,
                @NotNull EditSession editSession,
                @NotNull com.sk89q.worldedit.world.World presentWorld
        ) {
            Objects.requireNonNull(playerName, "playerName is null");
            Objects.requireNonNull(editSession, "editSession is null");
            Objects.requireNonNull(presentWorld, "presentWorld is null");

            // if there is no EditSession associated with the player, add EditSession.
            if (this.editSessionMap.containsKey(playerName)) {
                List<EditSession> editSessionList = new ArrayList<>();
                editSessionList.add(editSession);
                this.editSessionMap.put(playerName, editSessionList);
            } else {
                List<EditSession> editSessionList = this.editSessionMap.get(playerName);
                if (!editSessionList.contains(editSession)) {
                    editSessionList.add(editSession);
                }
            }

            // add edit history
            if (this.editHist.containsKey(playerName)) {
                List history = editHist.get(playerName);
                history.add(presentWorld.getName());
            } else {
                List<String> history = new ArrayList<>();
                history.add(presentWorld.getName());
                editHist.put(playerName, history);
            }
        }

        @Nullable
        private EditSession getNextEditSession(@NotNull String playerName) {
            String worldname = getNextEditSessionWorldName(playerName);
            if (worldname == null) {
                return null;
            }
            List<EditSession> el = editSessionMap.get(playerName);
            for (EditSession i : el) {
               if (worldname.equals(i.getWorld().getName())) {
                  return i;
                }
            }
            return null;
        }

        @Nullable
        private String getNextEditSessionWorldName(@NotNull String playerName) {
            List<String> history = this.editHist.get(playerName);
            int size = history.size();
            if (size <= 0) {
                return null;
            }
            String worldname = history.get(size - 1);
            history.remove(size - 1);
            return worldname;
        }
            
    }

}
