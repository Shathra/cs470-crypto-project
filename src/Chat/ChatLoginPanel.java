//  ChatLoginPanel.java
//
//  Last modified 1/30/2000 by Alan Frindell
//  Last modified : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  GUI class for the login panel.
//
//  You should not have to modify this class.
package Chat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class ChatLoginPanel extends JPanel {

    JTextField _loginNameField;
    JPasswordField _passwordField;
    JTextField _portToListenField;
    JTextField _portToConnectField;
    JTextField _asPortField;
    JTextField _tgsPortField;
    JTextField _keyStoreNameField;
    JPasswordField _keyStorePasswordField;
    JLabel _errorLabel;
    JButton _connectButton;
    JButton _switchButton;
    ChatClient _client;

    public ChatLoginPanel(ChatClient client) {
        _client = client;

        try {
            componentInit();
        } catch (Exception e) {
            System.out.println("ChatLoginPanel error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void componentInit() throws Exception {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridBag);

        addLabel(gridBag, "Welcome to High Security Chat", SwingConstants.CENTER,
                1, 0, 2, 1);
        addLabel(gridBag, "Username: ", SwingConstants.LEFT, 1, 1, 1, 1);
        addLabel(gridBag, "Password: ", SwingConstants.LEFT, 1, 2, 1, 1);
        addLabel(gridBag, "KeyStore File Name: ", SwingConstants.LEFT, 1, 3, 1, 1);
        addLabel(gridBag, "KeyStore Password: ", SwingConstants.LEFT, 1, 4, 1, 1);
        addLabel(gridBag, "Port to Listen: ", SwingConstants.LEFT, 1, 5, 1, 1);
        addLabel(gridBag, "Port to Connect: ", SwingConstants.LEFT, 1, 6, 1, 1);
        addLabel(gridBag, "AS Port: ", SwingConstants.LEFT, 1, 7, 1, 1);
        addLabel(gridBag, "TGS Port: ", SwingConstants.LEFT, 1, 8, 1, 1);

        _loginNameField = new JTextField();
        addField(gridBag, _loginNameField, 2, 1, 1, 1);
        _passwordField = new JPasswordField();
        _passwordField.setEchoChar('*');
        addField(gridBag, _passwordField, 2, 2, 1, 1);

        _keyStoreNameField = new JTextField();
        addField(gridBag, _keyStoreNameField, 2, 3, 1, 1);
        _keyStorePasswordField = new JPasswordField();
        _keyStorePasswordField.setEchoChar('*');
        addField(gridBag, _keyStorePasswordField, 2, 4, 1, 1);

        _portToListenField = new JTextField();
        addField(gridBag, _portToListenField, 2, 5, 1, 1);
        _portToConnectField = new JTextField();
        addField(gridBag, _portToConnectField, 2, 6, 1, 1);

        _asPortField = new JTextField();
        addField(gridBag, _asPortField, 2, 7, 1, 1);
        _tgsPortField = new JTextField();
        addField(gridBag, _tgsPortField, 2, 8, 1, 1);

        _errorLabel = addLabel(gridBag, " ", SwingConstants.CENTER,
                1, 9, 2, 1);

        // just for testing purpose

        _loginNameField.setText("clienta");
        _passwordField.setText("passwordA");
        _keyStoreNameField.setText("storeA.jceks");
        _keyStorePasswordField.setText("passwordA");
        _asPortField.setText("" + Constants.AS_PORT);
        _tgsPortField.setText("" + Constants.TGS_PORT);
        _portToListenField.setText("" + Constants.CLIENT1_PORT);
        _portToConnectField.setText("" + Constants.CLIENT2_PORT);

        _errorLabel.setForeground(Color.red);

        _connectButton = new JButton("Connect");
        _switchButton = new JButton( "Switch Values");
        c.gridx = 1;
        c.gridy = 10;
        c.gridwidth = 2;
        gridBag.setConstraints(_connectButton, c);
        c.gridx = 2;
        gridBag.setConstraints(_switchButton, c);
        add(_connectButton);
        add(_switchButton);

        _connectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
        
        _switchButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
            	_loginNameField.setText("clientb");
                _passwordField.setText("passwordB");
                _keyStoreNameField.setText("storeB.jceks");
                _keyStorePasswordField.setText("passwordB");
                _portToListenField.setText("" + Constants.CLIENT2_PORT);
                _portToConnectField.setText("" + Constants.CLIENT1_PORT);
            }
        });
    }

    JLabel addLabel(GridBagLayout gridBag, String labelStr, int align,
            int x, int y, int width, int height) {
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel(labelStr);
        if (align == SwingConstants.LEFT) {
            c.anchor = GridBagConstraints.WEST;
        } else {
            c.insets = new Insets(10, 0, 10, 0);
        }
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        gridBag.setConstraints(label, c);
        add(label);

        return label;
    }

    void addField(GridBagLayout gridBag, JTextField field, int x, int y,
            int width, int height) {
        GridBagConstraints c = new GridBagConstraints();
        field.setPreferredSize(new Dimension(96,
                field.getMinimumSize().height));
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        gridBag.setConstraints(field, c);
        add(field);
    }

    private void connect() {

        int portToListen, portToConnect;
        int asPort = Constants.AS_PORT;
        int tgsPort = Constants.TGS_PORT;

        String loginName = _loginNameField.getText();
        char[] password = _passwordField.getPassword();

        String keyStoreName = _keyStoreNameField.getText();
        char[] keyStorePassword = _keyStorePasswordField.getPassword();

        if (loginName.equals("")
                || password.length == 0
                || keyStoreName.equals("")
                || keyStorePassword.length == 0
                || _portToListenField.getText().equals("")
                || _portToConnectField.getText().equals("")
                || _asPortField.getText().equals("")
                || _tgsPortField.getText().equals("")) {

            _errorLabel.setText("Missing required field.");

            return;

        } else {

            _errorLabel.setText(" ");

        }

        try {

            portToListen = Integer.parseInt(_portToListenField.getText());
            portToConnect = Integer.parseInt(_portToConnectField.getText());
            asPort = Integer.parseInt(_asPortField.getText());
            tgsPort = Integer.parseInt(_tgsPortField.getText());

        } catch (NumberFormatException nfExp) {

            _errorLabel.setText("Port field is not numeric.");

            return;
        }

        System.out.println("We are connecting to ...");

        switch (_client.connect(loginName,
                password,
                keyStoreName,
                keyStorePassword,
                portToListen,
                portToConnect,
                asPort,
                tgsPort)) {

            case ChatClient.SUCCESS:
                //  Nothing happens, this panel is now hidden
                _errorLabel.setText(" ");
                break;
            case ChatClient.CONNECTION_REFUSED:
            case ChatClient.BAD_HOST:
                _errorLabel.setText("Connection Refused!");
                break;
            case ChatClient.ERROR:
                _errorLabel.setText("ERROR!  Stop That!");
                break;

        }

        System.out.println("We finished connecting to ...");


    }
}
