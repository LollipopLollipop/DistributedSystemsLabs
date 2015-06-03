package lab0;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * This is a thread to maintain the receive side of the communication. 
 * It continuously check the server socket for new accepted connection, 
 * initializes new background stream listener for each accepted client socket, 
 * and keep track of all the listeners with a concurrentlinkedqueue
 */
public class BackgroundListener extends Thread{
	private MessagePasser mp;
	private String local_name;
	private ConcurrentLinkedQueue<BackgroundStreamListener> listener_queue;
	private boolean available = true; 
	public BackgroundListener(MessagePasser mp, String local_name, ConcurrentLinkedQueue<BackgroundStreamListener> listener_queue) {
		// TODO Auto-generated constructor stub
		this.mp = mp;
		this.local_name = local_name;
		this.listener_queue = listener_queue;
	}
	public void set_available(boolean flag){
		this.available = flag;
	}
	public void run()
	{
		
		Node node = mp.get_all_nodes().get(local_name);
		//Message msg = null;
		ServerSocket server_socket = null;
		try
		{	
			//set up listening socket at beginning
			server_socket = new ServerSocket(node.get_port());
			
			while(true)
			{
				//System.out.print("Wait accept");
				//proceed whenver new connection accepted
				Socket client_socket = server_socket.accept();
				//System.out.print("Accepted already");
				//create new stream listener for every accepted connection income 
				//use a ConcurrentLinkedQueue to keep track of all the listeners
				BackgroundStreamListener bgStreamLis = new BackgroundStreamListener(client_socket, mp, listener_queue);
				listener_queue.add(bgStreamLis);
				bgStreamLis.start();
			}
		}
		catch(ConnectException connection)
		{
			connection.printStackTrace();
			try
			{
				if( server_socket!= null)
					server_socket.close();
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}
		catch(SocketException soc)
		{
			soc.printStackTrace();
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
		catch(Exception others)
		{
			others.printStackTrace();
		}
	}

}
