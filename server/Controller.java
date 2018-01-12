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
 *wielow¹tkowy serwer komunikatora wszyscy-do-wszystkich, 
 *tworz¹cy po jednym w¹tku na jedno po³¹czenie z klientem, 
 *odbiera od niego komunikat i wysy³a do wszystkich u¿ytkowników chatu
 */
public class Controller
{
	/**
     * port, na którym nas³uchuje serwer
     */
    private static final int PORT = 9091;
    
    /**
     * Set, który przechowuje nazwy u¿tkowników czatu, wykorzystywany 
     * do sprawdzenia, czy dana nazwa ju¿ zosta³a uzyta 
     * oraz wypisania wszystkich rozmówców
     *
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * Set wszystkich obiektów PrintWriter, do wysy³ania wiadomoœci 
     * do wszystkich u¿ytkowników chatu
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    /**
     * historia chatu
     */
    private static String talk="";
    /**
     * wszystkie nazwy u¿ytkowników do wys³ania klientom
     */
    private static String nameString;
    /**
     * lista wszystkich komunikatów w serwerze 
     * (dodanie/usuniêcie u¿tykownika z dat¹ i godzin¹)
     */
    private static ArrayList<String> status= new ArrayList<String>();  
    
    /**
     * Konstruktor klasy Controller, tworzy instancjê widoku, s³ucha na wybranym porcie
     * oczekuj¹c na po³¹czenie oraz tworzy now¹ instancjê klasy wewnêtrznej 
     * odpowiedzialnej zakomunikacjê
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
	 * klasa wewnêtrzna, odpowiedzialna za wielow¹tkowoœæ. Ka¿da jej instancja 
	 * jest odpowiedzialna za odbieranie wiadomoœci od jednego klienta i wysy³anie 
	 * do pozosta³ych.
     */
    private static class Handler extends Thread 
    {
        /**
         * nazwa u¿ytkownika
         */
        private String name;
        /**
         * socket do nawi¿zania po³¹czenia z klientem
         */
        private Socket socket;
        /**
         * obiekt do odbierania wiadomoœci od klienta
         */
        private BufferedReader in;
        /**
         * obiekt do wysy³ania wiadomoœci
         */
        private PrintWriter out;
        
        /**
         * Kontruktor klasy Handler, przypisuj¹cy stworzony socket 
         * do pola klasy wewnêtrznej
         * @param socket do po³¹czenia z klientem
         */
        public Handler(Socket socket) 
        {	
            this.socket = socket;
        }
        
    

        /**
         * g³ówna czêœæ klasy, tworzy elementy do komunikacji tekstowe z klientem,
         * czeka na podanie unikatowej nazwy u¿ytkownika, wpisuje wiadonmoœci, 
         * które zosta³y nadane wczeœniej, zarz¹dza list¹ klientów po stronie serwera
         * (komunikaty 'added'), wysy³a wiadomoœci do wszystkich u¿ytkowników,
         * od³¹cza klientów, gdy chc¹ siê wylogowaæ
         */
        public void run() 
        {
            try 
            {
            	//tworzenie obiektów strumienia do obs³ugi komunikacji z klientem
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                //pyta o podanie unikatowego imienia. Jeœli wpisana nazwa ju¿
                //znajduje siê w hashSecie, nie mo¿na rozpocz¹æ chatu
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
                //dodanie strumienia wyjœcia do setu pozosta³ych, aby umo¿liwiæ
                //wysy³anie wiadomoœci do wszystkich u¿ytkowników 
                out.println("NAMEACCEPTED");
                writers.add(out);
                //wys³anie poprzednich wiadomoœci czatu 
                out.println(talk);
                //wpisywanie listy klientów i tworzenie komunikatu serwera
                writeNames();
                status.add(new Date()+": added >>"+name+"<<");
                
                //zbieranie wiadomoœci od u¿ytkownika, kontrola d³ugoœci
                //historii czatu, obs³uga ¿¹dania wylogowania oraz wys³ania wiadomoœci
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
            	//usuniêcie u¿ytkownika w razie wypadku
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
         * funkcja do usuniêcia u¿ytkownika, usuwa z listy imion i instancji PrintWriter,
         * dodaje komunikat po stronie serwera (komunikaty 'deleted') i zamyka socket
         * @param writer element hashSetu PrintWriter
         * @param name nazwa u¿ytkownika w hashSecie names, do usuniêcia
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
         * metoda do wpisywania ka¿demu u¿ytkownikowi zaktualizowanej listy aktywnych 
         * klientów czatu
         * wpisuje do zmiennej string wszystkie aktualne nicki, 
         * nastêpnnie wysy³a je wszystkim klientom
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


