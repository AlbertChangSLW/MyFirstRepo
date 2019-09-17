import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
 
 
public class ChatServer 
{	
	
	private ServerSocket ss;
	private ExecutorService exec;
	private Map<String,PrintWriter> si;
	private Map<String,String> im;
	
	public ChatServer() 
	{
		try 
		{
 
			ss = new ServerSocket(8000);
			si = new HashMap<String, PrintWriter>();
			exec = Executors.newCachedThreadPool();
			im = new HashMap<String,String>();
 
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private void putIn(String key,PrintWriter value)
	{
		synchronized(this) 
		{
			si.put(key, value);
		}
	}
	
	private synchronized void remove(String  key) 
	{
		si.remove(key);
		System.out.println("There are "+ si.size()+" people online.");
	}
	
	private synchronized void sendToAll(String message) 
	{
		for(PrintWriter out: si.values())
		{
		    out.println( message);
		}
		
	}
	
	private synchronized void sendToSpecificClient(String name,String message) 
	{
		PrintWriter pw = si.get(name); 
		if(pw != null) pw.println(message);	
	}
	
	public void start() 
	{
		try 
		{
			while(true) 
			{
				System.out.println("Wait for user to connect...");
				Socket socket = ss.accept();
				InetAddress address = socket.getInetAddress();
				System.out.println("User " + address.getHostAddress() + "has been connected ");
				exec.execute(new ListenerClient(socket)); 
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
    class ListenerClient implements Runnable 
    {
 		private Socket socket;
		private String name;
				
		public ListenerClient(Socket socket)
		{
			this.socket = socket;
		}
		
		private String getName() throws Exception 
		{
			try 
			{
				BufferedReader br = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), "UTF-8"));
				PrintWriter ipw = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(), "UTF-8"),true);
 
				while(true) 
				{
					String nameString = br.readLine();
					if ((nameString.trim().length() == 0) || si.containsKey(nameString))
					{
						ipw.println("FAIL");
					} 
					else 
					{
						ipw.println("OK");
						return nameString;
					}
				}
			}
			catch(Exception e)
			{
				throw e;
			}
		}
		
        @Override		
		public void run()
		{
			try 
			{
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
				name = getName();
				putIn(name, pw);
				Thread.sleep(100);
				sendToAll("Client " + name + " has connected");
				if (im.containsKey(name))
				{
					String offlineme = Search(name);
					String[] ome= offlineme.split("@");
					for (int i=0; i < ome.length;i++)
					sendToSpecificClient(name,ome[i]);
					DeleteInfo(name);
				}
				System.out.println(si);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				String msgString = null;
				
				
				while((msgString = br.readLine()) != null)
				{

					if(msgString.startsWith("@")) 
					{
						int index = msgString.indexOf(":");
						if(index >= 0) 
						{
							String theName = msgString.substring(1, index);
							String info = msgString.substring(index+1, msgString.length());
							info =  name + ":"+ info;
							if (si.containsKey(theName))
							{
								sendToSpecificClient(theName, info);
							}
							else
							{ 
								
								AddInfo(theName,info);
							}
							continue;
						}
					}
					else 
					{
						if (msgString.contains("Time taken to send and receive is"))
						{
							System.out.println(msgString);
						}
						else
						{
							sendToAll(name+":"+ msgString);
						}
					}
				}	
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			} 
			finally 
			{
				remove(name);
				sendToAll("Client " +name + " has disconnected");
				if(socket!=null) 
				{
					try 
					{
						socket.close();
					} 
					catch(IOException e) 
					{
						e.printStackTrace();
					}
				}	
			}
		}
	}
	

	
	public void AddInfo(String ne,String me)
	{
		if (im.containsKey(ne))
		{
			me=im.get(ne)+ "@" +me;
			im.put(ne,me);
		}
		else
		{
			im.put(ne,me);
		}
	}
	public void DeleteInfo(String ne)
	{
		im.remove(ne);
	}
	public String Search(String ne)
	{
		if (im.containsKey(ne))
		{
			return im.get(ne);

		}
		else
		{
			return null;
		}

	}
	public static void main(String[] args) 
	{
		ChatServer server = new ChatServer();
		server.start();
	}
}	
