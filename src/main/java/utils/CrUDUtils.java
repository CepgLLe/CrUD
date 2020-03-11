package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class CrUDUtils {

    private static final String defaultProps = "src/main/props/default.properties";
    private static final String userProps = "src/main/props/user.properties";
    //private static final String propsPath = "src/main/props/";
    //private static final Properties DEFAULT_PROPS = new Properties();
    private static final Properties PROPS = new Properties();


    public static void loadProps() throws IOException {
        //DEFAULT_PROPS.load(new FileInputStream(propsPath + defaultFilename));
        PROPS.load(new FileInputStream(defaultProps));
        if (PROPS.getProperty("MODE").equals("USER_MODE")) {
            File userPropsFile = new File(PROPS.getProperty("USER_PROPS_FILE"));
            PROPS.clear();
            PROPS.load(new FileInputStream(userPropsFile));
        }
        System.out.println("MODE: " + PROPS.getProperty("MODE") + " ," +
                "USER: " + PROPS.getProperty("USER_NAME"));
    }

    public static void getInfo() throws IOException {
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(
                             new FileInputStream(PROPS.getProperty("DATA_FILE")), StandardCharsets.UTF_8))) {
            String line;
            for (int i = 1; (line = br.readLine()) != null; i++) {
                System.out.println(i + " " + line);
            }
        }
    }

    public static void user() {
        System.out.println("User menu...");
        // body...
    }

    public static void createNewFile() throws IOException {
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            /*System.out.print("Change the directory? (N - recommended) [Y/N]: ");
            if (br.readLine().equalsIgnoreCase("Y")) {
                System.out.println("Enter the directory would you like:");
                fileDir = br.readLine();
            } else if (br.readLine().equalsIgnoreCase("N"))
                fileDir = PROPS.getProperty("DATA_DIR");*/

            System.out.print("Enter file name (name only): ");
            String filePath = PROPS.getProperty("DATA_DIR") + '/' + br.readLine() + ".crud";
            br.close();

            try (BufferedWriter bw =
                         new BufferedWriter(new OutputStreamWriter(
                                 new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
                bw.write("ID      Product name                  Price   Quan");
            }
        }
    }

    public static void deleteFile() throws IOException {
        getInfo();
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            System.out.print("Choose the file (enter number): ");
            int chosenNumber = Integer.parseInt(br.readLine());
            //br.close();
            // to be continue...
        }
    }

    public static void addFile() {

    }

    public static void reset() {

    }

    public static void getInstruction() throws IOException {
        try (BufferedReader br =
                new BufferedReader(new InputStreamReader(
                        new FileInputStream(PROPS.getProperty("INST")), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null)
                System.out.println(line);
        }
    }

    public static String getFileName() {
        return PROPS.getProperty("FILE_NAME");
    }

    private void addToInfoList() {

    }
}
