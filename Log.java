import java.io.*;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;

public class Log {

    public static void logResultado(String s){       
        String string = s;
        byte data[] = string.getBytes();
        Path p = Paths.get("./resultado.txt");

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p, CREATE, APPEND))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
