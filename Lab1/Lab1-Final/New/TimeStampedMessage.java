package lab1;

public class TimeStampedMessage extends Message{

	public TimeStampedMessage() {
		// TODO Auto-generated constructor stub
	}
	private Object timestamp;//could be int or array of int
	private ClockService clock;

	public TimeStampedMessage(String src, String dest, String kind, Object data, Object timestamp, ClockService clock)
	{
		super(src, dest, kind, data);
		this.timestamp = timestamp;
		this.clock = clock;
	}

	public Object get_timestamp(){return timestamp;}
	public void set_timestamp(Object timestamp){this.timestamp = timestamp;}
	public ClockService get_clock(){return clock;}

	/*
	public String toString()
	{
		if(clock.getClass().getName().equals("bin.LogicalClock"))
			return (super.toString() + "|timestamp:" + (Integer)this.timestamp);
		else if(clock.getClass().getName().equals("bin.VectorClock"))
		{
			return (super.toString() + "|timestamp:" + Arrays.toString((int[])timestamp));
		}
		else
			return null;
	}*/
	/*
	public int compareTo(TimeStampedMessage t_msg)
	{
		return clock.compare(timestamp, t_msg.getTimeStamp());
	}

	public boolean is_equal(TimeStampedMessage t_msg)
	{
		return clock.equal(timestamp, t_msg.getTimeStamp());
	}*/

	/*
	public TimeStampedMessage deepCopy()
	{
		ByteArrayOutputStream bo = null;
		ByteArrayInputStream bi = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		TimeStampedMessage new_msg = null;

		try
		{
			bo = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bo);
			oos.writeObject(this);

			bi = new ByteArrayInputStream(bo.toByteArray());
			ois = new ObjectInputStream(bi);
			new_msg = (TimeStampedMessage)ois.readObject();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(bo != null)
					bo.close();
				if(bi != null)
					bi.close();
				if(oos != null)
					oos.close();
				if(ois != null)
					ois.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
			return new_msg;
		}
	}*/
}


