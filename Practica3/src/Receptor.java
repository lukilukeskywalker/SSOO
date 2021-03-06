//Proyecto SETI por Lukas Gdanietz y Manuel Infantes
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Receptor implements Runnable{
	static final boolean debug_en=true;
	private int timeout=60;
	private String salida=null;
	private final BlockingQueue<Task> CTE;
	private final Semaphore proc_activos;
	private final Map<Integer, Task_Activo> TTP;
	private final ReentrantLock TTP_lock;
	private final BlockingQueue<Task_Activo> CR;
	private int num_vol=3;
	private PrintWriter objwriter;
	/**
	*Constructor Receptor 
	*@param objwriter Objeto de escritutra en fichero de salida
	*@param CTE Hilo al que reintroducir la tarea fallida
	*@param proc_activos Semaforo para permitir el acceso a CTE en Generador
	*@param TTP Tabla de procesos activos
	*@param lock Locl para bloquear la lectura u escritura de TTP
	*@param CR Cola de tareas con resultado, donde meteran los voluntarios su resultado
	*@param num_vol numero de voluntarios por tarea
	*/
	public Receptor(PrintWriter objwriter, BlockingQueue<Task> CTE, Semaphore proc_activos, Map<Integer, Task_Activo> TTP, ReentrantLock lock, BlockingQueue<Task_Activo> CR, int num_vol) {
		this.objwriter = objwriter;
		this.CTE=CTE;
		this.proc_activos=proc_activos;
		this.TTP=TTP;
		this.TTP_lock=lock;
		this.CR=CR;
		this.num_vol=num_vol;
	}

	
	public void run() {
		Task_Activo job;
		try {
			while((job=this.CR.poll(timeout, TimeUnit.SECONDS))!=null) {
				//job.status(State.DONE);	//basura, se puede obviar y eliminar.
				debug("Tarea: "+job);
				TTP_lock.lock();
				Task_Activo TTP_job;
				if((TTP_job=TTP.get(job.id))!=null) {
					TTP_job.voluntarios_act--;
					if(TTP_job.voluntarios_act == (num_vol-1)) {
						TTP_job.resultado=job.resultado;
					}
					else if(TTP_job.resultado != job.resultado) {	//Si no es igual el resultado lo eliminamos de TTP y reintroducmos en CTE
						debug("Se rechaza la tarea");
						this.TTP.remove(TTP_job.id);
						this.CTE.offer(TTP_job.parent(), timeout, TimeUnit.SECONDS);
					}else if(TTP_job.voluntarios_act==0) {
						TTP_job.status=State.DONE;//TTP_job.status(State.DONE);
						this.TTP.remove(TTP_job.id);
						proc_activos.release();
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
