package utils;

import ancillary.*;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.LinkedList;
import java.util.Properties;

public class CrUDUtils {

    private static final String defaultProps = "src/main/props/default.properties";
    //private static final String userProps = "src/main/props/user.properties";
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
        try (CrUDReader reader = new CrUDReaderAdapter(PROPS.getProperty("INFO_LIST"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    public static void user() {
        System.out.println("User menu...");
        // body...
    }

    public static void createNewFile() throws IOException {
        try (CrUDReader reader = new CrUDReaderAdapter(System.in)) {
            System.out.print("Enter file name (name only & 10 characters max): ");
            String fileName = reader.readLine();
            if (fileName.isEmpty() || fileName.length() > 10)
                throw new IndexOutOfBoundsException(">>> Enter the creating file name 10 characters max <<<");
            addToInfoList(fileName);
            String filePath = PROPS.getProperty("DATA_DIR") + '/' + fileName + ".crud";
            reader.close();

            try (CrUDWriter writer = new CrUDWriterAdapter(filePath)) {
                writer.write(String.format("%-8.8s%-30.30s%-8.8sf%-4.4s", "ID", "Product", "Price", "Quantity"));
            }
        }
    }

    public static void deleteFile() throws IOException {
        getInfo();

        try (CrUDReader reader = new CrUDReaderAdapter(System.in)) {
            System.out.print("Choose the file (enter numberX): ");
            int chosenNumber = Integer.parseInt(reader.readLine());
            //br.close();
            // to be continue...
        }
    }

    public static void addFile() {

    }

    public static void reset() {

    }

    public static void getInstruction() throws IOException {
        try (CrUDReader reader = new CrUDReaderAdapter(PROPS.getProperty("INST"))) {
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);
        }
    }

    public static String getFileName() {
        return PROPS.getProperty("FILE_NAME");
    }

    private static void addToInfoList(String fileName) throws IOException {
        LinkedList<String> buffList = new LinkedList<>();
        try (CrUDReader reader = new CrUDReaderAdapter(PROPS.getProperty("INFO_LIST"));
             CrUDWriter writer = new CrUDWriterAdapter(PROPS.getProperty("INFO_LIST"), true)) {
            int newID = 1;
            String line;
            if ((line = reader.readLine()) == null) {
                writer.write(String.
                        format("|%-8d|%-15.15s|%-20.20s|%-9.9s|",
                                newID, PROPS.getProperty("DATA_FILE"), PROPS.getProperty("USER_NAME"), "work file"));
            } else {
                buffList.add(line);
                while ((line = reader.readLine()) != null) buffList.add(line);
                reader.close();

                for (String s : buffList) {
                    if (s.contains(fileName + ".crud")) {
                        buffList.clear();
                        throw new FileAlreadyExistsException(">>> \"" + fileName + ".crud\" file already exists! Try again. <<<");
                    }
                }

                writer.newLine();
                writer.write(String.format("%-8d"));
            }
        }
    }
}
