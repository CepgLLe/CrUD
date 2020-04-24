package com.CepgLLe.example.tools;

import com.CepgLLe.example.services.CrUDBufferedReader;
import com.CepgLLe.example.services.CrUDBufferedWriter;

import java.io.*;
import java.util.*;

public class Extract {

    /* Properties */
    private final Properties DEFAULT_PROPS = new Properties();
    private final Properties USER_PROPS = new Properties();

    /* Directories */
    private final Map<String, File> DIRS = new HashMap<>();

    /* Files */
    private final Map<String, File> FILES = new HashMap<>();

    public Extract() {
        /* List:
           data
           info
           props
         */
        DIRS.put("data",
                new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "data"));
        DIRS.put("info",
                new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "info"));
        DIRS.put("props",
                new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "props"));

        /* List:
           example.crud
           instruction.info
           goods.crud
           info_list
           default.properties
           user.properties
         */
        FILES.put("goods.crud",
                new File(DIRS.get("data") + System.getProperty("file.separator") + "goods.crud"));
        FILES.put("info_list",
                new File(DIRS.get("data") + System.getProperty("file.separator") + "info_list"));
        FILES.put("example.crud",
                new File(DIRS.get("info") + System.getProperty("file.separator") + "example.crud"));
        FILES.put("instruction.info",
                new File(DIRS.get("info") + System.getProperty("file.separator") + "instruction.info"));
        FILES.put("default.properties",
                new File(DIRS.get("props") + System.getProperty("file.separator") + "default.properties"));
        FILES.put("user.properties",
                new File(DIRS.get("props") + System.getProperty("file.separator") + "user.properties"));
    }

    public boolean extracted() {
        for (Map.Entry<String, File> pair : FILES.entrySet())
            if (!pair.getValue().exists()) return false;
        return true;
    }

    public void extract() throws IOException {
        for (Map.Entry<String, File> pair : DIRS.entrySet())
            if (!pair.getValue().mkdirs())
                throw new IOException("Troubles with a directory creating");

        for (Map.Entry<String, File> pair : FILES.entrySet())
            if (!pair.getValue().createNewFile())
                throw new IOException("Troubles with a file creating");

        createNewProperties();
        DEFAULT_PROPS.store(new FileOutputStream(FILES.get("default.properties")),
                                                 FILES.get("default.properties").getAbsolutePath());
        USER_PROPS.store(new FileOutputStream(FILES.get("user.properties")),
                                              FILES.get("user.properties").getAbsolutePath());

        // Copy the default work file from resources to disk
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("data/goods.crud");
             CrUDBufferedReader reader = new CrUDBufferedReader(in);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(FILES.get("goods.crud").getAbsolutePath())) {
            while (reader.ready())
                writer.write(reader.read());
        }

        // Copy the information file of work files from resources to disk
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("data/info_list");
             CrUDBufferedReader reader = new CrUDBufferedReader(in);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(FILES.get("info_list").getAbsolutePath())) {
            while (reader.ready())
                writer.write(reader.read());
        }

        // Copy the example file from resources to disk
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("info/example.crud");
             CrUDBufferedReader reader = new CrUDBufferedReader(in);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(FILES.get("example.crud").getAbsolutePath())) {
            while (reader.ready())
                writer.write(reader.read());
        }

        // Copy the instruction file by work with the programme from resources to disk
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("info/instruction.info");
             CrUDBufferedReader reader = new CrUDBufferedReader(in);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(FILES.get("instruction.info").getAbsolutePath())) {
            while (reader.ready())
                writer.write(reader.read());
        }

        System.out.println(
                "--------------------------------------------------------------------------------\n" +
                "[INFO] Created new data files and properties for a current directory!\n" +
                "--------------------------------------------------------------------------------"
        );
    }

    public void loadProps() throws IOException {
        DEFAULT_PROPS.load(new FileInputStream(FILES.get("default.properties")));
        USER_PROPS.load(new FileInputStream(FILES.get("user.properties")));
    }

    public Properties getDefaultProps() {
        return DEFAULT_PROPS;
    }

    public Properties getUserProps() {
        return USER_PROPS;
    }

    public Map<String, File> getFILES() {
        return FILES;
    }

    private void createNewProperties() {
        final String NAME_DATA_FILE_VALUE   = "goods.crud";
        final String USER_NAME_VALUE        = "DEFAULT_USER";
        final String DATA_DIR_VALUE         = DIRS.get("data").getAbsolutePath();
        final String INFO_LIST_PATH_VALUE   = FILES.get("info_list").getAbsolutePath();
        final String INSTRUCTION_PATH_VALUE = FILES.get("instruction.info").getAbsolutePath();
        final String EXAMPLE_PATH_VALUE     = FILES.get("example.crud").getAbsolutePath();

        DEFAULT_PROPS.clear();
        DEFAULT_PROPS.put("INSTRUCTION_PATH", INSTRUCTION_PATH_VALUE);
        DEFAULT_PROPS.put("EXAMPLE_PATH",     EXAMPLE_PATH_VALUE);
        DEFAULT_PROPS.put("DATA_DIR",         DATA_DIR_VALUE);
        DEFAULT_PROPS.put("MODE",             "DEFAULT");
        DEFAULT_PROPS.put("NAME_DATA_FILE",   NAME_DATA_FILE_VALUE);
        DEFAULT_PROPS.put("USER_NAME",        USER_NAME_VALUE);
        DEFAULT_PROPS.put("INFO_LIST_PATH",   INFO_LIST_PATH_VALUE);

        USER_PROPS.clear();
        USER_PROPS.putAll(DEFAULT_PROPS);
        USER_PROPS.put("MODE", "USER");
    }
}
