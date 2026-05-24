package model;

import org.junit.jupiter.api.BeforeEach;

public class CellTest {

    private Cell accessibleCell;
    private Cell inaccessibleCell;

    @BeforeEach
    void setUp() {
        accessibleCell   = new Cell(3, 3, true);
        inaccessibleCell = new Cell(0, 0, false);
    }

    // -- Tests on coordinates
}
