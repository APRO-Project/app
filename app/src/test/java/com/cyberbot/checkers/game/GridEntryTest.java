package com.cyberbot.checkers.game;

import android.os.DropBoxManager;

import com.cyberbot.checkers.game.GridEntry;
import com.cyberbot.checkers.game.PlayerNum;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridEntryTest {

    @Test
    void shouldDefaultToNoPlayer() {
        GridEntry entry = new GridEntry(0, 0);
        assertEquals(PlayerNum.NOPLAYER, entry.getPlayer());
    }

    @Test
    void shouldReturnPreviouslySetPlayer() {
        GridEntry entry = new GridEntry(0, 0);

        entry.setPlayer(PlayerNum.FIRST);
        assertEquals(PlayerNum.FIRST, entry.getPlayer());

        entry.setPlayer(PlayerNum.NOPLAYER);
        assertEquals(PlayerNum.NOPLAYER, entry.getPlayer());


        entry.setPlayer(PlayerNum.SECOND);
        assertEquals(PlayerNum.SECOND, entry.getPlayer());
    }

    @Test
    void shouldReturnInitializedCoordinates() {
        GridEntry entry = new GridEntry(4, 9);

        assertEquals(4, entry.getX());
        assertEquals(9, entry.getY());
    }

    @ParameterizedTest
    @MethodSource("legalEntryProvider")
    void shouldReturnLegalForCorrectValues(int x, int y) {
        GridEntry entry = new GridEntry(x, y);
        assertTrue(entry.legal());
    }

    @ParameterizedTest
    @MethodSource("illegalEntryProvider")
    void shouldReturnIllegalForCorrectValues(int x, int y) {
        GridEntry entry = new GridEntry(x, y);
        assertFalse(entry.legal());
    }

    @Test
    @SuppressWarnings("SimplifiableJUnitAssertion")
    void shouldBeEqualWhenPlayersAreNot() {
        GridEntry entry1 = new GridEntry(10, 22);
        GridEntry entry2 = new GridEntry(10, 22);

        entry1.setPlayer(PlayerNum.FIRST);
        entry1.setPlayer(PlayerNum.SECOND);
        assertTrue(entry1.equals(entry2));
    }

    private static Stream<Arguments> legalEntryProvider() {
        return Stream.of(
                Arguments.of(0, 1),
                Arguments.of(11, 2),
                Arguments.of(34, 71),
                Arguments.of(13, 76),
                Arguments.of(0, 9),
                Arguments.of(23, 2)
        );
    }

    private static Stream<Arguments> illegalEntryProvider() {
        return Stream.of(
                Arguments.of(1, 1),
                Arguments.of(12, 2),
                Arguments.of(31, 71),
                Arguments.of(16, 76),
                Arguments.of(13, 9),
                Arguments.of(23, 97)
        );
    }
}
