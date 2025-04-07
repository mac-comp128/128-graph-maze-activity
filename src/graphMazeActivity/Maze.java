package graphMazeActivity;

import java.util.Random;
import java.awt.Color;

/**
 *  Created by Professor Josh Hug for UCB's Graphs Lab
 *  Updated for COMP128 to use kiltgraphics
 **/


import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.macalester.graphics.CanvasWindow;
import edu.macalester.graphics.Ellipse;
import edu.macalester.graphics.GraphicsText;
import edu.macalester.graphics.Line;

public class Maze {

    private int N;                 // dimension of maze
    private boolean[][] north;     // is there a wall to north of cell i, j
    private boolean[][] east;
    private boolean[][] south;
    private boolean[][] west;
    private static Random rgen;
    public static int DRAW_DELAY_MS = 50;
    private CanvasWindow canvas;

    public enum MazeType {
        SINGLE_GAP, POPEN_SOLVABLE
    }

    public void updateDrawing(MazeExplorer me) {
        canvas.removeAll();
        draw();
        for (int i = 0; i < N * N; i += 1) {
            if (me.marked[i]) {
                drawEdges(i, me);
            }
        }
        for (int i = 0; i < N * N; i += 1) {
            if (me.marked[i]) {
                draw(i, me);
            }
        }

        canvas.draw();
        canvas.pause(DRAW_DELAY_MS);
        
    }

    /** Returns neighbor vertices of vertex v. */
    public Iterable<Integer> adj(int v) {
        int x = toX(v);
        int y = toY(v);
        TreeSet<Integer> neighbors = new TreeSet<Integer>();
        if (!wallExists(x, y, "North")) {
            neighbors.add(xyTo1D(x, y + 1));
        }

        if (!wallExists(x, y, "East")) {
            neighbors.add(xyTo1D(x + 1, y));
        }

        if (!wallExists(x, y, "South")) {
            neighbors.add(xyTo1D(x, y - 1));
        }

        if (!wallExists(x, y, "West")) {
            neighbors.add(xyTo1D(x - 1, y));
        }

        return neighbors;
    }

    /** Returns x coordinate for given vertex.
      * For example if N = 10, and V = 12, returns 2. */
    public int toX(int v) {
        return v % N + 1;
    }

    /** Returns y coordinate for given vertex.
      * For example if N = 10, and V = 12, returns 1. */
    public int toY(int v) {
        return v / N + 1;
    }

    /** Returns one dimensional coordinate for vertex in position x, y. */
    public int xyTo1D(int x, int y) {
        return (y - 1) * N + (x - 1);
    }



    // returns true if wall exists
    private boolean wallExists(int x, int y, String s) {
        int tx = targetX(x, s);
        int ty = targetY(y, s);
        boolean ooBounds = (tx == 0 || ty == 0 || tx == N+1 || ty == N+1);

        if (ooBounds)
            return true;

        if (s.equals("North"))
            return north[x][y];

        if (s.equals("East"))
            return east[x][y];

        if (s.equals("South"))
            return south[x][y];

        if (s.equals("West"))
            return west[x][y];

        return true;
    }

    /** Returns number of spaces in the maze. */
    public int V() {
        return N * N;
    }

    /** Returns size of the maze. */
    public int N() {
        return N;
    }

    /** Creates a Maze from the given config file. */
    public Maze(String configFilename) {
        In in = new In(configFilename);
        int rseed = 0;
        N = 10;
        DRAW_DELAY_MS = 50;
        MazeType mt = MazeType.SINGLE_GAP;
        double pOpen = 0.48;
        String configPatternString = "(\\w+)\\s*=\\s*([a-zA-Z0-9_.]+)";
        Pattern configPattern = Pattern.compile(configPatternString);

        while (!in.isEmpty()) {
            String thisLine = in.readLine();
            if (thisLine.length() == 0 || thisLine.charAt(0) == '%') {
                continue;
            }

            Matcher m = configPattern.matcher(thisLine);
            if (m.find()) {
                String variable = m.group(1);
                String value = m.group(2);
                switch(variable) {
                    case "N": N = Integer.parseInt(value); break;
                    case "rseed": rseed = Integer.parseInt(value); break;
                    case "pOpen": pOpen = Double.parseDouble(value); break;
                    case "DRAW_DELAY_MS": DRAW_DELAY_MS = Integer.parseInt(value); break;
                    case "MazeType":
                        if (value.equals("SINGLE_GAP")) {
                            mt = MazeType.SINGLE_GAP;
                        }

                        if (value.equals("POPEN_SOLVABLE")) {
                            mt = MazeType.POPEN_SOLVABLE;
                        }

                        break;
                    default:
                        break;
                }
            }
        }
        init(rseed, pOpen, mt);
    }

    /** Creates a Maze with given N, random seed, parameter p (not used by
      * all maze types), and a maze type. */
    public Maze(int N, int rseed, double pOpen, MazeType mt) {
        this.N = N;
        init(rseed, pOpen, mt);
    }

    /** Initializes maze based on parameters set up by constructors. */
    private void init(int rseed, double p, MazeType mt) {
        canvas = new CanvasWindow("Maze Solver", 512, 512);
        rgen = new Random(rseed);
        if (mt == MazeType.SINGLE_GAP) {
            generateSingleGapMaze();
        }
        if (mt == MazeType.POPEN_SOLVABLE) {
            generatePopenSolvableMaze(p);
        }
    }


    // generate the maze
    private void generateSingleGapMaze(int x, int y, boolean[][] marked) {

        marked[x][y] = true;
        // while there is an unmarked neighbor
        while (!marked[x][y+1] || !marked[x+1][y] || !marked[x][y-1] || !marked[x-1][y]) {

            // pick random neighbor (could use Knuth's trick instead)
            while (true) {

                double r = rgen.nextDouble();

                if (r < 0.25 && !marked[x][y+1]) {
                    north[x][y] = south[x][y+1] = false;
                    generateSingleGapMaze(x, y + 1, marked);
                    break;
                }
                else if (r >= 0.25 && r < 0.50 && !marked[x+1][y]) {
                    east[x][y] = west[x+1][y] = false;
                    generateSingleGapMaze(x+1, y, marked);
                    break;
                }
                else if (r >= 0.5 && r < 0.75 && !marked[x][y-1]) {
                    south[x][y] = north[x][y-1] = false;
                    generateSingleGapMaze(x, y-1, marked);
                    break;
                }
                else if (r >= 0.75 && r < 1.00 && !marked[x-1][y]) {
                    west[x][y] = east[x-1][y] = false;
                    generateSingleGapMaze(x-1, y, marked);
                    break;
                }
            }
        }
    }

    // generate the maze starting from lower left
    private void generateSingleGapMaze() {
        boolean[][] marked = new boolean[N + 2][N + 2];
        for (int x = 0; x < N+2; x++) marked[x][0] = marked[x][N+1] = true;
        for (int y = 0; y < N+2; y++) marked[0][y] = marked[N+1][y] = true;

        // initialize all walls as present
        north = new boolean[N+2][N+2];
        east  = new boolean[N+2][N+2];
        south = new boolean[N+2][N+2];
        west  = new boolean[N+2][N+2];
        for (int x = 0; x < N+2; x++)
            for (int y = 0; y < N+2; y++)
                north[x][y] = east[x][y] = south[x][y] = west[x][y] = true;

        generateSingleGapMaze(1, 1, marked);
    }


    private void generatePopenSolvableMaze(double pOpen) {
        // initialize all walls as present
        north = new boolean[N+2][N+2];
        east  = new boolean[N+2][N+2];
        south = new boolean[N+2][N+2];
        west  = new boolean[N+2][N+2];
        for (int x = 0; x < N+2; x++)
            for (int y = 0; y < N+2; y++)
                north[x][y] = east[x][y] = south[x][y] = west[x][y] = true;


        for (int x = 1; x < N + 1; x += 1) {
            for (int y = 1; y < N + 1; y += 1) {
                double r = rgen.nextDouble();
                if (r < pOpen) {
                    if (inBounds(x, y+1)) {
                        north[x][y] = south[x][y+1] = false;
                    }
                }

                r = rgen.nextDouble();
                if (r < pOpen) {
                     if (inBounds(x+1, y)) {
                         east[x][y] = west[x+1][y] = false;
                     }
                }

                r = rgen.nextDouble();
                if (r < pOpen) {
                     if (inBounds(x, y-1)) {
                         south[x][y] = north[x][y-1] = false;
                     }
                }

                r = rgen.nextDouble();
                if (r < pOpen) {
                     if (inBounds(x-1, y)) {
                         west[x][y] = east[x-1][y] = false;
                     }
                }
            }
        }
    }

    /** Returns true if (x, y) is inside the bounds of the maze. */
    private boolean inBounds(int x, int y) {
        return (!(x == 0 || x == N + 1 || y == 0 || y == N + 1));
    }

    // Returns x coordinate of target given source location
    // and direction
    private int targetX(int x, String s) {
        if (s.equals("West"))
            return x - 1;
        if (s.equals("East"))
            return x + 1;
        return x;
    }

    // Returns y coordinate of target given source location
    // and direction
    private int targetY(int y, String s) {
        if (s.equals("South"))
            return y - 1;
        if (s.equals("North"))
            return y + 1;
        return y;
    }

    /** Draws a filled circle of desired color c in cell i. */
    private void draw(int i, Color c) {
        int x = toX(i);
        int y = toY(i);
        Ellipse e = new Ellipse(0, 0, scaleX(0.5), scaleX(0.5));
        e.setCenter(scaleX(x + 0.5), scaleY(y + 0.5));
        e.setFillColor(c);
        e.setFilled(true);
        canvas.add(e);
    }

    /** Draws a filled circle of desired color c in cell i. */
    private void draw(int x, int y, Color c) {
        Ellipse e = new Ellipse(0, 0, scaleX(0.5), scaleX(0.5));
        e.setCenter(scaleX(x + 0.5), scaleY(y + 0.5));
        e.setFillColor(c);
        e.setFilled(true);
        canvas.add(e);
    }

    /* Draws the state of cell i, including any back edges. */
    private void draw(int i, MazeExplorer me) {
        int x = toX(i);
        int y = toY(i);
        if (me.marked[i]) {
            Ellipse e = new Ellipse(0,0, scaleX(0.5), scaleX(0.5));
            e.setCenter(scaleX(x + 0.5), scaleY(y + 0.5));
            e.setFillColor(Color.BLUE);
            e.setFilled(true);
            canvas.add(e);
        }
        if (me.distTo[i] < Integer.MAX_VALUE) {
            GraphicsText text = new GraphicsText(Integer.toString(me.distTo[i]));
            text.setFillColor(Color.WHITE);
            text.setFilled(true);
            text.setCenter( scaleX(x+0.5), scaleY(y+0.5));
            canvas.add(text);
        }
    }


    private void drawEdges(int i, MazeExplorer me) {
        int x = toX(i);
        int y = toY(i);
        if (me.edgeTo[i] < Integer.MAX_VALUE) {
            int fromX = toX(me.edgeTo[i]);
            int fromY = toY(me.edgeTo[i]);
            Line l = new Line(scaleX(fromX + 0.5), scaleY(fromY + 0.5), scaleX(x + 0.5), scaleY(y + 0.5));
            l.setStrokeColor(Color.MAGENTA);
            l.setStroked(true);
            canvas.add(l);
        }
    }

    // draw the maze
    public void draw() {
        for (int x = 1; x <= N; x++) {
            for (int y = 1; y <= N; y++) {
                if (south[x][y]) {
                    Line l = new Line(scaleX(x), scaleY(y), scaleX(x + 1), scaleY(y));
                    l.setStrokeColor(Color.BLACK);
                    l.setStroked(true);
                    canvas.add(l);
                }
                if (north[x][y]) { 
                    Line l = new Line(scaleX(x), scaleY(y + 1), scaleX(x + 1), scaleY(y + 1));
                    l.setStrokeColor(Color.BLACK);
                    l.setStroked(true);
                    canvas.add(l);
                }
                if (west[x][y]) {
                    Line l = new Line(scaleX(x), scaleY(y), scaleX(x), scaleY(y + 1));
                    l.setStrokeColor(Color.BLACK);
                    l.setStroked(true);
                    canvas.add(l);
                }
                if (east[x][y]) {
                    Line l = new Line(scaleX(x + 1), scaleY(y), scaleX(x + 1), scaleY(y + 1));
                    l.setStrokeColor(Color.BLACK);
                    l.setStroked(true);
                    canvas.add(l);
                }
            }
        }
    }

    /* Draws the maze with all spots numbered by 1D index. */
    private void drawDotsByIndex() {
        for (int i = 0; i < V(); i += 1) {
            int x = toX(i);
            int y = toY(i);
            Ellipse e = new Ellipse(0,0, scaleX(0.5), scaleX(0.5));
            e.setCenter(scaleX(x + 0.5), scaleY(y + 0.5));
            e.setFillColor(Color.RED);
            e.setFilled(true);
            canvas.add(e);
            GraphicsText text = new GraphicsText(Integer.toString(i));
            text.setFillColor(Color.WHITE);
            text.setFilled(true);
            text.setCenter( scaleX(x+0.5), scaleY(y+0.5));
            canvas.add(text);
        }
    }

    /* Draws the maze with all spots numbered by x, y coordinates. */
    private void drawDotsByXY() {
        for (int i = 0; i < V(); i += 1) {
            int x = toX(i);
            int y = toY(i);
            Ellipse e = new Ellipse(0,0, scaleX(0.5), scaleX(0.5));
            e.setCenter(scaleX(x + 0.5), scaleY(y + 0.5));
            e.setFillColor(Color.RED);
            e.setFilled(true);
            canvas.add(e);
            GraphicsText text = new GraphicsText(Integer.toString(x) + ", " + Integer.toString(y));
            text.setStrokeColor(Color.WHITE);
            text.setStroked(true);
            text.setCenter( scaleX(x+0.5), scaleY(y+0.5));
            canvas.add(text);
        }
    }

    // helper functions that scale from user coordinates to screen coordinates and back
    private double scaleX(double x) { return canvas.getWidth()  * (x) / (N+2); }
    private double scaleY(double y) { return canvas.getHeight() * ((N+2) - y) / (N+2); }
}

