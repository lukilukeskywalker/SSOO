import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Voluntario extends Analizador implements Runnable{
	private final BlockingQueue<Task> CR;
	private Task task;
	final private int timeout=60;
	public Voluntario(Task task,BlockingQueue<Task> CR, long retardo, int probFallo) {
		// TODO Auto-generated constructor stub
		super(retardo, probFallo);
		this.task=task;
		this.CR=CR; //Cola de resultados
	}
	@Override
	public void run() {
		int resultado = this.analizarTarea(this.task);
		task.setResultado(resultado);
		//task.status(State.DONE); //Donde digo que es el final? en voluntario o en hilo receptor?
		this.setSalida(task);
	}
	int analizarTarea(Task tarea) {
		return analizar(tarea.data);
	}
	private boolean setSalida(Task job) {
		try {
			if(this.CR.offer(job, timeout, TimeUnit.SECONDS)) {
				return true;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
