//Proyecto SETI por Lukas Gdanietz y Manuel Infantes
import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//public class Distribuidor extends Thread{ //https://stackoverflow.com/questions/15471432/why-implements-runnable-is-preferred-over-extends-thread
public class Distribuidor implements Runnable{
	static final boolean debug_en=true;
	private int timeout=60;
	private final BlockingQueue<Task> CTE;
	private final Map<Integer, Task_Activo> TTP;
	private final ReentrantLock TTP_lock;
	private final BlockingQueue<Task_Activo> CR;
	private int num_vol=3;	//Esto me molesta... Al principio del problema se dicen 3, en la fase 2 se dice tantos como digan los argumentos
	
	/**
	*Constructor Genera un distribuidor
	*@param CTE Hilo de entrada
	*@param TTP Tabla de procesos activos
	*@param lock Lock para bloquear la lectura o escritura de TTP
	*@param CR Cola de tareas con resultado, donde meteran los voluntarios su resultado
	*@param num_vol Numero de voluntarios
	*/
	public Distribuidor(BlockingQueue<Task> CTE, Map<Integer, Task_Activo> TTP, ReentrantLock lock, BlockingQueue<Task_Activo> CR, int num_vol) {
		// TODO Auto-generated constructor stub
		this.CTE=CTE;
		this.TTP=TTP;
		this.CR=CR;	
		this.TTP_lock=lock;
		this.num_vol=num_vol;
		/*Este hilo no hace nada con esta cola, pero se la tiene que pasar a los voluntarios para que sepan donde entregar los resultados*/

	}
	public void run() {
		Task job;
		try {
			while((job=this.CTE.poll(timeout, TimeUnit.SECONDS))!=null) {
				Task_Activo TTP_task=new Task_Activo(job);//job.status(State.IN_PROGRESS);
				Thread[] voluntarios = new Thread[num_vol];
				TTP_lock.lock();//Se queda aqui hasta que podemos asegurarnos de escribir en TTP
				if(TTP.get(TTP_task.id)!=null)TTP.remove(TTP_task.id);	//Como dice el dicho, mejor prevenir que curar
				TTP_task.voluntarios_act=num_vol;//job.setVoluntarios(num_vol);
				TTP.put(TTP_task.id, TTP_task);//Los tasks se crean con un ID ascendente por definicion. La unica forma en la que se reintrodujera el mismo id como key
				//seria si CTE devuelve a otro thread el mismo job. Lo cual por definicion es imposible... o si se reintroduce en receptor de nuevo a CTE xD
				TTP_lock.unlock();
				for(int i=0; i<num_vol; i++) {
					Task_Activo cloned_task = new Task_Activo(job, i);//clonamos la tarea para que otros hilos no la manipulen(no sucedia, pero habia algo que me molestaba)
					Voluntario analizador_vol = new Voluntario(cloned_task, CR, 100, 20);
					voluntarios[i]=new Thread(analizador_vol);//analizador_vol.run();
					voluntarios[i].setName("Voluntario-"+i+" TASK_ID"+job.id);
					voluntarios[i].start();
				}
				//Ahora tenemos que guardar el trabao en una tabla de tareas en proceso. Para ello no debemos manipular la tabla mientras que otros Distribuidores
				//o otros receptores estan manipulando la tabla.
				//Planteamiento sobre usar readWriteLock: es un poco estupido porque imaginemos en el receptor descubrimos que el numero coincide. Tendremos que
				//incrementar algun contador que checkee que a sido comprobado por lo menos por un voluntario
				//va aser lock al final: https://stackoverflow.com/questions/2332765/what-is-the-difference-between-lock-mutex-and-semaphore
				
				
			}
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		/*try {
			job=this.CTE.take();//job = this.queue.poll(timeout, TimeUnit.SECONDS);
			System.out.println("El trabajo que recibe distribuidor es: "+ job);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	private static void debug(String message) {
		if(!debug_en)return;
		final String nombre = Thread.currentThread().getName();
		System.err.println(nombre +" " + message);
		System.out.flush();
	}

}
