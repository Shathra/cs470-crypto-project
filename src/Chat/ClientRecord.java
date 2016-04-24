//  ClientRecord.java
package Chat;


// socket
import java.net.*;


// You may need to expand this class for anonymity and revocation control.
public class ClientRecord {

    Socket _socket = null;

    public ClientRecord(Socket socket) {

        _socket = socket;
    }

    public Socket getClientSocket() {

        return _socket;
    }
}
