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
import java.util.HashMap;
import javax.crypto.*;

public class TGServerThread extends Thread {

    private TGServer _tgs;
    private ServerSocket _serverSocket = null;
    private int _portNum;
    private String _hostName;
    private JTextArea _outputArea;
    
    private String tgsKeyStoreFileName;
    private String tgsKeyStorePassword;
    
    private static final String kaAlias = "clienta";
    private static final String kbAlias = "clientb";
    private static final String kaPassword = "passwordA";
    private static final String kbPassword = "passwordB";
    private static final String kkdcAlias = "kdc";
    private static final String kkdcPassword = "passwordKDC";
    
    private HashMap<String,String> passTable;
    

    public TGServerThread(TGServer tgs) {

        super("AuthServerThread");
        
        passTable = new HashMap<String, String>();
        passTable.put( kaAlias, kaPassword);
        passTable.put( kbAlias, kbPassword);
        _tgs = tgs;
        _portNum = tgs.getPortNumber();
        tgsKeyStoreFileName = _tgs.getKeyStoreFileName();
        tgsKeyStorePassword = _tgs.getKeyStorePassword();
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
                
                //generate a symmetric key  between client a and client b
            	Key abKey; //the symmetric key
            	SecureRandom rand = new SecureRandom();
            	KeyGenerator generator = KeyGenerator.getInstance("AES");
            	generator.init(rand);
            	generator.init(128);
            	abKey = generator.generateKey();
            	String kAB = Utils.keyToString( abKey);
                
            	//read input from the client a
                String [] input = asIn.readLine().trim().split(" ");
                String idA_tocheck = input[0]; //id of client a (what client a claims)
                String idB = input[1]; //id of client b
                String TGT = input[2]; //ticket granting ticket
                
                _outputArea.append( idA_tocheck+" sent a request to communacite with "+idB);
                
                String [] TGT_in = Utils.decrypt_aes(kKDC, Constants.IV, TGT).split(" ");
                String sA = TGT_in[0]; //session key of client a
                String idA = TGT_in[1]; //real id of client a (as authentication server says)
                
                String timestamp = Utils.decrypt_aes(sA, Constants.IV, input[3]);
                String kB = Utils.getKey(tgsKeyStoreFileName, tgsKeyStorePassword, idB, passTable.get( idB)); //password of client b

                //check if id of client a is and time-stamp are true
                if(idA_tocheck.equals( idA) && Utils.checkTimestamp(timestamp)){
                	asOut.println( Utils.encrypt_aes(sA, Constants.IV, idB+" "+kAB+" "+Utils.encrypt_aes(kB, Constants.IV, idA+" "+kAB)));
                	_outputArea.append("Response has been sent to "+idA);
                }
                else{
                	System.out.println("Your ID is wrong or timestamp is wrong!");
                	socket.close();
                }
                
                
            }
        } catch (Exception e) {
            System.out.println("TGS thread error: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
