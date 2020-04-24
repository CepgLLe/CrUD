package com.CepgLLe.example.tools;

import com.CepgLLe.example.services.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Properties;

public class CrUDUtils {

    private static String absolutePathOfDefaultProps;
    private static String absolutePathOfUserProps;
    private static final Properties PROPS = new Properties();

    public static void setAbsolutePathOfDefaultProps(String absolutePathOfDefaultProps) {
        CrUDUtils.absolutePathOfDefaultProps = absolutePathOfDefaultProps;
    }

    public static void setAbsolutePathOfUserProps(String absolutePathOfUserProps) {
        CrUDUtils.absolutePathOfUserProps = absolutePathOfUserProps;
    }

    public static void putAllProps(Properties properties) {
        PROPS.putAll(properties);
        System.out.println("[RUN] MODE: " + PROPS.getProperty("MODE") + ", " +
                           "LAST USER: " + PROPS.getProperty("USER_NAME")  + ", " +
                           "SELECTED FILE: " + PROPS.getProperty("NAME_DATA_FILE"));
    }

    /**
     * The method prints the information list.
     * @throws IOException if an error occurred.
     */
    public static void getInfo() throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST_PATH"))) {
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);
        }
    }

    /**
     * The method selects a file for work.
     * @throws IOException if an error occurred.
     */
    public static void changeWorkFile() throws IOException {
        getInfo();

        int chosenNumber;
        try (CrUDBufferedReader consoleReader = new CrUDBufferedReader(System.in)) {

            System.out.print("Choose the file (enter ID or \"0\" for cancel): ");
            chosenNumber = Integer.parseInt(consoleReader.readLine());

            if (chosenNumber == 0) return;

            if (PROPS.getProperty("MODE").equals("DEFAULT"))
                changeDefaultMode(); // Changes the DEFAULT MODE to USER MODE.
            setPropsAndSave("USER_NAME", getData(consoleReader, "Enter your name (or \"exit\" to exit)"));

            String workFileName = null;
            try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST_PATH"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int fileNumber = 0;
                    if (!line.equals("|ID      |File Name      |Last changes by     |Status   |") &&
                        !line.equals("+--------+---------------+--------------------+---------+"))
                        fileNumber = Integer.parseInt(line.substring(1, line.indexOf('|', 1)).trim());
                    if (fileNumber == chosenNumber) {
                        int index = line.indexOf('|', 1) + 1;
                        workFileName = line.substring(index, line.indexOf('|', index)).trim();
                    }
                }
            }
            if (workFileName != null)
                setPropsAndSave("NAME_DATA_FILE", workFileName);
        } catch (NumberFormatException e) {
            changeWorkFile();
        }
    }

    /**
     * The method creates a new data file and sets file name. In process the user must to enter his name for
     * identification as the last user. Additionally, It carry out different procedures as: changes the DEFAULT
     * MODE to USER MODE, save the user name and file name to properties and creates a header in the file.
     * @throws IOException if an error occurred.
     */
    public static void createNewDataFile() throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(System.in)) {
            String fileName = getData(reader, "Enter file name (name only & 10 characters max)") + ".crud";
            String absolutePath = PROPS.getProperty("DATA_DIR") + fileName;

            if (fileName.equals(".crud") || fileName.length() > 10)
                throw new IndexOutOfBoundsException("Enter the creating file name 10 characters max");

            // Changes the DEFAULT MODE to USER MODE with changes
            if (PROPS.getProperty("MODE").equals("DEFAULT"))
                changeDefaultMode();
            setPropsAndSave("USER_NAME", getData(reader, "Enter your name (or \"exit\" to exit)"));

            create(reader, absolutePath);
            reader.close();

            try (CrUDBufferedWriter writer = new CrUDBufferedWriter(absolutePath)) {
                writer.write(String.format("|%-8.8s|%-30.30s|%-8.8s|%-4.4s|", "ID", "Product", "Price", "Quantity"));
                writer.newLine();
                writer.write("+--------+------------------------------+--------+----+");
            }

            setPropsAndSave("NAME_DATA_FILE", fileName);
        }
    }

    /**
     * The method creates the file on the disk an call the {@code addToInfoList} method.
     * @param reader is a console reader;
     * @param absolutePath is an absolute path of creatable file.
     * @throws IOException if an error occurred.
     */
    private static void create(CrUDBufferedReader reader, String absolutePath) throws IOException {
        String fileName = absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
        if (isTrue(reader, "Confirm \"" + fileName + "\" file creation?")) {
            File file = new File(absolutePath);
            if (file.createNewFile()) {
                System.out.println(fileName + " created!");
                addToInfoList(fileName);
            } else throw new IOException("Something is wrong while creating!");
        }
    }

    /**
     * The method delete a chosen data file.
     * @throws IOException if an error occurred.
     */
    public static void deleteFile() throws IOException {
        getInfo();

        ArrayList<String> buffList = new ArrayList<>();
        int chosenNumber;
        try (CrUDBufferedReader consoleReader = new CrUDBufferedReader(System.in)) {

            System.out.print("Choose the file (enter ID or \"0\" for cancel): ");
            chosenNumber = Integer.parseInt(consoleReader.readLine());

            if (chosenNumber == 0) return;
            else if (chosenNumber == 1) throw new InvalidObjectException("Undeletable file");

            if (PROPS.getProperty("MODE").equals("DEFAULT"))
                changeDefaultMode(); // Changes the DEFAULT MODE to USER MODE.
            setPropsAndSave("USER_NAME", getData(consoleReader, "Enter your name (or \"exit\" to exit)"));

            try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST_PATH"))) {
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
                } else throw new IOException("File have the \"deleted\" status");
            }
        }
    }

    /**
     * The method delete the data file from disk. The same request for cancel.
     * @param fileName is a data file;
     * @param reader is a console reader.
     * @throws IOException if an error occurred.
     */
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
                } else throw new IOException("Something is wrong while deleting!");
                break;
            }
        }
    }

    /**
     * The method print a small instruction and example.
     * @throws IOException if an error occurred.
     */
    public static void getInstruction() throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INSTRUCTION_PATH"))) {
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);
        }
        System.out.println("[INFO] Then you can see an example of work file.\n" +
                           "+-----------------------EXAMPLE-----------------------+");
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("EXAMPLE_PATH"))) {
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);
        }
    }

    /**
     * @return the path of selected file for work.
     */
    public static String getWorkFile() throws FileNotFoundException {
        File file = new File(PROPS.getProperty("DATA_DIR") +
                             System.getProperty("file.separator") +
                             PROPS.getProperty("NAME_DATA_FILE"));
        if (!file.exists()) throw new FileNotFoundException("Work file NOT found");
        return file.getAbsolutePath();
    }

    /**
     * The method add a new data file to info list with a status.
     * @param fileName is a created file name.
     * @throws IOException if an error occurred.
     */
    private static void addToInfoList(String fileName) throws IOException {
        ArrayList<String> buffList = new ArrayList<>();
        int id = 1;
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST_PATH"))) {
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.contains("ID") && !line.contains("Status"))
                    buffList.add(line);
        }

        // Checking a file name with same name.
        for (String s : buffList) {
            int newID;
            if (s.contains(fileName)) {
                buffList.clear();
                throw new FileAlreadyExistsException('"' + fileName + "\" file already exists! Try again.");
            }
            newID = Integer.parseInt(s.substring(1, s.indexOf('|', 1)).trim());
            if (newID > id) id = newID;
        }

        try (CrUDBufferedWriter writer = new CrUDBufferedWriter(PROPS.getProperty("INFO_LIST_PATH"),true)) {
            writer.newLine();
            writer.write(String.format("|%-8d|%-15.15s|%-20.20s|%-9.9s|",
                                        ++id, fileName, PROPS.getProperty("USER_NAME"), "work file"));
        }
    }

    /**
     * The method changes a file status in the "Status" column of the <a href="/data/info_list">
     * and user name in the "Last changes by" column.
     *
     * @param fileName is a file name with *.crud format which a status need to change
     * @param status is a status you want but 9 characters max
     * @throws IOException If an I/O error occurs
     */
    private static void changeFileStatus(String fileName, String status) throws IOException {
        ArrayList<String> buffList = new ArrayList<>();
        try (CrUDBufferedReader reader = new CrUDBufferedReader(PROPS.getProperty("INFO_LIST_PATH"))) {
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

        try (CrUDBufferedWriter writer = new CrUDBufferedWriter(PROPS.getProperty("INFO_LIST_PATH"))) {
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

    /**
     * The service method.
     * @param reader is a console reader;
     * @param question is a text of question in form (for example):
     *                 Are you sure?  [Y/N]: - if you set: {@code isTrue(reader, "Are you sure?");}.
     * @return true if answer is Y/y or false if answer is N/n.
     * @throws IOException if an error occurred.
     */
    private static boolean isTrue(CrUDBufferedReader reader, String question) throws IOException {
        String answer;
        while (true) {
            System.out.print(question + " [Y/N]: ");
            answer = reader.readLine();
            if (answer.equalsIgnoreCase("N")) return false;
            else if (answer.equalsIgnoreCase("Y")) return true;
        }
    }

    /**
     * The service method.
     * @param reader is a console reader;
     * @param message is a text of request in form (for example):
     *                Enter user name: - if you set: {@code getData(reader, "Enter user name");}.
     * @return entered data.
     * @throws IOException if an I/O error occurs or input the "exit".
     */
    private static String getData(CrUDBufferedReader reader, String message) throws IOException {
        String data;
        while (true) {
            System.out.print(message + ": ");
            data = reader.readLine();
            if (data.equals("exit")) throw new IOException("exit");
            else if (!data.isEmpty()) return data;
        }
    }

    /**
     * The method sets and save properties to file.
     * @param key is a props key;
     * @param value is a new value.
     * @throws IOException if an error occurred.
     */
    private static void setPropsAndSave(String key, String value) throws IOException {
        PROPS.put(key, value);
        PROPS.store(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(absolutePathOfUserProps), StandardCharsets.UTF_8)), null);
    }

    /**
     * The method changes the default mode to user mode.
     * @throws IOException if an error occurred.
     */
    private static void changeDefaultMode() throws IOException {
        PROPS.put("MODE", "USER");
        PROPS.store(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(absolutePathOfDefaultProps), StandardCharsets.UTF_8)), null);
    }
}
