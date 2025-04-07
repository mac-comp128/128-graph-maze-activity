/**
 *  Created by Professor Josh Hug for UCB's Graphs Lab
 *  Updated for COMP128 to use kiltgraphics
 **/


package graphMazeActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class MazeDemo {
    /* Runs a breadth first search or depth first search from (1, 1) to (N, N) on the graph in the config file. */
    public static void main(String[] args) throws URISyntaxException {
        URI uri = MazeDemo.class.getResource("/maze.config").toURI();
        String configFilePath = Paths.get(uri).toAbsolutePath().toString();
        Maze maze = new Maze(configFilePath);

        int startX = 1;
        int startY = 1;
        int targetX = maze.N();
        int targetY = maze.N();

        /* uncomment the following to run a depth first search */
        MazeExplorer mdfp = new MazeDepthFirstPaths(maze, startX, startY, targetX, targetY);
        mdfp.solve();

        /* uncomment the following to run a breadth first search */
        // MazeExplorer mbfp = new MazeBreadthFirstPaths(maze, startX, startY, targetX, targetY);
        // mbfp.solve();

        /* uncomment the following to detect a cycle */
        // MazeCycles mc = new MazeCycles(maze);
        // mc.solve();
    }

}
