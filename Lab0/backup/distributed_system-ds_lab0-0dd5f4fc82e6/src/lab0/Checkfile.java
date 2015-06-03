package lab0;

import java.util.concurrent.*;
import java.io.File;

/*
 * This is a thread to check whether the configuration file has been changed or not.
 * If changed, stop current jobs(threads), initialize again with new configuration and restart.
 */
public class Checkfile extends Thread
{
	private String conf_filename;
	private String local_name;
	private MessagePasser mp;
	private BackgroundSpeaker speaker;
	private BackgroundListener receiver;
	private ConcurrentLinkedQueue<BackgroundStreamListener> listener_queue;

	
	public Checkfile(String conf_filename, String local_name, MessagePasser mp, BackgroundSpeaker speaker, BackgroundListener receiver, ConcurrentLinkedQueue<BackgroundStreamListener> listener_queue)
	{
		this.conf_filename = conf_filename;
		this.local_name = local_name;
		this.mp = mp;
		this.speaker = speaker;
		this.receiver = receiver;
		this.listener_queue = listener_queue;
		this.setDaemon(true);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		File ff = new File(conf_filename);
		long last_modify = ff.lastModified();
		while(true)
		{
			long temp = ff.lastModified();
			if(temp != last_modify)
			{
				
				System.out.println("Note: Configuration file has been changed.");
				last_modify = temp;
				//Close current speaker and receiver threads by sending interrupt signals
				speaker.interrupt();
				//receiver.interrupt();
				//Empty listen_queue . 
				ConcurrentLinkedQueue<BackgroundStreamListener> cur_streamlistener = listener_queue;
				synchronized(cur_streamlistener)
				{
					while(!cur_streamlistener.isEmpty())
					{//Close all current sockets.
						cur_streamlistener.poll().interrupt();
					}
				}
				
				//Initialize messagepasser with new configuration file
				mp.setup();
				//restart new threads of speaker and receiver
				speaker = new BackgroundSpeaker(mp);
				speaker.start();
				//receiver = new BackgroundListener(mp,local_name,listener_queue);
				//receiver.start();
			}
			else
			{
				//Keep alive and inform CPU its state
				Thread.yield();
			}
		}
	}
}
