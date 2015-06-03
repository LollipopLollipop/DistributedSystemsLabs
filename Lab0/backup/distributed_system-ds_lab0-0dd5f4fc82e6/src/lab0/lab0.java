package lab0;

import java.io.*;

public class lab0
{
	private static final String ui_prompt = "> ";

	public static String check_action(String input)
	{
		String action = input.trim();
		String[] input_elements = action.split(" ");
		
		//check input length
		if(input_elements.length != 1)
		{
			return null;
		}
		if((!(action.equals("receive")) && !(action.equals("send")) && !(action.equals("quit"))))
		{
			return null;
		}
		
		return action;
	}

	public static void main(String[] args)
	{
		if(args.length != 2)
		{
			System.out.println("Usage: $java lab0 <conf_filename> <local_name>");
			System.exit(1);
		}
		String input = null;
		String action = null;
		BufferedReader br = null;
		System.out.println("Welcome!");
		System.out.println("Actions you can take: \n\tsend \t receive \t quit");
		MessagePasser mp = new MessagePasser(args[0],args[1]);
		mp.setup();
		BackgroundListener bgListener = new BackgroundListener(mp, args[1], mp.get_all_sockets());
		bgListener.start();
		BackgroundSpeaker bgSpeaker = new BackgroundSpeaker(mp);
		bgSpeaker.start();
		Checkfile inputfile = new Checkfile(args[0], args[1], mp, bgSpeaker, bgListener, mp.get_all_sockets());
		inputfile.start();
		try{
			br = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				System.out.print(ui_prompt);
				/* get user input */
				input = br.readLine();
				if((action = check_action(input)) != null)
				{
					if(action.equals("quit"))
						System.exit(1);
					
					else if(action.equals("receive"))
					{
						Message message = mp.receive();
						if(message == null)
							System.out.println("Receive buffer empty.");
						else
						{
							System.out.println("Receive message from" + message.get_source() + " to " + message.get_dest() 
									+ "\n Sequence Number: " + message.get_seq_num() + " Kind: " + message.get_kind() 
									+ " Data: " + (String)message.get_data() + " Duplicate: " + message.get_duplicate());
							
						}
					}
					else
					{	
						System.out.println("Destination name: ");
						String dst = br.readLine().trim();
						System.out.println("Message kind: ");
						String kind = br.readLine().trim();
						System.out.println("Data to send: ");
						String data = br.readLine();
						mp.send(new Message(args[1], dst, kind, data));
						System.out.println("Send message from" + args[1] + " to " + dst);
					}
			
				}
				else{
					System.out.println("invalid input...please try again.");
				}

			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try{
				if(br != null)
					br.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
}
