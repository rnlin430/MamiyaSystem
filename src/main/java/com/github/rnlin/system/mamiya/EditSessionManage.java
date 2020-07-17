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
        int pointer = historyPointer.get(pn);
        if (pointer <= -1)  return null;
        List<EditSession> array = history.get(pn);
        EditSession editSession = Objects.requireNonNull(array.get(pointer));
        historyPointer.put(pn, --pointer);
        return editSession;
    }

    /**
     * 履歴に基づいて最後にundo()を実行したEditSessionオブジェクトを返します。
     * 通常はredo処理時に使用するEditSessionを得たいときに使います。
     * @param player
     * @return EditSession Returns {@code null} if
     */
    @Nullable
    public EditSession getLastEditSessionUndone(@NotNull Player player) {
        if (!player.isOnline()) return null;
        String pn = player.getName();
        int pointer = historyPointer.get(pn);
//        int lastSessionPoint = history.get(pn).size() - 1;
        if (!isRedo(pn)) return null;
        pointer++;
        historyPointer.put(pn, pointer);
        return history.get(pn).get(pointer);
    }

    public int getHistoryPointer(Player player) {
        String name = player.getName();
        return historyPointer.get(name);
    }

    public boolean isUndo(@NotNull String playerName) {
        if (historyPointer == null) {
            return false;
        }
        if (-1 >= historyPointer.get(playerName) || !historyPointer.containsKey(playerName)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isRedo(@NotNull String playerName) {
        if (history.get(playerName) == null) {
            return false;
        }
        int historySize = history.get(playerName).size();
        if (historySize < 1) return false;
        if (historyPointer == null) {
            return false;
        }
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

        // Regist editSession history
        if (this.history.containsKey(playerName)) {
            Integer cpointer = getHistoryPointer(player);
            List<EditSession> playerEditsessionHist = this.history.get(playerName);

            for (;playerEditsessionHist.size() >= MamiyaSystemPlugin.historyLimit;) {
                playerEditsessionHist.remove(0);
                historyPointer.put(playerName,  --cpointer);
            }

//-------------------------------|
// 29  30  31  //index
// +   +   +   //editSessionHist
// ^           //cpointer
//-------------------------------|
            int lastSessionPoint = playerEditsessionHist.size() - 1;
            if (cpointer < lastSessionPoint) {
                for (int i = cpointer; i < lastSessionPoint; i++) {
                    playerEditsessionHist.remove(cpointer + 1);
                }
            }

            playerEditsessionHist.add(editSession);

        } else {
            this.history.put(playerName, new ArrayList<>(Arrays.asList(editSession)));
        }

        // historyPointer count up
        if (this.historyPointer.containsKey(playerName)) {
            Integer a = this.historyPointer.get(playerName);
            if (a >= MamiyaSystemPlugin.historyLimit) {

            } else {
                this.historyPointer.put(playerName, ++a);
            }
        } else {
            this.historyPointer.put(playerName, 0);
        }
        return editSession;
    }
}
