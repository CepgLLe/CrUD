package com.CepgLLe.example.services;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CrUDBufferedReader implements Closeable {

    private BufferedReader br;

    public CrUDBufferedReader(String name) throws IOException {
        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(name), StandardCharsets.UTF_8));
    }

    public CrUDBufferedReader(InputStream in) {
        this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public boolean ready() throws IOException {
        return br.ready();
    }

    public int read() throws IOException {
        return br.read();
    }

    public String readLine() throws IOException {
        return br.readLine();
    }

    public void close() throws IOException {
        br.close();
    }
}
