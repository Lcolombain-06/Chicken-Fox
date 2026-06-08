import control.FGController;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ClickHandler implements EventHandler<MouseEvent> {

    // !! Mêmes constantes que BoardRenderer et DebugGrid !!
    private static final int MARGIN_LEFT   = 6;
    private static final int MARGIN_TOP    = 5;
    private static final int BOARD_PIXEL_W = 468;
    private static final int BOARD_PIXEL_H = 468;
    private static final double CELL_W = BOARD_PIXEL_W / 7.0;
    private static final double CELL_H = BOARD_PIXEL_H / 7.0;

    // Facteur d'échelle : taille fenêtre / taille originale du PNG (480)
    private final double scale;

    // État du clic pour les oies (2 clics nécessaires)
    private int firstRow = -1;
    private int firstCol = -1;
    private boolean waitingForDestination = false;

    private final FGController controller;
    private final BoardRender renderer;

    public ClickHandler(int windowWidth, int windowHeight, FGController controller, BoardRender renderer) {
        this.scale      = windowWidth / 480.0;
        this.controller = controller;
        this.renderer   = renderer;
    }

    @Override
    public void handle(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;

        double px = event.getX();
        double py = event.getY();

        // Annuler le scale avant de convertir en cellule
        double unscaledX = px / scale;
        double unscaledY = py / scale;

        int col = (int)((unscaledX - MARGIN_LEFT) / CELL_W);
        int row = (int)((unscaledY - MARGIN_TOP)  / CELL_H);

        if (col < 0 || col > 6 || row < 0 || row > 6) {
            System.out.println("Clic hors du plateau ignoré.");
            return;
        }

        char rowChar = (char)('A' + row);
        char colChar = (char)('1' + col);

        System.out.println("Clic pixel (" + px + "," + py + ") --> cellule [" + row + "," + col + "] --> " + rowChar + colChar);

        int currentPlayer = controller.getModel().getIdPlayer();

        if (currentPlayer == 0) {
            handleFoxClick(rowChar, colChar);
        } else {
            handleGooseClick(row, col, rowChar, colChar);
        }
    }

    private void handleFoxClick(char rowChar, char colChar) {
        String command = "" + rowChar + colChar;
        System.out.println("Commande renard --> \"" + command + "\"");
        boolean ok =  controller.analyseAndPlay(command);
        if(ok) {
            controller.endOfTurn();
            renderer.refresh();
        }
    }

    private void handleGooseClick(int row, int col, char rowChar, char colChar) {
        if (!waitingForDestination) {
            firstRow = row;
            firstCol = col;
            waitingForDestination = true;
            System.out.println("Oie sélectionnée en" + rowChar + colChar + ": cliquez la destination.%n");
        } else {
            char fromRow = (char)('A' + firstRow);
            char fromCol = (char)('1' + firstCol);
            String command = "" + fromRow + fromCol + rowChar + colChar;
            System.out.println("Commande oie --> \"" + command + "\"");
            boolean ok = controller.analyseAndPlay(command);
            if(ok) {
                controller.endOfTurn();
                renderer.refresh();
            }
            waitingForDestination = false;
            firstRow = -1;
            firstCol = -1;
        }
    }

    //Réinitialise la sélection en cours (utile en fin de tour).
    public void reset() {
        waitingForDestination = false;
        firstRow = -1;
        firstCol = -1;
    }
}