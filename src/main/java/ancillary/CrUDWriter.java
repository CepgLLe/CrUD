package ancillary;

import java.io.IOException;

public interface CrUDWriter extends AutoCloseable {

    void write(String str) throws IOException;

    void write(String str, int off, int len) throws IOException;

    void newLine() throws IOException;

    @Override
    void close() throws IOException;
}
