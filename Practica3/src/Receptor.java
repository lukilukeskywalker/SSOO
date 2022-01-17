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
				job.status(State.DONE);
				debug("Tarea: "+job);
				if(debug_en)objwriter.print(Thread.currentThread().getName()+": ");
				objwriter.println(job.toString());
				objwriter.flush();
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
