//
//  AuthServerThread.java
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

import Chat.Utils.HashType;

//  Crypto
import java.security.*;
import java.security.spec.*;
import java.util.HashMap;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

public class AuthServerThread extends Thread {

    private AuthServer _as;
    private ServerSocket _serverSocket = null;
    private int _portNum;
    private String _hostName;
    private JTextArea _outputArea;
    
    private String asKeyStoreFilename;
    private String asKeyStorePassword;
    
    private static final String kaAlias = "clienta";
    private static final String kbAlias = "clientb";
    private static final String kaPassword = "passwordA";
    private static final String kbPassword = "passwordB";
    private static final String kkdcAlias = "kdc";
    private static final String kkdcPassword = "passwordKDC";
    
    private HashMap<String, String> passTable;

    public AuthServerThread(AuthServer as) {

        super("AuthServerThread");
        
        passTable = new HashMap<String, String>();
        passTable.put( kaAlias, kaPassword);
        passTable.put( kbAlias, kbPassword);
        
        _as = as;
        _portNum = as.getPortNumber();
        _outputArea = as.getOutputArea();
        _serverSocket = null;

        asKeyStoreFilename = _as.getAsKeyStoreName();
        asKeyStorePassword = _as.getAsKeyStorePassword();
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
                
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

    	        BufferedReader in = new BufferedReader(new InputStreamReader(
    	                socket.getInputStream()));
                
    	        _outputArea.append( "\nGot socket");
    	        
    	        String msg;
    	        boolean passwordCorrect = false;

				msg = in.readLine();
				
				msg = msg.trim();
                String id = msg.split( " ")[0];
                String hash = msg.split( " ")[1];
                
                _outputArea.append( "\n" + msg);
                if( passTable.containsKey( id)) {
                
                	
                	String kA = Utils.getKey( asKeyStoreFilename, asKeyStorePassword, id, passTable.get(id));
                	String ourHash = Utils.hash( kA);
	                if( hash.equals( ourHash))
	                	passwordCorrect = true;
	                
	                _outputArea.append( "\n" + asKeyStoreFilename + "-" + asKeyStorePassword);
	                _outputArea.append( "\nOur key: " + kA);
	                _outputArea.append( "\nOur hash: " + ourHash);
	                _outputArea.append( "\nReceived hash " + hash);
	                _outputArea.append( "\nRequest from " + id + " received " + passTable.get(id) + "\n");
	                
	                //Check if password is correct
                }
                if( passwordCorrect) {
                	
                	Key saKey;
                	SecureRandom rand = new SecureRandom();
                	KeyGenerator generator = KeyGenerator.getInstance("AES");
                	generator.init(rand);
                	generator.init(128);
                	saKey = generator.generateKey();
                	String sA = Utils.keyToString( saKey);
                	
                	String kKDC = Utils.getKey( asKeyStoreFilename, asKeyStorePassword, kkdcAlias, kkdcPassword);
                	String tgt = Utils.encrypt_aes( kKDC, Constants.IV, sA + " " + id);
                	String kA = Utils.getKey( asKeyStoreFilename, asKeyStorePassword, id, passTable.get( id));
                	out.println( Utils.encrypt_aes( kA, Constants.IV, sA) + " "+ tgt);
                	System.out.println( "Passwrod correct");
                }
                
                else {
                	
                	System.out.println( "Password is not correct");
                	socket.close();
                }
                
            }
        } catch (Exception e) {
            System.out.println("AS thread error: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
