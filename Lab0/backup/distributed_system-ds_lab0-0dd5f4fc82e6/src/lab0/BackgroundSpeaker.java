package lab0;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/*
 * This is a thread to maintain the send side of the communication. 
 * It continuously check the send queue of the message passer. Whenever a message is place on the send queue, 
 * it setups socket connection to destination node if not already done so before 
 * and writes the message to the corresponding output stream.
 */
public class BackgroundSpeaker extends Thread{
	private MessagePasser mp;
	public BackgroundSpeaker(MessagePasser mp) {
		// TODO Auto-generated constructor stub
		this.mp = mp;
	}
	public void run()
	{
		Message message = null;
		Node node = null;
		Socket client_socket = null;
		ObjectOutputStream out = null;
		BlockingQueue<Message> send_queue = mp.get_send_queue();
		HashMap<String, ObjectOutputStream> existed_sockets = new HashMap<String, ObjectOutputStream>();
		
		while(true)
		{
			try
			{
				//proceed whenver msg place on send_queue
				//use blocking_queue, wait and retrieve front message when not empty 
				message = send_queue.take(); 
				//identify the target node
				node = mp.get_all_nodes().get(message.get_dest()); 
			}
			catch(InterruptedException interrupted)
			{
				interrupted.printStackTrace();
				break;
			}
			
			try
			{
				//check if socket already setup and assign output stream accordingly
				if(existed_sockets.containsKey(node.get_name())){
					out = existed_sockets.get(node.get_name());		
				}
				else{
					client_socket = new Socket(node.get_ip(), node.get_port());
					out = new ObjectOutputStream(client_socket.getOutputStream());
					existed_sockets.put(node.get_name(), out);
				}
				out.writeObject(message);
				out.flush();
			}
			catch(ConnectException connection)
			{
				connection.printStackTrace();
				//close the socket when connection error
				try
				{
					if( client_socket!= null)
						client_socket.close();
				}
				catch(IOException io)
				{
					io.printStackTrace();
				}
			}
			catch(SocketException soc)
			{
				soc.printStackTrace();
				//remove the existing socket if error
				//clear and close the corresponding output stream
				ObjectOutputStream err_out = existed_sockets.remove(node.get_name());
				try{
					if(err_out != null)
						err_out.close();
				}
				catch(IOException io)
				{
					io.printStackTrace();
				}
			}
			catch(Exception others)
			{
				others.printStackTrace();
			}
		}
	}

}
