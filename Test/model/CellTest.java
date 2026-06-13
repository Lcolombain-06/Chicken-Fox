package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CellTest {

    private Cell accessibleCell;
    private Cell inaccessibleCell;

    @BeforeEach
    void setUp() {
        accessibleCell   = new Cell(3, 3, true);
        inaccessibleCell = new Cell(0, 0, false);
    }

    // -- Tests on coordinates

    @Test
    void getXReturnsCorrectValue() {
        assertEquals(3, accessibleCell.getX(), "getX() must return the column value");
    }

    @Test
    void getYReturnsCorrectValue() {
        assertEquals(3, accessibleCell.getY(), "getY() must return the row value");
    }

    @Test
    void inaccessibleCellCoordinatesAreCorrect() {
        assertEquals(0, inaccessibleCell.getX());
        assertEquals(0, inaccessibleCell.getY());
    }

    // -- Test on the cells accessibility

    @Test
    void accessibleCellIsAccessible(){
        assertTrue(accessibleCell.isAccessible(), "A cell created with accessible=true must be accessible");
    }

    @Test
    void inaccessibleCellIsNotAccessible() {
        assertFalse(inaccessibleCell.isAccessible(), "A cell created with accessible=false must not be accessible");
    }

    // Tests on neighbors

    @Test
    void newCellHasNoNeighbors(){
        assertTrue(accessibleCell.getNeighbors().isEmpty(), "A new cell just created must have no neighbors yet");
    }

    @Test
    void addNeighborIncreasesNeighborNumber(){
        Cell neighbor = new Cell(3,4,true);
        accessibleCell.addNeighbors(neighbor);
        assertEquals(1,accessibleCell.getNeighbors().size(), "After adding one neighbor, the size must be 1");
    }

    @Test
    void addedNeighborIsInList(){
        Cell neighbor = new Cell(3,4,true);
        accessibleCell.addNeighbors(neighbor);
        assertTrue(accessibleCell.getNeighbors().contains(neighbor), "The added neighbor must be present in the list");
    }

    @Test
    void multipleNeighborsCanBeAdded() {
        Cell n1 = new Cell(2, 3, true);
        Cell n2 = new Cell(4, 3, true);
        Cell n3 = new Cell(3, 2, true);
        accessibleCell.addNeighbors(n1);
        accessibleCell.addNeighbors(n2);
        accessibleCell.addNeighbors(n3);
        assertEquals(3, accessibleCell.getNeighbors().size(),
                "Three neighbors must have been added");
    }
}