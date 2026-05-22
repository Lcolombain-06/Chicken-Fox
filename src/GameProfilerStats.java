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
        return "game_number,fox_moves,geese_moves,total_moves,duration_ms,duration_s,result";
    }

    public String toCsvRow() {
        return String.join(",",
                String.valueOf(gameNumber),
                String.valueOf(foxMoves),
                String.valueOf(geeseMoves),
                String.valueOf(foxMoves + geeseMoves),
                String.valueOf(durationMs),
                String.format("%.3f", durationMs / 1000.0),
                result.name()
        );
    }

    public long   getDurationMs()  { return durationMs;  }
    public Result getResult()      { return result;      }
    public int    getTotalMoves()  { return foxMoves + geeseMoves; }
}
