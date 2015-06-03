package lab1;

import java.io.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class logger
{
	public static String check_action(String input)
	{
		String action = input.trim();
		String[] input_elements = action.split(" ");
		
		//check input lengths
		if(input_elements.length != 1)
		{
			return null;
		}
		if((!(action.equals("report")) && !(action.equals("quit"))))
		{
			return null;
		}
		
		return action;
	}
	public static void main(String[] args) throws IOException
	{
		if(args.length != 2)
		{
			System.out.println("Illegal input, Please check");
			return;
		}
		String input = null;
		String action = null;
		BufferedReader br = null;
		System.out.println("Welcome!");
		System.out.println("Actions you can take: \n\t report \t quit");
		MessagePasser mp = new MessagePasser(args[0],args[1]);
		try {
			mp.setup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LoggerListener loggerlistener = new LoggerListener(mp, args[1], mp.get_all_loggers());
		loggerlistener.start();
		
		BackgroundSpeaker bgSpeaker = new BackgroundSpeaker(mp);
		bgSpeaker.start();
		
		Checkfile inputfile = new Checkfile(args[0], args[1], mp, bgSpeaker, mp.get_all_sockets());
		inputfile.start();
				
		br = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			System.out.print("-> ");
			input = br.readLine();
			if((action = check_action(input)) != null)
			{
				if(action.equals("quit"))
					System.exit(1);
				
				else if(action.equals("report"))
				{
					ArrayList<TimeStampedMessage> receive_list = new ArrayList<TimeStampedMessage>(mp.get_receive_queue());
					Collections.sort(receive_list);
					
					/*
					for (TimeStampedMessage message : receive_list) {
					    System.out.println("Receive message from " + message.get_source() + " to " + message.get_dest() 
								+ "\n Sequence Number: " + message.get_seq_num() + " Kind: " + message.get_kind() 
								+ " Data: " + (String)message.get_data() + " Duplicate: " + message.get_duplicate() + "TimeStamp:"+ message.get_timestamp());
					}*/
					
					boolean concurrent = false;
					for(int i=0; i<receive_list.size();i++){
						TimeStampedMessage message = receive_list.get(i);
						if(!concurrent){
							if((i+1<receive_list.size()) && message.compareTo(receive_list.get(i+1))==0){
								concurrent = true;
								System.out.println("Concurrency occurs:");
							}
							System.out.println("Receive message from " + message.get_source() + " to " + message.get_dest() 
									+ "\n Sequence Number: " + message.get_seq_num() + " Kind: " + message.get_kind() 
									+ " Data: " + (String)message.get_data() + " Duplicate: " + message.get_duplicate() + " TimeStamp:"+ message.get_timestamp_string());
						}
						else{
							System.out.println("Receive message from " + message.get_source() + " to " + message.get_dest() 
									+ "\n Sequence Number: " + message.get_seq_num() + " Kind: " + message.get_kind() 
									+ " Data: " + (String)message.get_data() + " Duplicate: " + message.get_duplicate() + " TimeStamp:"+ message.get_timestamp_string());
							if((i+1)>=receive_list.size() || message.compareTo(receive_list.get(i+1))!=0){
								concurrent = false;
								System.out.println("Concurrency ends.");
							}
						}
					}
					/*TimeStampedMessage message = mp.receive();
					if(message == null){
						System.out.println("Receive buffer empty.");
					}
					else
					{
						System.out.println("Receive message from " + message.get_source() + " to " + message.get_dest() 
								+ "\n Sequence Number: " + message.get_seq_num() + " Kind: " + message.get_kind() 
								+ " Data: " + (String)message.get_data() + " Duplicate: " + message.get_duplicate() + "TimeStamp:"+ message.get_timestamp());
						
					}*/
				}
				/*
				else
				{	
					System.out.println("Destination name: ");
					System.out.print("-> ");
					String dst = br.readLine().trim();
					System.out.println("Message kind: ");
					System.out.print("-> ");
					String kind = br.readLine().trim();
					System.out.println("Data to send: ");
					System.out.print("-> ");
					String data = br.readLine();
					mp.send(new TimeStampedMessage(args[1], dst, kind, data, mp.get_clock_service().update(1),mp.get_clock_service()));
					System.out.println("Send message from " + args[1] + " to " + dst);
				}
				*/
			}
			else{
				System.out.println("invalid input...please try again.");
			}

		}
	}
}
