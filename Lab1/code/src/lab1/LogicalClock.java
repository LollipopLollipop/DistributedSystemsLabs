package lab1;

public class LogicalClock extends ClockService<Integer>{

	public LogicalClock() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void construct() {
		// TODO Auto-generated method stub
		
	}
	
	private Integer timestamp = 0;
	synchronized public Integer update(int d)
	{
		timestamp += d;
		return timestamp;
	}
	synchronized public Integer get_timestamp(){return timestamp;}
	synchronized public void check_and_update(TimeStampedMessage received_msg, int d)
	{
		timestamp = Math.max(timestamp, (int)received_msg.get_timestamp()) + d;
	}
	
	/*
	public boolean equal(Integer tm1, Integer tm2)
	{
		return tm1 == tm2;
	}*/

	public int compare(Integer timestamp_1, Integer timestamp_2)
	{
		if(timestamp_1 < timestamp_2)
			return -1;
		else if(timestamp_1 > timestamp_2)
			return 1;
		else
			return 0;
	}
	public String get_timestamp_string(){
		return String.valueOf(timestamp);
	}
	
	
	
}
