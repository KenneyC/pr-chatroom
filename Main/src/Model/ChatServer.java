package Model; /**
 * Created by Kenney on 9/06/17.
 */

import java.net.*;
import java.io.*;
import java.util.*;


public class ChatServer {
    protected static Vector handlers = new Vector();
    public ChatServer() {

    }
    public ChatServer (int port) throws IOException {
        /*
        Server for sockets, kind of like a network of sockets. Sockets are end
        points of a communication, think of it like hosts.

        A socket is bound to a port number: so its kind of like making a
        socket listen to a specifiv port number for incoming message,
        or make socket send messages through a port to a targe end-point

         */
        ServerSocket server = new ServerSocket (port);
        /*
        A dynamic array that keeps track of all the connected clients
        */

        /*
        Sit in a loop and wait for clients to connect to the program.
        A new ChatHandler for each clients that is connected to the chatroom.
        Start the ChatHandler after connection.
         */
        while (true) {
            Socket client = server.accept();
            ClientThread cl = new ClientThread(client);
            cl.start();
            System.out.println("Connection accepted client: " + client.getInetAddress());
            handlers.addElement(cl);
        }
    }


    /*
    Create a new Model.ChatServer object, and passing the command-line port as a
    parameter. The clients will connected to this port.
     */
    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("Syntax: Model.ChatServer <port>");
        }
        new ChatServer (Integer.parseInt(args[0]));
    }




}
