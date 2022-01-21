//Proyecto SETI por Lukas Gdanietz y Manuel Infantes

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class main {
	//private static final int FIFO_MAX_SIZE = 25;
	static final boolean debug_en=true;	//Para habilitar la salida de mensajes en este proceso;
	
	static private String entrada=null;
	static private String salida=null;
	static private int num_gen=2;
	static private int num_dist=3;
	static private int num_vol=3;	//Segun enunciado al principio 3, despues el numero que se meta
	static private int num_receptores=3;
	static private int num_tareas_max=25;
	private static Semaphore proc_activos;
	
	private static BufferedReader objReader = null;
	private static PrintWriter objWriter = null;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(!setParams(args))return;
		
		BlockingQueue<Task> CTE = new ArrayBlockingQueue<Task>(num_tareas_max);
		BlockingQueue<Task_Activo> CR = new ArrayBlockingQueue<Task_Activo>(num_tareas_max);
		Map<Integer, Task_Activo> TTP = new Hashtable<>(num_tareas_max);	//Tabla TTP. La key sera el ID de la tarea.
		ReentrantLock TTP_lock=new ReentrantLock();

		Thread[] generadores = new Thread[num_gen];//List<Thread> generadores = new ArrayList<Thread>();
		proc_activos=new Semaphore(num_tareas_max-num_receptores, true);	//La pregunta del millon es... cuantos procesos dejamos que entren en la cola CTE
		/*Juego de logica: Para ver cuantos proc_activos permito, supongamos que cada numero de voluntarios es capaz de finalizar 1 Tarea erroneamente
		 * reintroduciendola en CTE. Entonces pueden acoger otra tarea que estuviera finalizada, que otra vez podria tratar de reintroducir en CTE,
		 * pero en ese caso CTE ya habria reducido el numero de tareas por 1+num_recptores... creo*/
		for(int i=0; i < num_gen; i++) {
			Generador genTareas = new Generador(objReader, CTE, proc_activos);
			generadores[i]=new Thread(genTareas);//genTareas.start();//Fail, runnable no implementa el metodo start. El metodo start se implementa en thread
			generadores[i].setName("Generador-"+i);
			generadores[i].start();//generadores.add(genTareas);
		}
		debug("Se han creado los "+ num_gen +" generadores");
		Thread[] distribuidores = new Thread[num_dist];//List<Distribuidor> distribuidores = new ArrayList<Distribuidor>();
		for(int i=0; i < num_dist; i++) {
			Distribuidor disTareas = new Distribuidor(CTE, TTP, TTP_lock, CR, num_vol);
			distribuidores[i]=new Thread(disTareas);//disTareas.run();
			distribuidores[i].setName("Distribuidor-"+i);
			distribuidores[i].start();//distribuidores.add(disTareas);
		}
		debug("Se han creado los "+num_dist+" distribuidores");
		Thread[] receptores = new Thread[num_receptores];//List<Receptor> receptores = new ArrayList<Receptor>();
		for(int i=0; i < num_receptores; i++) {
			Receptor recpTareas = new Receptor(objWriter, CTE, proc_activos, TTP, TTP_lock, CR, num_vol);
			receptores[i]=new Thread(recpTareas);//recpTareas.run();
			receptores[i].setName("Receptor-"+i);
			receptores[i].start();//receptores.add(recpTareas);			
		}
		debug("Se han creado los "+ num_receptores+ " receptores");
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
		System.out.flush();
	}
	private static boolean setParams(String[] args) {
		if(!(args.length ==2 | args.length ==6 | args.length ==7)) {
			System.err.println("Se requieren 2, 6 o 7 parametros, fichero_entrada, fichero_salida, "
					+ "numero de generadores, numero de distribuidores, numero de receptores y "
					+ "numero de voluntarios, numero maximo de tareas activas");
			return false;
		}
		if(args.length >= 6) {
			num_gen=Integer.parseInt(args[2]);
			num_dist=Integer.parseInt(args[3]);
			num_receptores=Integer.parseInt(args[4]);
			num_vol=Integer.parseInt(args[5]);
			if(args.length==7) {
				num_tareas_max=Integer.parseInt(args[6]);
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
			FileWriter fich_salida=new FileWriter(salida);
			objWriter=new PrintWriter(fich_salida);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Se ha producido un error inicializando el fichero de salida");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
