import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//public class Distribuidor extends Thread{ //https://stackoverflow.com/questions/15471432/why-implements-runnable-is-preferred-over-extends-thread
public class Distribuidor implements Runnable{
	private int timeout=60;
	private final BlockingQueue<Task> CTE;
	private final BlockingQueue<Task> CR;
	private int num_vol=3;	//Esto me molesta... Al principio del problema se dicen 3, en la fase 2 se dice tantos como digan los argumentos

	
	public Distribuidor(BlockingQueue<Task> CTE, BlockingQueue<Task> CR, int num_vol) {
		// TODO Auto-generated constructor stub
		this.CTE=CTE;
		this.CR=CR;	
		this.num_vol=num_vol;
		/*Este hilo no hace nada con esta cola, pero se la tiene que pasar a los voluntarios para que sepan donde entregar los resultados*/

	}
	public void run() {
		Task job;
		try {
			while((job=this.CTE.poll(timeout, TimeUnit.SECONDS))!=null) {
				job.status(State.IN_PROGRESS);
				Thread[] voluntarios = new Thread[num_vol];
				for(int i=0; i<num_vol-1; i++) {
					Voluntario analizador_vol = new Voluntario(job, CR, 100, 20);
					voluntarios[i]=new Thread(analizador_vol);//analizador_vol.run();
					voluntarios[i].setName("Voluntario-"+i+" TASK_ID"+job.id);
					voluntarios[i].start();
				}
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
	private void debug(String message) {
		final String nombre = Thread.currentThread().getName();
		System.err.println(nombre +" " + message);
	}

}
