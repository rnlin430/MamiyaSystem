package com.github.rnlin.system.mamiya;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.spec.ECField;
import java.util.*;

public class EditSessionManage {

    private WorldEditPlugin we;
    private int maxLimitblock = -1;
    private HistoryManage historyManage;


    public EditSessionManage(WorldEditPlugin we) {
        this.we = we;
        historyManage = this.new HistoryManage();
    }

    /**
     * 履歴に基づいて過去のEditSessionオブジェクトを返します。
     * 通常はundo処理時に使用するEditSessionを得たいときに使います。
     * @param player
     * @return EditSession Returns {@code null} if
     */
    @Nullable
    public EditSession getHistEditSession(@NotNull Player player) {
        if (!player.isOnline()) return null;
        String pn = player.getName();
        EditSession editSession = historyManage.getNextEditSession(pn);
        if (editSession == null) {
            getEditSessionAddHistory(pn);
        }
System.out.println("getHistEditSession editSession=" + editSession);
        return editSession;
    }

    /**
     * プレイヤーがいるワールドのEditSessionオブジェクトを返します。
     * EditSession取得を履歴に記録します。
     * 通常は履歴に残したいpaste処理に使用するEditSessionオブジェクトを得たいときに使用します。
     * @param playerName プレイヤーID
     * @return EditSession EditSessionオブジェクトを返します。
     */
    @NotNull
    public EditSession getEditSessionAddHistory(@NotNull String playerName) {
        Player player = Objects.requireNonNull(Bukkit.getPlayer(playerName),
                playerName + "is offline.\nFailed to create EditSession.");

//        LocalSession session = we.getSession(player);
//        com.sk89q.worldedit.world.World presentWorld = session.getSelectionWorld();
//System.out.println("getEditSessionAddHistory presentWorld#Name=" + presentWorld.getName());
        com.sk89q.worldedit.world.World presentWorld = BukkitAdapter.adapt(player.getWorld());
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
        EditSession es = Objects.requireNonNull(getEditSession(playerName, presentWorld.getName()));
        return es;
    }

    /**
     * 指定したワールドのEditSessionオブジェクトを返します。
     * このメソッドでEditSessionオブジェクト取得しても履歴は記録しません。
     * @param playerName
     * @param worldName
     * @return EditSession
     */
    @Nullable
    public EditSession getEditSession(@NotNull String playerName, @NotNull String worldName) {
        EditSession editSession = historyManage.getEditSession(playerName, worldName);
        if (editSession == null) {
            return null;
        }
        return editSession;
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
            if (!this.editSessionMap.containsKey(playerName)) {
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
        private EditSession getEditSession(@NotNull String playerName, @NotNull String worldName) {
            List<EditSession> el = editSessionMap.get(playerName);
            for (EditSession i : el) {
                if (worldName.equals(i.getWorld().getName())) {
                    return i;
                }
            }
            return null;
        }

        @Nullable
        private EditSession getNextEditSession(@NotNull String playerName) {
            String worldName = getNextEditSessionWorldName(playerName);
            if (worldName == null) {
                return null;
            }
            List<EditSession> el = editSessionMap.get(playerName);
            for (EditSession i : el) {
               if (worldName.equals(i.getWorld().getName())) {
                  return i;
                }
            }
            return null;
        }

        @Nullable
        private String getNextEditSessionWorldName(@NotNull String playerName) {
            List<String> history = this.editHist.get(playerName);
            if (history == null) {
                return null;
            }
            int size = history.size();
            if (size <= 0) {
                return null;
            }
            String worldName = history.get(size - 1);
            history.remove(size - 1);
            return worldName;
        }
            
    }

}
