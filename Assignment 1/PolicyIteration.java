import java.util.TreeMap;
import java.util.Collections;

public class PolicyIterationApp{
  
    // define maze constants
	static final int MAZE_ROWS = 6;
    static final int MAZE_COLS = 6;
  
    // define reward constants
  	static final double E = -0.04; 
	static final double B = -1;    
    static final double G = 1;  
    
	// state references
    static char[][] maze = new char[MAZE_ROWS][MAZE_COLS];
    static char[][] optimalPolicy = new char[MAZE_ROWS][MAZE_COLS];
    static double[][] stateUtility = new double[MAZE_ROWS][MAZE_COLS];
    
	// algorithm parameters
    static int iterations = 50; 
    static double discount = 0.99;
    
    public static void main(String []args) {
         
        // create the maze
		initMaze();
		
        // perform policy iteration
		policyIteration();
		
		// print out the states
        // printResult();
        
    }
    
    // function to format and print the results of the states
    private static void printResult() {
        
       
        for(int r=0; r<MAZE_ROWS; r++) {
            // print the utility of each state
            for(int c=0; c<MAZE_COLS; c++) {
                System.out.print(String.format("%6.3f ", stateUtility[r][c]));
            }
            System.out.println();
        }
        
        
        for(int r=0; r<MAZE_ROWS; r++) {
            // print optimal policy
            for(int c=0; c<MAZE_COLS; c++) {
                System.out.print(String.format("%c ", optimalPolicy[r][c]));
            }
            System.out.println();
        }
        
    }
    
    // function to perform policy iteration
    private static void policyIteration() {
        
		// randomly initialize the starting policy to be 'Up'
        for(int r=0; r<MAZE_ROWS; r++) {
            for(int c=0; c<MAZE_COLS; c++) {
                
                optimalPolicy[r][c] = 'U';
                
            }
        }
        
        for(int i=0; i<iterations; i++) {
            
            policyEvaluation();
            policyImprovement();
            
            // print out the results
            if (i%5 == 0) {
                System.out.println("Iteration: " + i);
                printResult();
            }
            
        }
        
    }
    
    // perform policy improvement
    private static void policyImprovement() {
        
        for(int r=0; r<MAZE_ROWS; r++) {
            for(int c=0; c<MAZE_COLS; c++) {
                
                // perform policy improvement on maze cells that are empty
                if(maze[r][c] != 'e')
                    continue;
                else
                    optimalPolicy[r][c] = updatePolicy(r, c);
               
            }
        }
        
    }
    
    // perform policy evaluation
    private static void policyEvaluation() {
        
		// store the new state utility and update after calculation for each cell has been done
        double[][] newStateUtility = new double[MAZE_ROWS][MAZE_COLS];
        
        for(int r=0; r<MAZE_ROWS; r++) {
            for(int c=0; c<MAZE_COLS; c++) {
                
				// switch between rewards based on cell and use the bellman equation to get state utility
                switch(maze[r][c]) {
                    case 'e':
                        newStateUtility[r][c] = E + discount * optimalUtility(r, c);
                        break;
                    case 'g':
                        newStateUtility[r][c] = G + discount * optimalUtility(r, c);
                        break;
                    case 'b':
                        newStateUtility[r][c] = B + discount * optimalUtility(r, c);
                        break;
                    default:
                        newStateUtility[r][c] = 0 + discount * optimalUtility(r, c);
                        break;
                }
            }
        }
		
        // update the state utility when the new state utility have been calculated for all the cells
        stateUtility = newStateUtility;
        
    } 
    
	// helper function to update the optimal policy
    private static char updatePolicy(int r, int c) {
        
        // use a data structure to store the next state utility and the policy to move to next state
        TreeMap<Double, Character> policies = new TreeMap<Double, Character>(); 
        
        // if robot can move up
        if (r>0 && maze[r-1][c] != 'w') {
            policies.put(0.8 * getNextStateUtility(r, c, r-1, c) + 0.1 * getNextStateUtility(r, c, r, c+1) + 0.1 * getNextStateUtility(r, c, r, c-1), 'U');
        } else {
            // System.out.print("top wall ");
        }
        
        // if robot can move down
        if (r<MAZE_ROWS-1 && maze[r+1][c] != 'w') {
            policies.put(0.8 * getNextStateUtility(r, c, r+1, c) + 0.1 * getNextStateUtility(r, c, r, c+1) + 0.1 * getNextStateUtility(r, c, r, c-1), 'D');
        } else {
            // System.out.print("bottom wall ");
        }
        
        // if robot can move left
        if (c>0 && maze[r][c-1] != 'w') {
            policies.put(0.8 * getNextStateUtility(r, c, r, c-1) + 0.1 * getNextStateUtility(r, c, r+1, c) + 0.1 * getNextStateUtility(r, c, r-1, c), 'L');
        } else {
            // System.out.print("left wall ");
        }
        
        // if robot can move right
        if (c<MAZE_ROWS-1 && maze[r][c+1] != 'w') {
            policies.put(0.8 * getNextStateUtility(r, c, r, c+1) + 0.1 * getNextStateUtility(r, c, r+1, c) + 0.1 * getNextStateUtility(r, c, r-1, c), 'R');
        } else {
            // System.out.print("right wall");
        }
        
        // get maximum utility
        double maxUtility = policies.lastKey();
        
        // return the policy which is associated with the maximum utility 
        return policies.get(maxUtility); 

    }
    
    
    // helper function to get the next state utility
    private static double getNextStateUtility(int r, int c, int new_r, int new_c) {
        
        try {
            // if adjacent cell is not wall, return state utility of adjacent cell
            if(maze[new_r][new_c] != 'w') {
                return stateUtility[new_r][new_c];
            }
            // if adjacent cell is wall, return state utility of the current cell
            else {
                return stateUtility[r][c];
            }
        // if adjacent cell is out of bounds of the maze, return state utility of the current cell
        } catch (Exception e) {
            return stateUtility[r][c];
        }

    }
    
    // function to calculate state utility based on the policy
    private static double optimalUtility(int r, int c) {
		
		// get the next state utility based on the policy (U, D, L, R)
        switch (optimalPolicy[r][c]) {
            case 'U':
                return 0.8 * getNextStateUtility(r, c, r-1, c) + 0.1 * getNextStateUtility(r, c, r, c+1) + 0.1 * getNextStateUtility(r, c, r, c-1);
            case 'D':
                return 0.8 * getNextStateUtility(r, c, r+1, c) + 0.1 * getNextStateUtility(r, c, r, c+1) + 0.1 * getNextStateUtility(r, c, r, c-1);
            case 'L':
                return 0.8 * getNextStateUtility(r, c, r, c-1) + 0.1 * getNextStateUtility(r, c, r+1, c) + 0.1 * getNextStateUtility(r, c, r-1, c);
            case 'R':
                return 0.8 * getNextStateUtility(r, c, r, c+1) + 0.1 * getNextStateUtility(r, c, r+1, c) + 0.1 * getNextStateUtility(r, c, r-1, c);
        }
        
        return 0;
        
    }
     
    // helper function used to generate the maze
    private static void initMaze() { 
      
        for(int r=0; r<MAZE_ROWS; r++){
            for(int c=0; c<MAZE_COLS; c++) {
                maze[r][c] = 'e';
            }
        }
        
        // green blocks
        maze[0][0] = 'g';
        maze[0][2] = 'g';
        maze[0][5] = 'g';
        maze[1][3] = 'g';
        maze[2][4] = 'g';
        maze[3][5] = 'g';
        
        // brown blocks
        maze[1][1] = 'b';
        maze[1][5] = 'b';
        maze[2][2] = 'b';
        maze[3][3] = 'b';
        maze[4][4] = 'b';
        
        // walls
        maze[0][1] = 'w';
        maze[1][4] = 'w';
        maze[4][1] = 'w';
        maze[4][2] = 'w';
        maze[4][3] = 'w';
      
        
        for(int r=0; r<MAZE_ROWS; r++){
            for(int c=0; c<MAZE_COLS; c++) {
                System.out.print(maze[r][c] + " ");
            }
            System.out.println();
        }
        
    } 
  

}