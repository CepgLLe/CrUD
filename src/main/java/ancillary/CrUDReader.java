package ancillary;

import java.io.IOException;

public interface CrUDReader extends AutoCloseable {

    String readLine() throws IOException;

    @Override
    void close() throws IOException;
}

