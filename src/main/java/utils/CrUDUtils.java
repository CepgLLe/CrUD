package utils;

import ancillary.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Properties;

public class CrUDUtils {

    private static final String defaultProps = "src/main/props/default.properties";
    private static final Properties PROPS = new Properties();
    private static boolean isGot;


    public static void loadProps() throws IOException {
        isGot = false;
        //DEFAULT_PROPS.load(new FileInputStream(propsPath + defaultFilename));
        PROPS.load(new FileInputStream(defaultProps));
        if (PROPS.getProperty("MODE").equals("USER")) {
            File userPropsFile = new File(PROPS.getProperty("USER_PROPS_FILE"));
            PROPS.clear();
            PROPS.load(new FileInputStream(userPropsFile));
        }
        if (!isGot) {
            System.out.println("MODE: " + PROPS.getProperty("MODE") + ", " +
                    "LAST USER: " + PROPS.getProperty("USER_NAME"));
            isGot = true;
        }
    }

    public static void getInfo() throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST"))) {
            String line;
            while ((line = reader.readLine()) != null) System.out.println(line);
        }
    }

    public static void user() {
        System.out.println("User menu...");
        // body...
    }

    public static void createNewFile() throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(System.in)) {
            if (PROPS.getProperty("MODE").equals("DEFAULT")) {
                changeDefaultMode(); // Changes the DEFAULT MODE to USER MODE.
            }
            setUserName(reader); // Set and save the user name.

            System.out.print("Enter file name (name only & 10 characters max): ");
            String fileName = reader.readLine();
            System.out.println("");
            reader.close();

            if (fileName.isEmpty() || fileName.length() > 10)
                throw new IndexOutOfBoundsException(">>> Enter the creating file name 10 characters max <<<");
            // ...
            addToInfoList(fileName);
            String filePath = PROPS.getProperty("DATA_DIR") + fileName + ".crud";

            try (CrUDBufferedWriter writer = new CrUDBufferedWriter(filePath)) {
                writer.write(String.format("%-8.8s%-30.30s%-8.8sf%-4.4s", "ID", "Product", "Price", "Quantity"));
            }
        }
    }

    public static void deleteFile() throws IOException {
        getInfo();

        ArrayList<String> buffList = new ArrayList<>();
        int chosenNumber;
        try (CrUDBufferedReader consoleReader = new CrUDBufferedReader(System.in)) {
            if (PROPS.getProperty("MODE").equals("DEFAULT")) {
                changeDefaultMode(); // Changes the DEFAULT MODE to USER MODE.
            }
            setUserName(consoleReader); // Set and save the user name.
            System.out.print("Choose the file (enter number or \"0\" for cancel): ");
            chosenNumber = Integer.parseInt(consoleReader.readLine());

            if (chosenNumber == 0) return;
            else if (chosenNumber == 1) throw new InvalidObjectException(">>> Undeletable file <<<");

            try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.contains("ID") && !line.contains("Status")) buffList.add(line);
                }
            }

            String fileName;
            for (String s : buffList) {
                int index = s.indexOf('|',1);
                if (Integer.parseInt(s.substring(1, index).trim()) == chosenNumber && !s.contains("deleted")) {
                    fileName = s.substring(index + 1, s.indexOf('|', index + 1)).trim();
                    delete(fileName, consoleReader);
                }
            }
        }

    }

    private static void delete(String fileName, CrUDBufferedReader reader) throws IOException {
        System.out.print("Are you sure? [Y/N]: ");
        while (true) {
            String line = reader.readLine();
            if (line.equalsIgnoreCase("N")) break;
            else if (line.equalsIgnoreCase("Y")) {
                File file = new File(PROPS.getProperty("DATA_DIR") + fileName);
                if (file.delete()) {
                    System.out.println(fileName + " deleted!");
                    changeFileStatus(fileName, "deleted");
                } else throw new IOException(">>> Something is wrong! <<<");
                break;
            }
        }
    }

    public static void addFile() {
        // ...
    }

    public static void reset() {
        // ...
    }

    public static void getInstruction() throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INST"))) {
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);
        }
    }

    public static String getFileName() {
        return PROPS.getProperty("FILE_NAME");
    }

    private static void addToInfoList(String fileName) throws IOException {
        ArrayList<String> buffList = new ArrayList<>();
        int id = 1;
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST"))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.contains("ID") && !line.contains("Status")) buffList.add(line);
            }
        }

        // Checking a file name with same name.
        for (String s : buffList) {
            int newID;
            if (s.contains(fileName + ".crud")) {
                buffList.clear();
                throw new FileAlreadyExistsException(">>> \"" +
                        fileName + ".crud\" file already exists! Try again. <<<");
            }
            newID = Integer.parseInt(s.substring(1, s.indexOf('|')).trim());
            if (newID > id) id = newID;
        }

        try (CrUDBufferedWriter writer = new CrUDBufferedWriter(PROPS.getProperty("INFO_LIST"), true)) {
            writer.newLine();
            writer.write(String.
                    format("|%-8d|%-15.15s|%-20.20s|%-9.9s|",
                            ++id, fileName, PROPS.getProperty("USER_NAME"), "work file"));
        }
    }

    /**
     * The method changes a file status in the "Status" column of the <a href="src/main/data/info_list">
     * and user name in the "Last changes by" column.
     *
     * @param fileName is a file name with *.crud format which a status need to change
     * @param status is a status you want but 9 characters max
     * @throws IOException If an I/O error occurs
     */
    private static void changeFileStatus(String fileName, String status) throws IOException {
        ArrayList<String> buffList = new ArrayList<>();
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST"))) {
            String line;
            while ((line = reader.readLine()) != null) buffList.add(line);
        }

        for (int i = 0; i < buffList.size(); i++) {
            String s = buffList.get(i);
            if (s.contains(fileName)) {
                buffList.set(i,
                        s.replace(s.substring(26, s.indexOf('|', 26)).trim(),
                                String.format("%-20.20s", PROPS.getProperty("USER_NAME"))));
                buffList.set(i,
                        s.replace(s.substring(47, s.indexOf('|', 47)).trim(),
                                String.format("%-9.9s", status)));
                break;
            }
        }

        try (CrUDBufferedWriter writer = new CrUDBufferedWriter(PROPS.getProperty("INFO_LIST"))) {
            for (int i = 0; i < buffList.size(); i++) {
                if (i == buffList.size() - 1) writer.write(buffList.get(i));
                else {
                    writer.write(buffList.get(i));
                    writer.newLine();
                }
            }
        }

        System.out.println("Status changed.");
    }

    private static void setUserName(CrUDBufferedReader reader) throws IOException {
        System.out.print("Enter your name: ");
        PROPS.setProperty("USER_NAME", reader.readLine());
        storeUserProps();
    }

    // This method changes the DEFAULT MODE to USER MODE. It will change only if the MODE was DEFAULT
    private static void changeDefaultMode() throws IOException {
        PROPS.setProperty("MODE", "USER");
        PROPS.store(new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(defaultProps), StandardCharsets.UTF_8)), null);
        PROPS.clear();
        loadProps();
    }

    private static void storeUserProps() throws IOException {
        PROPS.store(new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(PROPS.getProperty("USER_PROPS_FILE")), StandardCharsets.UTF_8)), null);
    }
}
