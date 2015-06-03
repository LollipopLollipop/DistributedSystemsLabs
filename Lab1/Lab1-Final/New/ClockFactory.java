package lab1;

public class ClockFactory {

	public ClockFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static ClockService buildClock(ClockType model) {
		ClockService clock = null;
        switch (model) {
        case LOGICAL:
            clock = new LogicalClock();
            break;
 
        case VECTOR:
            clock = new VectorClock();
            break;
 
        default:
            // throw some exception
            break;
        }
        return clock;
    }

}
