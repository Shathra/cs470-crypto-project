//  ChatClient.java
//
//  Modified 1/30/2000 by Alan Frindell
//  Last modified 2/18/2003 by Ting Zhang 
//  Last modified : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  Chat Client starter application.
package Chat;

//  AWT/Swing
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Chat.Utils.HashType;

//  Java
import java.io.*;
import java.math.BigInteger;

// socket
import java.net.*;
import java.io.*;
import java.net.*;



//  Crypto
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import javax.security.auth.x500.*;

public class ChatClient extends Thread {

    public static final int SUCCESS = 0;
    public static final int CONNECTION_REFUSED = 1;
    public static final int BAD_HOST = 2;
    public static final int ERROR = 3;
    String _loginName;
    ChatServer _server;
    ChatClientThread _thread;
    ChatLoginPanel _loginPanel;
    ChatRoomPanel _chatPanel;
    PrintWriter _out = null;
    BufferedReader _in = null;
    CardLayout _layout;
    JFrame _appFrame;

    Socket _socket = null;
    SecureRandom secureRandom;
    KeyStore clientKeyStore;
    KeyStore caKeyStore;
//    KeyManagerFactory keyManagerFactory;
//    TrustManagerFactory trustManagerFactory;
	private ServerSocket _serverSocket;
	private JDialog waitDialog;
  
    //  ChatClient Constructor
    //
    //  empty, as you can see.
    public ChatClient() {

    	super("ChatClientListenThread");
        _loginName = null;
        _server = null;

        try {
            initComponents();
        } catch (Exception e) {
            System.out.println("ChatClient error: " + e.getMessage());
            e.printStackTrace();
        }

        _layout.show(_appFrame.getContentPane(), "Login");

    }

    public void show() {
        _appFrame.pack();
        _appFrame.setVisible(true);

    }
    
    public void run() {
    	
    	System.out.println( "Listening started");
    	try {
			_socket = _serverSocket.accept();
			System.out.println( "Listening ends");
			waitDialog.setVisible(false);
			waitDialog.dispatchEvent(new WindowEvent(
			waitDialog, WindowEvent.WINDOW_CLOSING));
			_out = new PrintWriter(_socket.getOutputStream(), true);

	        _in = new BufferedReader(new InputStreamReader(
	                _socket.getInputStream()));

	        _layout.show(_appFrame.getContentPane(), "ChatRoom");

	        _thread = new ChatClientThread(this);
	        _thread.start();
		} catch (IOException e) {
			System.out.println("ChatClient err: " + e.getMessage());
            e.printStackTrace();
		}
    }

    //  main
    //
    //  Construct the app inside a frame, in the center of the screen
    public static void main(String[] args) {
        
        ChatClient app = new ChatClient();

        app.show();
    }

    //  initComponents
    //
    //  Component initialization
    private void initComponents() throws Exception {

        _appFrame = new JFrame("WutsApp");
        _layout = new CardLayout();
        _appFrame.getContentPane().setLayout(_layout);
        _loginPanel = new ChatLoginPanel(this);
        _chatPanel = new ChatRoomPanel(this);
        _appFrame.getContentPane().add(_loginPanel, "Login");
        _appFrame.getContentPane().add(_chatPanel, "ChatRoom");
        _appFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
    }

    //  quit
    //
    //  Called when the application is about to quit.
    public void quit() {

        try {
            _socket.shutdownOutput();
            _thread.join();
            _socket.close();

        } catch (Exception err) {
            System.out.println("ChatClient error: " + err.getMessage());
            err.printStackTrace();
        }

        System.exit(0);
    }

    //
    //  connect
    //
    //  Called from the login panel when the user clicks the "connect"
    //  button. You will need to modify this method to add certificate
    //  authentication.  
    //  There are two passwords : the keystorepassword is the password
    //  to access your private key on the file system
    //  The other is your authentication password on the CA.
    //
    public int connect(String loginName, char[] password,
            String keyStoreName, char[] keyStorePassword,
            int portToListen, int portToConnect,
            int asPort, int tgsPort) {

    	boolean listen = false;
    	boolean connected = false;
        try {

            _loginName = loginName;


            //
            //  Read the client keystore
            //         (for its private/public keys)
            //  Establish secure connection to the CA
            //  Send public key and get back certificate
            //  Use certificate to establish secure connection with server
            //

            _socket = new Socket(Constants.HOST, portToConnect);
            _out = new PrintWriter(_socket.getOutputStream(), true);

            _in = new BufferedReader(new InputStreamReader(
                    _socket.getInputStream()));
            
            connected = true;
            Socket asSocket = new Socket( Constants.HOST, asPort);
            PrintWriter asOut = new PrintWriter(asSocket.getOutputStream(), true);

            BufferedReader asIn = new BufferedReader(new InputStreamReader(
                    asSocket.getInputStream()));
            
            asOut.println( loginName + " " + Utils.hash( Constants.KA, HashType.MD5));
            System.out.println( "send login name");
            
            String msg = asIn.readLine();
            msg = msg.trim();
            String session = msg.split(" ")[0];
            String tgt = msg.split( " ")[1];
            
            System.out.println( session + "," + tgt);

            _layout.show(_appFrame.getContentPane(), "ChatRoom");
            _thread = new ChatClientThread(this);
            _thread.start();
            return SUCCESS;

        } catch (UnknownHostException e) {

            System.err.println("Don't know about the serverHost: " + Constants.HOST);
            System.exit(1);

        } catch (IOException e) {

        	if( !connected)
        		listen = true;
        	
        	else {
        		
        		System.err.println("Couldn't get I/O for "
                        + "the connection to the serverHost: " + Constants.HOST);
        		e.printStackTrace();
        	}

        } catch (AccessControlException e) {

            return BAD_HOST;

        } catch (Exception e) {

            System.out.println("ChatClient err: " + e.getMessage());
            e.printStackTrace();
        }
        
        if( listen) {
        	
        	try {
        		
				_serverSocket = new ServerSocket(portToListen);
	            this.start();
	            waitDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(_loginPanel), "Please Wait", Dialog.ModalityType.DOCUMENT_MODAL);
	            waitDialog.setLayout( new GridBagLayout());
	            waitDialog.setSize(300,100);
	            waitDialog.add( new JLabel( "...waiting a participant..."));
	            waitDialog.setLocationRelativeTo((JFrame) SwingUtilities.getWindowAncestor(_loginPanel));
	            waitDialog.setVisible(true);
	            
	            return SUCCESS;
			} catch (IOException e) {
				
				System.err.println("Could not listen on port: " + portToListen);
	            System.exit(-1);
			}
        }

        return ERROR;

    }

    //  sendMessage
    //
    //  Called from the ChatPanel when the user types a carrige return.
    public void sendMessage(String msg) {

        try {

            msg = _loginName + "> " + msg;

            _out.println(msg);
            getOutputArea().append(msg + "\n");

        } catch (Exception e) {

            System.out.println("ChatClient err: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public Socket getSocket() {

        return _socket;
    }

    public JTextArea getOutputArea() {

        return _chatPanel.getOutputArea();
    }
}
