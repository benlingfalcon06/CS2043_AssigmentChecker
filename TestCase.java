import java.io.FileWriter;
import java.io.Exception;
import java.util.ArrayList;
import java.util.Scanner;

/*****************************************************
 * TestCase for test running the code
 * 
 * Author: Emmanuel Adeyemi-Kings
 * Date: 19th Novemeber 2025
 * Title: Software Engineering project submission 3
 ***************************************************/
public class TestCase
{
    private String expout;
    private String title;
    private ArrayList<String> input;
    
    public TestCase(String title1, File input1, File expout1)
    {
        //use teams message to refine the code before the end of friday
        //input is a txt file
        //expout is a txt file
        //i wanna read the files into this object and save its name as title
        input = new ArrayList<>();
        Scanner fileData = new Scanner(input1);

        while(fileData.hasNextLine())
        {
            input.add(fileData.nextLine());
        }

        //This is for the expected oup put
        Scanner fileout = new Scanner(expout1)
        while(fileout.hasNextLine())
        {
            expout = fileout.nextLine()
        }
    }
    
    //For test
    public static void main(String[] args)
    {
        
    }
}
