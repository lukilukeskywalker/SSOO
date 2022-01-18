import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Receptor implements Runnable{
	static final boolean debug_en=true;
	private int timeout=60;
	private String salida=null;
	private final BlockingQueue<Task> CTE;
	private final Map<Integer, Task> TTP;
	private final ReentrantLock TTP_lock;
	private final BlockingQueue<Task> CR;
	private int num_vol=3;
	private PrintWriter objwriter;
	public Receptor(PrintWriter objwriter, BlockingQueue<Task> CTE, Map<Integer, Task> TTP, ReentrantLock lock, BlockingQueue<Task> CR, int num_vol) {
		this.objwriter = objwriter;
		this.CTE=CTE;
		this.TTP=TTP;
		this.TTP_lock=lock;
		this.CR=CR;
		this.num_vol=num_vol;
	}

	
	public void run() {
		Task job;
		try {
			while((job=this.CR.poll(timeout, TimeUnit.SECONDS))!=null) {
				job.status(State.DONE);
				debug("Tarea: "+job);
				if(debug_en)objwriter.print(Thread.currentThread().getName()+": ");
				TTP_lock.lock();
				Task TTP_job=TTP.get(job.id);
				TTP_job.voluntarios_act--;//Un voluntario a finalizado
				if(TTP_job.voluntarios_act == (num_vol-1)) {
					TTP_job.resultado=job.resultado;
				}else if(TTP_job.resultado != job.resultado) {
					TTP_job.status(State.FAILING);
				}
				if(TTP_job.voluntarios_act == 0) {
					if(TTP_job.status==State.FAILING) {
						TTP.remove(TTP_job.id);
						Task new_job=new Task(TTP_job, -1);
						this.CTE.offer(new_job, timeout, TimeUnit.SECONDS);	//The question is: Que sucede cuando la cola esta llena, y se siguen cogiendo trabajos?.
						//Al final las colas se saturaran , y nunca solucionara tareas
					}
					else {
						TTP_job.status(State.DONE);
						TTP.remove(TTP_job.id);
						objwriter.println(TTP_job.toString());
						objwriter.flush();
					}
				}

				TTP_lock.unlock();
				
				//objwriter.println(job.toString());
				//objwriter.flush();
			}
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		//Pregunta. como pueden saber este receptor si otros receptores han procesado 
		
	}
	private static void debug(String message) {
		if(!debug_en)return;
		final String nombre = Thread.currentThread().getName();
		System.err.println(nombre +" " + message);
		System.out.flush();
	}

}
