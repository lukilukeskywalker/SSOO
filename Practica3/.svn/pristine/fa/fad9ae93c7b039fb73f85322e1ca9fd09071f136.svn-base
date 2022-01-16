import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Receptor implements Runnable{
	static final boolean debug_en=true;
	private int timeout=60;
	private String salida=null;
	private final BlockingQueue<Task> CR;
	private PrintWriter objwriter;
	public Receptor(PrintWriter objwriter, BlockingQueue<Task> CR) {
		this.objwriter = objwriter;
		this.CR=CR;
	}

	
	public void run() {
		Task job;
		try {
			while((job=this.CR.poll(timeout, TimeUnit.SECONDS))!=null) {
				debug("Tarea: "+job);
			}
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	private static void debug(String message) {
		if(!debug_en)return;
		final String nombre = Thread.currentThread().getName();
		System.err.println(nombre +" " + message);
	}

}
