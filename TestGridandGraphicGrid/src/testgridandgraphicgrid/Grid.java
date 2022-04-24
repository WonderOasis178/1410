package testgridandgraphicgrid;

import java.util.*;
import javax.swing.JOptionPane;

/**
 * The Grid class supports the understanding of using existing classes, sending
 * messages, distinguishing between objects and classes, learning sequential
 * control, selection, repetition, functions, argument/parameter
 * associations,and so on.
 *
 * @author Rick Mercer inspired by Rich Pattis' Karel the Robot & Disney's Epcot
 * Center
 */
public class Grid
{
    // class constants avaialable to wherever an instance of this class is constructed

    public final static int NORTH = 0;
    public final static int EAST = 1;
    public final static int SOUTH = 2;
    public final static int WEST = 3;
    public final static int MAX_ROWS = 22;
    public final static int MAX_COLUMNS = 36;

    //--class constant available only to other classes in this folder
    final static char intersectionChar = '.';
    final static char beenThereChar = ' ';
    final static char blockChar = '#';
    final static char thingHereChar = 'O';
    final static char moverOnThingChar = '&';
    final static char moverNorth = '^';
    final static char moverSouth = 'v';
    final static char moverEast = '>';
    final static char moverWest = '<';

//--instance variables
    private Random ranNum;
    private int lastRow;       // the number of the last row
    private int lastCol;       // the number of the last column
    private char[][] rectangle = new char[MAX_ROWS][MAX_COLUMNS];
    private int currentRow;    // The row where the mover is
    private int currentCol;    // The column where the mover is
    private char icon;         // the symbol in the currentRow, currentCol
    private int facing;
    private boolean showPath;       // whether or not the path is shown
    private GraphicGrid myListener; // listener object to be notified when state changes.
    private boolean turningLeft = false;
    private int sleepTime = 500;    // Default sleep time of 500 milliseconds

//--Constructors (there are two)
    /**
     * Construct a totalRows by totalCols Grid object with the mover's start
     * position and direction are fixed by the arguments.
     *
     * @param totalRows The maximum number of rows (Note the first row == 0)
     * @param totalCols The maximum number of columns (Note the first column ==
     * 0)
     * @param startRow The row in which the mover begins
     * @param startCol The column in which the mover begins
     * @param startDirection The direction in which the mover will face. This
     * parameter should be specified as Grid.NORTH, Grid.EAST, Grid.SOUTH, or
     * Grid.WEST. However, you could actually send use an int argument of 0
     * (Grid.NORTH), 1 (Grid.EAST) , 2, or 3 (Grid.WEST), but it's not as clear.
     */
    public Grid(int totalRows,
            int totalCols,
            int startRow,
            int startCol,
            int startDirection)
    {
        // Check the initial position of the mover is within the Grid
        lastRow = totalRows - 1;
        lastCol = totalCols - 1;
        showPath = true;  // Show path when true, when it's false keep the intersection visible
        int r, c;
        for (r = 0; r <= lastRow; r++)
        {
            for (c = 0; c <= lastCol; c++)
            {
                rectangle[r][c] = intersectionChar;
            }
        }

        currentRow = startRow;
        currentCol = startCol;
        facing = startDirection;
        setIcon();
        rectangle[currentRow][currentCol] = icon;
    }

    /**
     * Construct a totalRows by totalCols Grid object that has a border all
     * around it except for one exit placed in a random spot with the mover's
     * start position at some random location inside the Grid object facing a
     * random direction (Grid.NORTH, Grid.SOUTH, Grid.EAST, or Grid.WEST).
     */
    public Grid(int totalRows, int totalCols)
    {
        if (totalRows > MAX_ROWS)
        {
            error("row argument " + totalRows + " too large");
        } else if (totalRows < 1)
        {
            error(totalRows + " rows must be >= 1");
        }
        if (totalCols > MAX_COLUMNS)
        {
            error("column argument " + totalCols + " too large");
        }
        if (totalCols < 1)
        {
            error(totalCols + " columns must be >= 1");
        }

        // Set up a border on the edges with one escape route
        ranNum = new Random();

        showPath = true;  // Show path when true, when it's false 0 keep the intersection visible
        lastRow = totalRows - 1;
        lastCol = totalCols - 1;

        int r, c;
        for (r = 0; r <= lastRow; r++)
        {
            for (c = 0; c <= lastCol; c++)
            {
                rectangle[r][c] = intersectionChar;
            }
        }

        for (c = 0; c <= lastCol; c++)
        {
            rectangle[0][c] = blockChar;         // block first row
            rectangle[lastRow][c] = blockChar;   // blocked last row
        }

        for (r = 0; r <= lastRow; r++)
        {
            rectangle[r][0] = blockChar;        // block first column
            rectangle[r][lastCol] = blockChar;  // block last column
        }

        // Put the mover somewhere in the Grid, but NOT a border
        ranNum = new Random();
        currentRow = Math.abs(ranNum.nextInt()) % (lastRow - 1) + 1;
        currentCol = Math.abs(ranNum.nextInt()) % (lastCol - 1) + 1;

        // Pick a random direction
        int direct = Math.abs(ranNum.nextInt()) % 4;
        if (direct == 0)
        {
            facing = NORTH;
        } else if (direct == 1)
        {
            facing = EAST;
        } else if (direct == 2)
        {
            facing = SOUTH;
        } else
        {
            facing = WEST;
        }

        setIcon();
        rectangle[currentRow][currentCol] = icon;

        // Put one opening on any of the four edges
        if (Math.abs(ranNum.nextInt()) % 2 == 0)
        { // set on top or bottom at any column
            c = Math.abs(ranNum.nextInt()) % lastCol;

            if (c == 0)
            {
                c++;           // avoid upper and lower left corner exits (see below)
            }
            if (c == lastCol)
            {
                c--;           // avoid upper and lower right corner exits (see below)
            }
            if (Math.abs(ranNum.nextInt()) % 2 == 0)
            {
                r = lastRow;  // half the time. on the bottom
            } else
            {
                r = 0;        // the other half, on the top
            }
        } else
        { // set on left or right at any column
            r = Math.abs(ranNum.nextInt()) % lastRow;

            if (r == 0)    // avoid upper right and left corner exits
            {
                r++;
            }
            if (r == lastRow)
            {
                r--;          // avoid  lower left and  lower right exits
            }
            if (Math.abs(ranNum.nextInt()) % 2 == 0)
            {
                c = lastCol;  // half the time in the right column
            } else
            {
                c = 0;        // the other half, put on left
            }
        }
        rectangle[r][c] = intersectionChar;
    }

// -accessors
    /**
     * The row in which this Grid object's mover is currently located.
     *
     * @return the row where the mover currently is. Note: the first row is 0
     */
    public int moverRow()
    {
        return currentRow;
    }

    /**
     * The column in which this Grid object's mover is currently located
     *
     * @return the row where the mover currently is. Note: the first column is 0
     */
    public int moverColumn()
    {
        return currentCol;
    }

    /**
     * Find out how many rows are in this particular Grid object
     *
     * @return the number of rows in this Grid object
     */
    public int getRows()
    { // lastRow is the number of the last row as in 0..lastRow
        // so the total number of rows is one more than that
        return lastRow + 1;
    }

    /**
     * Find out how many columns are in this particular Grid object
     *
     * @return the number of columns in this Grid object
     */
    public int getColumns()
    { // lastCol is the number of the last column as in 0..lastCol
        // so the total number of columns is one more than that
        return lastCol + 1;
    }

    /**
     * Find out if the mover could move one space forward
     *
     * @return true if the mover could currently move forward by one space
     */
    public boolean frontIsClear()
    {
        if (facing == NORTH)
        {
            if (currentRow == 0)
            {
                return false;
            } else if (rectangle[currentRow - 1][currentCol] == blockChar)
            {
                return false;
            } else
            {
                return true;
            }
        } else if (facing == EAST)
        {
            if (currentCol == lastCol)
            {
                return false;
            } else if (rectangle[currentRow][currentCol + 1] == blockChar)
            {
                return false;
            } else
            {
                return true;
            }
        } else if (facing == SOUTH)
        {
            if (currentRow == lastRow)
            {
                return false;
            } else if (rectangle[currentRow + 1][currentCol] == blockChar)
            {
                return false;
            } else
            {
                return true;
            }
        } else // Must be facing West
        {
            if (currentCol == 0)
            {
                return false;
            } else if (rectangle[currentRow][currentCol - 1] == blockChar)
            {
                return false;
            } else
            {
                return true;
            }
        }
    }

    /**
     * Find out if the mover could move to the right with 3 turnLeft()s and a
     * move(1)
     *
     * @return true if the mover could currently move right by one space
     */
    public boolean rightIsClear()
    {
        boolean result = true;

        if (facing == Grid.WEST)
        {
            if ((currentRow == 0)
                    || (rectangle[currentRow - 1][currentCol] == blockChar))
            {
                result = false;
            }
        } else if (facing == Grid.NORTH)
        {
            if ((currentCol == lastCol)
                    || (rectangle[currentRow][currentCol + 1] == blockChar))
            {
                result = false;
            }
        } else if (facing == Grid.EAST)
        {
            if ((currentRow == lastRow)
                    || (rectangle[currentRow + 1][currentCol] == blockChar))
            {
                result = false;
            }
        } else // must be WEST
        {
            if ((currentCol == 0)
                    || (rectangle[currentRow][currentCol - 1] == blockChar))
            {
                result = false;
            }
        }

        return result;
    }

    /**
     * Show the current state of this Grid object
     */
    public String toString()
    {
        int r, c;

        String result = "The Grid:\n";
        for (r = 0; r <= lastRow; r++)
        {
            for (c = 0; c <= lastCol; c++)
            {
                result += (char) rectangle[r][c] + " ";
            }
            result += "\n";
        }
        return result;
    }

// -modifiers
    /**
     * The mover will be facing 90 degrees to the left.
     */
    public void turnLeft()
    {
        if (facing == NORTH)
        {
            facing = WEST;
        } else if (facing == EAST)
        {
            facing = NORTH;
        } else if (facing == SOUTH)
        {
            facing = EAST;
        } else // must be facing west
        {
            facing = SOUTH;
        }

        setIcon();
        rectangle[currentRow][currentCol] = icon;
        updateState();
    }

    // Only called from
    private void setIcon()
    {
        if (rectangle[currentRow][currentCol] == moverOnThingChar)
        {
            if (facing == NORTH)
            {
                icon = moverNorth;
            } else if (facing == EAST)
            {
                icon = moverEast;
            } else if (facing == SOUTH)
            {
                icon = moverSouth;
            } else // must be west
            {
                icon = moverWest;
            }
        } else
        {
            if (facing == NORTH)
            {
                icon = moverNorth;
            } else if (facing == EAST)
            {
                icon = moverEast;
            } else if (facing == SOUTH)
            {
                icon = moverSouth;
            } else // must be west
            {
                icon = moverWest;
            }
        }
    }

    public void move()
    {
        move(1);
    }

    /**
     * The mover will move spaces spaces forward if possible. If this is not
     * possible, the program will be terminated with an appropriate message.
     *
     * @param spaces The number of spaces the mover should move forward.
     */
    private void move(int spaces)
    {
        int oldRow = currentRow;
        int oldCol = currentCol;

        if (facing == NORTH)
        {
            currentRow -= spaces;
        } else if (facing == EAST)
        {
            currentCol += spaces;
        } else if (facing == SOUTH)
        {
            currentRow += spaces;
        } else // must be west
        {
            currentCol -= spaces;
        }

        // Fix the intersection that is about to be moved away from
        if (rectangle[oldRow][oldCol] == moverOnThingChar)
        {
            rectangle[oldRow][oldCol] = thingHereChar;
        } else if ((rectangle[oldRow][oldCol] == icon) && showPath)
        {
            rectangle[oldRow][oldCol] = beenThereChar;
        } else
        {
            rectangle[oldRow][oldCol] = intersectionChar;
        }

        int r, c;
        if (facing == NORTH)
        {
            for (r = oldRow; r > currentRow; r--)
            {
                if (r <= 0)
                {
                    if ((rectangle[r][currentCol] != thingHereChar) && showPath)
                    {
                        rectangle[r][currentCol] = beenThereChar;
                    }
                    error("Fell off the NORTH edge");
                }

                checkForBlock(r - 1, currentCol);

                if ((rectangle[r][currentCol] != thingHereChar) && showPath)
                {
                    rectangle[r][currentCol] = beenThereChar;
                }
            }
        } else if (facing == EAST)
        {
            for (c = oldCol; c < currentCol; c++)
            {
                checkForBlock(currentRow, c + 1);
                if (c >= lastCol)
                {
                    if ((rectangle[currentRow][c] != thingHereChar) && showPath)
                    {
                        rectangle[currentRow][c] = beenThereChar;
                    }
                    error("Fell off the EAST edge");
                }
                if ((rectangle[currentRow][c] != thingHereChar) && showPath)
                {
                    rectangle[currentRow][c] = beenThereChar;
                }
            }
        }
        if (facing == SOUTH)
        {
            for (r = oldRow; r < currentRow; r++)
            {
                checkForBlock(r + 1, currentCol);
                if (r >= lastRow)
                {
                    if ((rectangle[r][currentCol] != thingHereChar) && showPath)
                    {
                        rectangle[r][currentCol] = beenThereChar;
                    }
                    error("Fell off the SOUTH edge");
                }
                if ((rectangle[r][currentCol] != thingHereChar) && showPath)
                {
                    rectangle[r][currentCol] = beenThereChar;
                }
            }
        } else
        { // Direction Must be WEST
            for (c = oldCol; c > currentCol; c--)
            {
                if (c <= 0)
                {
                    if ((rectangle[currentRow][c] != thingHereChar) && showPath)
                    {
                        rectangle[currentRow][c] = beenThereChar;
                    }
                    error("Fell off the WEST edge");
                }

                checkForBlock(currentRow, c - 1);

                if ((rectangle[currentRow][c] != thingHereChar) && showPath)
                {
                    rectangle[currentRow][c] = beenThereChar;
                }
            }
        }

        if (rectangle[currentRow][currentCol] == thingHereChar)
        {
            rectangle[currentRow][currentCol] = moverOnThingChar;
        } else
        {
            rectangle[currentRow][currentCol] = icon;
        }

        updateState();
    }

    /**
     * Place a block on an intersection intersection (blockRow, blockCol). The
     * mover will not be allowed to move into this intersection. If blockRow or
     * blockCol are not within the Grid, or the intersection is already blocked,
     * the program terminates.
     *
     * @param blockRow the row in which the block will be placed (if possible).
     * @param blockCol the column in which the block will be placed (if
     * possible).
     */
    public void block(int blockRow, int blockCol)
    {
        if (blockRow > lastRow
                || blockRow < 0
                || blockCol > lastCol
                || blockCol < 0)
        {
            error("Can't block intersection at Grid(" + blockRow + ", " + blockCol + ")");
        }

        // Can't block the place where the a block has been placed
        if (rectangle[blockRow][blockCol] == blockChar)
        {
            error("Can't block intersection that is already blocked at (" + blockRow + ", " + blockCol + ")");
        }

        // Can't block the place where the a block has been placed
        if (rectangle[blockRow][blockCol] == thingHereChar
                || rectangle[blockRow][blockCol] == moverOnThingChar)
        {
            error("Can't block intersection with a thing put down at(" + blockRow + ", " + blockCol + ")");
        }

        if (rectangle[blockRow][blockCol] == icon)
        {
            error("Can't block where the mover is at Grid(" + blockRow + ", " + blockCol + ")");
        }

        // Can block the specified row and column
        rectangle[blockRow][blockCol] = blockChar;
        updateState();
    }

    /**
     * Put down a thing on the Grid where the mover is currently located. If it
     * is blocked, or if there is a thing there already, the program terminates.
     * This method pressumes that the mover can never move into a blocked
     * intersection or off the edge. both of which are check in the move method
     */
    public void putDown()
    { // All the requred work is in the other putDown method
        putDown(currentRow, currentCol);
    }

    /**
     * Place a thing on the intersection (blockRow, blockCol). If blockRow or
     * blockCol are not within the Grid, the program will terminate with an
     * appropriate message.
     *
     * @param blockRow the row in which the block will be placed (if possible).
     * @param blockCol the column in which the block will be placed (if
     * possible).
     */
    public void putDown(int putDownRow, int putDownCol)
    {
        if (putDownRow > lastRow
                || putDownRow < 0
                || putDownCol > lastCol
                || putDownCol < 0)
        {
            error("Can't block intersection at Grid(" + putDownRow + ", " + putDownCol + ")");
        }

        if (rectangle[putDownRow][putDownCol] == thingHereChar
                || rectangle[putDownRow][putDownCol] == moverOnThingChar
                || rectangle[putDownRow][putDownCol] == blockChar)
        {
            error("This intersection has a thing or it has been blocked already(" + putDownRow + ", " + putDownCol + ")");
        }

        if (rectangle[putDownRow][putDownCol] == icon)
        {
            rectangle[putDownRow][putDownCol] = moverOnThingChar;
        } else
        {
            rectangle[putDownRow][putDownCol] = thingHereChar;
        }

        updateState();
    }

    /**
     * Pick up a thing from the Grid where the mover is currently located
     */
    public void pickUp()
    {
        if (rectangle[currentRow][currentCol] != thingHereChar
                && rectangle[currentRow][currentCol] != moverOnThingChar)
        {
            error("Attempt to pick up when nothing is at Grid(" + currentRow + ", " + currentCol + ")");
        }

        rectangle[currentRow][currentCol] = icon;
        updateState();
    }

    /**
     * Change the state of this Grid object to either show the path taken by the
     * mover--if not currently shown, or to *not* show the path--if currently
     * shown.
     */
    public void toggleShowPath()
    {
        showPath = !showPath;
    }

    private void error(String message)
    {
        System.out.println("\nERROR** " + message + "\n");
        System.out.println(this.toString());
        JOptionPane.showMessageDialog(null, message + "\nProgram will terminate");
        System.exit(0);
    }

    private void checkForBlock(int r, int c)
    {
        if (rectangle[r][c] == blockChar)
        {
            if (facing == NORTH)     // must be moving NORTH
            {
                rectangle[r + 1][c] = icon;
            } else if (facing == EAST)  // must be moving EAST
            {
                rectangle[r][c - 1] = icon;
            } else if (facing == SOUTH) // must be moving SOUTH
            {
                rectangle[r - 1][c] = icon;
            } else if (facing == WEST)  //  must be moving WEST
            {
                rectangle[r][c + 1] = icon;
            }
            error("Attempt to move through the block at Grid(" + r + ", " + c + ")");
        }
    }

// The following methods were added by Andrew Wilt to make his GraphicGrid class work
    /**
     * This method sets the listener that will be notified whenever the state of
     * this particular Grid object has been changed. This can be used for
     * displaying a Grid object in a graphical manner.
     *
     * @param Grid The Listener object.
     */
    public void setGridListener(GraphicGrid listener)
    {  // Andy Wilt
        if (listener != null)
        {
            myListener = listener;
        }
        updateState();
    }

    private void updateState()
    { // Andy Wilt
        if (myListener == null)
        {
            return;
        }

        char[][] newState = cloneArray();
        myListener.stateChanged(newState);

        try
        {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e)
        {
        }
    }

    /**
     * Changes the amount of time that the Grid sleeps between moves.
     *
     * @param int time
     */
    public void setSleepTime(int time)
    { // Andy Wilt
        sleepTime = time;
    }

    private char[][] cloneArray()
    { // Andy Wilt
        char[][] temp = (char[][]) rectangle.clone();
        for (int j = 0; j <= lastRow; j++)
        {
            temp[j] = (char[]) temp[j].clone();
        }
        return temp;
    }

} // end class Grid
