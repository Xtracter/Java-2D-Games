package tetris;
import javax.swing.*;
import java.awt.*;

/**
 * This component shows a digital digit in the old
 * way:
 * <PRE>
 *           __
 *          |__| = 8
 *          |__|
 *</PRE>
 */
public class DigitalDigit extends JPanel
{
    // Global variables
    // The number shown
    private int shownNumber=0;

    // The size of the digit
    private int digitHeight=1;
    // The width of the digit
    private final static double relDigitWidth = 0.59406;

    // The number of polygons in a digit.
    private final static int NR_OF_POLYGONS = 7;

    private final static Color ACTIVECOLOUR=Color.green;
    private final static Color PASSIVECOLOUR=ACTIVECOLOUR.darker().darker().darker().darker();

    /* How the numbers are shown. The numbers are choosen
     * so that the horizontal bars have odd numbers and the
     * vertical have even numbers.
     *    _1_
     *   |   |
     *  0|_3_|2
     *   |   |
     *  4|___|6
     *     5
     */
    private boolean[][] digitPattern = { {true,  true,  true,  false, true,  true,  true }, // 0
            {false, false, true,  false, false, false, true }, // 1
            {false, true,  true,  true,  true,  true,  false}, // 2
            {false, true,  true,  true,  false, true,  true }, // 3
            {true,  false, true,  true,  false, false, true }, // 4
            {true,  true,  false, true,  false, true,  true }, // 5
            {true,  true,  false, true,  true,  true,  true }, // 6
            {false, true,  true,  false, false, false, true }, // 7
            {true,  true,  true,  true,  true,  true,  true }, // 8
            {true,  true,  true,  true,  false, true,  true }}; // 9

    private double[][] polyPositions = {{0.019802,0.079208},
            {0.089109,0.019802},
            {0.455446,0.079208},
            {0.089109,0.435644},
            {0.019802,0.485149},
            {0.089109,0.841584},
            {0.455446,0.485149}};

    // The polygon patterns
    // This is the true values (float) multiplied by 1e6
    private int[] horizxArray={ 49505, 346535, 396040, 346535,  49505,      0};
    private int[] horizyArray={     0,      0,  49505,  99010,  99010,  49505};

    private Polygon horizPolygon = new Polygon(horizxArray,horizyArray, 6);
    private Polygon vertPolygon = new Polygon(horizyArray,horizxArray, 6);

    /**
     * Constructs a digit with the initial value of 0.
     */
    public DigitalDigit()
    {
        setBackground(Color.black);
        setForeground(ACTIVECOLOUR);
        shownNumber=0;
        digitHeight = 30;
    }

    /**
     * Constructs a digit with the initial value of number.
     * @param number The number to be shown initially.
     */
    public DigitalDigit(int number)
    {
        this();
        if((number<0) || (number >9)) throw new IllegalArgumentException(number+" is not in the range 0-9");
        shownNumber=number;
    }

    /**
     * Constructs a digit with the initial value of number.
     * @param number The number to be shown initially.
     * @param size The height of the digit in pixels. It's approx. 60% wide.
     */
    public DigitalDigit(int number, int size)
    {
        this(number);
        if(size<1) throw new IllegalArgumentException("The size must be at least 1");
        digitHeight = size;
    }

    public DigitalDigit(int number, int size, boolean visible)
    {
        this(number);
        if(size<1) throw new IllegalArgumentException("The size must be at least 1");
        digitHeight = size;
        this.setVisible(visible);
    }
    /**
     * This method changes the digit shown.
     * @param number The new digit to show.
     */
    public synchronized void setDigit(int number)
    {
        setVisible(true);
        if((number<0) || (number >9)) throw new IllegalArgumentException(number+" is not in the range 0-9");
        shownNumber=number;
        repaint();
    }

    /**
     * I override update to avoid the background to be
     * cleared.
     */

    public void paint(Graphics g)
    {
        // Create offscreen Image to play with
        Image offI = createImage((int)(relDigitWidth*digitHeight), digitHeight);
        Graphics offG = offI.getGraphics();

        offG.setColor(Color.black);
        offG.fillRect(0,0,(int)(relDigitWidth*digitHeight), digitHeight);

        for(int poly=0; poly<NR_OF_POLYGONS; poly++) {
            if(digitPattern[shownNumber][poly]) {
                // This polygon is used in this digit
                // Therefore use ACTIVECOLOUR
                offG.setColor(ACTIVECOLOUR);
            } else {
                // Use PASSIVECOLOUR
                offG.setColor(PASSIVECOLOUR);
            }

            if((poly%2)==1) {
                // Odd number -> Horizontal bar
                drawMovedPolygon(offG, horizPolygon,
                        (int)(polyPositions[poly][0]*digitHeight),
                        (int)(polyPositions[poly][1]*digitHeight),
                        digitHeight);
            } else {
                // Even number -> Vertical
                drawMovedPolygon(offG, vertPolygon,
                        (int)(polyPositions[poly][0]*digitHeight),
                        (int)(polyPositions[poly][1]*digitHeight),
                        digitHeight);
            }
        }
        g.drawImage(offI,0,0, null);

        // Clean up
        offG.dispose();
        offI.flush();
    }


    /**
     * Returns the width of a digit.
     */
    public int getWidth()
    {
        return (int)(relDigitWidth*digitHeight);
    }

    /**
     * Returns the width of a digit.
     */
    public int getHeight()
    {
        return digitHeight;
    }


    /**
     * Displays a polygon at the specified coordinates
     * The polygon has it's value divided by 1e6.
     */
    private void drawMovedPolygon(Graphics g, Polygon p, int x, int y, int mult)
    {
        int[] xArray = new int[p.npoints];
        int[] yArray = new int[p.npoints];

        // Create a new polygon that is moved
        for(int i=0; i<p.npoints; i++) {
            int xPos = (int)(mult*(double)p.xpoints[i]/((double)1e6)+x);
            xArray[i] = xPos;
            int yPos = (int)(mult*(double)p.ypoints[i]/((double)1e6)+y);
            yArray[i] = yPos;
        }

        g.fillPolygon(xArray, yArray, p.npoints);

    }

    /**
     * Returns the preferred size of the panel. That is the
     * size to use to avoid empty spaces in the panel.
     */
    public Dimension preferredSize()
    {
        return new Dimension(getWidth(), getHeight());
    }

}

