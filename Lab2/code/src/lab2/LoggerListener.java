package lab2;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/* act as a "Receiver" to receive Messages from other nodes and storing them in receive_queue after checking and reacting to corresponding rule */
public class LoggerListener extends Thread
{
	private MessagePasser mp;
	private String local_name;
	private ConcurrentLinkedQueue<LoggerStreamListener> socket_queue;
	private ClockService clockservice;
	
	public LoggerListener(MessagePasser mp, String local_name, ConcurrentLinkedQueue<LoggerStreamListener> sq)
	{
		this.mp = mp;
		this.local_name = local_name;
		this.socket_queue = sq;
	}
	public ServerSocket server_socket = null;
	/*
	 * This is used to close server_socket explicitly.
	 * Will not been used here.
	 */
	public void set_available() throws IOException{
		if(this.server_socket != null)
			server_socket.close();
		this.server_socket = null;
	}
	
	public void run()
	{	
		Node user = mp.get_users().get(local_name);
		try
		{
			server_socket = new ServerSocket(user.get_port());
			if(mp.get_debug()){
				System.out.println("I am listenning on port:"+user.get_port());
			}
			//server_socket.setReuseAddress(true);		
			while(true)
			{
				//Wait for socket connection, will block here
				Socket clientSocket = server_socket.accept();
				if(mp.get_debug()){
					System.out.println("This is to set a new socket:"+clientSocket.getPort());
				}
				LoggerStreamListener bgStreamLis = new LoggerStreamListener(clientSocket, mp, socket_queue, mp.get_clock_service());
				socket_queue.add(bgStreamLis);
				bgStreamLis.start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
