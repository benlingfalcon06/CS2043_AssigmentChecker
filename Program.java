import java.io.File;

public class Program 
{
    private final File javaFile;

    public Program(File javaFile) 
  {
        this.javaFile = javaFile;
  }

    public File getJavaFile() 
    {
        return javaFile;
    }

    public String getMainClassName() 
    {
        String name = javaFile.getName();
        return name.substring(0, name.length() - 5); 
    }

    public String getClassPathDir() 
    {
        return javaFile.getParent();
    }
}

