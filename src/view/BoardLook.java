package view;

import boardifier.view.ContainerLook;
import model.Board;
import model.Cell;

public class BoardLook extends ContainerLook {
    private Board board;
    private static final char[] COO = {'A', 'B', 'C', 'D', 'E', 'F', 'G'};

    public BoardLook(Board board) {
        super(board, 2, 4, 1, 1, 2);
        this.board = board;
    }

    @Override
    protected void render() {
        setSize(getWidth(), getHeight());
        clearShape();

        for (int row = 0; row < nbRows; row++)
            for (int col = 0; col < nbCols; col++)
                renderCell(row, col);

        renderInners();
    }

    private void renderCell(int row, int col) {
        Cell cell = board.getCell(col, row);
        if (!cell.isAccessible()) return;

        int centerY = row * rowHeight + 1;
        int centerX = col * colWidth + 2;

        shape[14][centerX] = "" + (col + 1);
        shape[centerY][0] = "" + COO[row];
        shape[centerY][centerX] = "+";

        for (Cell neighbor : cell.getNeighbors())
            renderNeighborLink(centerY, centerX, neighbor, row, col);
    }

    private void renderNeighborLink(int cy, int cx, Cell neighbor, int row, int col) {
        int dRow = neighbor.getY() - row;
        int dCol = neighbor.getX() - col;

        if      (dRow == -1 && dCol ==  0) shape[cy - 1][cx]     = "|";
        else if (dRow ==  0 && dCol ==  1) { shape[cy][cx + 1] = "-"; shape[cy][cx + 2] = "-"; }
        else if (dRow ==  0 && dCol == -1) shape[cy][cx - 1]     = "-";
        else if (dRow == -1 && dCol == -1) shape[cy - 1][cx - 2] = "\\";
        else if (dRow == -1 && dCol ==  1) shape[cy - 1][cx + 2] = "/";
    }
}