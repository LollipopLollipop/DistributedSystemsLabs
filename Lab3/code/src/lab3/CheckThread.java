package lab3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CheckThread extends Thread{
	private MessagePasser mp;
	private Lock lock;
	int in_critical_section;
	Condition not_in_critical_section; 
	public CheckThread(MessagePasser mp) {
		// TODO Auto-generated constructor stub
		System.out.println("CheckThread is being running");
		this.mp = mp;
		this.lock = mp.get_lock();
		this.in_critical_section = mp.get_critical_section_status();
		this.not_in_critical_section = mp.get_not_in_critical_section_condition();	
	}
	public void run(){
		while(true){
			lock.lock();
			if(mp.flag_use == 0){
				lock.unlock();
				continue;
			}
			else{
				//lock.lock();
				//System.out.println("Out of operation");
				while(mp.get_critical_section_status()!=1){
					try {
						//System.out.println("In checkthread, status:"+mp.get_critical_section_status());
						mp.get_not_in_critical_section_condition().await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//lock.unlock();
				//System.out.println("Back to operation\n\n");
				mp.flag_use = 0;
			}
			lock.unlock();
		}
	}
}
