import java.util.concurrent.atomic.AtomicInteger;

enum State{
		NEW,
		IN_PROGRESS, 
		DONE
	}
public class Task {
	private static AtomicInteger gen = new AtomicInteger();
	public int id;
	public int sub_id;
	public String data;
	public int resultado;
	
	State taskState;
	public Task(String trabajo) {
		taskState=State.NEW;
		this.data=trabajo;
		this.id=gen.getAndIncrement();
	}
	public State status(State state) {
		taskState=state;
		return taskState;
	}
	public int setResultado(int resultado) {
		this.resultado=resultado;
		return this.resultado;
	}
	public String toString() {
		return "TaskID: " + id + " Dato: " +data+ " Estado: "+ taskState;
	}

}
