package lab3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MutualExclusionCheck extends Thread{
	private MessagePasser mp;
	private Queue<Message> request_queue = new LinkedList<Message>();
	private int ack_count=0;
	public MutualExclusionCheck(MessagePasser mp) {
		// TODO Auto-generated constructor stub
		this.mp = mp;
	}
	public void run(){
		Message msg = null;
		BlockingQueue<Message> request_release_queue = mp.get_request_release_queue();
		Lock lock = mp.get_lock();
		Condition not_in_critical_section = mp.get_not_in_critical_section_condition();
		while(true){
			try {
				msg = request_release_queue.take();
				mp.get_receive_count().addAndGet(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msg.get_kind().equals("REQUEST_ACK")){
				System.out.println("received req ack");
				ack_count++;
				if(ack_count==mp.get_groups().get("Group"+Integer.toString(mp.get_local_id())).size()){
					//System.out.println("ACK FULL");
					ack_count=0;
					lock.lock();
					mp.set_critical_section_status(1);
					//System.out.println("In ACK check, status:"+mp.get_critical_section_status());
					not_in_critical_section.signal();
					lock.unlock();
				}
				
			}
			else if(msg.get_kind().equals("REQUEST")){
				//System.out.println("received req");
				//System.out.println()
				
				lock.lock();
				if(mp.get_critical_section_status()==1 || mp.get_voted_status()){
					request_queue.add(msg);
				}
				else{
					mp.send(
							new Message(mp.get_local_name(), msg.get_source(), "REQUEST_ACK", 
									null),
							false);
					mp.get_send_count().addAndGet(1);
					mp.set_voted_status(true);
				}
				lock.unlock();
			}
			else if(msg.get_kind().equals("RELEASE")){
				//System.out.println("received release");
				if(!request_queue.isEmpty()){
					Message tmp = request_queue.remove();
					mp.send(
							new Message(mp.get_local_name(), tmp.get_source(), "REQUEST_ACK", 
									null),
							false);
					mp.get_send_count().addAndGet(1);
					mp.set_voted_status(true);
					
				}
				else
					mp.set_voted_status(false);
			}
		}
	}
}
