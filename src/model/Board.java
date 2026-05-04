package model;

import boardifier.control.Logger;
import boardifier.model.GameStageModel;
import boardifier.model.ContainerElement;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;

/**
 * Main board represent the element where pawns are put when played
 * Thus, a simple ContainerElement with 7 rows and 7 column is needed.
 * However, not all cell are reachable, since the board have a "plus" shape.
 * Nevertheless, in order to "simplify" the work for the controller part,
 * this class also contains method to determine all the valid cells to put a
 * pawn with a given value.
 */
public class Board extends ContainerElement {
    private Cell[][] cells;

    public Board(int x, int y, GameStageModel gameStageModel) {
        // call the super-constructor to create a 7x7 grid, named "board", and in x,y in space.
        super("board", x, y, 7 , 7, gameStageModel);
        initBoard();
    }

    // return a cell of the board (usefull for the movements of the pawns, they can check neighbors).
    public Cell getCell(int x, int y) {
        return this.cells[x][y];
    }

    public void setValidCells(int number) {
        resetReachableCells(false);
        List<Point> valid = computeValidCells(number);
        if (valid != null) {
            for(Point p : valid) {
                reachableCells[p.y][p.x] = true;
            }
        }
    }

    // For now, determine all the accesible cell of the board.
    public List<Point> computeValidCells(int number) {
        List<Point> lst = new ArrayList<>();

        for(int y = 0; y < 7; y++) {
            for(int x = 0; x < 7; x++) {

                Cell c = cells[y][x];

                if (!c.isAccessible()) continue;

                // all accesible cells (using the boolean value of the Cell class)
                lst.add(new Point(x, y));
            }
        }

        return lst;
    }


    // initialise the board with all corner remove (to form the "plus" shape).
    private void initBoard() {
        cases = new Case[7][7];

        for(int y = 0; y < 7; y++) {
            for(int x = 0; x < 7; x++) {

                boolean accessible = true;

                // forbiden zone (corners)
                if ((x < 2 || x > 4) && (y < 2 || y > 4)) {
                    accessible = false;
                }

                cases[y][x] = new Case(x, y, accessible);
            }
        }

        initNeighbors();
    }

    private void initNeighbors() {
        int[][] orthogonal = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] diagonal = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};

        for(int y = 0; y < 7 ) {
            for (int x = 0; x < 7; x++) {
                Cell c = cells[y][x];

                if (!c.isAccessible()) continue;

                // orthogonal neighbors
                for (int[] d : orthogonal) {
                    int nx = x + d[0];
                    int ny = y + d[1];

                    if(cells[ny][nx].isAccessible()) {
                        c.addNeighbor(cells[ny][nx]);
                    }
                }

                // diagonal (for even cases)
                if ((x + y) % 2 == 0) {
                    for (int[] d : diagonal) {
                        int nx = x + d[0];
                        int ny = y + d[1];

                        if(cells[ny][nx].isAccessible()) {
                            c.addNeighbor(cells[ny][nx]);
                        }
                    }
                }
            }
        }
    }
}
