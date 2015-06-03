package lab1;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import lab1.Message;
import lab1.Rule;

public class LoggerStreamListener extends Thread
{
	private Socket socket;
	private MessagePasser mp;
	private ClockService clockservice;
	private ConcurrentLinkedQueue<LoggerStreamListener> socket_queue;
	public LoggerStreamListener(Socket conn_sock, MessagePasser mp, ConcurrentLinkedQueue<LoggerStreamListener> sq, ClockService clockservice)
	{
		this.socket = conn_sock;
		this.mp = mp;
		this.clockservice = clockservice;
		this.socket_queue = sq;
	}

	public void run()
	{
		/*
		 * String from_user = null;
	
		ObjectInputStream ois = null;
		ConcurrentLinkedQueue<TimeStampedMessage> receive_queue = mp.getReceiveQueue();
		TimeStampedMessage msg = null;
		String[] src_dest = null;
		*/
		//???? A B send to logger?
		/*msg = (TimeStampedMessage)ois.readObject(); // it will lead to EOFException when from_user goes offline
			src_dest = msg.getSrc().split("\\$");
			if(src_dest.length == 2)
			{
				msg.setSrc(src_dest[0]);
				msg.setDest(src_dest[1]);
			}
			receive_queue.add(msg);
			from_user = msg.getSrc();
		*/
		
		ObjectInputStream in = null;
		try{
			//retrieve the input stream for the particular socket
			in = new ObjectInputStream(socket.getInputStream());
			
			while(true){
				//TimeStampedMessage message = (Message)in.readObject();
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
						mp.get_receive_queue().add(mp.get_delayed_receive_queue().poll());
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println(e);
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
