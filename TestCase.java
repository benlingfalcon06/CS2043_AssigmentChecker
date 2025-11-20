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
    private ArrayList<String> expout;
    private String title;
    private ArrayList<String> input;
    
    public static void create(String title1, String input1, String expout1)
    {
        File test = new File(title1);
        input = new ArrayList<>();

        Scanner fileData = new Scanner(test);

        while(fileData.hasNextLine())
        {
            input.add(fileData.nextLine());
        }


    }
    
    //For test
    public static void main(String[] args)
    {
        
    }
}
