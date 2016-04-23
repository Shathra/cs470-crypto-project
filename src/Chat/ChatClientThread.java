/**
 *  Created 2/16/2003 by Ting Zhang 
 *  Part of implementation of the ChatClient to receive
 *  all the messages posted to the chat room.
 */
package Chat;

// socket
import java.net.*;
import java.io.*;

//  Swing
import javax.swing.JTextArea;

//  Crypto
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

public class ChatClientThread extends Thread {

    private ChatClient _client;
    private JTextArea _outputArea;
    private Socket _socket = null;
    private String kAB;
    
    //Server related fields
    private ServerSocket _serverSocket = null;

    public ChatClientThread(ChatClient client) {

        super("ChatClientThread");
        _client = client;
        _socket = client.getSocket();
        _outputArea = client.getOutputArea();
        kAB = client.getKAB();
    }

    public void run() {

        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    _socket.getInputStream()));

            String msg;

            while ((msg = in.readLine()) != null) {

            	msg = msg.trim();
            	String msg_enc = msg.split( " ")[0];
            	String mac = msg.split( " ")[1];
            	String msg_dec = Utils.decrypt_aes(kAB, Constants.IV, msg_enc);
            	String macComputed = Utils.generateMAC(msg_dec, kAB);
            	
            	if( macComputed.equals(mac))
            		consumeMessage(msg_dec);
            	
            	else
            		consumeMessage( "System> EVE ATTACKED");
            }

            _socket.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void consumeMessage(String msg) {


        if (msg != null) {
            _outputArea.append(msg + "\n");
        }

    }
}
