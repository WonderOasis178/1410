package testgridandgraphicgrid;

/**
 * GraphicGrid is a companion to the Grid class by Rick Mercer. It displays a
 * Grid graphically in a Window. Changes to the Grid object can be seen in the
 * window. A program that has been written to use a Grid can be adapted very
 * easily to use the GraphicGrid. Example: ... Grid someGrid = new
 * Grid(5,8,3,4,Grid.WEST); // The Grid object constructed originally // Make a
 * GraphicGrid object sending the Grid object as a parameter. GraphicGrid
 * visibleGrid = new GraphicGrid(someGrid); ... This last line will start the
 * Window that displays the Grid someGrid. Any changes to someGrid such as
 * someGrid.move(), or someGrid.turnLeft() will be shown in the window.
 *
 * If an error occurs, a modal dialog message box pops up to inform you that the
 * program will terminate
 *
 * @author Andrew Wilt
 */
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class GraphicGrid extends JFrame
{

    public static void main(String[] args)
    {
        // When run, you can play with graphic grids
        new GraphicGrid();
    }

    /**
     * The Frame that holds the Grid to be displayed graphically The size of the
     * Window is set according to the size of the Grid.
     *
     * @param Grid The Grid object that is going to be sending stateChanges.
     */
    public GraphicGrid(Grid g)
    {
        super("Watch the actions of the grid");
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        myGrid = g;
        setSize(g.getColumns() * spotWidth + spacing, g.getRows() * spotHeight + spacing);
        myPanel = new GraphicGridPanel(g, spotWidth, spotHeight);
        myGrid.setGridListener(this);
        JSlider speedBar = new JSlider(JSlider.HORIZONTAL);
        speedBar.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting())
                {
                    myGrid.setSleepTime((int) source.getValue() * 10);
                }
            }
        });
        Container contentPane = getContentPane();
        contentPane.add(myPanel, "Center");
        contentPane.add(speedBar, "South");
        this.show();
    }

    public GraphicGrid()
    {
        super("Interactive Grid");
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        numRows = defaultNRows;
        numCols = defaultNColumns;
        myGrid = new Grid(defaultNRows, defaultNColumns, 0, 0, Grid.EAST);
        myPanel = new GraphicGridPanel(myGrid, spotWidth, spotHeight);
        myGrid.setGridListener(this);
        controls = makeButtons();
        Dimension gridSize = myPanel.getSize();
        Dimension controlSize = controls.getSize();
        setSize(new Dimension(Math.max(gridSize.width, controlSize.width) + 30, (gridSize.height + controlSize.height)));
        Container contentPane = getContentPane();

        contentPane.add(myPanel, "Center");
        contentPane.add(controls, "South");
        this.show();
    }

    /**
     * Called by the Grid object whenever the state changes.
     *
     * @param char[][] 2D Array of chars that represents the Grid.
     */
    public void stateChanged(char[][] rect)
    {
        myPanel.stateChanged(rect);
    }

    private JPanel makeButtons()
    {
        JPanel gridControls = new JPanel();
        gridControls.setOpaque(true);
        gridControls.setLayout(new BoxLayout(gridControls, BoxLayout.Y_AXIS));
        MyTextFieldListener textListener = new MyTextFieldListener();

        // First row of objects
        JPanel firstRow = new JPanel();
        firstRow.setOpaque(true);
        JButton newGrid = new JButton("newGrid");
        final GraphicGrid outside = this;
        newGrid.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (moverRowNum < 0 || moverColNum < 0)
                {
                    myGrid = new Grid(numRows, numCols);
                } else
                {
                    myGrid = new Grid(numRows, numCols, moverRowNum, moverColNum, currentDirection);
                }
                myPanel.setGrid(myGrid);
                myGrid.setGridListener(outside);
                frontIsClearLabel.setText(String.valueOf(myGrid.frontIsClear()));
                Dimension gridSize = myPanel.getSize();
                Dimension controlSize = controls.getSize();
                setSize(new Dimension(Math.max(gridSize.width, controlSize.width), (gridSize.height + controlSize.height)));
                repaint();
            }
        });
        firstRow.add(newGrid);

        firstRow.add(new JLabel("nRows", SwingConstants.RIGHT));

        JTextField nRows = new JTextField(textFieldWidth);
        nRows.setText(String.valueOf(defaultNRows));
        nRows.getDocument().addDocumentListener(textListener);
        nRows.getDocument().putProperty("name", "nRows");
        firstRow.add(nRows);

        firstRow.add(new JLabel("nCols", SwingConstants.RIGHT));

        JTextField nCols = new JTextField(textFieldWidth);
        nCols.setText(String.valueOf(defaultNColumns));
        nCols.getDocument().addDocumentListener(textListener);
        nCols.getDocument().putProperty("name", "nCols");
        firstRow.add(nCols);
        gridControls.add(firstRow);

        // Second row of objects
        JPanel secondRow = new JPanel();
        secondRow.setOpaque(true);
        secondRow.add(new JLabel("moverRow", SwingConstants.RIGHT));

        JTextField moverRow = new JTextField(textFieldWidth);
        moverRow.setText(String.valueOf(0));
        moverRow.getDocument().addDocumentListener(textListener);
        moverRow.getDocument().putProperty("name", "moverRow");
        secondRow.add(moverRow);

        secondRow.add(new JLabel("moverCol", SwingConstants.RIGHT));

        JTextField moverCol = new JTextField(textFieldWidth);
        moverCol.setText(String.valueOf(0));
        moverCol.getDocument().addDocumentListener(textListener);
        moverCol.getDocument().putProperty("name", "moverCol");
        secondRow.add(moverCol);

        secondRow.add(new JLabel("moverDirection", SwingConstants.RIGHT));

        String[] dirList =
        {
            "NORTH", "SOUTH", "EAST", "WEST"
        };
        JComboBox moverDirection = new JComboBox(dirList);
        moverDirection.setSelectedIndex(2);

        moverDirection.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                currentDirection
                        = getDirection((String) ((JComboBox) e.getSource()).getSelectedItem());
            }
        });

        secondRow.add(moverDirection);
        gridControls.add(secondRow);
        gridControls.add(Box.createRigidArea(new Dimension(0, 5)));

        // Fourth row of objects
        JPanel fourthRow = new JPanel();
        fourthRow.setOpaque(true);
        JButton block = new JButton("block");
        block.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                myGrid.block(blockRowNum, blockColNum);
            }
        });
        fourthRow.add(block);

        fourthRow.add(new JLabel("row", SwingConstants.RIGHT));

        JTextField blockRow = new JTextField(textFieldWidth);
        blockRow.setText(String.valueOf(blockRowNum));
        blockRow.getDocument().addDocumentListener(textListener);
        blockRow.getDocument().putProperty("name", "blockRow");
        fourthRow.add(blockRow);

        fourthRow.add(new JLabel("cols", SwingConstants.RIGHT));

        JTextField blockCol = new JTextField(textFieldWidth);
        blockCol.setText(String.valueOf(blockColNum));
        blockCol.getDocument().addDocumentListener(textListener);
        blockCol.getDocument().putProperty("name", "blockCol");
        fourthRow.add(blockCol);

        JButton putDown = new JButton("putDown");
        putDown.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                myGrid.putDown(putDownRowNum, putDownColNum);
            }
        });
        fourthRow.add(putDown);

        fourthRow.add(new JLabel("row", SwingConstants.RIGHT));

        JTextField putDownRow = new JTextField(textFieldWidth);
        putDownRow.setText(String.valueOf(putDownRowNum));
        putDownRow.getDocument().addDocumentListener(textListener);
        putDownRow.getDocument().putProperty("name", "putDownRow");
        fourthRow.add(putDownRow);

        fourthRow.add(new JLabel("cols", SwingConstants.RIGHT));

        JTextField putDownCol = new JTextField(textFieldWidth);
        putDownCol.setText(String.valueOf(putDownColNum));
        putDownCol.getDocument().addDocumentListener(textListener);
        putDownCol.getDocument().putProperty("name", "putDownCol");
        fourthRow.add(putDownCol);
        gridControls.add(fourthRow);

        // Fifth row of objects
        JPanel fifthRow = new JPanel();
        fifthRow.setOpaque(true);
        JButton move = new JButton("move");
        move.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                myGrid.move();
            }
        });
        fifthRow.add(move);

        JButton turnLeft = new JButton("turnLeft");
        turnLeft.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                myGrid.turnLeft();
            }
        });
        fifthRow.add(turnLeft);

        JButton pickUp = new JButton("pickUp");
        pickUp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                myGrid.pickUp();
            }
        });
        fifthRow.add(pickUp);

        JCheckBox showPath = new JCheckBox("showPath");
        showPath.setSelected(true);
        showPath.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                myGrid.toggleShowPath();
            }
        });
        fifthRow.add(showPath);
        gridControls.add(fifthRow);

        // Sixth line of objects
        JPanel sixthRow = new JPanel();
        sixthRow.setOpaque(true);
        JButton frontIsClear = new JButton("frontIsClear");
        frontIsClear.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                frontIsClearLabel.setText(String.valueOf(myGrid.frontIsClear()));
            }
        });
        sixthRow.add(frontIsClear);

        frontIsClearLabel = new JLabel(String.valueOf(myGrid.frontIsClear()), SwingConstants.LEFT);
        sixthRow.add(frontIsClearLabel);

        gridControls.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Grid Commands"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        gridControls.setSize(gridControls.getPreferredSize());

        return gridControls;
    }

    private int getNumber(String txt)
    {
        try
        {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e)
        {
            return -1;
        }
    }

    private int getDirection(String dir)
    {
        if (dir.equals("NORTH"))
        {
            return Grid.NORTH;
        } else if (dir.equals("SOUTH"))
        {
            return Grid.SOUTH;
        } else if (dir.equals("EAST"))
        {
            return Grid.EAST;
        } else
        {
            return Grid.WEST;
        }
    }

    class MyTextFieldListener implements DocumentListener
    {

        public void insertUpdate(DocumentEvent e)
        {
            setValue(e);
        }

        public void removeUpdate(DocumentEvent e)
        {
            setValue(e);
        }

        public void changedUpdate(DocumentEvent e)
        {
        }

        private void setValue(DocumentEvent e)
        {
            Document field = e.getDocument();
            String txt = new String();
            try
            {
                txt = field.getText(0, field.getLength());
            } catch (BadLocationException exc)
            {
                return;
            }
            int temp = getNumber(txt);
            if (field.getProperty("name").equals("moverRow"))
            {
                if (txt.length() == 0)
                {
                    moverRowNum = -1;
                }
                if (temp >= 0)
                {
                    moverRowNum = temp;
                }
            }
            if (field.getProperty("name").equals("moverCol"))
            {
                if (txt.length() == 0)
                {
                    moverColNum = -1;
                }
                if (temp >= 0)
                {
                    moverColNum = temp;
                }
            }
            if (field.getProperty("name").equals("nRows"))
            {
                if (temp >= 0 && temp <= Grid.MAX_ROWS)
                {
                    numRows = temp;
                }
            }
            if (field.getProperty("name").equals("nCols"))
            {
                if (temp >= 0 && temp <= Grid.MAX_COLUMNS)
                {
                    numCols = temp;
                }
            }
            if (field.getProperty("name").equals("blockRow"))
            {
                if (temp >= 0)
                {
                    blockRowNum = temp;
                }
            }
            if (field.getProperty("name").equals("blockCol"))
            {
                if (temp >= 0)
                {
                    blockColNum = temp;
                }
            }
            if (field.getProperty("name").equals("putDownRow"))
            {
                if (temp >= 0)
                {
                    putDownRowNum = temp;
                }
            }
            if (field.getProperty("name").equals("putDownCol"))
            {
                if (temp >= 0)
                {
                    putDownColNum = temp;
                }
            }
            if (field.getProperty("name").equals("moveSpaces"))
            {
                if (temp >= 0)
                {
                    moveNumSpaces = temp;
                }
            }
        }
    }

    protected JLabel frontIsClearLabel;
    protected JLabel rightIsClearLabel;
    private Grid myGrid;
    private JPanel controls;
    private GraphicGridPanel myPanel;
    private int defaultNRows = 7;
    private int defaultNColumns = 7;
    private int numRows;
    private int numCols;
    private int moverRowNum;
    private int moverColNum;
    private int currentDirection = Grid.EAST;
    private int blockRowNum;
    private int blockColNum;
    private int putDownRowNum;
    private int putDownColNum;
    private int moveNumSpaces;
    private static final int textFieldWidth = 3;
    private static final int spotWidth = 20;
    private static final int spotHeight = 20;
    private static final int spacing = 100;
}

class GraphicGridPanel extends JPanel
{

    private static Color foreground = Color.black;
    private static Color background = Color.white;
    private static Color moverColor = Color.blue;

    /**
     * Constructs the Panel that the Grid will be drawn on.
     *
     * @ param Grid The grid that will be drawn.
     * @ param int spotWidth The width of each square that might hold an element
     * in the Grid.
     * @ param int spotHeight The height of each square in the Grid.
     */
    public GraphicGridPanel(Grid g, int spotWidth, int spotHeight)
    {
        myGrid = g;
        setOpaque(true);
        setBackground(background);
        setForeground(foreground);
        this.setFont(new Font("Serif", Font.PLAIN, 18));
        gridColumnSize = spotWidth;
        gridRowSize = spotHeight;
        setSize(new Dimension(gridColumnSize * myGrid.getColumns() + 60, gridRowSize * myGrid.getRows() + 60));
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (myGrid != null)
        {
            Dimension d = getSize();
            int x = d.width / 2 - gridColumnSize * myGrid.getColumns() / 2;
            int y = d.height / 2 - gridRowSize * myGrid.getRows() / 2;
            for (int r = 0; r < myGrid.getRows(); r++)
            {
                for (int c = 0; c < myGrid.getColumns(); c++)
                {
                    drawChar(g, x + c * gridColumnSize, y + r * gridRowSize, myRect[r][c]);
                }
            }
        }
    }

    private void drawChar(Graphics g, int x, int y, char c)
    {
        switch (c)
        {
            case Grid.intersectionChar:
                g.fillOval(x + gridColumnSize / 2 - 2, y + gridRowSize / 2 - 2, 4, 4);
                break;
            case Grid.beenThereChar:
                break;
            case Grid.blockChar:
                g.setColor(new Color(168, 0, 0));
                g.fillRect(x, y, gridColumnSize, gridRowSize);
                g.setColor(foreground);
                g.drawLine(x, y + gridRowSize / 3, x + gridColumnSize - 1, y + gridRowSize / 3);
                g.drawLine(x, y + gridRowSize * 2 / 3, x + gridColumnSize - 1, y + gridRowSize * 2 / 3);
                break;
            case Grid.thingHereChar:
                g.fillOval(x + gridColumnSize / 10, y + gridRowSize / 10, gridColumnSize - 2 * gridColumnSize / 10, gridRowSize - 2 * gridRowSize / 10);
                g.setColor(new Color(243, 218, 88));
                g.fillOval(x + gridColumnSize / 3, y + gridRowSize / 3, gridColumnSize - 2 * gridColumnSize / 3, gridRowSize - 2 * gridRowSize / 3);
                g.setColor(foreground);
                break;
            case Grid.moverOnThingChar:
                int[] xpts =
                {
                    x + gridColumnSize / 2, x + gridColumnSize / 9, x + gridColumnSize / 2, x + gridColumnSize - 2 * gridColumnSize / 9
                };
                int[] ypts =
                {
                    y + gridRowSize / 9, y + gridRowSize / 2, y + gridRowSize - 2 * gridRowSize / 9, y + gridRowSize / 2
                };
                g.setColor(new Color(80, 231, 252));
                g.fillRect(x + gridColumnSize / 4, y + gridRowSize / 4, gridColumnSize - 2 * gridColumnSize / 4, gridRowSize - 2 * gridRowSize / 4);
                g.fillPolygon(xpts, ypts, xpts.length);
                g.setColor(foreground);
                break;
            case Grid.moverNorth:
                drawMover(g, x, y, 120, 300);
                break;
            case Grid.moverSouth:
                drawMover(g, x, y, 300, 300);
                break;
            case Grid.moverEast:
                drawMover(g, x, y, 30, 300);
                break;
            case Grid.moverWest:
                drawMover(g, x, y, 210, 300);
                break;
        }
    }

    private void drawMover(Graphics g, int x, int y, int startAngle, int degrees)
    {
        g.setColor(background);
        g.fillRect(x, y, gridColumnSize, gridRowSize);
        g.setColor(moverColor);
        g.fillArc(x, y, gridColumnSize, gridRowSize, startAngle, degrees);
        g.setColor(foreground);
    }

    /**
     * Changes the Grid that this panel draws.
     *
     * @ param Grid The new grid to be drawn.
     */
    public void setGrid(Grid g)
    {
        myGrid = g;
        setSize(new Dimension(gridColumnSize * myGrid.getColumns() + 60, gridRowSize * myGrid.getRows() + 60));
    }

    /**
     * Called by the Grid object whenever its state changes.
     *
     * @param char[][] rect The 2D array of chars that represents the Grid
     */
    public void stateChanged(char[][] rect)
    {
        myRect = rect;
        repaint();
    }

    private Dimension myObjectSize;
    private int gridRowSize;
    private int gridColumnSize;
    private Grid myGrid;
    private char[][] myRect;

}
