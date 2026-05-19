package model;

import boardifier.control.Logger;
import boardifier.model.GameElement;
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
        return this.cells[y][x];
    }

    public int setValidCells(Pawn pawn, int row, int col){
        int nbrValidCells = 0;

        // Set every Cell at false
        for (int r=0; r<7; ++r){
            for (int c=0; c<7; ++c){
                reachableCells[r][c] = false;
            }
        }

        Cell current = cells[row][col];
        if (pawn.isFox()){
            for (Cell neighbor : current.getNeighbors()){
                int neighborX = neighbor.getX();
                int neighborY = neighbor.getY();

                // Is the Cell free ?
                if (getElement(neighborY, neighborX) == null){
                    reachableCells[neighborY][neighborX] = true;
                    nbrValidCells += 1;
                }

                // It is occupied so we check behind this cell
                else {

                    int jumpX = neighborX + (neighborX - col);
                    int jumpY = neighborY + (neighborY - row);

                    if (jumpX < 7 && jumpX >= 0 && jumpY >=0 && jumpY < 7){
                        Cell jumpToCell = cells[jumpY][jumpX];
                        if (jumpToCell.isAccessible() && getElement(jumpY,jumpX) == null){
                            reachableCells[jumpY][jumpX] = true;
                            nbrValidCells += 1;
                        }
                    }
                }

            }
        }
        else {
            // Geese,
            for (Cell neighbor : current.getNeighbors()) {
                int nx = neighbor.getX();
                int ny = neighbor.getY();
                // forbidden the movement in down direction
                if (ny <= row && (nx == col || ny == row) && getElement(ny, nx) == null) {
                    reachableCells[ny][nx] = true;
                }

            }
        }

        return nbrValidCells;
    }



    // initialise the board with all corner remove (to form the "plus" shape).
    private void initBoard() {
        cells = new Cell[7][7];

        for(int y = 0; y < 7; y++) {
            for(int x = 0; x < 7; x++) {

                boolean accessible = true;

                // forbiden zone (corners)
                if ((x < 2 || x > 4) && (y < 2 || y > 4)) {
                    accessible = false;
                }

                cells[y][x] = new Cell(x, y, accessible);
            }
        }

        initNeighbors();
    }

    private void initNeighbors() {
        int[][] orthogonal = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] diagonal = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};

        for(int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                Cell c = cells[y][x];

                if (!c.isAccessible()) continue;

                // orthogonal neighbors
                for (int[] d : orthogonal) {
                    int nx = x + d[0];
                    int ny = y + d[1];
                    //ajout verification des limites
                    if (nx < 0 || ny < 0 || nx >= 7 || ny >= 7) continue;
                    if(cells[ny][nx].isAccessible()) {
                        c.addNeighbors(cells[ny][nx]);
                    }
                }

                // diagonal (for even cases)
                if ((x + y) % 2 == 0) {
                    for (int[] d : diagonal) {
                        int nx = x + d[0];
                        int ny = y + d[1];

                        if (nx < 0 || ny < 0 || nx >= 7 || ny >= 7) continue;

                        if (cells[ny][nx].isAccessible()) {
                            c.addNeighbors(cells[ny][nx]);
                        }
                    }
                }
            }
        }
    }
}
