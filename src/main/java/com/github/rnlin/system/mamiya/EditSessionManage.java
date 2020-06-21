package com.github.rnlin.system.mamiya;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EditSessionManage {

    private WorldEditPlugin we;
    private int maxLimitblock = -1;
    // Index number to undo
    private Map<String, Integer> historyPointer = new HashMap<>();
    private Map<String, List<EditSession>> history = new HashMap<>();
//    private HistoryManage historyManage;


    public EditSessionManage(WorldEditPlugin we) {
        this.we = we;
//        historyManage = this.new HistoryManage();
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
        Integer pointer = historyPointer.get(pn);
        if (pointer <= -1)  return null;
        List<EditSession> array = history.get(pn);
        EditSession editSession = Objects.requireNonNull(array.get(pointer));
        historyPointer.put(pn, --pointer);
        return editSession;
    }

    public int getHistoryPointer(Player player) {
        String name = player.getName();
        return historyPointer.get(name);
    }

    public boolean isUndo(@NotNull String playerName) {
        if (-1 >= historyPointer.get(playerName) || !historyPointer.containsKey(playerName)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isRedo(@NotNull String playerName) {
        int historySize = history.get(playerName).size();
        if (historySize < 1) return false;
        if (historySize - 1 <= historyPointer.get(playerName)) {
            return false;
        } else {
            return true;
        }
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

        // historyPointer count up
        if (this.historyPointer.containsKey(playerName)) {
            Integer a = this.historyPointer.get(playerName);
            this.historyPointer.put(playerName, ++a);
        } else {
            this.historyPointer.put(playerName, 0);
        }

        // history
        if (this.history.containsKey(playerName)) {
            int cpointer = getHistoryPointer(player) - 1;
            int lastSessionPoint = this.history.get(playerName).size() - 1;
            if (cpointer < lastSessionPoint) {
                for (int i = cpointer; i < lastSessionPoint; i++) {
                    this.history.get(playerName).remove(cpointer + 1);
                }
            }
            this.history.get(playerName).add(editSession);
        } else {
            this.history.put(playerName, new ArrayList<EditSession>(Arrays.asList(editSession)));
        }
        return editSession;
    }

//    @Nullable
//    public EditSession getEditSession(@NotNull String playerName) {
//        List<EditSession> esList = historyManage.getEditSessions(playerName);
//        return esList.get(esList.size() - 1);
//    }
//
//    /**
//     * 指定したワールドのEditSessionオブジェクトを返します。
//     * このメソッドでEditSessionオブジェクト取得しても履歴は記録しません。
//     * @param playerName
//     * @param worldName
//     * @return EditSession
//     */
//    @Nullable
//    public EditSession getEditSessionOfWorld(@NotNull String playerName, @NotNull String worldName) {
//        List<EditSession> editSessionList = historyManage.getEditSessionsOfWorld(playerName, worldName);
//        if (editSessionList == null) {
//            return null;
//        }
//        EditSession editSession = editSessionList.get(editSessionList.size() - 1);
//        return editSession;
//    }
//
//    private class HistoryManage {
//
//        // Connects(Manage) the EditSession instance of each world of the player.
//        private final Map<String, List<EditSession>> editSessionMap = new HashMap<>();
//
//        // Save edit history for all world.
//        private final Map<String, List<String>> editHist = new HashMap<>();
//
//
//        private void addEditSessionToHistory(
//                @NotNull String playerName,
//                @NotNull EditSession editSession,
//                @NotNull com.sk89q.worldedit.world.World presentWorld
//        ) {
//            Objects.requireNonNull(playerName, "playerName is null");
//            Objects.requireNonNull(editSession, "editSession is null");
//            Objects.requireNonNull(presentWorld, "presentWorld is null");
//
//            // if there is no EditSession associated with the player, add EditSession.
//            if (!this.editSessionMap.containsKey(playerName)) {
//System.out.println("addEditSessionToHistory#if(!this...)");
//                List<EditSession> editSessionList = new ArrayList<>();
//                editSessionList.add(editSession);
//                this.editSessionMap.put(playerName, editSessionList);
//            } else {
//System.out.println("addEditSessionToHistory#else");
//                List<EditSession> editSessionList = this.editSessionMap.get(playerName);
//System.out.println("editSessionList.size()=" + editSessionList.size());
//                editSessionList.add(editSession);
//            }
//
//            // add edit history
//            if (this.editHist.containsKey(playerName)) {
//                List history = editHist.get(playerName);
//                history.add(presentWorld.getName());
//            } else {
//                List<String> history = new ArrayList<>();
//                history.add(presentWorld.getName());
//                editHist.put(playerName, history);
//            }
//        }
//
//        @Nullable
//        private List<EditSession> getEditSessionsOfWorld(@NotNull String playerName, @NotNull String worldName) {
//            List<EditSession> el = editSessionMap.get(playerName);
//            List<EditSession> editSessionList = new ArrayList<>();
//            for (EditSession i : el) {
//                if (worldName.equals(i.getWorld().getName())) {
//                    editSessionList.add(i);
//                }
//                return editSessionList;
//            }
//            return null;
//        }
//
//        @Nullable
//        private List<EditSession> getEditSessions(@NotNull String playerName) {
//            return editSessionMap.get(playerName);
//        }
//
//        @Nullable
//        private EditSession getNextEditSession(@NotNull String playerName) {
//            String worldName = getNextEditSessionWorldName(playerName);
//            int historyIndex = editHist.size();
//            if (worldName == null || historyIndex == 0) {
//                return null;
//            }
//            List<EditSession> el = editSessionMap.get(playerName);
//            EditSession i = el.get(historyIndex - 1);
//           if (worldName.equals(i.getWorld().getName())) {
//              return i;
//           } else {
//               Bukkit.getPlayer(playerName).sendMessage(ChatColor.YELLOW + "editHistとワールド名の不一致が起きました。");
//               return null;
//           }
//        }
//
//        @Nullable
//        private String getNextEditSessionWorldName(@NotNull String playerName) {
//            List<String> history = this.editHist.get(playerName);
//            if (history == null) {
//                return null;
//            }
//            int size = history.size();
//            if (size <= 0) {
//                return null;
//            }
//            String worldName = history.get(size - 1);
//            history.remove(size - 1);
//            return worldName;
//        }
//
//    }

}
