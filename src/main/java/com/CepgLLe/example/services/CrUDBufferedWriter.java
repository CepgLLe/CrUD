package com.CepgLLe.example.services;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CrUDBufferedWriter implements Closeable {

    private BufferedWriter bw;

    public CrUDBufferedWriter(String name) throws IOException {
        this.bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name), StandardCharsets.UTF_8));
    }

    public CrUDBufferedWriter(String name, boolean append) throws IOException {
        this.bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name, append), StandardCharsets.UTF_8));
    }

    public void write(int c) throws IOException {
        bw.write(c);
    }

    public void write(String str) throws IOException {
        bw.write(str);
    }

    public void write(String str, int off, int len) throws IOException {
        bw.write(str, off, len);
    }

    public void newLine() throws IOException {
        bw.newLine();
    }

    public void close() throws IOException {
        bw.close();
    }
}
