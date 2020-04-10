package com.CepgLLe.example;

import com.CepgLLe.example.services.*;
import com.CepgLLe.example.tools.CrUDUtils;
import com.CepgLLe.example.tools.Extract;

import java.io.*;
import java.util.LinkedList;
import java.util.Locale;

/**
 * <title> com.CepgLLe.example.CrUD </title>
 * <b>com.CepgLLe.example.CrUD</b> is a program for Create-Update-Delete goods. The com.CepgLLe.example.CrUD is create file like a table
 * with four columns.
 * <h3>Columns:</h3>
 * <ul>
 *     <li>ID number - from 1 to 99999999 (8 character column)</li>
 *     <li>Product name                   (30 character column)</li>
 *     <li>Price - from 0.01 to 99999999  (8 character column)</li>
 *     <li>Quantity - from 0 to 9999      (4 character column)</li>
 * </ul>
 * You can work with default file, create new data files, delete data file and
 * use another *.crud files.
 *
 * @author Dmitrii Charuskii
 */
public class CrUD {

    private static String workFilePath;

    static {
        try {
            Extract extract = new Extract();
            if (!extract.extracted())
                extract.extract();
//            CrUDUtils.loadProps();
            System.out.println("[INFO] Run with \"-use\" if you don't know how it's work!");
            System.out.println("[INFO] Welcome! The program by Dmitrii Charuiskii");
        } catch (Exception ex) {
            System.err.println("[ERROR] Error while start! Message: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
         if (args.length > 0) {
//             workFilePath = CrUDUtils.getWorkFile();
             switch (args[0]) {
                 case "-use":
                     try {
                         if (args.length != 1) throw new IndexOutOfBoundsException(">>> Unknown command <<<");
                         else CrUDUtils.getInstruction();
                     } catch (IndexOutOfBoundsException ex) {
                         System.err.println(ex.getMessage());
                     } catch (IOException ex) {
                         System.err.println(">>> Unknown exception <<<");
                     }
                     break;
                 case "-stgs":
                     try {
                         if (args.length == 1) System.out.println("Options: " + '\n' +
                                 "-stgs info - get files list with creators and statuses" + '\n' +
                                 "-stgs ch   - change the work file" + '\n' +
                                 "-stgs cr   - create a new data file" + '\n' +
                                 "-stgs d    - delete data file");
                         else if (args.length == 2)
                             switch (args[1]) {
                                 case "info":
                                     CrUDUtils.getInfo();
                                     break;
                                 case "ch":
                                     CrUDUtils.changeWorkFile();
                                     break;
                                 case "cr":
                                     CrUDUtils.createNewDataFile();
                                     break;
                                 case "d":
                                     CrUDUtils.deleteFile();
                                     break;
                             }
                         else throw new IndexOutOfBoundsException(">>> Unknown command <<<");
                     } catch (IOException ex) {
                         String message = ex.getMessage();
                         if (message.equals("exit")) return;
                         else System.err.println(message);
                     }
                     break;
                 case "-cr":
                     try {
                         create(args);
                     } catch (IOException e) {
                         System.err.println(">>> Error while create! <<< " + e.getMessage());
                     }
                     break;
                 case "-u":
                     try {
                         update(args);
                     } catch (IOException e) {
                         System.err.println(">>> Error while update! <<< " + e.getMessage());
                     }
                     break;
                 case "-d":
                     try {
                         delete(args);
                     } catch (IOException e) {
                         System.err.println(">>> Error while delete! <<< " + e.getMessage());
                     }
                     break;
                 case "-print":
                     try (CrUDBufferedReader reader = new CrUDBufferedReader(workFilePath)) {
                         String line;
                         while ((line = reader.readLine()) != null)
                             System.out.println(line);
                     } catch (IOException e) {
                         System.err.println(">>> Error while print! <<< " + e.getMessage());
                     }
                     break;
             }
         }
    }

    private static void create(String[] args) throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(workFilePath);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(workFilePath, true)) {

            String line;
            int lastId = 0;
            while ((line = reader.readLine()) != null) {
                int id = 0;
                if (!line.equals("|ID      |Product name                  |Price   |Quan|") &&
                    !line.equals("+--------+------------------------------+--------+----+"))
                    id = Integer.parseInt(line.substring(1, line.indexOf('|', 1)).trim());
                if (id > lastId) lastId = id;
            }
            reader.close();

            if (lastId < 100000000) lastId++;

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length - 2; i++) {
                sb.append(args[i]).append(" ");
            }

            float priceDouble = Float.parseFloat(args[args.length - 2]);

            int quantityInt = Integer.parseInt(args[args.length - 1]);

            writer.newLine();
            writer.write(String
                    .format(Locale.ROOT,"|%-8d|%-30.30s|%-8.2f|%-4d|", lastId, sb, priceDouble, quantityInt));
        }
    }

    private static void update(String[] args) throws IOException {
        LinkedList<String> buffList = new LinkedList<>();
        try (CrUDBufferedReader reader = new CrUDBufferedReader(workFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) buffList.add(line);
            reader.close();

            for (int i = 0; i < buffList.size(); i++) {
                String buff = buffList.get(i)
                        .substring(1, buffList.get(i).indexOf('|', 1))
                        .trim();
                if (buff.equals(args[1])) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 2; j < args.length - 2; j++) {
                        sb.append(args[j]).append(' ');
                    }
                    buffList.set(i, String
                            .format(Locale.ROOT, "|%-8.8s|%-30.30s|%-8.2f|%-4d|",
                                    buff, sb, Double.parseDouble(args[args.length - 2]), Integer.parseInt(args[args.length - 1])));
                }
            }
        }
        try (CrUDBufferedWriter writer = new CrUDBufferedWriter(workFilePath)) {
            for (int i = 0; i < buffList.size(); i++) {
                if (i == buffList.size() - 1) writer.write(buffList.get(i));
                else {
                    writer.write(buffList.get(i));
                    writer.newLine();
                }
            }
        }
    }

    private static void delete(String[] args) throws IOException {
        LinkedList<String> buffList = new LinkedList<>();
        try (CrUDBufferedReader reader = new CrUDBufferedReader(workFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) buffList.add(line);
            buffList.removeIf(s -> s.substring(1, s.indexOf('|', 1)).trim().equals(args[1]));
        }

        for (int i = 1; i < buffList.size(); i++) {
            String get = buffList.get(i).substring(buffList.get(i).indexOf('|', 1));
            buffList.set(i, String.format("|%-8d%s", i, get));
        }

        try (CrUDBufferedWriter writer = new CrUDBufferedWriter(workFilePath)) {
            for (int i = 0; i < buffList.size(); i++) {
                if (i == buffList.size() - 1) writer.write(buffList.get(i));
                else {
                    writer.write(buffList.get(i));
                    writer.newLine();
                }
            }
        }
    }
}
