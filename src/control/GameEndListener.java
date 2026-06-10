package control;

/**
 * Interface permettant à FGController de communiquer avec MainApp
 * sans dépendre directement de sa classe.
 *
 * MainApp implémente cette interface, FGController ne connaît que l'interface.
 * Cela résout le problème de dépendance entre packages.
 */
public interface GameEndListener {
    void showEndGame(String winnerName);
    void updateCurrentPlayer(String name, boolean isFox);
}
