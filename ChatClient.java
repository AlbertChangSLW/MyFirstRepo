import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
 
public class ChatClient extends JFrame implements ActionListener {
	
	static private Socket cs;
	JLabel lblIPAdress, lblUsername;
	JTextField txtIPAdress, txtUsername, txtmsg;
	JButton sendbtn,connect;
	JTextArea chatarea;
	JScrollPane scroll;
	String serverIP,username,mess;
	int validuser=0;
	public ChatClient() 
	{
		this.setTitle("ChatClient");
		this.setSize(400, 260);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		lblIPAdress = new JLabel("IP Adress:");
		lblIPAdress.setBounds(10,10,61,21);
		add(lblIPAdress);
		
		lblUsername = new JLabel("Username");
		lblUsername.setBounds(158,10,61,21);
		add(lblUsername);
		
		txtIPAdress = new JTextField("");
		txtIPAdress.setBounds(81, 10, 72, 21);
		add(txtIPAdress);
		
		txtUsername = new JTextField("");
		txtUsername.setBounds(223,10,72,21);
		add(txtUsername);
				
		chatarea = new JTextArea("");
		chatarea.setVisible(true);
		scroll = new JScrollPane(chatarea);
		scroll.setBounds(10,42,350,110);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(scroll);
		
		txtmsg = new JTextField("");
		txtmsg.setBounds(20,162,200,22);
		add(txtmsg);
		
		connect= new JButton("connect");
		connect.setBounds(300,10,80,21);
		connect.addActionListener(this);
		add(connect);

		sendbtn= new JButton("Send");
		sendbtn.setBounds(260,163,90,21);
		sendbtn.addActionListener(this);
		add(sendbtn);
		
		this.setVisible(true);
	}
 
	public static void main(String[] args) throws Exception 
	{
		ChatClient client=new ChatClient();
	}
			
	public void start() 
	{
		try {
			
				if(!mess.trim().isBlank()) 
				{
					Scanner sc = new Scanner(mess);
					ExecutorService exec = Executors.newCachedThreadPool();
					exec.execute(new ListenerServer());
					PrintWriter pw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"), true);
				while(true) 
				{
			    	pw.println(sc.nextLine()+"-"+System.nanoTime());
				}
			}
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			if (cs !=null) 
			{
				try 
				{
					cs.close();
				} 
				catch(IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
 	@Override
 	public void actionPerformed(ActionEvent e) 
 	{
	 	if(e.getSource().equals(connect))
	 	{
		 	try 
		 	{
				serverIP=txtIPAdress.getText();
			 	username=txtUsername.getText();
				cs = new Socket(serverIP, 8000);
			 	PrintWriter pw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"),true);
				BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream(),"UTF-8"));
			 	setName(username);
			 	while(true) 
			 	{
					if (username.trim().equals("")) 
					{
					System.out.println("User must have some username");
					}
					else 
					{	
						pw.println(username);
						String pass = br.readLine();
						if (pass != null && (!pass.equals("OK"))) 
						{
							System.out.println("The username already exist, you don't need another connection");
							validuser=0;
							break;
						} 
						else 
						{
						System.out.println("The User"+username+" has connected, you can chat now");
						validuser=1;
						break;
						}
					}
		 		} 
			 	if(validuser==1) 
			 	{
				 	JOptionPane.showMessageDialog(this, "Authentication successful!!");
				 	ExecutorService exec = Executors.newCachedThreadPool();
					exec.execute(new ListenerServer());
					
				}
			 	else
			 	{
				 	JOptionPane.showMessageDialog(this, "Authentication failed!! Username already exists. Please try other username");
			 	}
				 
		 	}
		 	catch(Exception ex) 
		 	{
			 	ex.printStackTrace();
		 	}
	 	}
	 	else if(e.getSource().equals(sendbtn)) 
	 	{
		 
			try
			{
				mess=txtmsg.getText();
				if(!mess.trim().isBlank()) 
				{
					PrintWriter pw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"), true);
				    pw.println(mess+"-"+System.nanoTime());
					txtmsg.setText("");
				}
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally 
			{
				if (cs !=null)
				{
					int a=1;
				}
			}
		 
	 	}
	 }
	private void setName(Scanner scan) throws Exception 
	{
		String name;
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"),true);
		BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream(),"UTF-8"));
		
		while(true) 
		{	
			System.out.println("Enter your username");
			name = scan.nextLine();
			if (name.trim().equals("")) 
			{
				System.out.println("User must have some username");
			} 
			else 
			{	
				pw.println(name);
				String pass = br.readLine();
				if (pass != null && (!pass.equals("OK"))) 
				{
					System.out.println("The username already exist, you don't need another connection");
				} 
				else 
				{
					System.out.println("The User"+name+" has connected, you can chat now");
					break;
				}
			}
		}
	}
	
 	class ListenerServer implements Runnable 
 	{
    	@Override
		public void run() 
		{
			try 
			{
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"),true);
				BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
				String msgString;
				while((msgString = br.readLine())!= null)
				{

					if(msgString.startsWith("Client"))
					{
						chatarea.append(msgString+"\n");
					}
					else if(!msgString.isBlank() && !msgString.startsWith("Client"))
					{
						long end=System.nanoTime();
				    	String uname = msgString.substring(0,msgString.indexOf(":"));
				    	String message = msgString.substring(msgString.indexOf(":")+1,msgString.indexOf("-"));
				    	String tmstamp=msgString.substring(msgString.indexOf("-")+1,msgString.length());
				    	chatarea.append(uname+": "+message +"\n");
				    	if (msgString.toString().contains("-"))
				    	{
				    		long start=Long.parseLong(msgString.substring(msgString.indexOf("-")+1));
					    	pw.println("Time taken to send and receive is: "+(end-start)+"ns");
				    	} 
					}
				}
			} 
			catch(Exception e) 
			{
					e.printStackTrace();
			}
		}
	}
}
