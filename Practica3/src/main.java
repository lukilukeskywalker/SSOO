import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class main {
	private static final int FIFO_MAX_SIZE = 25;
	static private String entrada=null;
	static private String salida=null;
	static private int num_gen=1;
	static private int num_dist=1;
	static private int num_vol=1;
	static private int num_receptores=1;
	static private int hilos=-1;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		setParams(args);
		BlockingQueue<String> CTE = new ArrayBlockingQueue<String>(FIFO_MAX_SIZE);
		BlockingQueue<String> CE = new ArrayBlockingQueue<String>(FIFO_MAX_SIZE);
		
		Generador genTareas;
		Distribuidor distribuidor;
		genTareas = new Generador(entrada, CTE, assign_id());
		distribuidor = new Distribuidor(CTE, assign_id());
		genTareas.run();
		distribuidor.run();
		try {
			TimeUnit.SECONDS.sleep(60);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	private static int assign_id() {
		hilos=hilos+1;
		return hilos;
	}
	private static void setParams(String[] args) {
		if(args.length !=2 | args.length !=6) {
			System.err.println("Se requieren 2 o 6 parametros, fichero_entrada, fichero_salida, "
					+ "numero de generadores, numero de distribuidores, numero de receptores y "
					+ "numero de voluntarios");
			return;
		}
		if(args.length == 6) {
			num_gen=Integer.parseInt(args[3]);
			num_dist=Integer.parseInt(args[4]);
			num_receptores=Integer.parseInt(args[5]);
			num_vol=Integer.parseInt(args[6]);
		}
		entrada=args[0];
		salida=args[1];
	}

}
