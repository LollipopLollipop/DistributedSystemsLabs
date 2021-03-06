package lab1;

//import ClockService;

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
	private ClockService clockservice;
	private ConcurrentLinkedQueue<BackgroundStreamListener> socket_queue;
	public BackgroundStreamListener(Socket conn_sock, MessagePasser mp, ConcurrentLinkedQueue<BackgroundStreamListener> sq, ClockService clockservice) {
		// TODO Auto-generated constructor stub
		this.mp = mp;
		this.socket = conn_sock;
		this.clockservice = clockservice;
		this.socket_queue = sq;
	}
	
	public void run()
	{
		ObjectInputStream in = null;
		try{
			//retrieve the input stream for the particular socket
			in = new ObjectInputStream(socket.getInputStream());
			
			while(true){
				TimeStampedMessage message = (TimeStampedMessage)in.readObject();
				this.clockservice.check_and_update(message,1);
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
						//return;
						continue;
					}
					else if(rule.get_action().equals("delay")){
						mp.get_delayed_receive_queue().add(message);
						//return;
						continue;
					}
					message.set_duplicate("true");
					mp.get_receive_queue().add(message);
					if(rule.get_action().equals("duplicate")){
						//System.out.println("duplicate");
						TimeStampedMessage message_copy = message.copy();
						//message_copy.set_seq_num(mp.get_seq_num().addAndGet(1));
						this.clockservice.update(1);
						mp.get_receive_queue().add(message_copy);
					}
					while(!mp.get_delayed_receive_queue().isEmpty()){
						//????Update
						mp.get_receive_queue().add(mp.get_delayed_receive_queue().poll());
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
			try {
				if(mp.get_debug()){
					System.out.println("Before close socket, size of queque:" + this.socket_queue.size());
				}
				this.socket_queue.remove(this);
				this.socket.close();
				if(mp.get_debug()){
					System.out.println("After close socket, size of queue:" + this.socket_queue.size());
				}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
