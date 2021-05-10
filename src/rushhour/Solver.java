package rushhour;

import rushhourtest.IllegalMoveException;
import rushhourtest.RushHour;

import java.io.*;
import java.util.*;

public class Solver {

    //Implementation 1
    public static void solveFromFile(String inputPath, String outputPath) throws FileNotFoundException {
            //create board from the text file inputPath
            RushHour initialState = new RushHour(inputPath);

            //find goal state
            //RushHour finishedState = BFS(initialState);  //find solution with BFS algorithm
            //RushHour finishedState = aStar(initialState);  find solution with aStar algorithm
            RushHour finishedState = DFS1(initialState);

            LinkedList<RushHour>  path0 = new LinkedList<>();
            //Recover path from goalState to initialState
            LinkedList path1 = recoverPath(path0,initialState,finishedState);

            //get all the moves to finish the board
            String solution = "";
            solution = getMoves(path1);

            //write moves into outputPath
        try {
            writeMoves(solution, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

        public static void writeMoves(String solution, String filename) throws IOException {
            File file = new File(filename);
            try{
                FileWriter writer = new FileWriter(file);
                writer.write(solution);
                writer.flush();
            }
            catch(FileNotFoundException ex){
                throw ex;
            }

        }

        public static String getMoves(LinkedList<RushHour> path) {
            String moves = "";

            for(int i=path.size()-1; i>=0;i--)
            {
                moves += path.get(i).getMove();
                moves += "\n";
            }

            return moves;
        }

        public static Set<RushHour> generateNeighbours(RushHour source) {
            Set<RushHour> neighbours = new HashSet<>();
            //all moves for each car, within a distance of 1 for each cars
            try {
                neighbours = source.children();
            } catch (IllegalMoveException e) {
                e.printStackTrace();
            }
            return neighbours;
        }

        public static RushHour aStar(RushHour source){

            PriorityQueue<RushHour> openQueue = new PriorityQueue<RushHour>(
                    new Comparator<RushHour>() {
                        public int compare(RushHour r1, RushHour r2) {
                            return r1.getF() - r2.getF();  // you can also compare from H instead of F
                        }
                    });

            Map<RushHour, Integer> closedSet = new HashMap<RushHour, Integer>(); // Integer may represent the f value to give you more information about the vertex

            source.setG(0);
            source.setParent(null);
            openQueue.add(source);
            while (!openQueue.isEmpty()) {
                RushHour r = openQueue.remove();
                Set<RushHour> Nv = generateNeighbours(r); // in the project generateAllNeighbours(v)
                for (RushHour u : Nv) {
                    if (u.isSolved()) { // check if u is a solved state
                        u.setG(r.getG()+1);
                        u.setParent(r);
                        return u;
                    }
                    else if (!openQueue.contains(u) && !closedSet.containsKey(u)) {
                        u.setG(r.getG()+1);
                        u.setParent(r);
                        openQueue.add(u);
                    }
                    // can make it more sophisticated:
                    // e.g. check if u is in openQueue with a higher value - update the value of u
                    // or move from closedSet to openQueue
                }
                closedSet.put(r, r.getF()); // here could use a set, no need to use a Map
            }
            return source;
        }

    public static RushHour DFS1(RushHour source) {
        Stack<RushHour> stack = new Stack<RushHour>();
        stack.push(source);
        Set<RushHour> visitedNodes = new HashSet<RushHour>();
        visitedNodes.add(source);

        while (!stack.isEmpty()) {
            RushHour v = stack.pop();
            Set<RushHour> Nv = generateNeighbours(v);
            for (RushHour u : Nv) {
                if(u.isSolved()){  // If we find the goal state exit
                    u.setParent(v);
                    return u;
                }
                if (!visitedNodes.contains(u)) {
                    stack.push(u);
                    visitedNodes.add(u);
                    u.setParent(v);
                }
            }
        }
        return source;
    }

        public static RushHour BFS(RushHour source) {
            LinkedList<RushHour> queue = new LinkedList<>();
            queue.addLast(source);
            Set<RushHour> visitedNodes = new HashSet<RushHour>();
            visitedNodes.add(source);

            while (!queue.isEmpty()) {
                RushHour v = queue.removeFirst();
                Set<RushHour> Nv = generateNeighbours(v);
                for (RushHour u : Nv) {
                    if(u.isSolved()){  // If we find the goal state exit
                        u.setParent(v);
                        return u;
                    }
                    if (!visitedNodes.contains(v)) { // check if the hash of this vertex is not in the list of the visited nodes
                        queue.addLast(u);
                        visitedNodes.add(u);
                        u.setParent(v);
                    }
                }
            }
            return source;
        }

        public static LinkedList<RushHour> recoverPath( LinkedList<RushHour> path, RushHour source, RushHour target) {
            while(!source.equals(target)){
                path.add(target);
                if(target.getParent() == null)
                    break;
                target = target.getParent();
            }
            return path;
        }

        public static void main(String args[]) {

            try {
                solveFromFile("src\\solver\\A00.txt", "src\\solver\\A00(1).sol");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }



}
