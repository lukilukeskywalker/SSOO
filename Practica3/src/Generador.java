//Proyecto SETI por Lukas Gdanietz y Manuel Infantes
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
//public class Generador extends Thread{ //https://stackoverflow.com/questions/15471432/why-implements-runnable-is-preferred-over-extends-thread
public class Generador implements Runnable{
	static final boolean debug_en=true;
	private BufferedReader objReader = null;
	private final BlockingQueue<Task> queue;
	private Semaphore proc_activos;//private AtomicInteger proc_activos;
	private int timeout;
	
	public Generador(BufferedReader objReader, BlockingQueue<Task> queue, Semaphore proc_activos) {
		//Thread.currentThread().setName(threadName);
		this.proc_activos=proc_activos;
		this.queue=queue;
		this.timeout=60;
		this.objReader=objReader;
	}
	public void run() {
		String trabajo_pendiente;
		//synchronized(objReader) {//BufferedReader esta sincronizado con sigo mismo
		try {
			while(( trabajo_pendiente = objReader.readLine())!=null) {
				this.proc_activos.acquire();//Decrementa el contador de tareas que pueden entrar en CTE. Si es 0 se queda esperando
				Task trabajo = new Task(trabajo_pendiente);
				this.setJob(trabajo);
				this.debug(trabajo.toString());
				//TimeUnit.SECONDS.sleep(1);
			}
		}catch(IOException e) {
			e.printStackTrace();
			return;
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		//}
	}
	private boolean setJob(Task job) {
		/*try {
			this.queue.put(job);
		}catch(InterruptedException e) {
			e.printStackTrace();
			return false;
		}*/
		//mejor usar offer
		try {
			if(this.queue.offer(job, timeout, TimeUnit.SECONDS)) {
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
