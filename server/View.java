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
 *widok serwera komunikatora, s�u�y do zarz�dzania aplikacj�,
 *wypisuje lub czy�ci histori� wydarze�, podaje list� aktywnych u�ytkownik�w  
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
		 * pole tekstowe do wypisywania komunikat�w
		 */
		private JTextArea textArea= new JTextArea(15, 40);
		/**
		 * string do przechowywania komand
		 */
		private String commands;
		/**
		 * lista do przechowywania historii komunikat�w serwera
		 */
		private static ArrayList<String> status= new ArrayList<String>();
		/**
		 * hashSet do przechowywanis imion u�ytkownik�w
		 */
		private static HashSet<String> names = new HashSet<String>();
		
		/**
		 * konstruktor widoku serwera, ustawia cechy ka�dego z element�w okna,
		 * daj�c podstawow� funkcjonalno��
		 * @param talk
		 * @param status
		 * @param names
		 */
		public View(String talk, ArrayList<String> status, HashSet<String> names) 
		{
			//zapami�tywanie statusu oraz imion u�ytkownik�w do p�zniejszego 
			//ich wypisania
			this.status=status;
			this.names=names;
			//ustawienie wi�kszego pola do wypisywania komunikat�w, 
			//uniemo�liwienie ich edycji, ustawienie zawijania tekstu, 
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
			
			//dodanie s�uchacza zdarze� do pola komend,
			//aby obs�u�y� podstawowe operacje
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
		 *metoda do obs�ugi komend z linii polece� 
		 */
		public void checkString() 
		{
			//wypisanie wszystkich aktywnych u�ytkownik�w
			if(commands.startsWith("writeAll")) 
			{
				writeAll();
			}
			//wyczyszczenie historii komunikat�w serwera
			else if(commands.startsWith("statusClear"))
			{
				status.clear();
			}
			//wypisanie historii komunikat�w serwera
			else if(commands.startsWith("getStatus")) 
			{
				for(String stats: status) 
				{
					textArea.append(stats+"\n");
				}
			}
			//wypisanie mo�liwych komend
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
		 *metoda do wypisania wszystkich nazw aktywnych u�ytkownik�w 
		 */
		public void writeAll() 
		{
			for(String name: names) 
			{
				textArea.append(name+"\n");
			}
		}//writeAll
	}

