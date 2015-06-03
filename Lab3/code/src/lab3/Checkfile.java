package lab3;
import java.util.concurrent.*;
import java.io.File;
import java.io.IOException;

/*
 * This is a thread to check whether the configure file has been changed or not.
 * If it has been changed, we reload new rules and create new Speaker Thread to speaker messages.
 */
public class Checkfile extends Thread
{
	private final String conf_filename;
	private final String local_name;
	private final MessagePasser mp;
	private BackgroundSpeaker speaker;
	private ConcurrentLinkedQueue<BackgroundStreamListener> exits_socket;

	public Checkfile(String conf_filename, String local_name, MessagePasser mp, BackgroundSpeaker speaker, ConcurrentLinkedQueue<BackgroundStreamListener> exits_socket)
	{
		this.conf_filename = conf_filename;
		this.local_name = local_name;
		this.mp = mp;
		this.speaker = speaker;
		this.exits_socket = exits_socket;
		this.setDaemon(true);
	}

	public void run()
	{
		//Get the configure's last edit date
		File configure_file = new File(conf_filename);
		long date = configure_file.lastModified();
		while(true)
		{
			long temp = configure_file.lastModified();
			if(temp != date)
			{
				System.out.println("File has been changed");
				date = temp;
				

				//Close speaker thread and clear all current sockets connections
				speaker.interrupt();				
				
				synchronized(exits_socket)
				{
					while(!exits_socket.isEmpty())
					{
						BackgroundStreamListener temp_socket = (BackgroundStreamListener)exits_socket.poll();
						temp_socket.interrupt();
					}
				}

				// restart initing MessagePasser, speakerer and Receiver threads
				try {
					mp.setup();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				speaker = new BackgroundSpeaker(mp);
				speaker.start();
			}
			else
			{
				Thread.yield();
			}
		}
	}
}
