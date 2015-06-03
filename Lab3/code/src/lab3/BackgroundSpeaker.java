package lab3;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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
		//HashMap<String, ObjectOutputStream> existed_sockets = mp.get_existed_sockets();
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
				System.out.println("send_queue broken");
				break;
			}
			ObjectOutputStream out = null;
			Socket client_socket = null;
			try
			{
				if(existed_sockets.containsKey(user.get_name())){
					if(mp.get_debug()){
						System.out.println("Socket exists");
						
					}
					out = existed_sockets.get(user.get_name());		
				}
				else{
					if(mp.get_debug()){
						System.out.println("Socket not exists");
						
					}
					client_socket = new Socket(user.get_ip(), user.get_port());
					out = new ObjectOutputStream(client_socket.getOutputStream());
					existed_sockets.put(user.get_name(), out);
				}
				out.writeObject(message);
				//System.out.println("Send message from " + message.get_source() + " to " +
				//		message.get_dest() + "with content" + message.toString());
				//System.out.println("-> ");
				out.flush();
			}
			catch(Exception e)
			{
				System.out.println(e);
				ObjectOutputStream temp_out = existed_sockets.remove(user.get_name());
				try{
					if(temp_out != null)
						temp_out.close();
				}
				catch(IOException ioe)
				{
					System.out.println(e);
				}
				if(client_socket!=null)
					try {
						client_socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		}
	}

}
