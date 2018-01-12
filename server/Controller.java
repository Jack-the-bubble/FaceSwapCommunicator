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


/**
 * @author Boba
 *wielow�tkowy serwer komunikatora wszyscy-do-wszystkich, 
 *tworz�cy po jednym w�tku na jedno po��czenie z klientem, 
 *odbiera od niego komunikat i wysy�a do wszystkich u�ytkownik�w chatu
 */
public class Controller
{
	/**
     * port, na kt�rym nas�uchuje serwer
     */
    private static final int PORT = 9091;
    
    /**
     * Set, kt�ry przechowuje nazwy u�tkownik�w czatu, wykorzystywany 
     * do sprawdzenia, czy dana nazwa ju� zosta�a uzyta 
     * oraz wypisania wszystkich rozm�wc�w
     *
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * Set wszystkich obiekt�w PrintWriter, do wysy�ania wiadomo�ci 
     * do wszystkich u�ytkownik�w chatu
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    /**
     * historia chatu
     */
    private static String talk="";
    /**
     * wszystkie nazwy u�ytkownik�w do wys�ania klientom
     */
    private static String nameString;
    /**
     * lista wszystkich komunikat�w w serwerze 
     * (dodanie/usuni�cie u�tykownika z dat� i godzin�)
     */
    private static ArrayList<String> status= new ArrayList<String>();  
    
    /**
     * Konstruktor klasy Controller, tworzy instancj� widoku, s�ucha na wybranym porcie
     * oczekuj�c na po��czenie oraz tworzy now� instancj� klasy wewn�trznej 
     * odpowiedzialnej zakomunikacj�
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
	 * klasa wewn�trzna, odpowiedzialna za wielow�tkowo��. Ka�da jej instancja 
	 * jest odpowiedzialna za odbieranie wiadomo�ci od jednego klienta i wysy�anie 
	 * do pozosta�ych.
     */
    private static class Handler extends Thread 
    {
        /**
         * nazwa u�ytkownika
         */
        private String name;
        /**
         * socket do nawi�zania po��czenia z klientem
         */
        private Socket socket;
        /**
         * obiekt do odbierania wiadomo�ci od klienta
         */
        private BufferedReader in;
        /**
         * obiekt do wysy�ania wiadomo�ci
         */
        private PrintWriter out;
        
        /**
         * Kontruktor klasy Handler, przypisuj�cy stworzony socket 
         * do pola klasy wewn�trznej
         * @param socket do po��czenia z klientem
         */
        public Handler(Socket socket) 
        {	
            this.socket = socket;
        }
        
    

        /**
         * g��wna cz�� klasy, tworzy elementy do komunikacji tekstowe z klientem,
         * czeka na podanie unikatowej nazwy u�ytkownika, wpisuje wiadonmo�ci, 
         * kt�re zosta�y nadane wcze�niej, zarz�dza list� klient�w po stronie serwera
         * (komunikaty 'added'), wysy�a wiadomo�ci do wszystkich u�ytkownik�w,
         * od��cza klient�w, gdy chc� si� wylogowa�
         */
        public void run() 
        {
            try 
            {
            	//tworzenie obiekt�w strumienia do obs�ugi komunikacji z klientem
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                //pyta o podanie unikatowego imienia. Je�li wpisana nazwa ju�
                //znajduje si� w hashSecie, nie mo�na rozpocz�� chatu
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
                //dodanie strumienia wyj�cia do setu pozosta�ych, aby umo�liwi�
                //wysy�anie wiadomo�ci do wszystkich u�ytkownik�w 
                out.println("NAMEACCEPTED");
                writers.add(out);
                //wys�anie poprzednich wiadomo�ci czatu 
                out.println(talk);
                //wpisywanie listy klient�w i tworzenie komunikatu serwera
                writeNames();
                status.add(new Date()+": added >>"+name+"<<");
                
                //zbieranie wiadomo�ci od u�ytkownika, kontrola d�ugo�ci
                //historii czatu, obs�uga ��dania wylogowania oraz wys�ania wiadomo�ci
                //do wszystkich
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
            	//usuni�cie u�ytkownika w razie wypadku
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
        
        
        
        /**
         * funkcja do usuni�cia u�ytkownika, usuwa z listy imion i instancji PrintWriter,
         * dodaje komunikat po stronie serwera (komunikaty 'deleted') i zamyka socket
         * @param writer element hashSetu PrintWriter
         * @param name nazwa u�ytkownika w hashSecie names, do usuni�cia
         */
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
        
        
        /**
         * metoda do wpisywania ka�demu u�ytkownikowi zaktualizowanej listy aktywnych 
         * klient�w czatu
         * wpisuje do zmiennej string wszystkie aktualne nicki, 
         * nast�pnnie wysy�a je wszystkim klientom
         */
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


