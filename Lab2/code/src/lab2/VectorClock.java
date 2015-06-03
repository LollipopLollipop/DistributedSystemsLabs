package lab2;

import java.util.Arrays;

public class VectorClock extends ClockService<int[]>{

	public VectorClock(int local_id, int size)
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
	synchronized public int[] update(int d)
	{
		timestamp[local_id]+=d;
		
		return timestamp.clone();//not sure if it works
	}
	synchronized public void check_and_update(TimeStampedMessage received_msg, int d)
	{
		for(int i = 0; i < timestamp.length; i++)
		{
			timestamp[i] = Math.max(timestamp[i], ((int[])received_msg.get_timestamp())[i]);
		}
		timestamp[local_id]+=d;
	}
	

	
	public String get_timestamp_string(){
		String vector_string = "";
		for(int i=0; i<size; i++){
			vector_string += (String.valueOf(timestamp[i]) + " ");
		}
		return (vector_string.trim());
	}
}


