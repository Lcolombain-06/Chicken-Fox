import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.view.View;
import control.ProfilingController;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Profiler — runs N bot-vs-bot games and records statistics to a CSV file.
 *
 * Usage:
 *   java Profiler [nbGames] [maxDurationSeconds] [outputFile]
 *
 * Defaults:
 *   nbGames          = 1000
 *   maxDurationSec   = 15      (game lasting more than this → DRAW_TIMEOUT)
 *   outputFile       = "profiler_results.csv"
 *
 * CSV columns:
 *   game_number, fox_moves, geese_moves, total_moves, duration_ms, duration_s, result
 *
 * Result values: FOX_WIN | GEESE_WIN | DRAW_TIMEOUT
 *
 * Players:
 *   index 0 → Fox  (HoleDecider)
 *   index 1 → Geese (GooseDecider)
 * The fox wins when geese remaining < 4.
 * The geese win when the fox has 0 reachable cells. (like in the simple game)
 */
public class Profiler {

    private static final int    DEFAULT_NB_GAMES       = 1000;
    private static final long   DEFAULT_MAX_DURATION_S = 15;
    private static final String DEFAULT_OUTPUT_FILE    = "profiler_results.csv";

    public static void main(String[] args) {

        int    nbGames      = DEFAULT_NB_GAMES;
        long   maxDurationS = DEFAULT_MAX_DURATION_S;
        String outputFile   = DEFAULT_OUTPUT_FILE;

        if (args.length >= 1) nbGames      = Integer.parseInt(args[0]);
        if (args.length >= 2) maxDurationS = Long.parseLong(args[1]);
        if (args.length >= 3) outputFile   = args[2];

        System.out.printf("=== Profiler ===%n");
        System.out.printf("Games to run      : %d%n",   nbGames);
        System.out.printf("Max duration/game : %d s%n", maxDurationS);
        System.out.printf("Output CSV        : %s%n%n", outputFile);

        List<GameProfilerStats> results = new ArrayList<>(nbGames);

        for (int i = 1; i <= nbGames; i++) {
            GameProfilerStats stats = runGame(i, maxDurationS);
            results.add(stats);

            if (i % 100 == 0 || i == nbGames) {
                System.out.printf("  [%4d/%4d]  %-14s  %d moves  %.2f s%n",
                        i, nbGames,
                        stats.getResult(),
                        stats.getTotalMoves(),
                        stats.getDurationMs() / 1000.0);
            }
        }

        writeCsv(outputFile, results);
    }

    // -----------------------------------------------------------------------
    // Run one game in a dedicated thread with a hard timeout
    // -----------------------------------------------------------------------
    private static GameProfilerStats runGame(int gameNumber, long maxDurationS) {

        // shared counters written by ProfilingController
        int[]     foxMovesRef   = {0}; // must add the default value inside the variable creation, to avoid a crash
        int[]     geeseMovesRef = {0};
        boolean[] stopFlag      = {false};

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<GameProfilerStats.Result> future = executor.submit(() -> {
            try {
                // fresh model: two computer players
                Model model = new Model();
                model.addComputerPlayer("Fox(bot)");    // index 0 → fox
                model.addComputerPlayer("Geese(bot)");  // index 1 → geese

                StageFactory.registerModelAndView(
                        "hole",
                        "model.HoleStageModel",
                        "view.HoleStageView");

                View view = new View(model);

                ProfilingController control = new ProfilingController(
                        model, view, foxMovesRef, geeseMovesRef, stopFlag);

                control.setFirstStageName("hole");
                control.startGame();   // initialises the stage
                control.stageLoop();   // blocks until game over or stopFlag set

                // read winner set by ProfilingController
                int winner = model.getIdWinner();
                if (winner == 0) return GameProfilerStats.Result.FOX_WIN;
                if (winner == 1) return GameProfilerStats.Result.GEESE_WIN;
                return GameProfilerStats.Result.DRAW_TIMEOUT;

            } catch (Exception e) {
                // any crash is treated as a draw so the profiler keeps running (should change that later)
                return GameProfilerStats.Result.DRAW_TIMEOUT;
            }
        });

        GameProfilerStats.Result result;
        try {
            result = future.get(maxDurationS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            stopFlag[0] = true;         // signal the game loop to exit
            future.cancel(true);
            result = GameProfilerStats.Result.DRAW_TIMEOUT;
        } catch (InterruptedException | ExecutionException e) {
            stopFlag[0] = true;
            future.cancel(true);
            result = GameProfilerStats.Result.DRAW_TIMEOUT;
        } finally {
            executor.shutdownNow();
        }

        long duration = System.currentTimeMillis() - startTime;
        return new GameProfilerStats(gameNumber, foxMovesRef[0], geeseMovesRef[0], duration, result);
    }

    // -----------------------------------------------------------------------
    // CSV output
    // -----------------------------------------------------------------------
    private static void writeCsv(String path, List<GameProfilerStats> results) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            pw.println(GameProfilerStats.csvHeader());
            for (GameProfilerStats s : results) pw.println(s.toCsvRow());
            System.out.printf("%nCSV written → %s  (%d rows)%n", path, results.size());
        } catch (IOException e) {
            System.err.println("ERROR writing CSV: " + e.getMessage());
        }
    }

    // perhaps add a method to quickly show in the console the summary of the party
}
