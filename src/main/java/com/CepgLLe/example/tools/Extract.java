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
           props
           data
         */
        DIRS.put("props",
                new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "props"));
        DIRS.put("data",
                new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "data"));

        /* List:
           default.properties
           user.properties
           goods.crud
           info_list
         */
        FILES.put("default.properties",
                new File(DIRS.get("props") + System.getProperty("file.separator") + "default.properties"));
        FILES.put("user.properties",
                new File(DIRS.get("props") + System.getProperty("file.separator") + "user.properties"));
        FILES.put("goods.crud",
                new File(DIRS.get("data") + System.getProperty("file.separator") + "goods.crud"));
        FILES.put("info_list",
                new File(DIRS.get("data") + System.getProperty("file.separator") + "info_list"));
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

        final String GOODS_FILE = Objects.requireNonNull(this.getClass()
                                                             .getClassLoader()
                                                             .getResource("data/goods.crud")).getFile();
        try (CrUDBufferedReader reader = new CrUDBufferedReader(GOODS_FILE);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(FILES.get("goods.crud").getAbsolutePath())) {
            while (reader.ready())
                writer.write(reader.read());
        }

        final String INFO_FILE = Objects.requireNonNull(this.getClass()
                                                            .getClassLoader()
                                                            .getResource("data/info_list")).getFile();
        try (CrUDBufferedReader reader = new CrUDBufferedReader(INFO_FILE);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(FILES.get("info_list").getAbsolutePath())) {
            while (reader.ready())
                writer.write(reader.read());
        }
        System.out.println(
                "---------------------------------------------------------------------------------------------\n" +
                "[INFO] Created new data files and properties for a current directory!\n" +
                "---------------------------------------------------------------------------------------------"
        );
    }

    private void createNewProperties() {
        final String DATA_DIR_VALUE         = DIRS.get("data").getAbsolutePath();
        final String INSTRUCTION_PATH_VALUE = Objects.requireNonNull(this.getClass().getClassLoader().getResource("info/instruction.info")).getFile();
        final String NAME_DATA_FILE_VALUE   = "goods.crud";
        final String EXAMPLE_PATH_VALUE     = Objects.requireNonNull(this.getClass().getClassLoader().getResource("info/example.crud")).getFile();
        final String USER_NAME_VALUE        = "DEFAULT_USER";
        final String USER_PROPS_PATH_VALUE  = FILES.get("user.properties").getAbsolutePath();
        final String INFO_LIST_PATH_VALUE   = FILES.get("info_list").getAbsolutePath();

        DEFAULT_PROPS.clear();
        DEFAULT_PROPS.put("DATA_DIR",         DATA_DIR_VALUE);
        DEFAULT_PROPS.put("INSTRUCTION_PATH", INSTRUCTION_PATH_VALUE);
        DEFAULT_PROPS.put("MODE",             "DEFAULT");
        DEFAULT_PROPS.put("NAME_DATA_FILE",   NAME_DATA_FILE_VALUE);
        DEFAULT_PROPS.put("EXAMPLE_PATH",     EXAMPLE_PATH_VALUE);
        DEFAULT_PROPS.put("USER_NAME",        USER_NAME_VALUE);
        DEFAULT_PROPS.put("USER_PROPS_PATH",  USER_PROPS_PATH_VALUE);
        DEFAULT_PROPS.put("INFO_LIST_PATH",   INFO_LIST_PATH_VALUE);

        USER_PROPS.clear();
        USER_PROPS.putAll(DEFAULT_PROPS);
        USER_PROPS.put("MODE", "USER");
    }
}
