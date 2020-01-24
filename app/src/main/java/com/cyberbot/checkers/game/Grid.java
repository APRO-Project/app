package com.cyberbot.checkers.game;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cyberbot.checkers.preferences.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a grid in the checkers game. Basically, it is used to store {@link GridEntry} objects,
 * that represent player pieces and also to perform several game logic tasks such as possible
 * moves and captures calculations.
 *
 * It also implements {@link Iterable} so one can iterate conveniently over its entries.
 */
public class Grid implements Iterable<GridEntry> {
    private final int size;
    private final int playerRows;
    private final ArrayList<GridEntry> gridEntries;
    private HashMap<GridEntry, ArrayList<Destination>> movableEntriesCache;

    // Preferences
    private boolean canMoveBackwards = false;
    private boolean canCaptureBackwards = true;
    private boolean flyingKing = true;
    private boolean mandatoryCapture = true;

    public int getSize() {
        return size;
    }

    public int getPlayerRows() {
        return playerRows;
    }

    /**
     * Default constructor, that creates {@link Grid} object
     * with 10x10 board and 4 player rows.
     */
    public Grid() {
        this(10, 4);
    }

    /**
     * Constructs {@link Grid} object with default {@link Preferences}.
     *
     * @param size Size of the board. Board's dimensions will be [{@code size} x {@code size}].
     *             Minimum value is 8
     * @param playerRows Number of player rows - must not be greater than {@code size / 2 - 1}
     */
    public Grid(int size, int playerRows) {
        if(size < 8) {
            throw new IllegalArgumentException("Size of the board should be at least 8");
        }
        if(size - (playerRows * 2) < 2) {
            throw new IllegalArgumentException("There should be 2 no-player rows on the board at minimum");
        }

        this.size = size;
        this.playerRows = playerRows;
        gridEntries = new ArrayList<>();
        movableEntriesCache = null;

        for (int i = 0; i < size * size; ++i) {
            int y = i / size;
            GridEntry entry = new GridEntry(i % size, y);

            if (y < playerRows && entry.legal()) {
                entry.setPlayer(PlayerNum.FIRST);
                entry.setPieceType(PieceType.ORDINARY);
            } else if (y >= size - playerRows && entry.legal()) {
                entry.setPlayer(PlayerNum.SECOND);
                entry.setPieceType(PieceType.ORDINARY);
            }

            gridEntries.add(entry);
        }
    }

    /**
     * Constructs {@link Grid} object.
     *
     * @param size See {@link Grid#Grid(int, int)}
     * @param playerRows See {@link Grid#Grid(int, int)}
     * @param canMoveBackwards Allow ordinary pieces to move backwards
     * @param canCaptureBackwards Allow ordinary pieces to capture backwards
     * @param flyingKing Allow king to move by multiple steps
     * @param mandatoryCapture Force captures
     */
    public Grid(int size, int playerRows, boolean canMoveBackwards, boolean canCaptureBackwards, boolean flyingKing, boolean mandatoryCapture) {
        this(size, playerRows);
        this.canMoveBackwards = canMoveBackwards;
        this.canCaptureBackwards = canCaptureBackwards;
        this.flyingKing = flyingKing;
        this.mandatoryCapture = mandatoryCapture;
    }

    /**
     * Constructs {@link Grid} object based on given {@link Preferences}.
     *
     * @param prefs {@link Preferences} object. Cannot be null
     * @return {@link Grid} object
     *
     * @see Preferences
     */
    @NotNull
    @Contract("_ -> new")
    public static Grid fromPreferences(@NotNull Preferences prefs) {
        return new Grid(
                prefs.getGridSize(),
                prefs.getPlayerRows(),
                prefs.getCanMoveBackwards(),
                prefs.getCanCaptureBackwards(),
                prefs.getFlyingKing(),
                prefs.getMandatoryCapture()
        );
    }

    /**
     * Look for specific {@link GridEntry} in {@link Grid#gridEntries}.
     *
     * @param x x-coordinate of searched {@link GridEntry}
     * @param y y-coordinate of searched {@link GridEntry}
     * @return {@link GridEntry} object if such exists in {@link Grid#gridEntries}. Otherwise, a
     * {@link RuntimeException} is thrown
     * @throws IndexOutOfBoundsException When called with {@code x} or {@code y} greater than
     * {@link Grid#size} or less than zero
     */
    @NotNull
    public GridEntry getEntryByCoords(int x, int y) throws IndexOutOfBoundsException {
        if (x >= size || y >= size || x < 0 || y < 0) {
            throw new IndexOutOfBoundsException("Coordinates (" + x + ", " + y + ") out of bounds for grid with size " + size);
        }

        for (GridEntry e : gridEntries) {
            if (e.getX() == x && e.getY() == y) return e;
        }

        throw new RuntimeException("Entry (" + x + ", " + y + ") not found in Grid");
    }

    /**
     * Sets {@code player} as {@link PlayerNum#NOPLAYER} and {@code pieceType} as {@link PieceType#UNASSIGNED}
     * in {@link GridEntry} that matches given coordinates. When there is no such entry,
     * a {@link RuntimeException} is thrown.
     *
     * @param entry {@link GridEntry} to be "removed"
     *
     * @see PlayerNum
     * @see PieceType
     */
    public void removeGridEntry(@NotNull GridEntry entry) {
        int index = gridEntries.indexOf(entry);
        if(index == -1) {
            throw new RuntimeException("Entry (" + entry.getX() + ", " + entry.getY() + ") not found in gridEntries");
        }

        gridEntries.get(index).setPlayer(PlayerNum.NOPLAYER);
        gridEntries.get(index).setPieceType(PieceType.UNASSIGNED);
    }

    /**
     * Get {@link Destination} object based on given {@code src} and {@code dst} entries.
     *
     * @param src Source entry. Cannot be null
     * @param dst Destination entry. Cannot be null
     * @return {@link Destination} that matches given {@code dst}, new {@link Destination} with
     * {@code src} as the destination when {@code src == dst} or null when there is no destinations
     * for given {@code src} or {@code dst} is not in found in possible destinations
     *
     * @see Destination
     * @see Grid#getMovableEntries(PlayerNum)
     */
    @Nullable
    public Destination getDestination(@NotNull GridEntry src, @NotNull GridEntry dst) {
        if(src != dst) {
            ArrayList<Destination> destinations = getMovableEntries(src.getPlayer()).get(src);
            if(destinations == null) return null;

            for(Destination destination: destinations) {
                if(destination.getDestinationEntry() == dst) return destination;
            }
        }
        else return new Destination(src);

        return null;
    }

    /**
     * Tells if a move is allowed from {@code src} to {@code dst}.
     *
     * @param src Source entry. Cannot be null
     * @param dst Destination entry. Cannot be null
     * @return {@code true} if move from {@code src} to {@code dst} is allowed, {@code false} otherwise
     */
    public boolean destinationAllowed(@NotNull GridEntry src, @NotNull GridEntry dst) {
        if(src == dst) return true;

        getMovableEntries(src.getPlayer());

        ArrayList<Destination> destinations = movableEntriesCache.get(src);
        if(destinations == null) return false;

        for(Destination destination: destinations) {
            if(destination.getDestinationEntry() == dst) return true;
        }

        return false;
    }

    /**
     * Perform a move based on given {@code src} and {@code dst}. If the move is not
     * allowed based by {@link Grid#destinationAllowed(GridEntry, GridEntry)} decision or when
     * {@code src == dst}, it isn't executed.
     *
     * Moving a piece includes swapping {@code player} and {@code pieceType} of involved entries.
     *
     * If any of entries is not found in {@link Grid#gridEntries}, a {@link RuntimeException} is
     * thrown.
     *
     * @param src Source entry. Cannot be null
     * @param dst Destination entry. Cannot be null
     * @return {@code true} is move was executed or {@code false} if it's not allowed
     */
    public boolean attemptMove(@NotNull GridEntry src, @NotNull GridEntry dst) {
        if(src == dst) return false;

        if(!destinationAllowed(src, dst)) return false;

        final int srcIdx = gridEntries.indexOf(src);
        final int dstIdx = gridEntries.indexOf(dst);

        if (srcIdx == -1 || dstIdx == -1) {
            throw new RuntimeException("GridEntry destination or source not part of the Grid");
        }

        gridEntries.get(dstIdx).setPlayer(src.getPlayer());
        gridEntries.get(dstIdx).setPieceType(src.getPieceType());

        gridEntries.get(srcIdx).setPlayer(PlayerNum.NOPLAYER);
        gridEntries.get(srcIdx).setPieceType(PieceType.UNASSIGNED);

        // TODO: Promote piece to king if necessary (an update javadoc)

        movableEntriesCache = null;

        return true;
    }

    @NonNull
    @Override
    public Iterator<GridEntry> iterator() {
        return gridEntries.iterator();
    }

    /**
     * Get all adjacent entries to {@code entry}. Only legal entries are considered adjacent.
     *
     * @param entry {@link GridEntry}, for which we want to get adjacent entries
     * @return {@link ArrayList} with all adjacent entries to given {@code entry}
     */
    @NotNull
    private ArrayList<GridEntry> getAdjacentEntries(@NotNull GridEntry entry) {
        ArrayList<GridEntry> adjacentEntries = new ArrayList<>();

        // Short names for code readability
        final int x = entry.getX();
        final int y = entry.getY();

        // Bottom corner
        if (x == 0 && y == size - 1) {
            adjacentEntries.add(getEntryByCoords(1, size - 2));
        }
        // Upper corner
        else if (y == 0 && x == size - 1) {
            adjacentEntries.add(getEntryByCoords(size - 2, 1));
        }
        // Upper edge
        else if (x == 0) {
            adjacentEntries.add(getEntryByCoords(1, y - 1));
            adjacentEntries.add(getEntryByCoords(1, y + 1));
        }
        // Left edge
        else if (y == 0) {
            adjacentEntries.add(getEntryByCoords(x - 1, 1));
            adjacentEntries.add(getEntryByCoords(x + 1, 1));
        }
        // Bottom edge
        else if (x == size - 1) {
            adjacentEntries.add(getEntryByCoords(x - 1, y - 1));
            adjacentEntries.add(getEntryByCoords(x - 1, y + 1));
        }
        // Right edge
        else if (y == size - 1) {
            adjacentEntries.add(getEntryByCoords(x - 1, y - 1));
            adjacentEntries.add(getEntryByCoords(x + 1, y - 1));
        }
        // Mid entries
        else {
            adjacentEntries.add(getEntryByCoords(x - 1, y - 1));
            adjacentEntries.add(getEntryByCoords(x - 1, y + 1));
            adjacentEntries.add(getEntryByCoords(x + 1, y - 1));
            adjacentEntries.add(getEntryByCoords(x + 1, y + 1));
        }

        return adjacentEntries;
    }

    /**
     * Get all allowed moves for specified {@code entry}. Moves are calculated differently
     * based on {@link PieceType}.
     *
     * @param entry {@link GridEntry} we want to get moves for
     * @return {@link ArrayList} of allowed moves
     *
     * @see Grid#calculateOrdinaryPieceMoves(GridEntry)
     */
    @NotNull
    private ArrayList<GridEntry> getAllowedMoves(@NotNull GridEntry entry) {
        ArrayList<GridEntry> allowedMoves = new ArrayList<>();

        if(entry.getPieceType() == PieceType.ORDINARY) {
            allowedMoves = calculateOrdinaryPieceMoves(entry);
        }
        else if(entry.getPieceType() == PieceType.KING) {
            // TODO: Calculate king moves (and update javadoc)
        }

        return allowedMoves;
    }

    /**
     * Calculate allowed moves for {@link PieceType#ORDINARY}. {@link Grid#canMoveBackwards}
     * preference is taken into account during calculations.
     *
     * If supplied {@code entry} has different {@code pieceType} than {@link PieceType#ORDINARY},
     * a {@link IllegalArgumentException} is thrown.
     *
     * @param entry {@link GridEntry} we want to calculate moves for
     * @return {@link ArrayList} of calculated moves
     */
    @NotNull
    private ArrayList<GridEntry> calculateOrdinaryPieceMoves(@NotNull GridEntry entry) {
        if(entry.getPieceType() != PieceType.ORDINARY) {
            throw new IllegalArgumentException("Attempt to calculate ordinary piece moves for non-ordinary piece");
        }

        ArrayList<GridEntry> allowedMoves = new ArrayList<>();
        for (GridEntry adjEntry : getAdjacentEntries(entry)) {
            if (adjEntry.getPlayer() == PlayerNum.NOPLAYER) {
                if (canMoveBackwards
                        || (entry.getPlayer() == PlayerNum.FIRST && entry.getY() < adjEntry.getY())
                        || (entry.getPlayer() == PlayerNum.SECOND && entry.getY() > adjEntry.getY())) {
                    allowedMoves.add(adjEntry);
                }
            }
        }
      
        return allowedMoves;
    }

    /**
     * Get all allowed captures for specified {@code entry}. Captures are calculated differently
     * base on {@link PieceType}
     *
     * @param entry {@link GridEntry} we want to get captures for
     * @return {@link ArrayList} of allowed captures
     */
    @NotNull
    private ArrayList<CaptureChain> getAllowedCaptures(@NotNull GridEntry entry) {
        CaptureChain root = new CaptureChain(entry, null, null);

        if(entry.getPieceType() == PieceType.ORDINARY) {
            calculateOrdinaryPieceCaptures(root, entry.getPlayer());
        }
        else if(entry.getPieceType() == PieceType.KING) {
            // TODO: Calculate king captures (and update javadoc)
        }

        ArrayList<CaptureChain> allowedCaptures;

        if(mandatoryCapture) allowedCaptures = root.getLongestCaptures();
        else allowedCaptures = root.getAllCaptures();

        return allowedCaptures;
    }

    /**
     * Calculate allowed captures for {@link PieceType#ORDINARY}. {@link Grid#canCaptureBackwards}
     * preference is taken into account during calculations.
     *
     * Method doesn't return any value - calculations are performed recursively and results are
     * stored in {@code lastCapture} parameter.
     *
     * There's no checks if the method was invoked for {@link PieceType#ORDINARY} because of
     * recursive nature of it, so the caller should be sure to call it correctly. Also, the caller
     * needs to be sure, that {@code player} is not set to {@link PlayerNum#NOPLAYER}.
     *
     * @param lastCapture {@link CaptureChain} object representing last capture that has taken place.
     *                                        It should have been set to root capture when first invoked
     * @param player {@link PlayerNum} representing the player who is capturing
     *
     * @see CaptureChain
     */
    private void calculateOrdinaryPieceCaptures(@NotNull CaptureChain lastCapture, final PlayerNum player) {
        GridEntry lastLocation = lastCapture.getLocationAfterCapture();

        for(GridEntry adjEntry: getAdjacentEntries(lastLocation)) {
            // Check if can capture backwards or adjacent piece is ahead
            if(canCaptureBackwards
                    || (lastLocation.getPlayer() == PlayerNum.FIRST && lastLocation.getY() < adjEntry.getY())
                    || (lastLocation.getPlayer() == PlayerNum.SECOND && lastLocation.getY() > adjEntry.getY())) {

                // Check if adjacent piece belongs to other player and wasn't captured yet
                if (adjEntry.getPlayer() != PlayerNum.NOPLAYER
                        && adjEntry.getPlayer() != player
                        && !lastCapture.checkIfEntryCaptured(adjEntry)) {

                    int entryAfterX = adjEntry.getX() + (adjEntry.getX() - lastLocation.getX());
                    int entryAfterY = adjEntry.getY() + (adjEntry.getY() - lastLocation.getY());

                    // Check if entry after captured piece is on the Grid
                    if (entryAfterX >= 0 && entryAfterX < size
                            && entryAfterY >= 0 && entryAfterY < size) {

                        GridEntry entryAfter = getEntryByCoords(entryAfterX, entryAfterY);
                        // Check if entry after captured piece belongs to nobody
                        if (entryAfter.getPlayer() == PlayerNum.NOPLAYER) {
                            CaptureChain nextCapture = new CaptureChain(entryAfter, adjEntry, lastCapture);
                            lastCapture.addNextCapture(nextCapture);
                            calculateOrdinaryPieceCaptures(nextCapture, player);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get all movable entries for given {@code player}. This includes all moves and captures,
     * respecting the {@link Grid#mandatoryCapture} preference.
     *
     * Results are saved in {@link Grid#movableEntriesCache} because of high calling rate
     * and quite complex operations withing the method. The cache should be cleared after
     * {@link Grid#attemptMove(GridEntry, GridEntry)} is successfully called ({@code true} returned).
     *
     * {@code player} should not be {@link PlayerNum#NOPLAYER} - otherwise {@link IllegalArgumentException}
     * is thrown.
     *
     * @param player Player we want to get movable entries for
     * @return {@link HashMap}, where {@link GridEntry} is the key and represents source of the move
     * and {@link ArrayList} of {@link Destination} that represents all possible destinations for the
     * source entry
     */
    @NotNull
    public HashMap<GridEntry, ArrayList<Destination>> getMovableEntries(PlayerNum player) {
        if(player == PlayerNum.NOPLAYER) {
            throw new IllegalArgumentException("Cannot get movable entries for NOPLAYER");
        }

        if (movableEntriesCache != null) return movableEntriesCache;

        HashMap<GridEntry, ArrayList<Destination>> movableEntries = new HashMap<>();
        HashMap<GridEntry, ArrayList<CaptureChain>> possibleCaptures = new HashMap<>();
        int longestCaptureLength = 1;

        // Check for captures first
        for (GridEntry entry : gridEntries) {
            if (entry.getPlayer() == player) {
                ArrayList<CaptureChain> allowedCaptures = getAllowedCaptures(entry);
                if (!allowedCaptures.isEmpty()) {
                    possibleCaptures.put(entry, getAllowedCaptures(entry));
                    for (CaptureChain capture : allowedCaptures) {
                        if (capture.getCaptureLength() > longestCaptureLength) {
                            longestCaptureLength = capture.getCaptureLength();
                        }
                    }
                }
            }
        }

        // Add moves if captures aren't mandatory
        if (!mandatoryCapture || possibleCaptures.isEmpty()) {
            for (GridEntry entry : gridEntries) {
                if (entry.getPlayer() == player) {
                    ArrayList<GridEntry> allowedMoves = getAllowedMoves(entry);
                    if (!allowedMoves.isEmpty()) {
                        ArrayList<Destination> destinations = new ArrayList<>();
                        for (GridEntry destination : allowedMoves) {
                            destinations.add(new Destination(destination));
                        }

                        movableEntries.put(entry, destinations);
                    }
                }
            }
        }

        // Add captures
        if (!possibleCaptures.isEmpty()) {
            for (Map.Entry<GridEntry, ArrayList<CaptureChain>> possibleCapture : possibleCaptures.entrySet()) {
                ArrayList<Destination> destinations = new ArrayList<>();
                for (CaptureChain capture : possibleCapture.getValue()) {
                    if (!mandatoryCapture || capture.getCaptureLength() == longestCaptureLength) {
                        destinations.add(new Destination(capture));
                    }
                }

                if (!destinations.isEmpty()) {
                    if (!movableEntries.containsKey(possibleCapture.getKey())) {
                        movableEntries.put(possibleCapture.getKey(), destinations);
                    } else {
                        ArrayList<Destination> otherDestinations = movableEntries.get(possibleCapture.getKey());
                        if(otherDestinations == null) {
                            throw new RuntimeException("This should not happen. Contact developers on GitHub if it did");
                        }
                        otherDestinations.addAll(destinations);
                    }
                }
            }
        }

        movableEntriesCache = movableEntries;

        return movableEntries;
    }

    int getValue(){
        int value = 0;
        for (GridEntry gridEntry : gridEntries) {
            // Count pieces
            value += 10;

            if (gridEntry.getPieceType() == PieceType.KING) {
                value += 30;
            }

            // Prioritize sides
            value += Math.abs((gridEntry.getX()) + gridEntry.getY() - getSize()) / 4;

            // Prioritize forward
            if (gridEntry.getPlayer() == PlayerNum.FIRST) {
                value += gridEntry.getY() / 2;
            } else if (gridEntry.getPlayer() == PlayerNum.SECOND) {
                value += Math.abs((gridEntry.getY() - getSize()) / 2);
            }
        }

        return value;
    }

    static Grid simulateMove(Grid grid, GridEntry src, Destination dst) {
        grid.getEntryByCoords(src.getX(), src.getY()).setPlayer(PlayerNum.NOPLAYER);
        grid.getEntryByCoords(src.getX(), src.getY()).setPieceType(PieceType.UNASSIGNED);

        for (GridEntry destroyed:dst.getCapturedPieces()){
            grid.getEntryByCoords(destroyed.getX(),destroyed.getY()).setPieceType(PieceType.UNASSIGNED);
            grid.getEntryByCoords(destroyed.getX(),destroyed.getY()).setPlayer(PlayerNum.NOPLAYER);
        }

        grid.getEntryByCoords(dst.getDestinationEntry().getX(),dst.getDestinationEntry().getY()).setPlayer(PlayerNum.NOPLAYER);
        grid.getEntryByCoords(dst.getDestinationEntry().getX(),dst.getDestinationEntry().getY()).setPieceType(PieceType.UNASSIGNED);

        return grid;
    }

    boolean won(PlayerNum enemy){
        if (getMovableEntries(enemy).isEmpty()){
            return true;
        }
        int enemyNo = 0;
        for (GridEntry gridEntry : gridEntries){
            if (gridEntry.getPlayer() == enemy){
                enemyNo++;
            }
        }
        return enemyNo == 0;
    }

    boolean lost(PlayerNum player){
        if (getMovableEntries(player).isEmpty()){
            return true;
        }
        int playerNo = 0;
        for (GridEntry gridEntry : gridEntries){
            if (gridEntry.getPlayer() == player){
                playerNo++;
            }
        }
        return playerNo == 0;
    }
}