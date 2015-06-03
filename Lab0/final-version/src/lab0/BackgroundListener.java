package lab0;

import java.io.IOException;
import java.io.ObjectInputStream;
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
	private ConcurrentLinkedQueue<BackgroundStreamListener> socket_queue;
	public BackgroundListener(MessagePasser mp, String local_name, ConcurrentLinkedQueue<BackgroundStreamListener> sq) {
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
				BackgroundStreamListener bgStreamLis = new BackgroundStreamListener(clientSocket, mp, socket_queue);
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