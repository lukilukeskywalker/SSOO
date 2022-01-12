import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Distribuidor extends Thread{
	public int id;
	private int timeout;
	private final BlockingQueue<String> queue;
	public Distribuidor(BlockingQueue<String> queue, int process_id) {
		// TODO Auto-generated constructor stub
		this.queue=queue;
		this.id=process_id;
		timeout = 60;	//Contains the amount of secs to wait for new data in the queue
	}
	public void run() {
		String job;
		try {
			job=this.queue.take();//job = this.queue.poll(timeout, TimeUnit.SECONDS);
			System.out.println("El trabajo que recibe distribuidor es: "+ job);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
