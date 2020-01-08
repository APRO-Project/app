package com.cyberbot.checkers.game;

import com.cyberbot.checkers.game.Grid;
import com.cyberbot.checkers.game.GridEntry;
import com.cyberbot.checkers.game.PlayerNum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    @Test
    void shouldHaveADefaultConstructor() {
        Grid grid = new Grid();
        assertEquals(8, grid.getSize());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPlayerRowsTooBig() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(12, 6));
    }

    @Test
    void shouldReturnSize() {
        Grid grid = new Grid(12, 0);
        assertEquals(12, grid.getSize());
    }

    @Test
    void shouldIterateProperly() {
        Grid grid = new Grid(3, 0);

        final int[] x = {0, 1, 2, 0, 1, 2, 0, 1, 2};
        final int[] y = {0, 0, 0, 1, 1, 1, 2, 2, 2};

        int i = 0;
        for (GridEntry entry : grid) {
            assertEquals(x[i], entry.getX());
            assertEquals(y[i++], entry.getY());
        }
    }

    @Test
    void shouldNotInitializePlayersWhenRowsSetToZero() {
        Grid grid = new Grid(10, 0);

        for (GridEntry entry : grid) {
            Assertions.assertEquals(PlayerNum.NOPLAYER, entry.getPlayer());
        }
    }

    @Test
    void shouldInitializePlayersInLegalEntries() {
        Grid grid = new Grid(8, 3);

        for (GridEntry entry : grid) {
            if (entry.getPlayer() != PlayerNum.NOPLAYER) {
                assertTrue(entry.legal());
            }
        }
    }

    @Test
    void shouldProperlyInitializePlayers() {
        Grid grid = new Grid(8, 3);

        final int[][] first = {
                {1, 0}, {3, 0}, {5, 0}, {7, 0},
                {0, 1}, {2, 1}, {4, 1}, {6, 1},
                {1, 2}, {3, 2}, {5, 2}, {7, 2}
        };

        final int[][] second = {
                {0, 5}, {2, 5}, {4, 5}, {6, 5},
                {1, 6}, {3, 6}, {5, 6}, {7, 6},
                {0, 7}, {2, 7}, {4, 7}, {6, 7}
        };

        int f = 0;
        int s = 0;

        for (GridEntry entry : grid) {
            if (entry.getPlayer() == PlayerNum.FIRST) {
                assertEquals(first[f][0], entry.getX());
                assertEquals(first[f++][1], entry.getY());
            } else if (entry.getPlayer() == PlayerNum.SECOND) {
                assertEquals(second[s][0], entry.getX());
                assertEquals(second[s++][1], entry.getY());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("invalidEntryIndexesProvider")
    void shouldThrowIndexOutOfBoundsExceptionWhenGettingInvalidEntries(int size, int x, int y) {
        Grid grid = new Grid(size, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> grid.getEntryByCoords(x, y));
    }

    @ParameterizedTest
    @MethodSource("validEntryIndexesProvider")
    void shouldReturnValidEntries(int size, int x, int y) {
        Grid grid = new Grid(size, 0);
        GridEntry entry = grid.getEntryByCoords(x, y);
        assertEquals(x, entry.getX());
        assertEquals(y, entry.getY());
    }

    @ParameterizedTest
    @MethodSource("occupiedEntryMovesProvider")
    void shouldDisallowMoveWhenEntryOccupied(
            int size, int playerRows, int srcX, int srcY, int dstX, int dstY) {
        Grid grid = new Grid(size, playerRows);
        GridEntry srcEntry = grid.getEntryByCoords(srcX, srcY);
        GridEntry dstEntry = grid.getEntryByCoords(dstX, dstY);

        assertFalse(grid.moveAllowed(srcEntry, dstEntry));
    }

    @ParameterizedTest
    @MethodSource("illegalEntryMovesProvider")
    void shouldDisallowMoveWhenEntryIllegal(
            int size, int playerRows, int srcX, int srcY, int dstX, int dstY) {
        Grid grid = new Grid(size, playerRows);
        GridEntry srcEntry = grid.getEntryByCoords(srcX, srcY);
        GridEntry dstEntry = grid.getEntryByCoords(dstX, dstY);

        assertFalse(grid.moveAllowed(srcEntry, dstEntry));
    }

    @ParameterizedTest
    @MethodSource("validMovesProvider")
    void shouldAllowMoveToValidEntry(
            int size, int playerRows, int srcX, int srcY, int dstX, int dstY) {
        Grid grid = new Grid(size, playerRows);
        GridEntry srcEntry = grid.getEntryByCoords(srcX, srcY);
        GridEntry dstEntry = grid.getEntryByCoords(dstX, dstY);

        assertTrue(grid.moveAllowed(srcEntry, dstEntry));
    }

    /**
     * @return Stream of Arguments (int size, int x, int y)
     */
    private static Stream<Arguments> invalidEntryIndexesProvider() {
        return Stream.of(
                Arguments.of(10, 10, 0),
                Arguments.of(10, 100, 0),
                Arguments.of(10, 0, 10),
                Arguments.of(10, 0, 100),
                Arguments.of(30, 0, 30),
                Arguments.of(30, 30, 0),
                Arguments.of(2, 30, 0),
                Arguments.of(2, 2, 1)
        );
    }

    /**
     * @return Stream of Arguments (int size, int x, int y)
     */
    private static Stream<Arguments> validEntryIndexesProvider() {
        return Stream.of(
                Arguments.of(11, 10, 0),
                Arguments.of(120, 100, 0),
                Arguments.of(13, 0, 10),
                Arguments.of(300, 0, 100),
                Arguments.of(34, 0, 30),
                Arguments.of(38, 30, 0),
                Arguments.of(233, 30, 0),
                Arguments.of(5, 2, 1)
        );
    }


    /**
     * @return Stream of Arguments (int size, int playerRows, int srcX, int srcY, int dstX, int dstY)
     */
    private static Stream<Arguments> occupiedEntryMovesProvider() {
        return Stream.of(
                Arguments.of(8, 3, 2, 1, 4, 5),
                Arguments.of(8, 2, 0, 5, 5, 6),
                Arguments.of(8, 3, 2, 3, 6, 1),
                Arguments.of(10, 4, 2, 3, 3, 2),
                Arguments.of(10, 4, 7, 2, 8, 7)
        );
    }

    /**
     * @return Stream of Arguments (int size, int playerRows, int srcX, int srcY, int dstX, int dstY)
     */
    private static Stream<Arguments> illegalEntryMovesProvider() {
        return Stream.of(
                Arguments.of(8, 3, 2, 1, 4, 6),
                Arguments.of(8, 3, 0, 5, 6, 2),
                Arguments.of(8, 3, 2, 3, 6, 4),
                Arguments.of(10, 4, 2, 3, 3, 5),
                Arguments.of(10, 4, 7, 2, 7, 5)
        );
    }

    /**
     * @return Stream of Arguments (int size, int playerRows, int srcX, int srcY, int dstX, int dstY)
     */
    private static Stream<Arguments> validMovesProvider() {
        return Stream.of(
                Arguments.of(8, 3, 2, 1, 5, 4),
                Arguments.of(8, 3, 1, 0, 4, 3),
                Arguments.of(10, 4, 2, 3, 6, 5),
                Arguments.of(10, 4, 4, 3, 7, 4)
        );
    }
}
