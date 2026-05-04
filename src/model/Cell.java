import java.util.ArrayList;

public class Cell {
    private int x;
    private int y;
    private boolean accessible:
    private ArrayList<Case> neighbors;

    public Cell (int x, int y, boolean accessible) {
        this.x = x;
        this.y = y;
        this.accessible = accessible;
        this.neighbors = new ArrayList<>();
    }

    // Methods to get the coordinate of the cell.
    public int getX() {
        return this.x;
    }

    public int getX() {
        return this.y;
    }

    // method to get the accesibility of the cell.
    public boolean isAccessible() {
        return this.accessible;
    }

    // method to change accesibility of the cell (for those in the corner of the grid, or if a pawn is on it).
    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    // return the list of accessible case from this one.
    public ArrayList<Case> getNeighbors() {
        return this.neighbors;
    }

    public void addNeighbors(Cell c) {
        this.neighbors.add(c);
    }
}