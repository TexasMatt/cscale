package cscale;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit test for simple App.
 * 
 * @author msmith
 */
@RunWith(Parameterized.class)
public class AppTest 
{
	@Parameters
	public static Iterable<String[]> data() {
        return Arrays.asList(new String[][]{{"one"}, {"two"}, {"three"}, {"four"}, {"five"}});
    }
	
	private String filename;
	
	private BufferedReader field = null;
	private BufferedReader script = null;
	private BufferedReader output = null;
	 
	private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	
	public AppTest(String filename) {
		this.filename = filename;
	}
	
	@Before
	public void setup()
	{
		field = new BufferedReader(
	            new InputStreamReader(getClass().getResourceAsStream("/fields/" + filename + ".txt")));
		script = new BufferedReader(
	            new InputStreamReader(getClass().getResourceAsStream("/scripts/" + filename + ".txt")));
		output = new BufferedReader(
	            new InputStreamReader(getClass().getResourceAsStream("/output/" + filename + ".txt")));
		
		System.setOut(new PrintStream(outStream));
	}
	
	@After
    public void teardown() throws IOException
    {
        if (field != null) {
        	field.close();
        }
        field = null;
        
        if (script != null) {
        	script.close();
        }
        script = null;
        
        if (output != null) {
        	output.close();
        }
        output = null;
        
        System.setOut(null);
    }
    
    @Test
	public void run() throws IOException
    {
    	String field = "src/test/resources/fields/" + filename + ".txt";
    	String script = "src/test/resources/scripts/" + filename + ".txt";
    	
    	App.main(new String[]{field,script});
    	
    	StringBuilder outString = new StringBuilder();
    	String line;
    	while((line = output.readLine()) != null) {
    		outString.append(line);
    	}
    	
    	// Remove new line characters to make comparison more reliable
        assertEquals( outString.toString(), outStream.toString().replaceAll("[\\r\\n]", ""));
    }
    
  
}
