package server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author Boba
 *widok serwera komunikatora, służy do zarządzania aplikacją,
 *wypisuje lub czyści historię wydarzeń, podaje listę aktywnych użytkowników  
 */
public class View {
	    /**
	     * okno widoku serwera
	     */
	    private JFrame frame = new JFrame("SerwerView");
		/**
		 * pole tekstowe do wpisywania komend
		 */
		private JTextField textField= new JTextField(40);
		/**
		 * pole tekstowe do wypisywania komunikatów
		 */
		private JTextArea textArea= new JTextArea(15, 40);
		/**
		 * string do przechowywania komand
		 */
		private String commands;
		/**
		 * lista do przechowywania historii komunikatów serwera
		 */
		private static ArrayList<String> status= new ArrayList<String>();
		/**
		 * hashSet do przechowywanis imion użytkowników
		 */
		private static HashSet<String> names = new HashSet<String>();
		
		/**
		 * konstruktor widoku serwera, ustawia cechy każdego z elementów okna,
		 * dając podstawową funkcjonalność
		 * @param talk
		 * @param status
		 * @param names
		 */
		public View(String talk, ArrayList<String> status, HashSet<String> names) 
		{
			//zapamiętywanie statusu oraz imion użytkowników do pózniejszego 
			//ich wypisania
			this.status=status;
			this.names=names;
			//ustawienie większego pola do wypisywania komunikatów, 
			//uniemożliwienie ich edycji, ustawienie zawijania tekstu, 
			//wpisanie pierwszego komunikatu do pola wypisania
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.append(">>type \"help\" to see options\n");
			frame.getContentPane().add(new JScrollPane(textArea), "North");
			frame.getContentPane().add(textField, "Center");
			frame.setResizable(false);
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			
			//dodanie słuchacza zdarzeń do pola komend,
			//aby obsłużyć podstawowe operacje
			textField.addActionListener(
			new ActionListener() 
			{
				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					textArea.append(">>"+textField.getText()+"\n");
					commands=textField.getText();
					textField.setText("");
					checkString();
				}
			});
		}//View
		
		/**
		 *metoda do obsługi komend z linii poleceń 
		 */
		public void checkString() 
		{
			//wypisanie wszystkich aktywnych użytkowników
			if(commands.startsWith("writeAll")) 
			{
				writeAll();
			}
			//wyczyszczenie historii komunikatów serwera
			else if(commands.startsWith("statusClear"))
			{
				status.clear();
			}
			//wypisanie historii komunikatów serwera
			else if(commands.startsWith("getStatus")) 
			{
				for(String stats: status) 
				{
					textArea.append(stats+"\n");
				}
			}
			//wypisanie możliwych komend
			else if(commands.startsWith("help")) 
			{
				textArea.append("<<Following funtcions are available: \n\n<<writeAll - write all active clients \n\n<<getStatus - time when added and deleted client and nick  \n\n<<statusClear \n");
			}
			else 
			{
				textArea.append("command not known \n");
			}
		}//checkString
		
		/**
		 *metoda do wypisania wszystkich nazw aktywnych użytkowników 
		 */
		public void writeAll() 
		{
			for(String name: names) 
			{
				textArea.append(name+"\n");
			}
		}//writeAll
	}

