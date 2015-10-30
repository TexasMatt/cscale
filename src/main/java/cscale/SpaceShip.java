package cscale;

/**
 * SpaceShip class to maintain the move and shots fired count
 * as well as store the current co-ordinates.
 * 
 * @author msmith
 *
 */
public class SpaceShip {
	int shipX = 0;
	int shipY = 0;
	
	int countShotsFired = 0;
	int countMoves = 0;
	
	public SpaceShip(int X, int Y) {
		shipX = Y;
		shipY = Y;
	}
	
	public int getX() {
		return shipX;
	}
	
	public void setX(int newX) {
		shipX = newX;
	}
	
	public int getY() {
		return shipY;
	}
	
	public void setY(int newY) {
		shipY = newY;
	}
	
	public int getShotsFired() {
		return countShotsFired;
	}
	
	public int getCountMoves() {
		return countMoves;
	}
	
	public void move(String direction) {
		countMoves++;
		switch(direction.toLowerCase()) {
			case App.MOVE_NORTH :
				shipY--;
				break;
			case App.MOVE_SOUTH :
				shipY++;
				break;
			case App.MOVE_EAST :
				shipX++;
				break;
			case App.MOVE_WEST :
				shipX--;
				break;
			default:
				break;
		}
	}
	
	public void fire() {
		countShotsFired++;
	}
	
	
}
