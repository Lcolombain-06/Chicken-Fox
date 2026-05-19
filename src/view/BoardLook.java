package view;

import boardifier.view.ContainerLook;
import model.Board;
import model.Cell;

public class BoardLook extends ContainerLook {
    private Board board;

    public BoardLook(Board board) {
        super(board, 2, 4, 1, 1, 2);
        this.board = board;
    }

    @Override
    protected void render() {
        setSize(getWidth(), getHeight());
        clearShape();

        char[] coo = {'A', 'B', 'C', 'D', 'E', 'F', 'G'};

        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                Cell cell = board.getCell(row, col);

                if (!cell.isAccessible()) continue;

                // positions into the cell
                int top = row * rowHeight;
                int left = col * colWidth;

                int centerY = top + 1;
                int centerX = left + 2;


                shape[0][centerX] = "" + (col + 1);
                shape[centerY][0] = "" + coo[row];

                // center of this cell
                shape[centerY][centerX] = "+";

                // Neighbors
                for (Cell neighbor : cell.getNeighbors()) {

                    int dRow = neighbor.getX() - row;
                    int dCol = neighbor.getY() - col;

                    // up
                    if (dRow == -1 && dCol == 0) {
                        shape[centerY - 1][centerX] = "|";
                    }

                    // right
                    else if (dRow == 0 && dCol == 1) {
                        shape[centerY][centerX + 1] = "-";
                        shape[centerY][centerX + 2] = "-";

                    }

                    // left
                    else if (dRow == 0 && dCol == -1) {
                        shape[centerY][centerX - 1] = "-";
                    }

                    // diagonale haut-gauche
                    else if (dRow == -1 && dCol == -1) {
                        shape[centerY - 1][centerX - 2] = "\\";
                    }

                    // diagonale haut-droite
                    else if (dRow == -1 && dCol == 1) {
                        shape[centerY - 1][centerX + 2] = "/";
                    }
                }
            }
        }

        // Debug test
        /**
         for (int i = 0; i < nbRows; i++) {
         for (int j = 0; j < nbCols; j++) {
         System.out.println("cell ["+i+"]["+j+"] has " + grid[i][j].size() + " looks");
         }
         }**/
        renderInners();
    }
}