package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.Board;
import model.Cell;
import model.HoleStageModel;
import model.Pawn;

public class GooseDecider extends Decider {

    private static final int DEPTH = 3;

    // Piece type constants for the virtual board
    private static final int EMPTY = 0;
    private static final int GOOSE = 1;
    private static final int FOX   = 2;

    // Loop detection state attributes
    private int[] lastMove = null;
    private int repeatCount = 0;

    public GooseDecider(Model model, Controller control) {
        super(model, control);
    }

    // -------------------------------------------------------------------------
    // decide() : Entry point called by the framework's game loop
    // -------------------------------------------------------------------------
    @Override
    public ActionList decide() {
        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // 1. Build a virtual snapshot of the board (no side-effects on the real board layout)
        int[][] virtualBoard = buildVirtualBoard(board);

        // 2. Find the current simulated fox position
        int foxRow = -1, foxCol = -1;
        outer:
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (virtualBoard[y][x] == FOX) {
                    foxRow = y;
                    foxCol = x;
                    break outer;
                }
            }
        }

        // 3. Run Minimax from the geese's perspective (maximizing player)
        int[] best = minimaxBestMove(virtualBoard, board, foxRow, foxCol, DEPTH);
        board.clearValidCells();

        // Safety fallback if no valid move could be found by the algorithm
        if (best == null) {
            System.out.println("GEESE BOT HAS NO VALID MOVES");
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        // 4. LOOP DETECTION MECHANISM
        // If the AI repeatedly picks the exact same move, increment counter; otherwise reset it
        if (lastMove != null &&
                lastMove[0] == best[0] && lastMove[1] == best[1] &&
                lastMove[2] == best[2] && lastMove[3] == best[3]) {
            repeatCount++;
        } else {
            repeatCount = 0;
            lastMove = best;
        }

        // If a repetitive state loop is triggered (attempting the same move 3 times), force an alternative move
        if (repeatCount >= 2) {
            best = findAnyValidMove(virtualBoard, board);
            repeatCount = 0;
            lastMove = best;
        }

        // 5. Generate and fire the real Boardifier movement sequence
        GameElement pawn = board.getElement(best[0], best[1]);
        ActionList actions = ActionFactory.generateMoveWithinContainer(model, pawn, best[2], best[3]);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    /**
     * Fallback loop-breaker: Scans and immediately returns the first valid legal move available.
     * @return an integer array containing {fromRow, fromCol, toRow, toCol} or null if trapped.
     */
    private int[] findAnyValidMove(int[][] vBoard, Board board) {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (vBoard[y][x] != GOOSE) continue;

                boolean[][] reachable = getGooseReachable(board, vBoard, y, x);
                for (int ry = 0; ry < 7; ry++) {
                    for (int rx = 0; rx < 7; rx++) {
                        if (reachable[ry][rx]) {
                            return new int[]{y, x, ry, rx};
                        }
                    }
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // minimaxBestMove : Returns {fromRow, fromCol, toRow, toCol} for the best
    //                   goose move at the root level
    // -------------------------------------------------------------------------
    private int[] minimaxBestMove(int[][] vBoard, Board board, int foxRow, int foxCol, int depth) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (vBoard[y][x] != GOOSE) continue;

                // Get valid moves for this goose using the real board topology
                boolean[][] reachable = getGooseReachable(board, vBoard, y, x);

                for (int ry = 0; ry < 7; ry++) {
                    for (int rx = 0; rx < 7; rx++) {
                        if (!reachable[ry][rx]) continue;

                        // Apply move on a copy
                        int[][] next = applyGooseMove(vBoard, y, x, ry, rx);

                        // Fox minimizes next (depth-1), geese maximize current
                        int score = minimax(next, board, foxRow, foxCol, depth - 1, false);

                        if (score > bestScore) {
                            bestScore = score;
                            bestMove  = new int[]{y, x, ry, rx};
                        }
                    }
                }
            }
        }
        return bestMove;
    }

    // -------------------------------------------------------------------------
    // minimax : Standard minimax calculation without alpha-beta pruning
    //   maximizing = true  → Geese's turn
    //   maximizing = false → Fox's turn
    // -------------------------------------------------------------------------
    private int minimax(int[][] vBoard, Board board,
                        int foxRow, int foxCol,
                        int depth, boolean maximizing) {

        // Terminal leaf node evaluation
        if (depth == 0) return evaluate(vBoard, board, foxRow, foxCol);

        if (maximizing) {
            // --- GEESE TURN ---
            int best = Integer.MIN_VALUE;
            boolean hasMoves = false;

            for (int y = 0; y < 7; y++) {
                for (int x = 0; x < 7; x++) {
                    if (vBoard[y][x] != GOOSE) continue;

                    boolean[][] reachable = getGooseReachable(board, vBoard, y, x);
                    for (int ry = 0; ry < 7; ry++) {
                        for (int rx = 0; rx < 7; rx++) {
                            if (!reachable[ry][rx]) continue;
                            hasMoves = true;

                            int[][] next = applyGooseMove(vBoard, y, x, ry, rx);
                            int score = minimax(next, board, foxRow, foxCol, depth - 1, false);
                            if (score > best) best = score;
                        }
                    }
                }
            }
            // Geese have no moves left → Fox wins eventually, heavily penalize
            return hasMoves ? best : -10000;

        } else {
            // --- FOX TURN ---
            int best = Integer.MAX_VALUE;
            boolean hasMoves = false;

            boolean[][] foxReachable = getFoxReachable(board, vBoard, foxRow, foxCol);
            for (int ry = 0; ry < 7; ry++) {
                for (int rx = 0; rx < 7; rx++) {
                    if (!foxReachable[ry][rx]) continue;
                    hasMoves = true;

                    // Check if this is a capture jump (distance of 2 cells)
                    int capturedRow = -1, capturedCol = -1;
                    if (Math.abs(ry - foxRow) == 2 || Math.abs(rx - foxCol) == 2) {
                        capturedRow = (foxRow + ry) / 2;
                        capturedCol = (foxCol + rx) / 2;
                    }

                    int[][] next = applyFoxMove(vBoard, foxRow, foxCol, ry, rx, capturedRow, capturedCol);
                    int score = minimax(next, board, ry, rx, depth - 1, true);
                    if (score < best) best = score;
                }
            }
            // Fox has no moves left → Geese win, reward strongly
            return hasMoves ? best : 10000;
        }
    }

    // -------------------------------------------------------------------------
    // evaluate : Heuristic score from the geese's point of view (higher = better)
    // -------------------------------------------------------------------------
    private int evaluate(int[][] vBoard, Board board, int foxRow, int foxCol) {
        int score = 0;
        int foxMoves    = 0;
        int geeseDanger = 0;

        // foxRow/foxCol are the simulated matrix coordinates passed as arguments
        Cell foxCell = board.getCell(foxCol, foxRow); // foxCol=x, foxRow=y → correct mapping

        for (Cell neighbor : foxCell.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            if (vBoard[ny][nx] == EMPTY) {
                foxMoves++;
            } else if (vBoard[ny][nx] == GOOSE) {
                int jumpX = nx + (nx - foxCol);
                int jumpY = ny + (ny - foxRow);
                if (jumpX >= 0 && jumpX < 7 && jumpY >= 0 && jumpY < 7
                        && board.getCell(jumpX, jumpY).isAccessible()
                        && vBoard[jumpY][jumpX] == EMPTY) {
                    geeseDanger++;
                }
            }
        }

        score -= foxMoves    * 10;
        score -= geeseDanger * 50;

        int geeseCount = 0;
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (vBoard[y][x] == GOOSE) geeseCount++;
            }
        }
        score += geeseCount * 20;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (vBoard[y][x] == GOOSE) {
                    int dist = Math.abs(y - foxRow) + Math.abs(x - foxCol);
                    score -= dist;
                }
            }
        }

        // Apply a penalty if the fox moves downwards (advancing aggressively into geese territory)
        score += foxRow * 5;

        return score;
    }

    // -------------------------------------------------------------------------
    // Helpers : Build / copy virtual boards and compute reachable cells
    // -------------------------------------------------------------------------

    /** Snapshot of the real board as a simple int[][]. */
    private int[][] buildVirtualBoard(Board board) {
        int[][] vb = new int[7][7];
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                GameElement e = board.getElement(y, x);
                if (e == null) {
                    vb[y][x] = EMPTY;
                    continue;
                }
                Pawn p = (Pawn) e;
                vb[y][x] = p.isFox() ? FOX : GOOSE;
            }
        }
        return vb;
    }

    /** Deep copy of a 7x7 int array. */
    private int[][] copyBoard(int[][] src) {
        int[][] copy = new int[7][7];
        for (int y = 0; y < 7; y++) copy[y] = src[y].clone();
        return copy;
    }

    /** Returns a new board layout after moving a goose from (fy,fx) to (ty,tx). */
    private int[][] applyGooseMove(int[][] vb, int fy, int fx, int ty, int tx) {
        int[][] next = copyBoard(vb);
        next[ty][tx] = GOOSE;
        next[fy][fx] = EMPTY;
        return next;
    }

    /** Returns a new board layout after moving the fox; removes captured goose if any. */
    private int[][] applyFoxMove(int[][] vb, int fy, int fx, int ty, int tx,
                                 int capRow, int capCol) {
        int[][] next = copyBoard(vb);
        next[ty][tx] = FOX;
        next[fy][fx] = EMPTY;
        if (capRow >= 0) next[capRow][capCol] = EMPTY;
        return next;
    }

    /**
     * Computes reachable cells for a goose at (row,col) using the virtual board matrix.
     * Geese can move up (lower row index) or horizontally, one step, to an empty cell.
     */
    private boolean[][] getGooseReachable(Board board, int[][] vb, int row, int col) {
        boolean[][] reach = new boolean[7][7];
        Cell current = board.getCell(col, row);
        for (Cell neighbor : current.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();
            boolean vertical   = (nx == col && ny < row);
            boolean horizontal = (ny == row && nx != col);
            if ((vertical || horizontal) && vb[ny][nx] == EMPTY) {
                reach[ny][nx] = true;
            }
        }
        return reach;
    }

    /**
     * Computes reachable cells for the fox at (row,col) using the virtual board matrix.
     * Fox can move to empty neighbors or jump over a goose to an empty cell behind it.
     */
    private boolean[][] getFoxReachable(Board board, int[][] vb, int row, int col) {
        boolean[][] reach = new boolean[7][7];
        Cell current = board.getCell(col, row);
        for (Cell neighbor : current.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();
            if (vb[ny][nx] == EMPTY) {
                reach[ny][nx] = true;
            } else if (vb[ny][nx] == GOOSE) {
                int jumpX = nx + (nx - col);
                int jumpY = ny + (ny - row);
                if (jumpX >= 0 && jumpX < 7 && jumpY >= 0 && jumpY < 7
                        && board.getCell(jumpX, jumpY).isAccessible()
                        && vb[jumpY][jumpX] == EMPTY) {
                    reach[jumpY][jumpX] = true;
                }
            }
        }
        return reach;
    }
}