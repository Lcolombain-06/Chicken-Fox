package control;

/**
 * Interface that allows FGController to communicate with MainApp
 * without depending directly on its class.
 *
 * MainApp implements this interface; FGController only knows the interface.
 * This solves the cross-package dependency problem.
 */
public interface GameEndListener {
    void showEndGame(String winnerName);
    void updateCurrentPlayer(String name, boolean isFox);
}