package View; /**
 * Created by Kenney on 9/06/17.
 */

import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.util.Observer;
import java.util.Observable;


public class ChatClient{

    /*
    Hidden class that allows the user that is opening the client to access the chat server.
     */
    static class AccessChat extends Observable {
        private Socket s;
        private OutputStream o;

        /*
        Notifies all the peers that are connected to the server, that is currently observing the input streams of all
         updates
         */
        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }

        /*
        Initiate for the client that is newly connected on to the server, create new server.
         */
        public void InitSocket(String server, int port) throws IOException {
            s = new Socket(server,port);
            o = s.getOutputStream();

            /*
            When this client has a socket create a new thread to put all process in. Notify all observers of other
            peers when a new message has been inputted.
             */
            Thread receive = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader((new InputStreamReader(s.getInputStream())));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            notifyObservers(line);
                        }
                    }  catch (IOException e) {
                        notifyObservers(e);
                    }
                }
            };
            receive.start();
        }
        /*
        When a new input as been typed in, write it onto the output stream and then flush.
         */
        public void send(String text) {
            try {
                o.write((text + "\n").getBytes());
                o.flush();
            } catch (IOException e) {
                notifyObservers(e);
            }
        }
        /*
        When the client decides to quit close the socket.
         */
        public void close() {
            try {
                s.close();
            } catch (IOException e) {
                notifyObservers(e);
            }
        }

    }

    /*
    Class for the chat GUI, the setting up is connecting to the stream is all in this class
     */
    static class ChatFrame extends JFrame implements Observer {
        private JTextArea textArea;
        private JTextField inputTextField;
        private AccessChat CA;
        private String ChatterName;

        /*
        When instantiated, put the chataccess class into a field, and add an this class as an observer to the
        chat access and run the UI building method
         */
        public ChatFrame(AccessChat CA) {
            this.CA = CA;
            CA.addObserver(this);
            askName();
        }

        public void askName() {
            JPanel namePanel = new JPanel(new BorderLayout());
            JTextArea message = new JTextArea(10,30);
            message.setEditable(false);
            message.setLineWrap(true);
            message.setVisible(true);
            message.setText("Please enter your display name:");
            namePanel.add(message, BorderLayout.NORTH);
            JTextField nameField = new JTextField();
            nameField.setVisible(true);
            namePanel.add(nameField, BorderLayout.CENTER);
            JButton confirm = new JButton("Confirm");
            confirm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    namePanel.setVisible(false);
                    remove(namePanel);
                    ChatterName = nameField.getText();
                    buildFrame();
                }
            });
            namePanel.add(confirm, BorderLayout.SOUTH);
            add(namePanel);
        }

        /*
        Function for building the main UI for chatting.
         */
        public void buildFrame() {
            setSize(350,580);
            textArea = new JTextArea( 40,40);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setVisible(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            inputTextField = new JTextField();
            inputTextField.setVisible(true);

            /*
            Add a listener for the enter button, whenever the enter button is pressed,
            send the message in the textfield to the other peers also append it to the the client's chat box
             */
            inputTextField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String input = inputTextField.getText();
                    if(input != null && input.trim().length() >0) {
                        CA.send(ChatterName + "> " + input);
                    }
                    inputTextField.selectAll();
                    inputTextField.requestFocus();
                    inputTextField.setText("");
                }
            });
            box.add(inputTextField);
            /*
            Add a listener to the window to check whenever the closed, close the chat access when it is closed.
             */
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    CA.close();
                }
            });
        }

        /*
        Looking at the observers, append the messages
         */
        @Override
        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textArea.append(finalArg.toString());
                    textArea.append("\n");
                }
            });
        }

    }


    public static void main (String args[]) throws IOException {
        if (args.length != 2) {
            throw new RuntimeException("Syntax: View.ChatClient <host> <port>");
        }
        /**
         * Set up frame and new chat access object
         */
        AccessChat access = new AccessChat();
        JFrame frame = new ChatFrame(access);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        try {
            access.InitSocket("localhost", 8000);
        } catch(IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

}
