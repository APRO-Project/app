package com.cyberbot.checkers.game;

import androidx.annotation.Nullable;

public class JGridEntry {
    int y;
    int x;
    JPlayerNum player = JPlayerNum.NOPLAYER;

    JGridEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    boolean legal() {
        return ((x % 2) ^ (y % 2)) > 0;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ") - " + player;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this.getClass() != obj.getClass())
            return false;

        return this.x == ((JGridEntry) obj).x && this.y == ((JGridEntry) obj).y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}

enum JPlayerNum {
    NOPLAYER,
    FIRST,
    SECOND
}