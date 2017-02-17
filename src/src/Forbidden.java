package src.src;

import tester.*;

import java.awt.*;
import java.util.ArrayList;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.util.Iterator;
import java.util.Random;

// Represents a single square of the game area
class Cell {
    // represents absolute height of this cell, in feet
    double height;
    // In logical coordinates, with the origin at the top-left corner of the screen
    int x;
    int y;
    // the four adjacent cells to this one
    Cell left;
    Cell top;
    Cell right;
    Cell bottom;
    // reports whether this cell is flooded or not
    boolean isFlooded;
    //Constructor
    Cell(double height, int x, int y) {
        this.height = height;
        this.x = x;
        this.y = y;
        isFlooded = false;
    }
    //Makes c the top cell
    void makeTop(Cell c) {
        this.top = c;
    }
    //Makes c the bottom cell
    void makeBot(Cell c) {
        this.bottom = c;
    }
    //Makes c the left cell
    void makeLeft(Cell c) {
        this.left = c;
    }
    //Makes c the right cell
    void makeRight(Cell c) {
        this.right = c;
    }
    //Draws the cell
    WorldImage draw(int waterheight) {
        double colorconst = Math.min(255,
                255 - (ForbiddenIslandWorld.ISLAND_HEIGHT - this.height) * 8);
        if (this.isFlooded) {
            return new RectangleImage(ForbiddenIslandWorld.CELL_SIZE,
                    ForbiddenIslandWorld.CELL_SIZE,
                    OutlineMode.SOLID, new Color(0, 0, Math.min((int) (50 + this.height * 5),
                        255)));
        }
        else if (waterheight > this.height) {
            return new RectangleImage(ForbiddenIslandWorld.CELL_SIZE,
                    ForbiddenIslandWorld.CELL_SIZE, OutlineMode.SOLID, new Color((int) (150 +
                        (waterheight - this.height) * 2), (int) (64 - this.height), 0));
        }
        else {
            return new RectangleImage(ForbiddenIslandWorld.CELL_SIZE,
                    ForbiddenIslandWorld.CELL_SIZE, OutlineMode.SOLID,
                    new Color((int) (colorconst),
                            (int) Math.min(255, 255 -
                                    (ForbiddenIslandWorld.ISLAND_HEIGHT - this.height) * 5),
                            (int) (colorconst)));

        }
    }
    //Checks for flooded neighbors
    boolean nextFlooded() {
        return this.bottom.isFlooded || this.top.isFlooded ||
                this.right.isFlooded || this.left.isFlooded;
    }
    //Runs floodfill on the given cell
    void floodFill(int wh) {
        if (!(this.isFlooded) && this.height < wh) {
            this.isFlooded = true;
            this.top.floodFill(wh);
            this.bottom.floodFill(wh);
            this.left.floodFill(wh);
            this.right.floodFill(wh);
        }
    }
    //Checks if two cells have the same position
    boolean samePos(Cell that) {
        return this.x == that.x &&
                this.y == that.y;
    }
}

//Represents an ocean cell
class OceanCell extends Cell {
    //Constructor
    OceanCell(int x, int y) {
        super(0, x, y);
        this.isFlooded = true;
    }
    //Draws the ocean cell
    WorldImage draw(int waterheight) {
        return new RectangleImage(10, 10, OutlineMode.SOLID, Color.blue);
    }
}

//Represents a list of something
interface IList<T> extends Iterable<T> {
    //Determines if an item is a Cons
    boolean isCons();
    //Treats a list as a cons
    ConsList<T> asCons();
}

//Representation of a non-empty list
class ConsList<T> implements IList<T> {
    T first;
    IList<T> rest;
    //Constructor
    ConsList(T first, IList<T> rest) {
        this.first = first;
        this.rest = rest;
    }
    //Determines if the list is a cons
    public boolean isCons() {
        return true;
    }
    //Treats the list like a cons
    public ConsList<T> asCons() {
        return this;
    }
    //Iterator method
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }
}

//Representation of an empty list
class MtList<T> implements IList<T> {
    //Determines if the list is cons
    public boolean isCons() {
        return false;
    }
    //Treats the list as cons
    public ConsList<T> asCons() {
        return null;
    }
    //Iterator method
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }
}

//Iterators for ILists
class IListIterator<T> implements Iterator<T> {
    // the list of items that this iterator iterates over
    IList<T> items;
    // the index of the next item to be returned
    int nextIdx;
    // Construct an iterator for a given IList
    IListIterator(IList<T> items) {
        this.items = items;
        this.nextIdx = 0;
    }
    // Determines if there is a next item
    public boolean hasNext() {
        return this.items.isCons();
    }
    // Determines the next item
    public T next() {
        ConsList<T> itemsAsCons = this.items.asCons();
        T answer = itemsAsCons.first;
        this.items = itemsAsCons.rest;
        return answer;
    }
    // Implements the remove method, but it doesn't do anything
    public void remove() {
        throw new UnsupportedOperationException("Don't do this!");
    }
}

//Class representing the pilot
class Pilot {
    Cell c;
    int parts;
    //Constructor for random starting point
    Pilot(IList<Cell> ilc) {
        this.c = new Utils().start(ilc);
        this.parts = 0;
    }
    //Initial constructor
    Pilot() {
        this.c = new Cell(32, 32, 32);
        this.parts = 0;
    }
}

//Class for utilities multiple classes need
class Utils {
    //Generates a random non-flooded cell
    Cell start(IList<Cell> ilc) {
        ArrayList<Cell> lands = new ArrayList<Cell>();
        for (Cell c : ilc) {
            if (!(c.isFlooded)) {
                lands.add(c);
            }
        }
        return lands.get(new Random().nextInt(lands.size()));
    }
}

//Class representing all targets
class Target {
    Cell c;
    //Creates a target with a random position
    Target(IList<Cell> ilc) {
        this.c = new Utils().start(ilc);
    }
    //Creates the target
    Target() {
        this.c = new Cell(0, 0, 0);
    }
    //Draws the target
    WorldImage draw() {
        return new CircleImage(5, OutlineMode.SOLID, Color.PINK);
    }
    //Updates the list of targets (Deletes if pilot picks up)
    Target update(ArrayList<Target> stuff, Pilot me) {
        me.parts += 1;
        return this;
    }
}

class Helicopter extends Target {
    //Creates a helicopter at the highest point
    Helicopter(IList<Cell> ilc) {
        this.c = heliStart(ilc);
    }
    //Draws the helicopter
    WorldImage draw() {
        return new FromFileImage("KEY_Helicopter_sprite.png");
    }
    //Updates the list (deletes if all pilot has all 3 parts)
    Target update(ArrayList<Target> stuff, Pilot me) {
        if (me.parts == 3) {
            return this;
        }
        else {
            return new Target();
        }
    }
    //Gets the tallest cell for helicopter
    Cell heliStart(IList<Cell> ilc) {
        Cell max = new Cell(0, 0, 0);
        for (Cell top : ilc) {
            if (top.height > max.height) {
                max = top;
            }
        }
        return max;
    }
}

//Class representing the world
class ForbiddenIslandWorld extends World {
    // All the cells of the game, including the ocean
    IList<Cell> board;
    // the current height of the ocean
    int waterHeight;
    //Constants used in the game
    static final int ISLAND_SIZE = 64;
    ArrayList<ArrayList<Double>> height;
    static final int ISLAND_HEIGHT = 32;
    ArrayList<ArrayList<Cell>> cells;
    static final int CELL_SIZE = 10;
    //The pilot the player controls
    Pilot me = new Pilot();
    //The stuff on the ground
    ArrayList<Target> stuff = new ArrayList<Target>();
    //Counts the ticks
    int ticks = 0;

    //Constructor (Takes in "mountain" for one mountain)
    ForbiddenIslandWorld(String type) {
        //Create the grid using arraylists
        this.height = new ArrayList<ArrayList<Double>>();
        this.cells = new ArrayList<ArrayList<Cell>>();

        //creates the island based on input
        if (type.equals("mountain")) {
            mountain();
        }
        else if (type.equals("diamond")) {
            randomDiamond();
        }
        else {
            terrain();
        }
        //Adds the cells to the list of cells based on the heights
        for (int i = 0; i <= ForbiddenIslandWorld.ISLAND_SIZE; i += 1) {
            cells.add(new ArrayList<Cell>());
            for (int j = 0; j <= ForbiddenIslandWorld.ISLAND_SIZE; j += 1) {
                if (this.height.get(i).get(j) <= 0) {
                    cells.get(i).add(new OceanCell(i * ForbiddenIslandWorld.CELL_SIZE,
                            j * ForbiddenIslandWorld.CELL_SIZE));
                }
                else {
                    cells.get(i).add(new Cell(this.height.get(i).get(j),
                            i * ForbiddenIslandWorld.CELL_SIZE,
                            j * ForbiddenIslandWorld.CELL_SIZE));
                }
            }
        }

        //Fills in the cell's borders
        for (int i = 0; i <= ForbiddenIslandWorld.ISLAND_SIZE; i += 1) {
            for (int j = 0; j <= ForbiddenIslandWorld.ISLAND_SIZE; j += 1) {
                if (j + 1 > ForbiddenIslandWorld.ISLAND_SIZE) {
                    this.cells.get(i).get(j).bottom = this.cells.get(i).get(j);
                }
                else {
                    this.cells.get(i).get(j).bottom = this.cells.get(i).get(j + 1);
                }
                if (j - 1 < 0) {
                    this.cells.get(i).get(j).top = this.cells.get(i).get(j);
                }
                else {
                    this.cells.get(i).get(j).top = this.cells.get(i).get(j - 1);
                }
                if (i + 1 > ForbiddenIslandWorld.ISLAND_SIZE) {
                    this.cells.get(i).get(j).right = this.cells.get(i).get(j);
                }
                else {
                    this.cells.get(i).get(j).right = this.cells.get(i + 1).get(j);
                }
                if (i - 1 < 0) {
                    this.cells.get(i).get(j).left = this.cells.get(i).get(j);
                }
                else {
                    this.cells.get(i).get(j).left = this.cells.get(i - 1).get(j);
                }
            }
        }
        //Turns the arraylist into a List<T>
        this.board = new MtList<Cell>();
        for (ArrayList<Cell> row : cells) {
            for (Cell cur : row) {
                this.board = new ConsList<Cell>(cur, this.board);
            }
        }
        //Generates correct pilot position
        me = new Pilot(board);
        //Creates an ArrayList of targets
        stuff.add(new Target(board));
        stuff.add(new Target(board));
        stuff.add(new Target(board));
        stuff.add(new Helicopter(board));
    }

    //Ticks and what not
    public void onTick() {
        //Increases waterheight every 10 ticks and floods
        if (this.ticks % 10 == 0 && this.ticks != 0) {
            this.waterHeight += 1;
            for (Cell cur : this.board) {
                if (cur.nextFlooded() && cur.height <= this.waterHeight && !(cur.isFlooded)) {
                    cur.floodFill(waterHeight);
                }
            }
        }
        this.ticks += 1;
    }
    //Decides when to end the world
    public WorldEnd worldEnds() {
        //If player is on flooded cell
        if (this.me.c.isFlooded) {
            return new WorldEnd(true, this.finalScene("ur garbo"));
        }
        //If you collect all the targets
        else if (this.stuff.size() == 0) {
            return new WorldEnd(true, this.finalScene("ur ok"));
        }
        else {
            return new WorldEnd(false, this.makeScene());
        }
    }
    //Creates the last scene
    WorldScene finalScene(String msg) {
        WorldScene end = this.makeScene();
        end.placeImageXY(new TextImage(msg, 32, Color.BLACK), ForbiddenIslandWorld.CELL_SIZE *
                        ForbiddenIslandWorld.ISLAND_SIZE / 2,
                ForbiddenIslandWorld.CELL_SIZE * ForbiddenIslandWorld.ISLAND_SIZE / 2);
        return end;
    }
    //Key presses
    public void onKeyEvent(String key) {
        Target del = new Target();
        //Moves left
        if (key.equals("left") && !(me.c.left.isFlooded)) {
            for (Target t : this.stuff) {
                if (t.c.samePos(me.c.left)) {
                    del = t.update(this.stuff, me);
                }
            }
            this.stuff.remove(del);
            me.c = me.c.left;
        }
        //Moves right
        else if (key.equals("right") && !(me.c.right.isFlooded)) {
            for (Target t : this.stuff) {
                if (t.c.samePos(me.c.right)) {
                    del = t.update(this.stuff, me);
                }
            }
            this.stuff.remove(del);
            me.c = me.c.right;
        }
        //Move down
        else if (key.equals("down") && !(me.c.bottom.isFlooded)) {
            for (Target t : this.stuff) {
                if (t.c.samePos(me.c.bottom)) {
                    del = t.update(this.stuff, me);
                }
            }
            this.stuff.remove(del);
            me.c = me.c.bottom;
        }
        //Move up
        else if (key.equals("up") && !(me.c.top.isFlooded)) {
            for (Target t : this.stuff) {
                if (t.c.samePos(me.c.top)) {
                    del = t.update(this.stuff, me);
                }
            }
            this.stuff.remove(del);
            me.c = me.c.top;
        }
        //New random terrain
        else if (key.equals("t")) {
            this.board = new ForbiddenIslandWorld("").board;
            this.reset();
        }
        //New mountain
        else if (key.equals("m")) {
            this.board = new ForbiddenIslandWorld("mountain").board;
            this.reset();
        }
        //New random heights
        else if (key.equals("r")) {
            this.board = new ForbiddenIslandWorld("diamond").board;
            this.reset();
        }
    }
    //Resets the world to starting version
    void reset() {
        this.waterHeight = 0;
        this.ticks = 0;
        this.me = new Pilot(this.board);
        this.stuff = new ArrayList<Target>();
        this.stuff.add(new Target(this.board));
        this.stuff.add(new Target(this.board));
        this.stuff.add(new Target(this.board));
        this.stuff.add(new Helicopter(this.board));
    }
    //Draws the scene
    public WorldScene makeScene() {
        WorldScene world = getEmptyScene();
        //Draws the initial board
        for (Cell c : this.board) {
            world.placeImageXY(c.draw(this.waterHeight),
                    c.x + ForbiddenIslandWorld.CELL_SIZE / 2,
                    c.y + ForbiddenIslandWorld.CELL_SIZE / 2);
        }
        //Draws objects to pick up
        for (Target t : this.stuff) {
            world.placeImageXY(t.draw(), t.c.x + ForbiddenIslandWorld.CELL_SIZE / 2,
                    t.c.y + ForbiddenIslandWorld.CELL_SIZE / 2);
        }
        //Places the pilot
        world.placeImageXY(new FromFileImage("Sprite_Munitions_Navy_DP_aim.png"),
                me.c.x + ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2,
                me.c.y + ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2);
        return world;
    }
    //Gets the manhattan distance of two points
    double getManDist(int i, int j) {
        return Math.abs(i - ForbiddenIslandWorld.ISLAND_SIZE / 2) +
                Math.abs(j - ForbiddenIslandWorld.ISLAND_SIZE / 2);
    }
    //Creates a diamond island with random heights
    void randomDiamond() {
        this.height = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i <= ForbiddenIslandWorld.ISLAND_SIZE; i += 1) {
            height.add(new ArrayList<Double>());
            for (int j = 0; j <= ForbiddenIslandWorld.ISLAND_SIZE; j += 1) {
                if (this.getManDist(i, j) >= 32) {
                    this.height.get(i).add((double) 0);
                }
                else {
                    this.height.get(i).add((double) (1 + (new Random().nextInt(
                            ForbiddenIslandWorld.ISLAND_HEIGHT))));
                }
            }
        }
    }
    //Creates a new mountain
    void mountain() {
        for (int i = 0; i <= ForbiddenIslandWorld.ISLAND_SIZE; i += 1) {
            height.add(new ArrayList<Double>());
            for (int j = 0; j <= ForbiddenIslandWorld.ISLAND_SIZE; j += 1) {
                height.get(i).add(ForbiddenIslandWorld.ISLAND_HEIGHT - this.getManDist(i, j));
            }
        }
    }
    //Creates a new random terrain island
    void terrain() {
        //Creates the heights all at 0
        for (int i = 0; i <= ForbiddenIslandWorld.ISLAND_SIZE; i += 1) {
            height.add(new ArrayList<Double>());
            for (int j = 0; j <= ForbiddenIslandWorld.ISLAND_SIZE; j += 1) {
                height.get(i).add((double) 0);
            }
        }
        //Sets the initial values
        height.get(ForbiddenIslandWorld.ISLAND_SIZE / 2).set(
                ForbiddenIslandWorld.ISLAND_SIZE / 2, (double) ForbiddenIslandWorld.ISLAND_HEIGHT);
        height.get(ForbiddenIslandWorld.ISLAND_SIZE / 2).set(0, 1.0);
        height.get(0).set(ForbiddenIslandWorld.ISLAND_SIZE / 2, 1.0);
        height.get(ForbiddenIslandWorld.ISLAND_SIZE).set(ForbiddenIslandWorld.ISLAND_SIZE / 2, 1.0);
        height.get(ForbiddenIslandWorld.ISLAND_SIZE / 2).set(ForbiddenIslandWorld.ISLAND_SIZE, 1.0);
        //Generates the terrain recursively
        based(0, ForbiddenIslandWorld.ISLAND_SIZE / 2, 0, ForbiddenIslandWorld.ISLAND_SIZE / 2,
                0, 0, ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE / 2);
        based(ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE,
                ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE, 0, 0,
                ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE / 2);
        based(0, ForbiddenIslandWorld.ISLAND_SIZE / 2, 0, ForbiddenIslandWorld.ISLAND_SIZE / 2,
                ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE / 2,
                ForbiddenIslandWorld.ISLAND_SIZE, ForbiddenIslandWorld.ISLAND_SIZE);
        based(ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE,
                ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE,
                ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE / 2,
                ForbiddenIslandWorld.ISLAND_SIZE, ForbiddenIslandWorld.ISLAND_SIZE);
    }
    void based(int xtl, int xtr, int xbl, int xbr, int ytl, int ytr, int ybl, int ybr) {
        //Sets values for the heights of cells
        double tl = height.get(xtl).get(ytl);
        double tr = height.get(xtr).get(ytr);
        double bl = height.get(xbl).get(ybl);
        double br = height.get(xbr).get(ybr);
        double t = (Math.random() * 2 - 1) * (xtr - xtl) + (tl + tr) / 2;
        double b = (Math.random() * 2 - 1) * (xtr - xtl) + (bl + br) / 2;
        double l = (Math.random() * 2 - 1) * (xtr - xtl) + (tl + bl) / 2;
        double r = (Math.random() * 2 - 1) * (xtr - xtl) + (tr + br) / 2;
        double m = (Math.random() * 2 - 1) * (xtr - xtl) + (tl + tr + bl + br) / 4;
        //Only set values if they are equal to 0
        if (height.get(xtl + ((xtr - xtl) / 2)).get(ytl) == 0) {
            height.get(xtl + ((xtr - xtl) / 2)).set(ytl, t);
        }
        if (height.get(xtl + ((xtr - xtl) / 2)).get(ybl) == 0) {
            height.get(xtl + ((xtr - xtl) / 2)).set(ybl, b);
        }
        if (height.get(xtl).get(ytl + ((ybl - ytl) / 2)) == 0) {
            height.get(xtl).set(ytl + ((ybl - ytl) / 2), l);
        }
        if (height.get(xtr).get(ytr + ((ybr - ytr)) / 2) == 0) {
            height.get(xtr).set(ytr + ((ybr - ytr)) / 2, r);
        }
        if (height.get(xtl + ((xtr - xtl) / 2)).get(ytr + ((ybr - ytr) / 2)) == 0) {
            height.get(xtl + ((xtr - xtl) / 2)).set(ytr + ((ybr - ytr) / 2), m);
        }
        //Recursively call only if length divided by two is greater than or equal to 2
        if ((xtr - xtl) / 2 >= 2) {
            based(xtl, xtl / 2 + xtr / 2, xbl, xbl / 2 + xbr / 2, ytl, ytr, ytl / 2 + ybl / 2,
                    ytr / 2 + ybr / 2);
            based(xtl + ((xtr - xtl) / 2), xtr, xbl + (xbr - xbl) / 2, xbr, ytl, ytr,
                    ytl / 2 + ybl / 2, ytr / 2 + ybr / 2);
            based(xtl, xtl / 2 + xtr / 2, xbl, xbl / 2 + xbr / 2, ytl / 2 + ybl / 2,
                    ytr / 2 + ybr / 2, ybl, ybr);
            based(xtl + ((xtr - xtl) / 2), xtr, xbl + (xbr - xbl) / 2, xbr, ytl / 2 + ybl / 2,
                    ytr / 2 + ybr / 2, ybl, ybr);
        }
    }
}

//Class with examples
class ExamplesForbidden {
    Cell normal = new Cell(31, 20, 20);
    OceanCell ocean = new OceanCell(50, 51);
    MtList<Cell> mt = new MtList<Cell>();
    ConsList<Cell> clc = new ConsList<Cell>(this.normal, this.mt);
    IListIterator<Cell> iter = new IListIterator<Cell>(this.clc);
    Target tar = new Target();
    Pilot me = new Pilot();
    IList<Cell> test_board = new ConsList<Cell>(new Cell(5, 5, 5), new MtList<Cell>());
    Helicopter heli = new Helicopter(test_board);
    Cell cell2 = new Cell(10, 10, 10);
    Cell cell3 = new Cell(10, 10, 10);

    //Initializes data
    void initData() {
        this.normal = new Cell(31, 20, 20);
        this.ocean = new OceanCell(50, 51);
        this.iter = new IListIterator<Cell>(this.clc);
        this.cell3.isFlooded = true;
        this.tar = new Target();
    }
    //Tests the methods that change values
    boolean testMutation(Tester t) {
        initData();
        this.normal.makeBot(this.ocean);
        this.normal.makeTop(this.ocean);
        this.normal.makeLeft(this.ocean);
        this.normal.makeRight(this.ocean);
        this.normal.floodFill(40);
        this.cell2.makeBot(this.cell2);
        this.cell2.makeLeft(this.cell2);
        this.cell2.makeRight(this.cell2);
        this.cell2.makeTop(this.cell2);
        this.cell2.floodFill(5);
        return t.checkExpect(this.normal.bottom, this.ocean) &&
                t.checkExpect(this.normal.top, this.ocean) &&
                t.checkExpect(this.normal.left, this.ocean) &&
                t.checkExpect(this.normal.right, this.ocean) &&
                t.checkExpect(this.normal.nextFlooded(), true) &&
                t.checkExpect(cell2.nextFlooded(), false) &&
                t.checkExpect(this.normal.isFlooded, true) &&
                t.checkExpect(this.cell2.isFlooded, false) &&
                t.checkExpect(this.normal.bottom.samePos(this.normal.right), true) &&
                t.checkExpect(this.normal.samePos(this.normal.right), false) &&
                t.checkExpect(this.heli.update(new ArrayList<Target>(), this.me), new Target()) &&
                t.checkExpect(this.cell2.draw(12), new RectangleImage(10, 10, OutlineMode.SOLID,
                        new Color(154, 54, 0))) &&
                t.checkExpect(this.cell3.draw(20), new RectangleImage(10, 10, OutlineMode.SOLID,
                        new Color(0, 0, 100))) &&
                t.checkExpect(tar.draw(), new CircleImage(7, OutlineMode.SOLID, Color.gray));
    }
    //Tests methods in the iterator class
    boolean testIter(Tester t) {
        initData();
        return t.checkExpect(this.iter.hasNext(), true) &&
                t.checkExpect(this.iter.next(), this.normal) &&
                t.checkExpect(this.iter.hasNext(), false) &&
                t.checkException(new UnsupportedOperationException("Don't do this!"),
                        this.iter, "remove");
    }
    //Tests all of the other methods
    boolean testMethods(Tester t) {
        initData();
        tar.update(new ArrayList<Target>(), me);
        tar.update(new ArrayList<Target>(), me);
        tar.update(new ArrayList<Target>(), me);
        return t.checkExpect(this.mt.isCons(), false) &&
                t.checkExpect(this.clc.isCons(), true) &&
                t.checkExpect(this.mt.asCons(), null) &&
                t.checkExpect(this.clc.asCons(), this.clc) &&
                t.checkExpect(this.normal.draw(5), new RectangleImage(10, 10,
                         OutlineMode.SOLID, new Color(247, 250, 247))) &&
                t.checkExpect(this.ocean.draw(0), new RectangleImage(10, 10,
                        OutlineMode.SOLID, Color.blue)) &&
                t.checkExpect(this.mt.iterator(), new IListIterator<Cell>(this.mt)) &&
                t.checkExpect(this.clc.iterator(), this.iter) &&
                t.checkExpect(this.tar.draw(),
                        new CircleImage(10, OutlineMode.SOLID, Color.gray)) &&
                t.checkExpect(this.me.parts, 3) &&
                t.checkExpect(this.heli.update(new ArrayList<Target>(), me), this.heli);
    }
    //Runs the game
    public static void main(String[] args) {
        ForbiddenIslandWorld mount = new ForbiddenIslandWorld("mountain");
        ForbiddenIslandWorld rand = new ForbiddenIslandWorld("diamond");
        ForbiddenIslandWorld terrain = new ForbiddenIslandWorld("");
        terrain.bigBang((ForbiddenIslandWorld.ISLAND_SIZE + 1) * (ForbiddenIslandWorld.CELL_SIZE),
                (ForbiddenIslandWorld.ISLAND_SIZE + 1) * (ForbiddenIslandWorld.CELL_SIZE), .25);
    }
}