import java.util.concurrent.atomic.AtomicInteger;

enum State{
		NEW,
		IN_PROGRESS, 
		DONE,
		FAILING
	}
public class Task {
	private static AtomicInteger gen = new AtomicInteger();
	public int id;
	public int sub_id=-1;
	public String data;
	private int resultado;
	public State status;
	
	public Task(String trabajo) {
		this.status=State.NEW;
		this.data=trabajo;
		this.id=gen.getAndIncrement();
	}
	public Task(Task task, int sub_id) {
		this.id=task.id;
		this.data=task.data;
		this.status=task.status;
		this.sub_id=sub_id;
		
	}
	public State status(State state) {
		status=state;
		return status;
	}
	public int setResultado(int resultado) {
		this.resultado=resultado;
		return this.resultado;
	}
	public void setSubId(int subId) {
		this.sub_id=subId;
	}
	public String toString() {
		if(this.status == State.DONE)return "SUB_ID"+ sub_id +" TaskID: " + id + " Dato: " +data+ " Resultado: "+ resultado +" Estado: "+ status;
		return "TaskID: " + id + " Dato: " +data+ " Estado: "+ this.status;
	}

}
