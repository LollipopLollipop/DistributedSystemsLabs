package lab2;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OrderingCheck extends Thread{
	private MessagePasser mp;
	private ConcurrentLinkedQueue<MulticastMessage> hold_back_queue;
	private int[] local_timestamp;
	private int[] source_timestamp;
	
	public OrderingCheck(MessagePasser mp){
		this.mp = mp;
		this.hold_back_queue = mp.get_hold_back_queue_2();
		this.local_timestamp = (int[]) mp.get_clock_service().get_timestamp();
	}
	public void run(){
		while(true){
			//synchronized(this.hold_back_queue)
			while(hold_back_queue.size()!=0){
				System.out.println("now the second buffer queue is not empty");
				System.out.println("local clock is" + Arrays.toString((int[])local_timestamp));
				MulticastMessage message = hold_back_queue.poll();
				String source = message.get_orig_src();
				source_timestamp = (int[]) message.get_timestamp();
				System.out.println("source clock is" + Arrays.toString((int[])source_timestamp));
				int source_id=mp.get_node_ids().get(source);
				//System.out.println("source id is " + source_id);
				//check_ordering(message.get_timestamp(), mp.get_local_id(), source_id);
				if(check_ordering(source_timestamp, local_timestamp, source_id)){
					//hold_back_queue.remove(message);
					System.out.println("Ordering fulfilled");
					mp.get_receive_queue().add(message);
					//local_timestamp[source_id]+=1;
					local_timestamp[source_id]=source_timestamp[source_id];
					//local_timestamp[mp.get_local_id()]++;
				}
				else{
					//re-add the msg back to the queue if it can not be delivered currently
					hold_back_queue.add(message);
				}
			}
			//}
		}
	}
	
	public boolean check_ordering(int[] source_timestamp, int[] local_timestamp, int source_id){
		if(source_timestamp.length!=local_timestamp.length){
			return false;
		}
		else if(source_timestamp[source_id]!=(local_timestamp[source_id]+1)){
			//though the text suggests source_timestamp[source_id]==(local_timestamp[source_id]+1
			//however, considering possible one-to-one conversation in between 
			//also possible intentionally drop
			//better to change it to >=
			return false;
		}
		else{
			for(int i=0; i<source_timestamp.length && i != source_id; i++){
				if(source_timestamp[i]>local_timestamp[i])
					return false;
			}
			return true;
		}
		
	}
}
