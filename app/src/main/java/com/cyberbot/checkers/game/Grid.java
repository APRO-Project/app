package com.cyberbot.checkers.game;

import java.util.ArrayList;

public class Grid {
    public int size;
    public ArrayList<GridEntry> gridEntries;

    public Grid(int size, int playerRows) {
        this.size = size;
        gridEntries = new ArrayList<>();

        for(int i = 0; i < size*size; ++i) {
            int y = i / size;
            GridEntry entry = new GridEntry(i % size, y);

            if(y < playerRows && entry.legal())
                entry.player = PlayerNum.FIRST;
            else if(y >= size - playerRows && entry.legal())
                entry.player = PlayerNum.SECOND;

            gridEntries.add(entry);
        }
    }

    public GridEntry getEntryByCoords(int x, int y) throws IndexOutOfBoundsException {
        if(x >= size || y >= size)
            throw new IndexOutOfBoundsException("Coordinates (" + x + ", " + y + ") out of bounds for grid with size " + size);

        for(GridEntry e: gridEntries) {
            if(e.x == x && e.y == y)
                return e;
        }

        throw new RuntimeException("Entry (" + x + ", " + y + ") not found in Grid");
    }

    public boolean moveAllowed(GridEntry src, GridEntry dst) {
        return src == dst || (dst.player == PlayerNum.NOPLAYER && dst.legal());
    }

    public boolean attemptMove(GridEntry src, GridEntry dst) {
        if(dst == src || !moveAllowed(src, dst))
            return false;

        int srcIdx = gridEntries.indexOf(src);
        int dstIdx = gridEntries.indexOf(dst);

        if(srcIdx == -1 || dstIdx == -1)
            throw new RuntimeException("GridEntry destination or source not part of the Grid");

        gridEntries.get(dstIdx).player = src.player;
        gridEntries.get(srcIdx).player = PlayerNum.NOPLAYER;

        return true;
    }
}