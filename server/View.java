package server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class View {
	    JFrame frame = new JFrame("SerwerView");
		JTextField textField= new JTextField(40);
		JTextArea textArea= new JTextArea(15, 40);
		String commands;
		
		private static String talk;
		//
		private static ArrayList<String> status= new ArrayList<String>();
		private static HashSet<String> names = new HashSet<String>();
		
		public View(String talk, ArrayList<String> status, HashSet<String> names) 
		{
			this.talk=talk;
			this.status=status;
			this.names=names;
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.append(">>type \"help\" to see options\n");
			frame.getContentPane().add(new JScrollPane(textArea), "North");
			frame.getContentPane().add(textField, "Center");
			frame.setResizable(false);
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			
			textField.addActionListener(new ActionListener() 
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
		}
		
		public void checkString() 
		{
			if(commands.startsWith("writeAll")) 
			{
				writeAll();
			}
			else if(commands.startsWith("statusClear"))
			{
				status.clear();
			}
			else if(commands.startsWith("getStatus")) 
			{
				for(String stats: status) 
				{
					textArea.append(stats+"\n");
				}
			}
			else if(commands.startsWith("help")) 
			{
				textArea.append("<<Following funtcions are available: \n\n<<writeAll - write all active clients \n\n<<getStatus - time when added and deleted client and nick  \n\n<<statusClear \n");
			}
			else 
			{
				textArea.append("command not known \n");
			}
		}
		
		public void writeAll() 
		{
			for(String name: names) 
			{
				textArea.append(name+"\n");
			}
		}
	}//View

