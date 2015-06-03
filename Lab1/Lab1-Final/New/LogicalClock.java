package lab1;

public class LogicalClock extends ClockService{

	public LogicalClock() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void construct() {
		// TODO Auto-generated method stub
		
	}
	
	private int timestamp = 0;
	synchronized public int update()
	{
		timestamp ++;
		return timestamp;
	}
	synchronized public int get_timestamp(){return timestamp;}
	synchronized public void update(TimeStampedMessage received_msg)
	{
		timestamp = Math.max(timestamp, (int)received_msg.get_timestamp()) + 1;
	}
	
	/*
	public boolean equal(Integer tm1, Integer tm2)
	{
		return tm1 == tm2;
	}*/

	public int compare(int timestamp_1, int timestamp_2)
	{
		if(timestamp_1 < timestamp_2)
			return -1;
		else if(timestamp_1 > timestamp_2)
			return 1;
		else
			return 0;
	}
	
	
	
}
