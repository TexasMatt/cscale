package cscale;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Starfleet Mine Clearing Exercise
 * 
 * @author msmith
 *
 */
public class App 
{
    public static void main( String[] args ) {
    	
    	if (args == null || args.length != 2) {
    		System.out.println("Usage: java cscale.App <field file path> <script file path>");
    		System.exit(1);
    	}
    	
    	List<String[]> fieldList = new ArrayList<String[]>();
		List<String[]> scriptList = new ArrayList<String[]>();

    	loadInputData(args, fieldList, scriptList);
    	
		if (validate(fieldList, scriptList)) {
			StarField starfield = new StarField(fieldList);
	
			boolean minesClearedStepsRemaining = false;
			boolean minesPassed = false;
			
			for(int i = 0 ; i < scriptList.size(); i++) {
				minesClearedStepsRemaining = starfield.getMinesCount() == 0;
				if (minesClearedStepsRemaining) {
					continue;
				}
				minesPassed = starfield.getMinesPassed();
				if (minesPassed) {
					continue;
				}
				System.out.println("Step " + (i+1));
				System.out.println();
				starfield.printField();
				System.out.println(Arrays.toString(scriptList.get(i)).replaceAll("[\\[\\],]", ""));
				System.out.println();
				for(String command : scriptList.get(i)) {
					if(MOVE_COMMANDS.contains(command)) {
						starfield.moveShip(command);
					}
					else {
						starfield.shipFire(command);
					}
				}
				starfield.dropShip();
				starfield.printField();
			}

			printScore(starfield, minesClearedStepsRemaining);
		}
    }

	/**
	 * Prints the score of the script
	 * 
	 * @param starfield
	 * @param minesClearedStepsRemianing
	 */
	private static void printScore(StarField starfield, boolean minesClearedStepsRemaining) {
		boolean pass = true;
		int score = 0;
		if(starfield.getMinesCount() > 0) {
			pass = false;
		}
		else if (starfield.getMinesCount() == 0 && minesClearedStepsRemaining) {
			score = 1;
		}
		else if (starfield.getMinesCount() == 0 && !minesClearedStepsRemaining) {
			int intScore = starfield.getCompleteScore();
			score = intScore;
		}
		
		String passText = pass ? "pass" : "fail";
		System.out.println(passText + " (" + score + ")");
	}
    

	/**
	 * Reads the input files and populated the field and script lists.
	 * 
	 * @param args
	 * @param fieldList
	 * @param scriptList
	 */
    protected static void loadInputData(String[] args, List<String[]> fieldList, List<String[]> scriptList) {
		// Load the input files
    	try(FileReader field = new FileReader(args[0]);
    			FileReader script = new FileReader(args[1]);
    			BufferedReader fieldReader = new BufferedReader(field);
    			BufferedReader scriptReader = new BufferedReader(script))			
    	{
    		String line;
    	    while((line = fieldReader.readLine()) != null){
    	    	String[] cells = line.split("");
    	    	fieldList.add(cells);
    	    }
    	    line = null;
    	    while((line = scriptReader.readLine()) != null){
    	    	String[] commands = line.trim().split("\\s+");
    	    	scriptList.add(commands);
    	    }
    		
    	} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * Validate the input data of the field and script files.
	 * 
	 * @param fieldList
	 * @param scriptList
	 * @throws Exception 
	 */
	protected static boolean validate(List<String[]> fieldList, List<String[]> scriptList) {

		if (fieldList.size() > 0 && fieldList.get(0).length%2 == 0) {
			System.out.println("This field has no unique center, the field needs to have odd x and y values.");
			return false;
		}
		
		for(String[] cells : fieldList) {
			for(String cell : cells) {
				if (!cell.matches("[a-zA-Z.]")) {
					System.out.println("Field contains invalid charaters on line " + Arrays.toString(cells));
					return false;
				}
			}
		}
		
		for(String[] commands : scriptList) {
			for(String command : commands) {
				if (!command.matches("[a-zA-Z]+") || !VALID_COMMANDS.contains(command)) {
					System.out.println("Script contains invalid charaters on line " + Arrays.toString(commands));
					return false;
				}
			}
		}
		return true;
	}
	
	protected static final String MOVE_NORTH = "north";
	protected static final String MOVE_SOUTH = "south";
	protected static final String MOVE_EAST  = "east";
	protected static final String MOVE_WEST  = "west";
	
	protected static final String FIRE_ALPHA = "alpha";
	protected static final String FIRE_BETA  = "beta";
	protected static final String FIRE_GAMMA = "gamma";
	protected static final String FIRE_DELTA = "delta";
	
	private static final String MOVE_COMMANDS = "north|south|east|west";
	private static final String FIRE_COMMANDS = "alpha|beta|gamma|delta";
	private static final String VALID_COMMANDS = MOVE_COMMANDS + "|" + FIRE_COMMANDS;
	
}
