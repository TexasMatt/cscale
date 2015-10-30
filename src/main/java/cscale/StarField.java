package cscale;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * StarField class maintains the star field and mine field information,
 * decorates the field output for printing and sends commands to the ship.
 * 
 * @author msmith
 *
 */
public class StarField {
	
	/*
	 * I started by using a 2-dimensional array for the star field,
	 * but this was not ideal for resizing the field as the 
	 * ship moves. Changing to a 2-dimensional LinkedList since
	 * it is easy to add or remove elements on either side.
	 * 
	 *  Integer [][] starField; 
	 */
	LinkedList<LinkedList<Integer>> starField;
	
	/*
	 * Using sets to retrieve the min and max mine coordinates
	 * used as input to calculate minimum the star field boundaries.
	 */
	Set<String> minesMap;
	Set<Integer> minesXSet;
	Set<Integer> minesYSet;
	
	SpaceShip ship;
	
	int startingMineCount = 0;
	boolean minesPast = false;
	
	/**
	 * Creates a Starfield and sets the initial ship co-ordinates as
	 * well as starting mine count.
	 * 
	 * @param fieldData
	 */
	public StarField(List<String[]> fieldData) {
		starField = new LinkedList<LinkedList<Integer>>();
		minesMap = new TreeSet<String>();
		minesXSet = new TreeSet<Integer>();
		minesYSet = new TreeSet<Integer>();
				
		// Transform field letters into ints
		for(int i = 0; i < fieldData.size(); i++) {
			LinkedList<Integer> row = new LinkedList<Integer>();
			starField.add(row);
			for (int j = 0; j < fieldData.get(i).length; j++) {
				Integer cell = letterToInteger(fieldData.get(i)[j]);
				if(cell != null) {
					startingMineCount++;
					minesMap.add(getMineKey(i, j));
					minesXSet.add(j);
					minesYSet.add(i);
				}
				row.add(cell);
			}
		}
		
		// Calculate ship in it's starting co-ordinates
		ship = new SpaceShip((fieldData.get(0).length/2),(fieldData.size()/2));
		
		// This first input field could be verbose, so trim before first print.
		trimStarField();
		
	}

	/**
	 * Drops the ship 1km down into the cuboid
	 */
	public void dropShip() {
		for (List<Integer> row : starField) {
			for (int i = 0; i < row.size(); i++) {
				Integer cell = row.get(i);
				if(cell != null) {
					cell--;
				}
				row.set(i, cell);
			}
		}
	}
	
	/**
	 * Executes a firing pattern
	 * 
	 * @param pattern
	 */
	public void shipFire(String pattern) {
		ship.fire();
		// Calculate pattern
		switch (pattern.toLowerCase()) {
		case App.FIRE_ALPHA:
			fireOffset(-1, -1);
			fireOffset(-1,  1);
			fireOffset( 1, -1);
			fireOffset( 1,  1);
			break;
		case App.FIRE_BETA:
			fireOffset(-1,  0);
			fireOffset( 0, -1);
			fireOffset( 0,  1);
			fireOffset( 1,  0);
			break;
		case App.FIRE_GAMMA:
			fireOffset(-1, 0);
			fireOffset( 0, 0);
			fireOffset( 1, 0);
			break;
		case App.FIRE_DELTA:
			fireOffset(0, -1);
			fireOffset(0,  0);
			fireOffset(0,  1);
			break;
		default:
			break;
		}
		trimStarField();
	}

	/**
	 * Clears a mine at the coordinate if it is still below the ship.
	 * 
	 * @param x
	 * @param y
	 */
	private void fireOffset(int x, int y) {
		if ((y + ship.getY()) >= 0 && 
			y < starField.size() && 
			(x + ship.getX()) >= 0 && 
			x < starField.get(0).size()) {
			
			Integer cell = starField.get(ship.getY() + y).get(ship.getX() + x);
			
			if(cell != null && cell > 0) {
				starField.get(ship.getY() + y).set(ship.getX() + x, null);
			}
		}
	}
	
	/**
	 * Moves the ship in the starfield
	 * 
	 * @param direction
	 */
	public void moveShip(String direction) {
		ship.move(direction);
		
		// Align star field
		if (minesMap.size() > 0 &&
			shouldExpandField(direction)) {
			expandField(direction);
		}
		
		trimStarField();
	}

	/**
	 * Evaluates the star field to determine if there is sufficient space
	 * to center the ship.
	 * 
	 * @param direction
	 * @return true if the star field needs to be expanded.
	 */
	private boolean shouldExpandField(String direction) {
	
		int minX, maxX, xMaxRadius, minY, maxY, yMaxRadius;
		xMaxRadius = 0;
		xMaxRadius = caculateMaxRadius(minesXSet, ship.getX());
		
		maxX = ship.getX() + xMaxRadius;
		minX = ship.getX() - xMaxRadius;

		yMaxRadius = caculateMaxRadius(minesYSet, ship.getY());
		
		maxY = ship.getY() + yMaxRadius;
		minY = ship.getY() - yMaxRadius;
		
		boolean shouldExpand = false;
		
		switch (direction.toLowerCase()) {
		case App.MOVE_NORTH:
			shouldExpand = minY < 0;
			break;
		case App.MOVE_SOUTH:
			shouldExpand = maxY > starField.size();		
			break;
		case App.MOVE_EAST:
			shouldExpand = maxX > starField.get(0).size();
			break;
		case App.MOVE_WEST:
			shouldExpand = minX < 0;
			break;
		default:
			break;
		}
		
		return shouldExpand;
		
	}

	/**
	 * Trims the star field to remove empty rows and columns
	 * on either side.
	 */
	private void trimStarField() {
		remapMines();
		
		// If there are no more mines, the starfield is one '.'
		if(minesMap.size() == 0) {
			starField = new LinkedList<LinkedList<Integer>>();
			LinkedList<Integer> row = new LinkedList<Integer>();
			row.add(null);
			starField.add(row);
			return;
		}
		
		// X-Trim
		int minX, maxX, xMaxRadius;
		xMaxRadius = caculateMaxRadius(minesXSet, ship.getX());
		
		maxX = ship.getX() + xMaxRadius;
		minX = ship.getX() - xMaxRadius;
		
		for (int i = starField.get(0).size()-1 ; i > maxX; i--) {
			starField.forEach(e -> e.removeLast());
		}
		for (int i = 0 ; i < minX; i++) {
			starField.forEach(e -> e.removeFirst());
			ship.setX(ship.getX() - 1);
		}
		
		// Y-Trim
		int minY, maxY, yMaxRadius;
		yMaxRadius = caculateMaxRadius(minesYSet, ship.getY());
		
		maxY = ship.getY() + yMaxRadius;
		minY = ship.getY() - yMaxRadius;
		
		for (int i = starField.size()-1 ; i > maxY; i--) {
			starField.removeLast();
		}
		for (int i = 0 ; i < minY; i++) {
			starField.removeFirst();
			ship.setY(ship.getY() - 1);
		}
		remapMines();
	}

	/**
	 * Calculated the distance to the farthest mine in either direction.
	 * 
	 * @param set
	 * @param currentPos
	 * @return
	 */
	private int caculateMaxRadius(Set<Integer> set, int currentPos) {
		
		int maxRadius, minMine, maxMine, minShip2Mine, maxShip2Mine;
		
		minMine = Collections.min(set,null);
		maxMine = Collections.max(set,null);
		minShip2Mine = Math.abs(minMine - currentPos);
		maxShip2Mine = Math.abs(maxMine - currentPos);
		maxRadius = minShip2Mine > maxShip2Mine ? minShip2Mine : maxShip2Mine;
		return maxRadius;
	}
	
	/**
	 * When the star field changes, the co-ordinates on the mines can change.
	 */
	private void remapMines() {
		minesMap.clear();
		minesXSet.clear();
		minesYSet.clear();
		
		for (int i = 0; i < starField.size(); i++) {
			for (int j = 0; j < starField.get(i).size(); j++) {
				Integer cell = starField.get(i).get(j);
				if(cell != null) {
					minesMap.add(getMineKey(i, j));
					minesXSet.add(j);
					minesYSet.add(i);
				}
			}
		}
	}
	
	/**
	 * Creates a unique value for the mine coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private String getMineKey(int x, int y) {
		return x+"_"+y;
	}

	/**
	 * Expands the star field in a particular direction
	 * 
	 * @param direction
	 */
	private void expandField(String direction) {
		switch(direction.toLowerCase()) {
			case App.MOVE_NORTH :
				LinkedList<Integer> addNorth1 = new LinkedList<Integer>();
				starField.get(0).stream()
					.map(c -> (Integer)null)
					.forEach(addNorth1::add);
				starField.addFirst(addNorth1);
				LinkedList<Integer> addNorth2 = new LinkedList<Integer>();
				addNorth1.stream()
					.forEach(addNorth2::add);
				starField.addFirst(addNorth2);
				// When the field expands the ships coordinates change
				ship.setY(ship.getY()+2);
				break;
			case App.MOVE_SOUTH :
				LinkedList<Integer> addSouth1 = new LinkedList<Integer>();
				starField.get(0).stream()
					.map(c -> (Integer)null)
					.forEach(addSouth1::add);
				starField.addLast(addSouth1);
				LinkedList<Integer> addSouth2 = new LinkedList<Integer>();
				addSouth1.stream()
					.forEach(addSouth2::add);
				starField.addLast(addSouth2);
				break;
			case App.MOVE_EAST :
				starField.forEach(e -> e.addLast(null));
				starField.forEach(e -> e.addLast(null));
				break;
			case App.MOVE_WEST :
				starField.forEach(e -> e.addFirst(null));
				starField.forEach(e -> e.addFirst(null));
				// When the field expands the ships coordinates change
				ship.setX(ship.getX()+2);
				break;
			default:
				break;
		}
	}

	/**
	 * Helper method to convert cell character values into integers 
	 * 
	 * @param cell
	 * @return
	 */
	protected Integer letterToInteger(String cell) {
		if(cell.matches("[.]")) {
			return null;
		}
		else if(cell.matches("[A-Z]")) {
			return (int)cell.charAt(0)-38;
		}
		else if(cell.matches("[a-z]")) {
			return (int)cell.charAt(0)-96;
		}
		return 0;
	}
	
	/**
	 * Helper method to convert cell int values into characters
	 * 
	 * @param cell
	 * @return
	 */
	protected String integerToLetter(Integer cell) {
		if(cell == null) {
			return ".";
		}
		else if(cell <= 0) {
			minesPast = true;
			return "*";
		}
		else if(cell > 26) {
			return String.valueOf((char)(cell+38));
		}
		else {
			return String.valueOf((char)(cell+96));
		}
	}

	/**
	 * Helper method to print the star field
	 */
	public void printField() {
		StringBuilder output = new StringBuilder();
		
		for (List<Integer> row : starField) {
			for (Integer cell : row) {
				output.append(integerToLetter(cell));			
			}
			output.append("\n");
		}

		System.out.println(output.toString());
	}
	
	/**
	 * @return The count of the remaining mines
	 */
	public int getMinesCount() {
		return minesMap.size();
	}
	
	/**
	 * @return True if the are passed mines
	 */
	public boolean getMinesPassed() {
		return minesPast;
	}
	
	/**
	 * Calculates the score
	 * 
	 * @return
	 */
	public int getCompleteScore() {
		
		int score = 10 * startingMineCount;
		int maxShotsDeduction = 5 * startingMineCount;
		int maxMovesDeduction = 3 * startingMineCount;
		
		int actualShotsDeduction = ship.getShotsFired()*5;
		int actualMovesDeduction = ship.getCountMoves()*2;
		
		score -= (actualShotsDeduction > maxShotsDeduction) ?  maxShotsDeduction : actualShotsDeduction;
		score -= (actualMovesDeduction > maxMovesDeduction) ?  maxMovesDeduction : actualMovesDeduction;
		
		return score;
	}

	

}
