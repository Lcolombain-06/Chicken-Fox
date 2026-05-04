import java.util.ArrayList;

public class Cell {
    private int x;
    private int y;
    private boolean accessible:
    private ArrayList<Case> neighbors;
    private Pawn pawn;
    private boolean empty;

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


    public boolean isOccupied(){
        return this.pawn != null;
    }

    public Pawn getPawn() {
        return this.getPawn;
    }

    public void setPawn(Pawn pawn) {
        this.pawn = pawn;
    }

}


    // Faire les fonctions ;  isAccesible, isOccupied, isEmpty