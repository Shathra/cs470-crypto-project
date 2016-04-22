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
                Socket asSocket = new Socket( Constants.HOST, Constants.TGS_PORT);
                
                PrintWriter asOut = new PrintWriter(asSocket.getOutputStream(), true);
                BufferedReader asIn = new BufferedReader(new InputStreamReader(
                        asSocket.getInputStream()));
                
                String kKDC = Utils.getKey(Constants.keyStoreFileName, Constants.keyStorePassword, "KDC", Constants.KDCpassword);
                String kAB = Utils.getKey(Constants.keyStoreFileName, Constants.keyStorePassword, "AB", Constants.ABpassword);
                String kB = Utils.getKey(Constants.keyStoreFileName, Constants.keyStorePassword, "B", Constants.Bpassword);
                
                String [] input = asIn.readLine().split(" ");
                String idA_tocheck = input[0];
                String idB = input[1];
                String TGT = input[2];
                
                String [] TGT_in = Utils.decrypt_aes(kKDC, Utils.initVector, TGT).split(" ");
                String sA = TGT_in[0];
                String idA = TGT_in[1];
                
                String timestamp = Utils.decrypt_aes(sA, Utils.initVector, input[3]);

                if(idA_tocheck == idA){
                	asOut.println( Utils.encrypt_aes(sA, Utils.initVector, idB+" "+kAB+" "+Utils.encrypt_aes(kB, Utils.initVector, idA+" "+kAB)));
                }
                else{
                	System.out.println("Your ID is wrong!");
                }
                
                
            }
        } catch (Exception e) {
            System.out.println("AS thread error: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
