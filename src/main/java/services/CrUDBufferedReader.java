package services;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CrUDBufferedReader extends BufferedReader {

    private BufferedReader br;

    public CrUDBufferedReader(String name) throws IOException {
        super(new InputStreamReader(new FileInputStream(name), StandardCharsets.UTF_8));
        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(name), StandardCharsets.UTF_8));
    }

    public CrUDBufferedReader(InputStream in) {
        super(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public String readLine() throws IOException {
        return br.readLine();
    }

    public void close() throws IOException {
        br.close();
    }
}
