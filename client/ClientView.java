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
 *klient komunikatora, wysy³aj¹cy i odbieraj¹cy wiadomoœci od wszystkich 
 *u¿ytkowników chatu, umo¿liwia szybkie zalogowanie, wybór nazwy u¿ytkownika,
 *sprawdzenie listy aktualnie zalogowanych, przycisk do wylogowania oraz wys³ania 
 *wiadomoœci
 */
public class ClientView 
{
	//elementy okna dialogowego
	/**
	 * okno podstawowe klienta
	 */
	private JFrame frame;
    /**
     * g³ówne pole tekstowe, w którym pojawiaj¹ siê wiadomoœci i komunikaty
     */
    private JTextArea textArea;
    /**
     * pole tekstowe do wpisywania wiadomoœci
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
     * przycisk do sprawdzenia u¿ytkowników zalogowanych
     */
    private JButton listaGosci;
    /**
     * imiê uzytkownika czatu
     */
    private String name;
    /**
     * lista aktualnie zalogowanych u¿ytkowników
     */
    private String Names="# NOT CONNECTED \n";
    
    
    //elementy komunikacji z serewerem
    /**
     * obiekt do odczytywnia wiadomoœci od serwera
     */
    private BufferedReader in;
    /**
     * obiekt do wysy³ania wiadomoœci do serwera
     */
    private PrintWriter out;
    
    /**
     *konstruktor klienta, tworzy i konfiguruje ramkê, dodaje s³uchacze do przycisków,
     *uruchamia metodê odpowiedzialn¹ za komunikacjê z serwerem 
     */
    public ClientView()
    {
    	//
    	//pocz¹tek ustawiania okna
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
        
        //ustawienie g³ównego okna odpowiedzialnego za wyœwietlanie tekstu
        textArea = new JTextArea("", 40, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        font = new Font("Arial", Font.PLAIN, 15);
        textArea.setFont(font);
        
        
        //ustawienie drugiego okna tekstowego odpowiedzialnego za wpisywanie wiadomoœci
        //na pocz¹tku ob okna s¹ nieedytowalne
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
        
        //ustawienie przycisku do wysy³ania wiadomoœci, ma tak¹ sam¹ funkcjonalnoœæ,
        //co s³uchacz w drugim polu tekstowym
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
        
        //przycisk do wylogowania, czyœci g³ówne okno dialogowe, podaje komunikat 
        //o wylogowaniu i wysy³a go do wszystkich u¿ytkowników, wy³¹cza edytowalnoœæ
        //mniejszego okna dialogowego i usuwa s³uchacze ze wszystkich obiektów
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
        
        
        //przycisk do wypisywania w g³ównym oknie dialogowym wszystkich aktywnych 
        //u¿ytkowników
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
        
        
        
        
        //dodawanie elementow do panelu i uk³adanie elementów, uwidocznienie okna
        creationpanel.add(sendButton, BorderLayout.EAST);
        creationpanel.add(logOutButton, BorderLayout.WEST);
        creationpanel.add(listaGosci, BorderLayout.SOUTH);
        creationpanel.add(new JScrollPane(textArea), BorderLayout.NORTH);
        creationpanel.add(new JScrollPane(secTextArea), BorderLayout.CENTER);
        
        frame.add(creationpanel);
        
        frame.pack();
        frame.setVisible(true);
        //rozpoczêcie dzia³ania komunikatora
        try {
        	start();
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
    }
    
    /**metoda do otrzymania adresu serwera, aby utworzyæ z nim po³¹czenie
     * tworzy okno dialogowe, w które nale¿y wpisaæ adres
     * @return string z adresem serwera
     */
    private String getServerAddress() 
    {
    	return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Welcome to FaceSwap", JOptionPane.QUESTION_MESSAGE);
    }
    
    /** metoda do uzyskania nazwy u¿ytkownika, konstruuje okno dialogowe, 
     * w które nale¿y podaæ unikatow¹ nazwê u¿ytkownika
     * @return string zwracaj¹cy nazwê u¿ytkownika
     */
    private String getName() 
    {
    	name=JOptionPane.showInputDialog(frame, "Choose Your name:", "FaceSwap name window", JOptionPane.PLAIN_MESSAGE);
    	return name;
    }
    
    /**metoda odpowiedzialna za komunikacjê z serwerem i wypisanie wiadomoœci 
     * od innych u¿ytkowników: utworzenie po³¹czenia, podania nazwy u¿ytkownika,
     * wypisania wiadomoœci, zaktualizowania listy nazw u¿ytkowników 
     * @throws IOException zabezpieczenie w razie utraty po³¹czenia 
     * lub wyst¹pienia innego b³êdu
     */
    private void start() throws IOException
    {
    	//stworzenie instancji klasy socket w celu komunikacji, obiektów strumieni, 
    	//uzyskanie adresu serwera
    	String serverAddress= getServerAddress();
    	Socket socket = new Socket(serverAddress, 9091);
    	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	out= new PrintWriter(socket.getOutputStream(), true);
    	
    	while(true) 
    	{
    		//odczytanie wiadomoœci z serwera
    		try
    		{
    			String line = in.readLine();
    		//wywyo³anie metody do uzyskania nazwy u¿ytkownka i wys³anie 
    		//uzyskanej do serwera
    		if (line.startsWith("SUBMITNAME")) 
    		{
    			out.println(getName());
    		}
    		//umo¿liwienie wpisywania wiadomoœci
    		else if (line.startsWith("NAMEACCEPTED")) 
    		{
    			secTextArea.setEditable(true);
    		}
    		//wpisanie wiadomoœci do g³ównego okna dialogowego
    		else if (line.startsWith("MESSAGE")) 
    		{
    			textArea.append(line.substring(8)+"\n");
    		}
    		//zaktualizowanie listy nazw u¿ytkowników
    		else if(line.startsWith("NAMES")) 
    		{
    			Names=line.substring(5);
    		}
    		}
    		//obs³uga wyj¹tków analogicza z wylogowaniem: zamkniêcie edytowalnoœci
    		//mniejszego okna dialogowego, usuniêcie s³uchaczy zdarzeñ, 
    		//zamkniêcie socketu
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

