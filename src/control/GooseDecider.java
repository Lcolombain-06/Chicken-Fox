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

    public GooseDecider(Model model, Controller control) {
        super(model, control);
    }

    // -------------------------------------------------------------------------
    // decide() : entry point called by the game loop
    // -------------------------------------------------------------------------
    @Override
    public ActionList decide() {
        HoleStageModel stage = (HoleStageModel) model.getGameStage();
        Board board = stage.getBoard();

        // Build a virtual snapshot of the board (no side-effects on the real board)
        int[][] virtualBoard = buildVirtualBoard(board);

        // Find the fox position
        int foxRow = -1, foxCol = -1;
        outer:
        for (int y = 0; y < 7; y++)
            for (int x = 0; x < 7; x++)
                if (virtualBoard[y][x] == FOX) { foxRow = y; foxCol = x; break outer; }

        // Run minimax from the geese's perspective (maximising player)
        int[] best = minimaxBestMove(virtualBoard, board, foxRow, foxCol, DEPTH);

        // best = { fromRow, fromCol, toRow, toCol }
        board.clearValidCells();

        if (best == null) {
            System.out.println("GEESE BOT HAS NO VALID MOVES");
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        GameElement pawn = board.getElement(best[0], best[1]);
        ActionList actions = ActionFactory.generateMoveWithinContainer(model, pawn, best[2], best[3]);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    // -------------------------------------------------------------------------
    // minimaxBestMove : returns {fromRow, fromCol, toRow, toCol} for the best
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

                        // Fox minimises next (depth-1), geese maximise current
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
    // minimax : standard minimax without alpha-beta
    //   maximising = true  → geese's turn
    //   maximising = false → fox's turn
    // -------------------------------------------------------------------------
    private int minimax(int[][] vBoard, Board board,
                        int foxRow, int foxCol,
                        int depth, boolean maximising) {

        // Terminal / leaf evaluation
        if (depth == 0) return evaluate(vBoard, board, foxRow, foxCol);

        if (maximising) {
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
            // Geese have no moves → fox wins eventually, penalise
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

                    // Check if this is a capture (jump of 2)
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
            // Fox has no moves → geese win, reward strongly
            return hasMoves ? best : 10000;
        }
    }

    // -------------------------------------------------------------------------
    // evaluate : heuristic score from the geese's point of view (higher = better)
    // -------------------------------------------------------------------------
    private int evaluate(int[][] vBoard, Board board, int foxRow, int foxCol) {
        int score = 0;

        // A. Count fox free moves (fewer = better for geese)
        int foxMoves     = 0;
        int geeseDanger  = 0;

        Cell foxCell = board.getCell(foxCol, foxRow);
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
        score -= foxMoves   * 10;
        score -= geeseDanger * 50;

        // B. Count remaining geese (more = better)
        int geeseCount = 0;
        for (int y = 0; y < 7; y++)
            for (int x = 0; x < 7; x++)
                if (vBoard[y][x] == GOOSE) geeseCount++;
        score += geeseCount * 20;

        // C. Reward geese being close to the fox row (blockade pressure)
        for (int y = 0; y < 7; y++)
            for (int x = 0; x < 7; x++)
                if (vBoard[y][x] == GOOSE) {
                    int dist = Math.abs(y - foxRow) + Math.abs(x - foxCol);
                    score -= dist; // closer = less penalty
                }

        return score;
    }

    // -------------------------------------------------------------------------
    // Helpers : build / copy virtual boards and compute reachable cells
    // -------------------------------------------------------------------------

    /** Snapshot of the real board as a simple int[][]. */
    private int[][] buildVirtualBoard(Board board) {
        int[][] vb = new int[7][7];
        for (int y = 0; y < 7; y++)
            for (int x = 0; x < 7; x++) {
                GameElement e = board.getElement(y, x);
                if (e == null) { vb[y][x] = EMPTY; continue; }
                Pawn p = (Pawn) e;
                vb[y][x] = p.isFox() ? FOX : GOOSE;
            }
        return vb;
    }

    /** Deep copy of a 7x7 int array. */
    private int[][] copyBoard(int[][] src) {
        int[][] copy = new int[7][7];
        for (int y = 0; y < 7; y++) copy[y] = src[y].clone();
        return copy;
    }

    /** Returns a new board after moving a goose from (fy,fx) to (ty,tx). */
    private int[][] applyGooseMove(int[][] vb, int fy, int fx, int ty, int tx) {
        int[][] next = copyBoard(vb);
        next[ty][tx] = GOOSE;
        next[fy][fx] = EMPTY;
        return next;
    }

    /** Returns a new board after moving the fox; removes captured goose if any. */
    private int[][] applyFoxMove(int[][] vb, int fy, int fx, int ty, int tx,
                                 int capRow, int capCol) {
        int[][] next = copyBoard(vb);
        next[ty][tx] = FOX;
        next[fy][fx] = EMPTY;
        if (capRow >= 0) next[capRow][capCol] = EMPTY;
        return next;
    }

    /**
     * Computes reachable cells for a goose at (row,col) using the virtual board.
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
     * Computes reachable cells for the fox at (row,col) using the virtual board.
     * Fox can move to empty neighbors or jump over a goose to an empty cell behind.
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