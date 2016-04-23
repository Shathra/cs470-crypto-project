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


//  Java
import java.io.*;

// socket
import java.net.*;



//  Crypto
import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ChatClient extends Thread {

    public static final int SUCCESS = 0;
    public static final int CONNECTION_REFUSED = 1;
    public static final int BAD_HOST = 2;
    public static final int ERROR = 3;
    String _loginName;
    ChatClientThread _thread;
    ChatLoginPanel _loginPanel;
    ChatRoomPanel _chatPanel;
    PrintWriter _out = null;
    BufferedReader _in = null;
    CardLayout _layout;
    JFrame _appFrame;
    String kAB;

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
    private HashMap<Integer, String> clientTable;
	
    public ChatClient() {

    	super("ChatClientListenThread");
    	
    	clientTable = new HashMap<Integer, String>();
    	clientTable.put( 5001, "clienta");
    	clientTable.put( 5002, "clientb");
    	
        _loginName = null;

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

    	try {
			//_socket = _serverSocket.accept();
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

            String kA = Utils.getKey(keyStoreName, String.valueOf( keyStorePassword), loginName, String.valueOf(password));
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
            
            asOut.println( loginName + " " + Utils.hash( kA));
            System.out.println( "send :" + kA);
            
            String msg = asIn.readLine();
            msg = msg.trim();
            String session = msg.split(" ")[0];
            String sA = Utils.decrypt_aes( kA, Constants.IV, session);
            String tgt = msg.split( " ")[1];
            asSocket.close();
            
            //TGS
            Socket tgsSocket = new Socket( Constants.HOST, tgsPort);
            PrintWriter tgsOut = new PrintWriter(tgsSocket.getOutputStream(), true);

            BufferedReader tgsIn = new BufferedReader(new InputStreamReader(
                    tgsSocket.getInputStream()));
            
            String idA = loginName;
            String idB = clientTable.get( portToConnect);
            String timestamp = Utils.encrypt_aes( sA, Constants.IV, Utils.getCurrentTimestamp());
            tgsOut.println( idA + " " + idB + " " + tgt + " " + timestamp);
            
            msg = tgsIn.readLine().trim();
            String[] msgInput = Utils.decrypt_aes(sA, Constants.IV, msg).split( " ");
            
            kAB = msgInput[1];
            String ticket = msgInput[2];
            
            tgsSocket.close();
            
            String current = Utils.getCurrentTimestamp();
            //Communication Initialization
            _out.println( idA + " " + ticket + " " + Utils.encrypt_aes( kAB, Constants.IV, current));
            
            msg = _in.readLine().trim();
            String receivedTimestamp = Utils.decrypt_aes( kAB, Constants.IV, msg);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        	Date receivedTime = dateFormat.parse( receivedTimestamp);
        	Date currentTime = dateFormat.parse( current);
        	System.out.println( receivedTime);
        	System.out.println( currentTime);
            long diffInMillies = receivedTime.getTime() - currentTime.getTime();
        	TimeUnit timeUnit = TimeUnit.SECONDS;
            long diff = timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
            
            System.out.println( diff);
            if( diff != 1) {
            	
            	System.out.println( "Timestamp check failed");
            	return ERROR;
            }

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
				_socket = _serverSocket.accept();
				
	            /*waitDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(_loginPanel), "Please Wait", Dialog.ModalityType.DOCUMENT_MODAL);
	            waitDialog.setLayout( new GridBagLayout());
	            waitDialog.setSize(300,100);
	            waitDialog.add( new JLabel( "...waiting a participant..."));
	            waitDialog.setLocationRelativeTo((JFrame) SwingUtilities.getWindowAncestor(_loginPanel));
	            waitDialog.setVisible(true);*/
				
				_out = new PrintWriter(_socket.getOutputStream(), true);
	            _in = new BufferedReader(new InputStreamReader(
	                    _socket.getInputStream()));
	            
	            String msg = _in.readLine().trim();
	            
	            String[] msgInput = msg.split( " ");
	            
	            String kB = Utils.getKey( keyStoreName, String.valueOf( keyStorePassword), loginName, String.valueOf( password));
	            
	            String idA = msgInput[0];
	            String ticket = Utils.decrypt_aes( kB, Constants.IV, msgInput[1]);
	            String idADec = ticket.split( " ")[0];
	            kAB = ticket.split( " ")[1];
	            String timestamp = Utils.decrypt_aes( kAB, Constants.IV, msgInput[2]);
	            
	            if( !idA.equals( idADec))
	            	return ERROR;
	            
	            if( !Utils.checkTimestamp(timestamp))
	            	return ERROR;
	            
	            
	            String timestampRespond = "";
	            
	            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	        	Date receivedTime = null;
				try {
					receivedTime = dateFormat.parse( timestamp);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	        	Calendar cal = Calendar.getInstance(); // creates calendar
	            cal.setTime(receivedTime); // sets calendar time/date
	            cal.add(Calendar.SECOND, 1); //Add one month
	            Date d = cal.getTime();
	            
	        	timestampRespond  = dateFormat.format(d);
	            
	        	
	            _out.println( Utils.encrypt_aes( kAB, Constants.IV, timestampRespond));
	            		
	            //this.start();
	            
				/*System.out.println( "Listening ends");
				waitDialog.setVisible(false);
				waitDialog.dispatchEvent(new WindowEvent(
				waitDialog, WindowEvent.WINDOW_CLOSING));
				_out = new PrintWriter(_socket.getOutputStream(), true);

		        _in = new BufferedReader(new InputStreamReader(
		                _socket.getInputStream()));*/

		        _layout.show(_appFrame.getContentPane(), "ChatRoom");

		        _thread = new ChatClientThread(this);
		        _thread.start();
	            
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
            String mac = Utils.generateMAC( msg, kAB);
            getOutputArea().append(msg + "\n");
            msg = Utils.encrypt_aes( kAB, Constants.IV, msg);
            
            _out.println(msg + " " + mac);

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

	public String getKAB() {
		
		return kAB;
	}
}
