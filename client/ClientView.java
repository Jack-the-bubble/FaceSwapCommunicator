package client;



import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.*;

/**
 * @author Boba
 *klient komunikatora, wysy�aj�cy i odbieraj�cy wiadomo�ci od wszystkich 
 *u�ytkownik�w chatu, umo�liwia szybkie zalogowanie, wyb�r nazwy u�ytkownika,
 *sprawdzenie listy aktualnie zalogowanych, przycisk do wylogowania oraz wys�ania 
 *wiadomo�ci
 */
public class ClientView 
{
	//elementy okna dialogowego
	/**
	 * okno podstawowe klienta
	 */
	private JFrame frame;
    /**
     * g��wne pole tekstowe, w kt�rym pojawiaj� si� wiadomo�ci i komunikaty
     */
    private JTextArea textArea;
    /**
     * pole tekstowe do wpisywania wiadomo�ci
     */
    private JTextArea secTextArea;
    /**
     * obiekt do zmiany czcionki czatu
     */
    private Font font;
    /**
     * przycisk do wylogowania
     */
    private JButton logOutButton;
    /**
     * 
     */
    private JButton sendButton;
    /**
     * przycisk do sprawdzenia u�ytkownik�w zalogowanych
     */
    private JButton listaGosci;
    /**
     * imi� uzytkownika czatu
     */
    private String name;
    /**
     * lista aktualnie zalogowanych u�ytkownik�w
     */
    private String Names="# NOT CONNECTED \n";
    
    
    //elementy komunikacji z serewerem
    /**
     * obiekt do odczytywnia wiadomo�ci od serwera
     */
    private BufferedReader in;
    /**
     * obiekt do wysy�ania wiadomo�ci do serwera
     */
    private PrintWriter out;
    
    /**
     *konstruktor klienta, tworzy i konfiguruje ramk�, dodaje s�uchacze do przycisk�w,
     *uruchamia metod� odpowiedzialn� za komunikacj� z serwerem 
     */
    public ClientView()
    {
    	//
    	//pocz�tek ustawiania okna
    	//
    	JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame();
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("FaceSwap");
        frame.setResizable(false);
        
        JPanel creationpanel= new JPanel();
        creationpanel.setLayout(new BorderLayout());
        
        //okna dialogowe
        
        //ustawienie g��wnego okna odpowiedzialnego za wy�wietlanie tekstu
        textArea = new JTextArea("", 40, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        font = new Font("Arial", Font.PLAIN, 15);
        textArea.setFont(font);
        
        
        //ustawienie drugiego okna tekstowego odpowiedzialnego za wpisywanie wiadomo�ci
        //na pocz�tku ob okna s� nieedytowalne
        secTextArea= new JTextArea("", 2, 30);
        secTextArea.setLineWrap(true);
        secTextArea.setWrapStyleWord(true);
        secTextArea.setEditable(false);
        secTextArea.addKeyListener(new KeyListener() 
        {
            @Override
            public void keyTyped(KeyEvent ke) 
            {
                if(ke.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    //System.out.println(".keyPressed()");  
                }
            }

            @Override
            public void keyPressed(KeyEvent ke) 
            {        
                if(secTextArea.getText().length()!=0)
                {
                    if(ke.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                    	out.println(secTextArea.getText());
                    	secTextArea.setText("");
                    }
                }

            }

            @Override
            public void keyReleased(KeyEvent ke) {}
        });
        //koniec okien dialogowych
        
        //funkcje dla przyciskow
        
        //ustawienie przycisku do wysy�ania wiadomo�ci, ma tak� sam� funkcjonalno��,
        //co s�uchacz w drugim polu tekstowym
        sendButton = new JButton("SEND");
        sendButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent ae) 
            {
                if(secTextArea.getText().length()>0)
                {
                	try {
                		
                		out.println(secTextArea.getText());
                	}
                	catch(Exception e) {
                		secTextArea.append(name+": #lost connection\n");
                		e.printStackTrace();
                	}
                	secTextArea.setText("");
                }
            }
        });
        
        //przycisk do wylogowania, czy�ci g��wne okno dialogowe, podaje komunikat 
        //o wylogowaniu i wysy�a go do wszystkich u�ytkownik�w, wy��cza edytowalno��
        //mniejszego okna dialogowego i usuwa s�uchacze ze wszystkich obiekt�w
        logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent ae) 
            {
            	textArea.setText("Logging out"+"\n");
                secTextArea.setText("");
                out.println("#LOGOUT");
                secTextArea.setEditable(false);
                listaGosci.removeActionListener(listaGosci.getActionListeners()[0]);
                logOutButton.removeActionListener(listaGosci.getActionListeners()[0]);
                sendButton.removeActionListener(listaGosci.getActionListeners()[0]);
            }
        });
        
        
        //przycisk do wypisywania w g��wnym oknie dialogowym wszystkich aktywnych 
        //u�ytkownik�w
        listaGosci = new JButton("Show Clients");
        listaGosci.addActionListener(new ActionListener() 
        {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				textArea.append("\n#NAMES"+Names+"\n"+"\n");
			}
		});
        //koniec przyciskow
        
        
        
        
        //dodawanie elementow do panelu i uk�adanie element�w, uwidocznienie okna
        creationpanel.add(sendButton, BorderLayout.EAST);
        creationpanel.add(logOutButton, BorderLayout.WEST);
        creationpanel.add(listaGosci, BorderLayout.SOUTH);
        creationpanel.add(new JScrollPane(textArea), BorderLayout.NORTH);
        creationpanel.add(new JScrollPane(secTextArea), BorderLayout.CENTER);
        
        frame.add(creationpanel);
        
        frame.pack();
        frame.setVisible(true);
        //rozpocz�cie dzia�ania komunikatora
        try {
        	start();
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
    }
    
    /**metoda do otrzymania adresu serwera, aby utworzy� z nim po��czenie
     * tworzy okno dialogowe, w kt�re nale�y wpisa� adres
     * @return string z adresem serwera
     */
    private String getServerAddress() 
    {
    	return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Welcome to FaceSwap", JOptionPane.QUESTION_MESSAGE);
    }
    
    /** metoda do uzyskania nazwy u�ytkownika, konstruuje okno dialogowe, 
     * w kt�re nale�y poda� unikatow� nazw� u�ytkownika
     * @return string zwracaj�cy nazw� u�ytkownika
     */
    private String getName() 
    {
    	name=JOptionPane.showInputDialog(frame, "Choose Your name:", "FaceSwap name window", JOptionPane.PLAIN_MESSAGE);
    	return name;
    }
    
    /**metoda odpowiedzialna za komunikacj� z serwerem i wypisanie wiadomo�ci 
     * od innych u�ytkownik�w: utworzenie po��czenia, podania nazwy u�ytkownika,
     * wypisania wiadomo�ci, zaktualizowania listy nazw u�ytkownik�w 
     * @throws IOException zabezpieczenie w razie utraty po��czenia 
     * lub wyst�pienia innego b��du
     */
    private void start() throws IOException
    {
    	//stworzenie instancji klasy socket w celu komunikacji, obiekt�w strumieni, 
    	//uzyskanie adresu serwera
    	String serverAddress= getServerAddress();
    	Socket socket = new Socket(serverAddress, 9091);
    	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	out= new PrintWriter(socket.getOutputStream(), true);
    	
    	while(true) 
    	{
    		//odczytanie wiadomo�ci z serwera
    		try
    		{
    			String line = in.readLine();
    		//wywyo�anie metody do uzyskania nazwy u�ytkownka i wys�anie 
    		//uzyskanej do serwera
    		if (line.startsWith("SUBMITNAME")) 
    		{
    			out.println(getName());
    		}
    		//umo�liwienie wpisywania wiadomo�ci
    		else if (line.startsWith("NAMEACCEPTED")) 
    		{
    			secTextArea.setEditable(true);
    		}
    		//wpisanie wiadomo�ci do g��wnego okna dialogowego
    		else if (line.startsWith("MESSAGE")) 
    		{
    			textArea.append(line.substring(8)+"\n");
    		}
    		//zaktualizowanie listy nazw u�ytkownik�w
    		else if(line.startsWith("NAMES")) 
    		{
    			Names=line.substring(5);
    		}
    		}
    		//obs�uga wyj�tk�w analogicza z wylogowaniem: zamkni�cie edytowalno�ci
    		//mniejszego okna dialogowego, usuni�cie s�uchaczy zdarze�, 
    		//zamkni�cie socketu
    		catch(SocketException | ArrayIndexOutOfBoundsException s) {
    			textArea.append("#LOST CONNECTION\n");
    			secTextArea.setEditable(false);
                listaGosci.removeActionListener(listaGosci.getActionListeners()[0]);
                logOutButton.removeActionListener(listaGosci.getActionListeners()[0]);
                sendButton.removeActionListener(listaGosci.getActionListeners()[0]);
                socket.close();
    			break;
    		}
    	}
    
    }   
}

