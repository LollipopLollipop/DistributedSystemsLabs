package lab0;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * This is a thread to maintain each income stream of the receive side of the communication. 
 * It continuously check the send queue of the message passer. Whenever a message is place on the send queue, 
 * it setups socket connection to destination node if not already done so before 
 * and writes the message to the corresponding output stream.
 */
public class BackgroundStreamListener extends Thread{
	private Socket socket;
	private MessagePasser mp;
	private ConcurrentLinkedQueue<BackgroundStreamListener> listener_queue;
	public BackgroundStreamListener(Socket conn_sock, MessagePasser mp, ConcurrentLinkedQueue<BackgroundStreamListener> listener_queue) {
		// TODO Auto-generated constructor stub
		this.socket = conn_sock;
		this.mp = mp;
		this.listener_queue = listener_queue;
	}
	
	public void run()
	{
		ObjectInputStream in = null;
		try{
			//retrieve the input stream for the particular socket
			in = new ObjectInputStream(socket.getInputStream());
			
			while(true){
				//proceed whenver new msg arrived at the input stream
				Message message = (Message)in.readObject();
				Rule rule = mp.receive_rule_processing(message);		
				if(rule == null){
					//none matched 
					mp.get_receive_queue().add(message);
					while(!mp.get_delayed_receive_queue().isEmpty()){
						mp.get_receive_queue().add(mp.get_delayed_receive_queue().poll());
					}
				}
				else{
					System.out.println(rule.get_action());
					if(rule.get_action().equals("drop")){
						return; 
					}
					else if(rule.get_action().equals("delay")){
						mp.get_delayed_receive_queue().add(message);
						return;
					}
					message.set_duplicate("true");
					mp.get_receive_queue().add(message);
					if(rule.get_action().equals("duplicate")){
						System.out.println("duplicate");
						Message message_copy = message.copy();
						//message_copy.set_seqNum(++message_id);
						mp.get_receive_queue().add(message_copy);
					}
					while(!mp.get_delayed_receive_queue().isEmpty()){
						mp.get_receive_queue().add(mp.get_delayed_receive_queue().poll());
					}
				}
			}
		}
		catch(EOFException eof)
		{
			eof.printStackTrace();
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
		catch(Exception others)
		{
			others.printStackTrace();
		}
		finally
		{
			try
			{
				if(in != null)
					in.close();
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
			//remove this listener from queue when end
			listener_queue.remove(this); 
		}
	}
}
