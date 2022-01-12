import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
public class Generador extends Thread{
	public int id;
	private String entrada=null;
	private BufferedReader objReader = null;
	private final BlockingQueue<String> queue;
	public Generador(String entrada, BlockingQueue<String> queue, int process_id) {
		this.entrada=entrada;
		this.queue=queue;
		this.id=process_id;
		try {
			FileReader fich_entrada=new FileReader(this.entrada);
			objReader=new BufferedReader(fich_entrada);
		}catch(IOException e) {
			e.printStackTrace();
			return;
		}
		
	}
	public void run() {
		String trabajo_pendiente;
		try {
			while(( trabajo_pendiente = objReader.readLine())!=null) {
				System.out.println(trabajo_pendiente);
				this.setJob(trabajo_pendiente);
			}
		}catch(IOException e) {
			e.printStackTrace();
			return;
		}
		
	}
	private boolean setJob(String job) {
		try {
			this.queue.put(job);
		}catch(InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
