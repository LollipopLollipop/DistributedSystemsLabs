package lab1;

import java.util.Arrays;

public class VectorClock extends ClockService{

	public VectorClock() {
		// TODO Auto-generated constructor stub
	}
	public VectorClock(int size, int local_id)
	{
		this.size = size;
		this.local_id = local_id;
		this.timestamp = new int[size];
	}

	@Override
	protected void construct() {
		// TODO Auto-generated method stub
		
	}
	private int size;
	private int local_id;
	private int[] timestamp;
	
	synchronized public int[] get_timestamp(){return timestamp;}
	synchronized public int[] update()
	{
		timestamp[local_id]++;
		//return Arrays.copyOf(timestamp, size);
		return timestamp.clone();//not sure if it works
	}
	synchronized public void update(TimeStampedMessage received_msg)
	{
		for(int i = 0; i < timestamp.length; i++)
		{
			timestamp[i] = Math.max(timestamp[i], ((int[])received_msg.get_timestamp())[i]);
		}
		timestamp[local_id]++;
	}
	

	public boolean equals(int[] timestamp_1, int[] timestamp_2)
	{
		int i;
		if(timestamp_1.length != timestamp_2.length){
			System.err.println("unmatched timestamp array size");
			//return false;
			System.exit(1);
		}
			
		for(i = 0; i < timestamp_1.length; i++)
		{
			if(timestamp_1[i] != timestamp_2[i])
				return false;
		}
		
		return true;
	}
	
	public boolean not_equals(int[] timestamp_1, int[] timestamp_2)
	{
		int i;
		if(timestamp_1.length != timestamp_2.length){
			System.err.println("unmatched timestamp array size");
			System.exit(1);
		}
			
		for(i = 0; i < timestamp_1.length; i++)
		{
			if(timestamp_1[i] == timestamp_2[i])
				return false;
		}
		
		return true;
	}
	

	private boolean less_than_or_equals(int[] timestamp_1, int[] timestamp_2)
	{
		int i;
		if(timestamp_1.length != timestamp_2.length){
			System.err.println("unmatched timestamp array size");
			System.exit(1);
		}
		for(i = 0; i < timestamp_1.length; i++)
		{
			if(timestamp_1[i] > timestamp_2[i])
				return false;
		}
		
		return true;
	}

	private boolean less_than(int[] timestamp_1, int[] timestamp_2)
	{
		return (less_than_or_equals(timestamp_1, timestamp_2) && 
				not_equals(timestamp_1, timestamp_2));
	}
	/*
	public int compare(int[] tm1, int[] tm2)
	{
		if(less_than(tm1, tm2))
			return -1;
		else if(less_than(tm2, tm1))
			return 1;
		else   // tm1 == tm2 or tm1 || tm2
			return 0;
	}*/
}


