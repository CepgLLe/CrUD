import ancillary.*;
import utils.CrUDUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.Locale;

/**
 * <title> CrUD </title>
 * <b>CrUD</b> is a program for Create-Update-Delete goods. The CrUD is create file like a table
 * with four columns but without separating lines.
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
            CrUDUtils.loadProps();
            workFilePath = CrUDUtils.getWorkFile();
            System.out.println("Run with \"-use\" if you don't know how it's work!");
        } catch (FileNotFoundException ex) {
            System.out.println(">>> Properties files not found! <<<");
        } catch (IOException ex) {
            System.out.println(">>> Problems with data transition! <<<");
        }
    }

    public static void main(String[] args) /*throws IOException*/ {
         if (args.length > 0)
            switch (args[0]) {
                case "-use" :
                    try {
                        if (args.length != 1) throw new IndexOutOfBoundsException(">>> Unknown command <<<");
                        else CrUDUtils.getInstruction();
                    } catch (IndexOutOfBoundsException ex) {
                        System.out.println(ex.getMessage());
                    } catch (IOException ex) {
                        System.out.println(">>> Unknown exception <<<");
                    }
                    break;
                case "-stgs" :
                    try {
                        if (args.length == 1) System.out.println("Options: "              + '\n' +
                                "-stgs info  - get files list with creators and statuses" + '\n' +
                                "-stgs user  - changing the user"                         + '\n' +
                                "-stgs cr    - create a new data file"                    + '\n' +
                                "-stgs d     - delete data file"                          + '\n' +
                                "-stgs add   - adding *.crud files"                       + '\n' +
                                "-stgs reset - for use the \"DEFAULT MODE\"");
                        else if (args.length == 2)
                            switch (args[1]) {
                                case "info" :
                                    CrUDUtils.getInfo();
                                    break;
                                case "user" :
                                    CrUDUtils.user();
                                    break;
                                case "cr" :
                                    CrUDUtils.createNewDataFile();
                                    break;
                                case "d" :
                                    CrUDUtils.deleteFile();
                                    break;
                                case "add" :
                                    CrUDUtils.addFile();
                                    break;
                                case "reset" :
                                    CrUDUtils.reset();
                                    break;
                            }
                        else throw new IndexOutOfBoundsException(">>> Unknown command <<<");
                    } catch (IOException ex) {
                        String message = ex.getMessage();
                        if (message.equals("exit")) return;
                        else System.out.println(message);
                    }
                    break;
                case "-cr":
                    try {
                        create(args);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "-u":
                    try {
                        update(args);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "-d":
                    try {
                        delete(args);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        else System.out.println("Welcome! The program by Dmitrii Charuiskii");
    }

    private static void create(String[] args) throws IOException {
        try (CrUDBufferedReader reader = new CrUDBufferedReader(workFilePath);
             CrUDBufferedWriter writer = new CrUDBufferedWriter(workFilePath, true)) {

            String line;
            int lastId = 0;
            while ((line = reader.readLine()) != null) {
                int id = (!line.substring(0, 8).contains(" ")) ?
                        Integer.parseInt(line.substring(0, 8)) :
                        Integer.parseInt(line.substring(0, line.indexOf(" ")));
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
            writer.write(String.format("%-8d%-30.30s%-8.2f%-4d", lastId, sb, priceDouble, quantityInt).
                    replace(',', '.'));
        }
    }

    private static void update(String[] args) throws IOException {
        LinkedList<String> buffList = new LinkedList<>();
        try (CrUDBufferedReader reader = new CrUDBufferedReader(workFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) buffList.add(line);
            reader.close();

            for (int i = 0; i < buffList.size(); i++) {
                if (buffList.get(i).substring(0, 8).trim().equals(args[1])) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 2; j < args.length - 2; j++) {
                        sb.append(args[j]).append(' ');
                    }
                    buffList.set(i, String.
                            format(Locale.ROOT, buffList.get(i).substring(0, 8) + "%-30.30s%-8.2f%-4d",
                                    sb, Double.parseDouble(args[args.length - 2]), Integer.parseInt(args[args.length - 1])));
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
            buffList.removeIf(s -> s.substring(0, 8).trim().equals(args[1]));
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
