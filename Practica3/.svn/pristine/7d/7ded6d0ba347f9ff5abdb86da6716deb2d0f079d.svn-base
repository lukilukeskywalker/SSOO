//Proyecto SETI por Lukas Gdanietz y Manuel Infantes
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Voluntario extends Analizador implements Runnable{
	static final boolean debug_en=true;
	private final BlockingQueue<Task_Activo> CR;
	private Task_Activo task;
	final private int timeout=60;
	/**
	*Constructor Genera un Voluntario
	*@param CR COla de tareas con resultado, donde meteran los voluntarios su resultado
	*@param retardo Retardo
	*@param probFallo probabilidad de fallo
	*@param num_vol Numero de voluntarios
	*/
	public Voluntario(Task_Activo task,BlockingQueue<Task_Activo> CR, long retardo, int probFallo) {
		// TODO Auto-generated constructor stub
		super(retardo, probFallo);
		this.task=task;
		this.CR=CR; //Cola de resultados
	}
	@Override
	public void run() {
		int resultado = this.analizarTarea(this.task);
		task.resultado=resultado;//task.setResultado(resultado);
		//task.status(State.DONE); //Donde digo que es el final? en voluntario o en hilo receptor?
		this.setSalida(task);
	}
	int analizarTarea(Task tarea) {
		return analizar(tarea.data);
	}
	private boolean setSalida(Task_Activo job) {
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
	private static void debug(String message) {
		if(!debug_en)return;
		final String nombre = Thread.currentThread().getName();
		System.err.println(nombre +" " + message);
		System.out.flush();
	}

}
