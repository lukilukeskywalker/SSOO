import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class main {
	//private static final int FIFO_MAX_SIZE = 25;
	static final boolean debug_en=true;	//Para habilitar la salida de mensajes en este proceso;
	
	static private String entrada=null;
	static private String salida=null;
	static private int num_gen=5;
	static private int num_dist=1;
	static private int num_vol=3;	//Segun enunciado al principio 3, despues el numero que se meta
	static private int num_receptores=1;
	static private int num_tareas_max=25;
	
	private static BufferedReader objReader = null;
	private static PrintWriter objWriter = null;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(!setParams(args))return;
		
		BlockingQueue<Task> CTE = new ArrayBlockingQueue<Task>(num_tareas_max);
		BlockingQueue<Task> CR = new ArrayBlockingQueue<Task>(num_tareas_max);
		HashSet<Task> TTP = new HashSet<Task>(num_tareas_max);

		Thread[] generadores = new Thread[num_gen];//List<Thread> generadores = new ArrayList<Thread>();
		for(int i=0; i < num_gen; i++) {
			Generador genTareas = new Generador(objReader, CTE);
			generadores[i]=new Thread(genTareas);//genTareas.start();//Fail, runnable no implementa el metodo start. El metodo start se implementa en thread
			generadores[i].setName("Generador-"+i);
			generadores[i].start();//generadores.add(genTareas);
		}
		debug("Se han creado los generadores");
		Thread[] distribuidores = new Thread[num_dist];//List<Distribuidor> distribuidores = new ArrayList<Distribuidor>();
		for(int i=0; i < num_dist; i++) {
			Distribuidor disTareas = new Distribuidor(CTE, CR, num_vol);
			distribuidores[i]=new Thread(disTareas);//disTareas.run();
			distribuidores[i].setName("Distribuidor-"+i);
			distribuidores[i].start();//distribuidores.add(disTareas);
		}
		debug("Se han creado los distribuidores");
		Thread[] receptores = new Thread[num_receptores];//List<Receptor> receptores = new ArrayList<Receptor>();
		for(int i=0; i < num_receptores; i++) {
			Receptor recpTareas = new Receptor(objWriter, CR);
			receptores[i]=new Thread(recpTareas);//recpTareas.run();
			receptores[i].setName("Receptor-"+i);
			receptores[i].start();//receptores.add(recpTareas);			
		}
		debug("Se han creado los receptores");
		//((Generador) generadores).run();
		//Distribuidor distribuidor;
		//genTareas = new Generador(entrada, CTE);
		//distribuidor = new Distribuidor(CTE);
		//genTareas.run();
		//distribuidor.run();
		try {
			TimeUnit.SECONDS.sleep(60);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	private static void debug(String message) {
		if(!debug_en)return;
		final String nombre = Thread.currentThread().getName();
		System.err.println(nombre +" " + message);
	}
	private static boolean setParams(String[] args) {
		if(!(args.length ==2 | args.length ==6 | args.length ==7)) {
			System.err.println("Se requieren 2, 6 o 7 parametros, fichero_entrada, fichero_salida, "
					+ "numero de generadores, numero de distribuidores, numero de receptores y "
					+ "numero de voluntarios, numero maximo de tareas activas");
			return false;
		}
		if(args.length >= 6) {
			num_gen=Integer.parseInt(args[3]);
			num_dist=Integer.parseInt(args[4]);
			num_receptores=Integer.parseInt(args[5]);
			num_vol=Integer.parseInt(args[6]);
			if(args.length==7) {
				num_tareas_max=Integer.parseInt(args[7]);
			}
		}
		entrada=args[0];
		try {
			FileReader fich_entrada=new FileReader(entrada);
			objReader=new BufferedReader(fich_entrada);
		}catch(IOException e) {
			e.printStackTrace();
			System.err.println("Se ha producido un error inicializando el fichero de entrada");
			return false;
		}
		salida=args[1];
		try {
			objWriter=new PrintWriter(salida);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Se ha producido un error inicializando el fichero de salida");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
