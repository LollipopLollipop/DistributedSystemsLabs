package lab2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReliabilityCheck extends Thread{
	private ConcurrentLinkedQueue<MulticastMessage> hold_back_queue_1;
	private ConcurrentLinkedQueue<MulticastMessage> hold_back_queue_2;
	private MessagePasser mp;
	public ReliabilityCheck(MessagePasser mp){
		this.mp = mp;
		this.hold_back_queue_1 = mp.get_hold_back_queue_1();
		this.hold_back_queue_2 = mp.get_hold_back_queue_2();
	}
	
	public boolean comparemessage(MulticastMessage t_msg){
		ArrayList<MulticastMessage> received = mp.get_received_msgs();
		System.out.println("size"+received.size());
		
		if(received.size() == 0)
			return false;
		
		//String message = t_msg.toString();
		//for(TimeStampedMessage cur : received){
			//System.out.println("cur"+cur.toString());
		//	if(cur.toString().equals(message)){
		String message = t_msg.toOrigString();
		for(MulticastMessage cur : received){
			//System.out.println("cur"+cur.toString());
			//System.out.println("message" + message);
			if(cur.toOrigString().equals(message)){
				//System.out.println("Message is"+cur.toString());
				
				return true;
			}
		}
		return false;
	}
	public void run()
	{
		while(true){
			//synchronized(this.hold_back_queue_1){
				//for(TimeStampedMessage t_msg : hold_back_queue_1){
				while(hold_back_queue_1.size() != 0){
					System.out.println("Begin size of the queue "+this.hold_back_queue_1.size());
					MulticastMessage t_msg = hold_back_queue_1.poll();
					if(comparemessage(t_msg)){
						System.out.println("Duplicate");
						//do nothing if the msg is already received
					}
					else{
						//add to received msg list
						mp.get_received_msgs().add((MulticastMessage) t_msg);
						//
						//
						//!!!Check whether it is okay!!!
						System.out.println("source name"+t_msg.get_source());
						if(!t_msg.get_source().equals(mp.get_local_name()) && !t_msg.get_orig_src().equals(mp.get_local_name())){
							for(String dest : mp.get_groups().get(t_msg.get_target_group())){
								MulticastMessage new_msg = t_msg.copy();
								new_msg.set_source(mp.get_local_name());
								new_msg.set_dest(dest);
								System.out.println("Send to "+dest);
								mp.send(new_msg, true);
							}
							//System.out.println("Send to all");
							//System.out.println("size:"+mp.get_groups().get(t_msg.get_target_group()).size());
						}
						else{
							System.out.println("...wrong logic...msg from same source gets processed");
						}
						//synchronized(this.hold_back_queue_2){
						this.hold_back_queue_2.add(t_msg);
						//}
					}
					//right?
					//this.hold_back_queue_1.remove();
					System.out.println("End size of the queue"+this.hold_back_queue_1.size());
					
				}
			//}
		}
	}
}
