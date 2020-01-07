package com.cyberbot.checkers.game;

import java.util.ArrayList;

public class JGrid {
    int size;
    private int playerRows;
    private ArrayList<JGridEntry> gridEntries;

    JGridUpdateListener gridUpdateListener = null;

    public JGrid(int size, int playerRows) {
        this.size = size;
        this.playerRows = playerRows;
        gridEntries = new ArrayList<>();

        for(int i = 0; i < size*size; ++i) {
            int y = i / size;
            JGridEntry entry = new JGridEntry(i % size, i);

            if(y < playerRows && entry.legal())
                entry.player = JPlayerNum.FIRST;
            else if(y >= size - playerRows && entry.legal())
                entry.player = JPlayerNum.SECOND;

            gridEntries.add(entry);
        }
    }

    JGridEntry getEntryByCoords(int x, int y) throws IndexOutOfBoundsException {
        if(x >= size || y >= size)
            throw new IndexOutOfBoundsException("Coordinates (" + x + ", " + y + ") out of bounds for grid with size " + size);

        for(JGridEntry e: gridEntries) {
            if(e.x == x && e.y == y)
                return e;
        }

        throw new RuntimeException("Entry (" + x + ", " + y + ") not found in Grid");
    }

    boolean moveAllowed(JGridEntry src, JGridEntry dst) {
        return src == dst || (dst.player == JPlayerNum.NOPLAYER && dst.legal());
    }

    boolean attemptMove(JGridEntry src, JGridEntry dst) {
        if(dst == src || !moveAllowed(src, dst))
            return false;

        gridUpdateListener.move(this, src, dst);

        int srcIdx = gridEntries.indexOf(src);
        int dstIdx = gridEntries.indexOf(dst);

        if(srcIdx == -1 || dstIdx == -1)
            throw new RuntimeException("GridEntry destination or source not part of the Grid");

        gridEntries.get(dstIdx).player = src.player;
        gridEntries.get(srcIdx).player = JPlayerNum.NOPLAYER;

        return true;
    }
}

interface JGridUpdateListener {
    void move(JGrid grid, JGridEntry src, JGridEntry dst);
}