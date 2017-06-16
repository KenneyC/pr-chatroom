package Model; /**
 * Created by Kenney on 11/06/17.
 */

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Vector;


public class ClientThread extends Thread{

    private Socket s;
    private PrintStream o;
    private DataInputStream i;
    private static Vector peers;

    public ClientThread(Socket socket) {
        peers = ChatServer.handlers;
        this.s = socket;
    }
    /*
    Run thread. On this thread when there is an input, read the line inserted, if the line is not null,
    the broadcast to all the other peers.
     */
    @Override
    public void run() {
        try {
            o = new PrintStream(s.getOutputStream());
            i = new DataInputStream(s.getInputStream());
            while(true) {
                String input = i.readLine();
                if(input != null) {
                    broadcast(input);
                } else if(input.equals("/quit")) {
                    break;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Broadcast the message to all peers that is in saved in the vector object.
     */
    public static void broadcast(String input) {
        synchronized (peers) {
            Enumeration e = peers.elements();
            /*
            Cycle through all the chathandlers and write on their output
            the message that has been passed into the method. which should
            be the input of the current chat client.

            We use Enumeration to access a easy iteration method.
             */
            while (e.hasMoreElements()) {
                ClientThread c = (ClientThread) e.nextElement();
                c.getWriter().println(input);
            }
        }
    }

    public PrintStream getWriter() {
        return o;
    }

}
