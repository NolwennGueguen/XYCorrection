package IOUtils;

import ij.IJ;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class IOUtils {
    public static void printErrorToIJLog(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter ps = new PrintWriter(sw);
        exception.printStackTrace(ps);
        IJ.error(sw.toString());
        try {
            sw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        ps.close();
    }

    public static void writeToFile(String filePath, Properties properties) {
        try (PrintWriter out = new PrintWriter(filePath)) {
            properties.store(out, "");
        } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
