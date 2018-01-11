package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import java.util.ArrayList;
import java.util.Date;

public class Controller
{
	/**
     * The port that the server listens on.
     */
    private static final int PORT = 9091;
    
    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static String talk="";
    private static String nameString;
    private static ArrayList<String> status= new ArrayList<String>();  
    
    /**
     * The appplication constructor,creates view, listens on a port and
     *  spawns handler threads.
     */
	public Controller() throws IOException{
		new View(talk, status, names);
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try 
        {
        
            while (true) 
            {
                new Handler(listener.accept()).start();
            }
        } 
        finally 
        {
        	
            listener.close();
        }
	}
	 
	/**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler extends Thread 
    {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        
        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) 
        {	
            this.socket = socket;
        }
        
    

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() 
        {
            try 
            {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) 
                {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) 
                    {
                        return;
                    }
                    synchronized (names) 
                    {
                        if (!names.contains(name)) 
                        {
                            names.add(name);
                            break;
                            
                        }
                    }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);
                System.out.println("wpisuje poprzednie wiadomosci");
                out.println(talk);
                //wpisywanie listy klientów
                writeNames();
                status.add(new Date()+": added >>"+name+"<<");

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) 
                {
                    String input = in.readLine();
                    System.out.println(input.length());
                    if(input.length()>0) 
                    {
                    	talk+="MESSAGE " + name + ": " + input+"\n";
                    }
                    if(talk.length()>10000) 
                    {
                    	talk=talk.substring(1000);
                    }
                    /*if (input == null) 
                    {
                        return;
                    }*/
                    else if (input.startsWith("#LOGOUT")) 
                    {
                    	System.out.println("zamykam to okno");
                    	kickPlayer(out, name);
                    	writeNames();
                    }
                    if(input.length()>0) 
                    {
                    	for (PrintWriter writer : writers) 
                    	{
                    		writer.println("MESSAGE " + name + ": " + input);
                    	}
                    }
                }
            } 
            catch (IOException e) 
            {
                System.out.println(e);
            } 
            finally 
            {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) 
                {
                    names.remove(name);
                }
                if (out != null) 
                {
                    writers.remove(out);
                }
                try 
                {
                    socket.close();
                }
                catch (IOException e) 
                {
                	e.printStackTrace();
                }
            }
        }
        
        
        
        public void kickPlayer(PrintWriter writer, String name) 
        {

        	status.add(new Date()+": deleted >>"+name+"<<");
        	names.remove(name);
        	writers.remove(writer);
        	try 
        	{
                socket.close();
            } catch (IOException | ArrayIndexOutOfBoundsException e) 
        	{
            	e.printStackTrace();
            }
        }
        
        
        public void writeNames()
        {
            nameString="";
            for(String name: names) 
            {
            	nameString=nameString+" "+name;
            }
            for (PrintWriter writer : writers) 
            {
                writer.println("NAMES " +nameString+" ");
            }
        }
    }
}


