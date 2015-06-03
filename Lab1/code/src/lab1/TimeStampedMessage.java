package lab1;

import java.io.Serializable;

public class TimeStampedMessage extends Message implements Serializable, Comparable<TimeStampedMessage>{

	//public TimeStampedMessage() {
		// TODO Auto-generated constructor stub
	//}
	private Object timestamp;//could be int or array of int
	private String clock_type;

	public TimeStampedMessage(String src, String dest, String kind, Object data, Object timestamp, String clock_type)
	{
		super(src, dest, kind, data);
		this.timestamp = timestamp;
		this.clock_type = clock_type;
	}

	public Object get_timestamp(){return timestamp;}
	public String get_timestamp_string(){
		if(clock_type.equals("LOGICAL"))
			return String.valueOf(timestamp);
		else{
			int[] copy = (int[])timestamp;
			String vector_string = "";
			for(int i=0; i<copy.length; i++){
				vector_string += (String.valueOf(copy[i]) + " ");
			}
			return (vector_string.trim());
		}
	}
	public void set_timestamp(Object timestamp){this.timestamp = timestamp;}
	
	
	public TimeStampedMessage copy(){
		TimeStampedMessage new_message = new TimeStampedMessage(super.src,super.dest,super.kind,super.data, this.timestamp,this.clock_type);
		new_message.set_seq_num(super.seqNum);
		return new_message;	
	}
	
	
	public int compareTo(TimeStampedMessage t_msg)
	{
		//return clock_service.compare(timestamp, t_msg.get_timestamp());
		//System.out.println("here");
		if(clock_type.equals("LOGICAL")){
			if((Integer)timestamp < (Integer)t_msg.get_timestamp())
				return -1;
			else if((Integer)timestamp > (Integer)t_msg.get_timestamp())
				return 1;
			else
				return 0;
		}
		else{
			return compare_vector_clock((int[])timestamp, (int[])t_msg.get_timestamp());
		}
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
			if(timestamp_1[i] != timestamp_2[i])
				return true;
		}
		
		return false;
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
	
	public int compare_vector_clock(int[] timestamp_1, int[] timestamp_2)
	{
		if(less_than(timestamp_1, timestamp_2))
			return -1;
		else if(less_than(timestamp_2, timestamp_1))
			return 1;
		else   // tm1 == tm2 or tm1 || tm2
			return 0;
	}
	
	
}


