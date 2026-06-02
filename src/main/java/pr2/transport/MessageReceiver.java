package pr2.transport;

import java.io.IOException;

public interface MessageReceiver {
    byte[] receive() throws InterruptedException, IOException;
}
