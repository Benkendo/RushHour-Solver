package rushhourtest;

import java.io.*;
import java.util.*;

public class RushHour
{
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;

    public final static int UP = 0;
    public final static int DOWN = 1;
    public final static int LEFT = 2;
    public final static int RIGHT = 3;

    public final static int size = 6;

    private class Car
    {
        public Integer xyDirLen;
        public char carName;

        /**
         * @param dir - HORIZONTAL or VERTICAL
         * @param x,y - represent the top left position of the top left corner of the car
         */
        //+1 for dir,x,y for special case when they are zero
        public Car(int dir, int x, int y,int len) {
            this.xyDirLen = ((x+1)*1000) + ((y+1)*100) + ((dir+1)*10) + len;
        }
        public void setName(char name){
            this.carName =name;
        }
        public char getName(){
            return carName;
        }

        public int getX(){ return (xyDirLen/1000) - 1; }
        public int setX(int x){
            int temp = xyDirLen - (getX()*1000);
            return temp + (x*1000) + 1;
        }
        public int getY(){ return ((xyDirLen/100) % 10) -1 ;  }
        public int setY(int y){
            int temp = xyDirLen/100 - getY();
            return (temp*100) + (xyDirLen%100) + 1;
        }

        public int getDir(){ return ((xyDirLen/10) % 10) -1; }
        public int getLen(){ return xyDirLen % 10;   }

        @Override
        public boolean equals(Object other) {
            if (other == null)
                return false;

            if (!(other instanceof Car))
                return false;

            return (this.xyDirLen == ((Car)other).xyDirLen);
        }

    }

    //used for graph traversals
    private int hashCode;
    private RushHour parent;
    private String move;

    //used for A*
    private int f; // g+h
    private int g; // number of steps so far
    private int h; // heuristic value - estimated distance to target

    char board[][];
    // String is the name of the car
    HashMap<Character,Car> cars;

    //create a copy of Rushhour
    public RushHour(RushHour copy){

        //copy board
        board = new char[size][size];
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++){
                board[i][j] = copy.board[i][j];
            }
        }

        //copy cars
        int i,j;
        cars = new HashMap<>();
        for (i = 0; i < copy.board.length; i++) {
            for (j = 0; j < copy.board.length; j++) {
                if (copy.board[i][j] != '.') {
                    int dir = findDirection(i,j);
                    int len = 1;
                    if (dir == HORIZONTAL) {
                        int k = 0;
                        while (j+k < size && board[i][j+k] == board[i][j])
                            k++;
                        len = k;
                    }
                    if (dir == VERTICAL) {
                        int k = 0;
                        while (i+k < size && board[i+k][j] == board[i][j])
                            k++;
                        len = k;
                    }
                    Car newCar = new Car(dir, j, i, len);
                    newCar.setName(board[i][j]);
                    this.cars.putIfAbsent(board[i][j], newCar);
                }
            }
        }

        //copy parent
        setParent(copy.getParent());
    }

    public RushHour(String fileName) throws FileNotFoundException {

        //
        //	..CCC.
        //  ..XX..	// board[1][2] = X board[1][3] = X
        //  ..G...
        //  ..G..A
        //  ..G..A
        //  LL....
        board = new char[size][size];
        int i,j;
        File file= new File(fileName);
        Scanner reader = null;
        try {
            reader = new Scanner(file);
            for (i=0; i<size; i++) {
                String data = reader.nextLine();
                for (j=0; j<size; j++)
                    board[i][j] = data.charAt(j);
            }
        }
        catch (FileNotFoundException exception) {
            throw exception;
        }
        catch (Exception e) {
            throw new BadBoardException(e);
        }
        finally {
            if(reader!=null)
                reader.close();
        }

        // create list of cars
        cars = new HashMap<>();
        for (i = 0; i < board.length; i++) {
            for (j = 0; j < board.length; j++) {
                if (board[i][j] != '.') {
                    int dir = findDirection(i,j);
                    int len = 1;
                    if (dir == HORIZONTAL) {
                        int k = 0;
                        while (j+k < size && board[i][j+k] == board[i][j])
                            k++;
                        len = k;
                    }
                    if (dir == VERTICAL) {
                        int k = 0;
                        while (i+k < size && board[i+k][j] == board[i][j])
                            k++;
                        len = k;
                    }
                    Car newCar = new Car(dir, j, i, len);
                    newCar.setName(board[i][j]);
                    cars.putIfAbsent(board[i][j], newCar);
                }
            }
        }
    }

    /**
     * @param carName
     * @param direction
     * @param dist
     * Moves car with the given name for getLen() steps in the given direction
     * @throws IllegalMoveException if the move is illegal
     */
    public void makeMove(char carName, int direction, int dist) throws IllegalMoveException {
        Car car = cars.get(carName);

        if (car.getDir() == HORIZONTAL && (direction == UP || direction == DOWN)) {
            throw new IllegalMoveException("car" + carName + " tried moving vertically");
        }
        if (car.getDir() == VERTICAL && (direction == RIGHT || direction == LEFT)) {
            throw new IllegalMoveException("car" + carName + " tried moving horizontally");
        }
        checkLegalMove(car, direction, dist);
        switch (direction) {
            case RIGHT: {

                // erase from current position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()][car.getX()+j] ='.';

                // add to new position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()][car.getX()+dist+j] = carName;

                car.setX(car.getX() + dist);

                // create move
                this.move = carName + "R" + dist;

                return;
            }
            case LEFT: {
                // erase from current position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()][car.getX()+j] ='.';

                // add to new position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()][car.getX()-dist+j] = carName;

                car.setX(car.getX() - dist);

                // create move
                this.move = carName + "L" + dist;

                return;
            }
            case DOWN: {
                // erase from current position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()+j][car.getX()] ='.';

                // add to new position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()+dist+j][car.getX()] =carName;

                car.setY(car.getY() + dist);

                // create move
                this.move = carName + "D" + dist;

                return;
            }
            case UP: {

                // erase from current position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()+j][car.getX()] ='.';

                // add to new position
                for (int j = 0; j < car.getLen(); j++)
                    board[car.getY()-dist+j][car.getX()] =carName;

                car.setY(car.getY() - dist);

                // create move
                this.move = carName + "U" + dist;

                return;
            }
            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }

    }

    private boolean checkLegalMove(Car car, int direction, int dist) throws IllegalMoveException
    {
        switch (direction) {
            case RIGHT: {
                if (car.getX() + car.getLen() + dist > size) {
                    //throw new IllegalMoveException("move " + carName + " RIGHT " + dist + ": OUT OF BOUNDS");
                    return false;
                }
                for (int j = 0; j < dist; j++) {
                    if (board[car.getY()][car.getX() + car.getLen() + j] != '.') {
                        //throw new IllegalMoveException("move " + carName + " RIGHT " + dist + ": " + board[car.getY()][car.getX() + car.getLen() + j] + " IN A WAY");
                        return false;
                    }
                }
                return true;
            }
            case LEFT: {
                if (car.getX() - dist < 0) {
                    //throw new IllegalMoveException("move " + carName + " LEFT " + dist + ": OUT OF BOUNDS");
                    return false;
                }
                for (int j = 0; j < dist; j++) {
                    if (board[car.getY()][car.getX()-j-1] !='.') {
                        //throw new IllegalMoveException("move " + carName + " LEFT " + dist + ": " + board[car.getY()][car.getX()-j-1] + " IN A WAY");
                        return false;
                    }
                }
                return true;
            }
            case DOWN: {
                if (car.getY() + car.getLen() + dist > size) {
                    //throw new IllegalMoveException("move " + carName + " DOWN " + dist + ": OUT OF BOUNDS");
                    return false;
                }
                for (int j = 0; j < dist; j++) {
                    if (board[car.getY()+car.getLen()+j][car.getX()] !='.') {
                        // throw new IllegalMoveException("move " + carName + " DOWN " + dist + ": " + board[car.getY()][car.getX()+car.getLen()+j] + " IN A WAY");
                        return false;
                    }
                }
                return true;
            }
            case UP: {
                if (car.getY() - dist < 0) {
                    //throw new IllegalMoveException("move " + carName + " UP " + dist + ": OUT OF BOUNDS");
                    return false;
                }
                for (int j = 0; j < dist; j++) {
                    if (board[car.getY()-j-1][car.getX()] !='.') {
                        //throw new IllegalMoveException("move " + carName + " UP " + dist + ": " + board[car.getY()-j-1][car.getX()] + " IN A WAY");
                        return false;
                    }
                }
                return true;
            }
            default:
                throw new IllegalMoveException("Bad direction: " + direction);
        }
    }

    private int findDirection(int i, int j){
        if ((j>=1 && board[i][j] == board[i][j-1]) || (j<=size-2 && board[i][j] == board[i][j+1]))
            return HORIZONTAL;
        else if ((i>=1 && board[i][j] == board[i-1][j]) || (i<=size-2 && board[i][j] == board[i+1][j]))
            return VERTICAL;
        else
            throw new BadBoardException("board[" + i + "][" + j + "j]");

    }

    /**
     * @return true if and only if the board is solved,
     * i.e., the XX car is touching the right edge of the board
     */
    public boolean isSolved() {
        Car xCar = cars.get('X');
        return (xCar.getX()+xCar.getLen() == size);
    }

    public Set<RushHour> children() throws IllegalMoveException {

        Set<RushHour> neighbours = new HashSet<>();
        //using iterator to loop over the cars
        Iterator<Map.Entry<Character, Car>> itr = cars.entrySet().iterator();
        int dist;

        while(itr.hasNext())
        {
            Car car = itr.next().getValue();

            switch(car.getDir()){

            case HORIZONTAL: {
                    //RIGHT
                // calculating max dist for RIGHT
                int maxDistR = 0;
                for(int i=car.getX()+car.getLen()+1;i<size;i++)
                {
                    if(board[car.getX()][i] == '.')
                        maxDistR++;
                    else
                        break;
                }
                    //create children from right move
                    for (dist = 0; dist < maxDistR; dist++) {
                        //copy RushHour
                        RushHour child = new RushHour(this);
                        //makemove
                        child.makeMove(car.getName(), RIGHT, dist);
                        //add this child to neighbours
                        neighbours.add(child);

                    }

                    // LEFT
                // calculating max dist for LEFT
                int maxDistL = 0;
                for(int i=car.getX()-1;i>=0;i--)
                {
                    if(board[i][car.getY()] == '.')
                        maxDistL++;
                    else
                        break;
                }
                    //create children from left move
                    for (dist = 0; dist < maxDistL; dist++) {
                        //copy RushHour
                        RushHour child = new RushHour(this);
                        //makemove
                        child.makeMove(car.getName(), LEFT, dist);
                        //add this child to neighbours
                        neighbours.add(child);
                    }
                }

            case VERTICAL: {
                    // TOP
                // calculating max dist for TOP
                int maxDistT = 0;
                for(int i=car.getY()-1;i>=0;i--)
                {
                    if(board[car.getX()][i] == '.')
                        maxDistT++;
                    else
                        break;
                }
                    //create children from top move
                    for(dist=0;dist<maxDistT;dist++) {
                        //copy RushHour
                        RushHour child = new RushHour(this);
                        //makemove
                        child.makeMove(car.getName(), UP, dist);
                        //add this child to neighbours
                        neighbours.add(child);
                    }

                    // DOWN
                    // calculating max dist for DOWN
                    int maxDistD = 0;
                    for(int i=car.getY()+car.getLen()+1;i<size;i++)
                    {
                        if(board[car.getX()][i] == '.')
                            maxDistD++;
                        else
                            break;
                    }
                    //create children from down move
                    for(dist=0;dist<maxDistD;dist++) {
                        //copy RushHour
                        RushHour child = new RushHour(this);
                        //Makemove
                        child.makeMove(car.getName(), DOWN, dist);
                        //add this child to neighbours
                        neighbours.add(child);
                    }
            }
            }
        }
        return neighbours;
    }

    public void setParent(RushHour v){
        this.parent = v;
    }

    public RushHour getParent(){
        return parent;
    }

    public String getMove(){
        return move;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (!(other instanceof Car))
            return false;

        return (this.hashCode() == other.hashCode());
    }

    //What if the Hash Code included the move !!???!
    @Override
    public int hashCode() {
        int hashcode = 1;
        for(int i=0; i<size;i++) {
            for (int j = 0; j < size; j++) {
                if(board[i][j] == '.')
                    hashcode += ((int) board[i][j])*i*j;
                else
                    hashcode += ((int) board[i][j])*i*j*(Math.pow(i,j));
            }
        }

        //What if the Hash Code included the move !!???!
        //Compute Hash Code
        final int prime = 1733;
        this.hashCode = hashcode * prime;
        return hashCode;
    }

    public int getF() {
        return f;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }
}
