/**
 * Stores statistics for a single profiled game session.
 */
public class GameProfilerStats {

    public enum Result {
        FOX_WIN, GEESE_WIN, DRAW_TIMEOUT
    }

    private final int    gameNumber;
    private final int    foxMoves;
    private final int    geeseMoves;
    private final long   durationMs;
    private final Result result;

    public GameProfilerStats(int gameNumber, int foxMoves, int geeseMoves,
                             long durationMs, Result result) {
        this.gameNumber  = gameNumber;
        this.foxMoves    = foxMoves;
        this.geeseMoves  = geeseMoves;
        this.durationMs  = durationMs;
        this.result      = result;
    }

    public static String csvHeader() {
        return "game_number,fox_moves,geese_moves,total_moves,duration_ms,FoxWin,GeeseWin,DrawGame";
    }

    public String toCsvRow() {
        int foxWin   = result == Result.FOX_WIN       ? 1 : 0;
        int geeseWin = result == Result.GEESE_WIN     ? 1 : 0;
        int draw     = result == Result.DRAW_TIMEOUT  ? 1 : 0;

        return String.join(",",
                String.valueOf(gameNumber),
                String.valueOf(foxMoves),
                String.valueOf(geeseMoves),
                String.valueOf(foxMoves + geeseMoves),
                String.valueOf(durationMs),
                String.valueOf(foxWin),
                String.valueOf(geeseWin),
                String.valueOf(draw)
        );
    }

    public long   getDurationMs()  { return durationMs;  }
    public Result getResult()      { return result;      }
    public int    getTotalMoves()  { return foxMoves + geeseMoves; }
}