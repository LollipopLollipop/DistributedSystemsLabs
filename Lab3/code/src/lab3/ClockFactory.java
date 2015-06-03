package lab3;

public class ClockFactory {

	public ClockFactory() {
		// TODO Auto-generated constructor stub
	}
	public ClockFactory(int local_id, int size){
		this.size = size;
		this.local_id = local_id;
	}
	private int size = -1;
	private int local_id = -1;
	
	public  ClockService buildClock(ClockType model) {
		ClockService clock = null;
        switch (model) {
        case LOGICAL:
            clock = new LogicalClock();
            break;
 
        case VECTOR:
        	clock = new VectorClock(this.local_id, this.size);
            break;
 
        default:
            // throw some exception
            break;
        }
        return clock;
    }

}
