//
//  TGServerThread.java
//
//  Written by : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  Accepts connection requests and processes them
package Chat;

// socket
import java.net.*;
import java.io.*;

// Swing
import javax.swing.JTextArea;

//  Crypto
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

public class TGServerThread extends Thread {

    private TGServer _tgs;
    private ServerSocket _serverSocket = null;
    private int _portNum;
    private String _hostName;
    private JTextArea _outputArea;
    
    private String tgsKeyStoreFileName;
    private String tgsKeyStorePassword;
    
    private static final String kaAlias = "clientA";
    private static final String kbAlias = "clientB";
    private static final String kaPassword = "passwordA";
    private static final String kbPassword = "passwordB";
    private static final String kkdcAlias = "kdc";
    private static final String kkdcPassword = "passwordKDC";
    

    public TGServerThread(TGServer tgs) {

        super("AuthServerThread");
        _tgs = tgs;
        _portNum = tgs.getPortNumber();
        _outputArea = tgs.getOutputArea();
        _serverSocket = null;

        try {

            InetAddress serverAddr = InetAddress.getByName(null);
            _hostName = serverAddr.getHostName();

        } catch (UnknownHostException e) {
            _hostName = "0.0.0.0";
        }
    }
    
    //  Accept connections and service them one at a time
    public void run() {
        try {
            _serverSocket = new ServerSocket(_portNum);
            _outputArea.append("AS waiting on " + _hostName + " port " + _portNum);
            while (true) {
                Socket socket = _serverSocket.accept();
                //
                //  Got the connection, now do what is required
                // 
                
                PrintWriter asOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader asIn = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                
                String kKDC = Utils.getKey(tgsKeyStoreFileName, tgsKeyStorePassword, kkdcAlias, kkdcPassword);
                String kB = Utils.getKey(tgsKeyStoreFileName, tgsKeyStorePassword, kbAlias, kbPassword);
                
            	Key abKey;
            	SecureRandom rand = new SecureRandom();
            	KeyGenerator generator = KeyGenerator.getInstance("AES");
            	generator.init(rand);
            	generator.init(128);
            	abKey = generator.generateKey();
            	String kAB = Utils.keyToString( abKey);
                
                String [] input = asIn.readLine().split(" ");
                String idA_tocheck = input[0];
                String idB = input[1];
                String TGT = input[2];
                
                String [] TGT_in = Utils.decrypt_aes(kKDC, Constants.IV, TGT).split(" ");
                String sA = TGT_in[0];
                String idA = TGT_in[1];
                
                String timestamp = Utils.decrypt_aes(sA, Constants.IV, input[3]);

                if(idA_tocheck == idA){
                	asOut.println( Utils.encrypt_aes(sA, Constants.IV, idB+" "+kAB+" "+Utils.encrypt_aes(kB, Constants.IV, idA+" "+kAB)));
                }
                else{
                	System.out.println("Your ID is wrong!");
                	socket.close();
                }
                
                
            }
        } catch (Exception e) {
            System.out.println("AS thread error: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
