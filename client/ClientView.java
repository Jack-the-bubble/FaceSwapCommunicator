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

public class ClientView 
{
	//elementy okna dialogowego
	private JFrame frame;
    private JTextArea textArea;
    private JTextArea secTextArea;
    private Font font;
    private Font fontBold;
    private JButton logOutButton;
    private JButton button;
    private JButton listaGosci;
    private String name;
    private String Names="testowa";
    
    
    //elementy komunikacji z serewerem
    private BufferedReader in;
    private PrintWriter out;
    
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
        textArea = new JTextArea("", 40, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        font = new Font("Arial", Font.PLAIN, 15);
        textArea.setFont(font);
        
        
        
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
                    System.out.println(".keyPressed()");  
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
        
        //funkcjie dla przyciskow
        button = new JButton("SEND");
        button.addActionListener(new ActionListener() 
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
                	//secTextArea.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        
        
        logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent ae) 
            {
            	System.out.print("kliknieto");
            	textArea.setText("Logging out"+"\n");
                secTextArea.setText("");
                out.println("#LOGOUT");
                secTextArea.setEditable(false);
                listaGosci.removeActionListener(listaGosci.getActionListeners()[0]);
                logOutButton.removeActionListener(listaGosci.getActionListeners()[0]);
                button.removeActionListener(listaGosci.getActionListeners()[0]);
            }
        });
        
        
        
        listaGosci = new JButton("Show Clients");
        listaGosci.addActionListener(new ActionListener() 
        {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				textArea.setFont(fontBold);
				textArea.append("\n#NAMES"+Names+"\n"+"\n");
				textArea.setFont(font);
			}
		});
        //koniec przyciskow
        
        
        
        
        //dodawanie elementow do panelu
        creationpanel.add(button, BorderLayout.EAST);
        creationpanel.add(logOutButton, BorderLayout.WEST);
        creationpanel.add(listaGosci, BorderLayout.SOUTH);
        creationpanel.add(new JScrollPane(textArea), BorderLayout.NORTH);
        creationpanel.add(new JScrollPane(secTextArea), BorderLayout.CENTER);
        
        frame.add(creationpanel);
        
        frame.pack();
        frame.setVisible(true);
        try {
        	start();
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
    }
    
    private String getServerAddress() 
    {
    	return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Welcome to FaceSwap", JOptionPane.QUESTION_MESSAGE);
    }
    
    private String getName() 
    {
    	name=JOptionPane.showInputDialog(frame, "Choose Your name:", "FaceSwap name window", JOptionPane.PLAIN_MESSAGE);
    	return name;
    }
    
    private void start() throws IOException
    {
    	String serverAddress= getServerAddress();
    	Socket socket = new Socket(serverAddress, 9091);
    	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	out= new PrintWriter(socket.getOutputStream(), true);
    	
    	while(true) 
    	{
    		try
    		{
    			String line = in.readLine();
    		
    		if (line.startsWith("SUBMITNAME")) 
    		{
    			out.println(getName());
    		}
    		else if (line.startsWith("NAMEACCEPTED")) 
    		{
    			secTextArea.setEditable(true);
    		}
    		else if (line.startsWith("MESSAGE")) 
    		{
    			textArea.append(line.substring(8)+"\n");
    		}
    		else if(line.startsWith("NAMES")) 
    		{
    			Names=line.substring(5);
    		}
    		}
    		catch(SocketException | ArrayIndexOutOfBoundsException s) {
    			textArea.append("#LOST CONNECTION\n");
    			secTextArea.setEditable(false);
                listaGosci.removeActionListener(listaGosci.getActionListeners()[0]);
                logOutButton.removeActionListener(listaGosci.getActionListeners()[0]);
                button.removeActionListener(listaGosci.getActionListeners()[0]);
                socket.close();
    			break;
    		}
    	}
    
    }   
}

