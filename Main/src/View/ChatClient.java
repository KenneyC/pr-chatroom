package View; /**
 * Created by Kenney on 9/06/17.
 */

import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
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
            When this client has
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

        public void send(String text) {
            try {
                o.write((text + "\n").getBytes());
                o.flush();
            } catch (IOException e) {
                notifyObservers(e);
            }
        }

        public void close() {
            try {
                s.close();
            } catch (IOException e) {
                notifyObservers(e);
            }
        }

    }

    static class ChatFrame extends JFrame implements Observer {
        private JTextArea textArea;
        private JTextField inputTextField;
        private AccessChat CA;

        public ChatFrame(AccessChat CA) {
            this.CA = CA;
            CA.addObserver(this);
            buildFrame();
        }

        public void buildFrame() {
            textArea = new JTextArea( 40,40);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setVisible(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            inputTextField = new JTextField();
            inputTextField.setVisible(true);
            inputTextField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String input = inputTextField.getText();
                    if(input != null && input.trim().length() >0) {
                        CA.send(input);
                    }
                    inputTextField.selectAll();
                    inputTextField.requestFocus();
                    inputTextField.setText("");
                }
            });
            box.add(inputTextField);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    CA.close();
                }
            });
        }

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
