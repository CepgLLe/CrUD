package utils;

import ancillary.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Properties;

public class CrUDUtils {

    private static final String defaultProps = "src/main/props/props/default.properties";
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
                    "LAST USER: " + PROPS.getProperty("USER_NAME")  + ", " +
                    "SELECTED FILE: " + PROPS.getProperty("DATA_FILE"));
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

    public static void createNewDataFile() throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(System.in)) {
            // Changes the DEFAULT MODE to USER MODE with changes
            if (PROPS.getProperty("MODE").equals("DEFAULT")) {
                changeDefaultMode();
            }
            //
            setPropsAndSave("USER_NAME", getData(reader, "Enter your name (or \"exit\" to exit)"));

            String fileName = getData(reader, "Enter file name (name only & 10 characters max)") + ".crud";

            if (fileName.equals(".crud") || fileName.length() > 10)
                throw new IndexOutOfBoundsException(">>> Enter the creating file name 10 characters max <<<");

            create(reader, fileName);
            reader.close();

            try (CrUDBufferedWriter writer = new CrUDBufferedWriter(PROPS.getProperty("DATA_DIR") + fileName)) {
                writer.write(String.format("%-8.8s%-30.30s%-8.8sf%-4.4s", "ID", "Product", "Price", "Quantity"));
            }

            setPropsAndSave("DATA_FILE", fileName);
        }
    }

    private static void create(CrUDBufferedReader reader, String fileName) throws IOException {
        if (!isTrue(reader, "Confirm \"" + fileName + "\" file creation?")) {
            File file = new File(PROPS.getProperty("DATA_DIR") + fileName);
            if (file.createNewFile()) {
                System.out.println(fileName + " created!");
                addToInfoList(fileName);
            } else throw new IOException(">>> Something is wrong while creating! <<<");
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
            //
            setPropsAndSave("USER_NAME", getData(consoleReader, "Enter your name (or \"exit\" to exit)"));

            // ...

            System.out.print("Choose the file (enter ID or \"0\" for cancel): ");
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
                } else throw new IOException(">>> File have the \"deleted\" status <<<");
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
                } else throw new IOException(">>> Something is wrong while deleting! <<<");
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

    public static String getWorkFile() {
        return PROPS.getProperty("DATA_DIR") + PROPS.getProperty("DATA_FILE");
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
            if (s.contains(fileName)) {
                buffList.clear();
                throw new FileAlreadyExistsException(">>> \"" +
                        fileName + "\" file already exists! Try again. <<<");
            }
            newID = Integer.parseInt(s.substring(1, s.indexOf('|', 1)).trim());
            if (newID > id) id = newID;
        }

        try (CrUDBufferedWriter writer = new CrUDBufferedWriter(PROPS.getProperty("INFO_LIST"), true)) {
            writer.newLine();
            writer.write(String.
                    format("|%-8d|%-15.15s|%-20.20s|%-9.9s|",
                            ++id, fileName, PROPS.getProperty("USER_NAME"), "created"));
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
                        s.replace(s.substring(26, s.indexOf('|', 26)),
                                String.format("%-20.20s", PROPS.get("USER_NAME"))));
                buffList.set(i,
                        buffList.get(i).replace(s.substring(47, s.indexOf('|', 47)),
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

    private static boolean isTrue(CrUDBufferedReader reader, String question) throws IOException {
        String answer;
        while (true) {
            System.out.print(question + " [Y/N]: ");
            answer = reader.readLine();
            if (answer.equalsIgnoreCase("N")) return false;
            else if (answer.equalsIgnoreCase("Y")) return true;
        }
    }

    private static String getData(CrUDBufferedReader reader, String message) throws IOException {
        String data;
        while (true) {
            System.out.print(message + ": ");
            data = reader.readLine();
            if (data.equals("exit")) throw new IOException("exit");
            else if (!data.isEmpty()) return data;
        }
    }

    private static void setPropsAndSave(String key, String value) throws IOException {
        PROPS.put(key, value);
        PROPS.store(new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(PROPS.getProperty("USER_PROPS_FILE")), StandardCharsets.UTF_8)), null);
    }

    // This method changes the DEFAULT MODE to USER MODE. It will change only if the MODE was DEFAULT
    private static void changeDefaultMode() throws IOException {
        PROPS.put("MODE", "USER");
        PROPS.store(new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(defaultProps), StandardCharsets.UTF_8)), null);
        PROPS.clear();
        loadProps();
    }
}
