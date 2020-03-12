package ancillary;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CrUDReaderAdapter implements CrUDReader {

    private BufferedReader br;

    public CrUDReaderAdapter(String name) throws IOException {
        this.br = new BufferedReader(new InputStreamReader(new FileInputStream(name), StandardCharsets.UTF_8));
    }

    public CrUDReaderAdapter(InputStream in) {
        this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    @Override
    public String readLine() throws IOException {
        String line;
        if ((line = br.readLine()) != null) return line;
        return null;
    }

    @Override
    public void close() throws IOException {
        br.close();
    }
}
