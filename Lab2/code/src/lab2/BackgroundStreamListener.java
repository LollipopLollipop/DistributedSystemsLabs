package lab2;

//import ClockService;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Queue;
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
	//private Queue<TimeStampedMessage> hold_back_queue_1;
	public BackgroundStreamListener(Socket conn_sock, MessagePasser mp, ConcurrentLinkedQueue<BackgroundStreamListener> sq, ClockService clockservice) {
		// TODO Auto-generated constructor stub
		this.mp = mp;
		this.socket = conn_sock;
		this.clockservice = clockservice;
		this.socket_queue = sq;
		//this.hold_back_queue_1 = mp.get_hold_back_queue_1();
	}
	
	public void run()
	{
		ObjectInputStream in = null;
		try{
			//retrieve the input stream for the particular socket
			in = new ObjectInputStream(socket.getInputStream());
			
			while(true){
				TimeStampedMessage message = (TimeStampedMessage)in.readObject();
				//check if the received msg is sent one-one or multicast
				boolean multicast = false;
				if(message instanceof MulticastMessage)
					multicast = true;
				
				if(multicast==false){
					System.out.println("It is a new one-to-one msg");
					//old version only one-to-one
					//only increment clock here if sent one-one
					//this.clockservice.check_and_update(message,1);
					if(message.get_sent_to_me()==true){
						//this.clockservice.check_and_update(message,1);
						this.clockservice.check_and_update(message,0);
					}
					else{
						this.clockservice.check_and_update(message,0);
						continue;
					}
				}
				else{
					System.out.println("It is a multicast msg");
					if(message.get_source().equals(mp.get_local_name())||((MulticastMessage)message).get_orig_src().equals(mp.get_local_name())){
						System.out.println("Discard due to self source");
						//only update clock if the msg is sent from myself
						//this.clockservice.check_and_update(message,0);
						continue;
					}
					if(((MulticastMessage)message).get_target_group()!=null && !mp.get_groups().get(((MulticastMessage)message).get_target_group()).contains(mp.get_local_name())){
						System.out.println("Not within the multicast group");
						//update clock and discard the msg if not sent to my group
						this.clockservice.check_and_update(message,0);
						continue;
					}
				}
				Rule rule = mp.receive_rule_processing(message);
				if(rule == null){
					//none matched 
					if(multicast == false){
						mp.get_receive_queue().add(message);
					}
					else{
						synchronized(mp.get_hold_back_queue_1()){
							mp.get_hold_back_queue_1().add((MulticastMessage) message);
						}
					}
					while(!mp.get_delayed_receive_queue().isEmpty()){
						if(multicast == false){
							mp.get_receive_queue().add(mp.get_delayed_receive_queue().poll());
						}
						else{
							synchronized(mp.get_hold_back_queue_1()){
								mp.get_hold_back_queue_1().add((MulticastMessage) mp.get_delayed_receive_queue().poll());
							}
						}
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
						//TimeStampedMessage message_copy = message.copy();
						//message_copy.set_seq_num(mp.get_seq_num().addAndGet(1));
						if(multicast == false){
							//this.clockservice.update(1);
							mp.get_receive_queue().add(message.copy());
						}
						else{
							synchronized(mp.get_hold_back_queue_1()){
								mp.get_hold_back_queue_1().add((MulticastMessage) message.copy());
								//duplicate rule at received end in multicast is erroneous 
							}
						}
					}
					while(!mp.get_delayed_receive_queue().isEmpty()){
						//????Update
						if(multicast == false){
							mp.get_receive_queue().add(mp.get_delayed_receive_queue().poll());
						}
						else{
							synchronized(mp.get_hold_back_queue_1()){
							
								mp.get_hold_back_queue_1().add((MulticastMessage) mp.get_delayed_receive_queue().poll());
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			//System.out.println(e);
			e.printStackTrace();
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
