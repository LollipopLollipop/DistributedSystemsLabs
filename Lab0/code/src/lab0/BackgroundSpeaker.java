package lab0;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
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
		if(mp.get_debug()){System.out.println("Speaker thread");}
		HashMap<String, ObjectOutputStream> existed_sockets = new HashMap<String, ObjectOutputStream>();
		BlockingQueue<Message> send_queue = mp.get_send_queue();
		while(true)
		{
			Message message = null;
			Node user = null;
			
			try
			{
				message = send_queue.take(); // throw interrupt error if the thread is interrupted when it is on the thread
				user = mp.get_users().get(message.get_dest()); //send message to that user
			}
			catch(InterruptedException inter)
			{
				//interrupted by file changed, exit the thread
				break;
			}
			ObjectOutputStream out = null;
			Socket client_socket = null;
			try
			{
				if(existed_sockets.containsKey(user.get_name())){
					out = existed_sockets.get(user.get_name());		
				}
				else{
					client_socket = new Socket(user.get_ip(), user.get_port());
					out = new ObjectOutputStream(client_socket.getOutputStream());
					existed_sockets.put(user.get_name(), out);
				}
				out.writeObject(message);
				out.flush();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}

}
