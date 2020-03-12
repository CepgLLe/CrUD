package ancillary;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class CrUDWriterAdapter implements CrUDWriter {

    private BufferedWriter bw;

    public CrUDWriterAdapter(String name) throws IOException {
        this.bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name), StandardCharsets.UTF_8));
    }

    public CrUDWriterAdapter(String name, boolean append) throws IOException {
        this.bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name, append), StandardCharsets.UTF_8));
    }

    @Override
    public void write(String str) throws IOException {
        bw.write(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        bw.write(str, off, len);
    }

    @Override
    public void newLine() throws IOException {
        bw.newLine();
    }

    @Override
    public void close() throws IOException {
        bw.close();
    }
}
