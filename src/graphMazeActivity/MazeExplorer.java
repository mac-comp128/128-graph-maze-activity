package graphMazeActivity;

/**
 *  Created by Professor Josh Hug for UCB's Graphs Lab
 *  Updated for COMP128 to use use kiltgraphics
 **/

public abstract class MazeExplorer {
    public int[] distTo; // Distances to draw over the maze from starting vertex on Canvas
    public int[] edgeTo; // Edges to draw on maze (from the adjacent vertex)
    public boolean[] marked; // keep track of visited vertices
    public Maze maze;

    public MazeExplorer(Maze m) {
        maze = m;

        distTo = new int[maze.V()];
        edgeTo = new int[maze.V()];
        marked = new boolean[maze.V()];
        for (int i = 0; i < maze.V(); i += 1) {
            distTo[i] = Integer.MAX_VALUE;
            edgeTo[i] = Integer.MAX_VALUE;
        }
    }
  
    // Notify the maze to update the canvas
    protected void updateMazeDrawing() {
        maze.updateDrawing(this);
    }

    /** Solves the maze, modifying distTo and edgeTo as it goes. */
    public abstract void solve();
}
