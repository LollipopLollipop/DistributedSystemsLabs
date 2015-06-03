package lab1;

import java.io.*;

public class lab1
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
		if((!(action.equals("receive")) && !(action.equals("send")) && !(action.equals("quit")) && !(action.equals("clock"))))
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
		System.out.println("Actions you can take: \n\tsend \t receive \t clock \t quit");
		MessagePasser mp = new MessagePasser(args[0],args[1]);
		try {
			mp.setup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BackgroundListener bgListener = new BackgroundListener(mp, args[1], mp.get_all_sockets());
		bgListener.start();
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
				
				else if(action.equals("receive"))
				{
					TimeStampedMessage message = mp.receive();
					if(message == null){
						System.out.println("Receive buffer empty.");
					}
					else
					{
						System.out.println("Receive message from " + message.get_source() + " to " + message.get_dest() 
								+ "\n Sequence Number: " + message.get_seq_num() + " Kind: " + message.get_kind() 
								+ " Data: " + (String)message.get_data() + " Duplicate: " + message.get_duplicate() + " TimeStamp: " + message.get_timestamp());
						
					}
				}
				else if(action.equals("send"))
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
					mp.send(new TimeStampedMessage(args[1], dst, kind, data, mp.get_clock_service().update(1), mp.get_clock_service_string()));
					//System.out.println("Send message from " + args[1] + " to " + dst + "with timestamp as " + mp.get_clock_service().get_timestamp());
				}	
				else{
					System.out.println("Current timestamp of this process is " + mp.get_clock_service().get_timestamp_string());
				}
			}
			else{
				System.out.println("invalid input...please try again.");
			}

		}
	}
}
