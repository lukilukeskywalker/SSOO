import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Rotonda
{
	private enum Estado { LIBRE, OCUPADO }
	private final Estado[] sectores;
	private int cochesDentro;
	private final Lock cerrojo;
	private final Condition[] libre;

	public Rotonda ( int accesos )
	{
		sectores = new Estado[accesos];
		cochesDentro = 0;
		cerrojo = new ReentrantLock();	
		libre = new Condition[accesos];
		for ( int i = 0; i < accesos; i++ )
		{
			sectores[i] = Estado.LIBRE;
			libre[i] = cerrojo.newCondition();
		}
	}

	public void usar ( int entrada, int salida, long tiempoDeGiro )
	throws InterruptedException
	{
		int posición;

		posición = entrada;
		acceder ( posición );
		debug ( "ha entrado a la posición " + posición );
		do
		{
			Coche.circular ( tiempoDeGiro );
			posición = rotar ( posición );
		}
		while ( posición != salida );
		salir ( posición );
	}

	private void acceder ( int posición )
	throws InterruptedException
	{
		cerrojo.lock();
		try
		{
			while ( ! puedoEntrar ( posición ) )
				libre[posición].await();
			cochesDentro++;
			sectores[posición] = Estado.OCUPADO;
		}
		finally
		{
			cerrojo.unlock();
		}
	}

	private boolean puedoEntrar ( int entrada )
	{
		return cochesDentro < ( sectores.length - 1 ) &&
		sectores[entrada] == Estado.LIBRE;
	}

	private int rotar ( int posición )
	throws InterruptedException
	{
		cerrojo.lock();
		try
		{
			final int siguiente = ( posición+1 ) % sectores.length;
			while ( sectores[siguiente] == Estado.OCUPADO )
				libre[siguiente].await();
			sectores[siguiente] = Estado.OCUPADO;
			sectores[posición] = Estado.LIBRE;
			debug ( "pasa de la posición " + posición + " a la " + siguiente );
			libre[posición].signalAll();
			return siguiente;
		}
		finally
		{
			cerrojo.unlock();
		}
	}

	private void salir ( int posición )
	{
		cerrojo.lock();
		try
		{
			cochesDentro--;
			sectores[posición] = Estado.LIBRE;
			avisoGeneral();
			debug ( "sale por la salida " + posición );
		}
		finally
		{
			cerrojo.unlock();
		}
	}

	private void avisoGeneral ()
	{
		cerrojo.lock();
		try
		{
			for ( int i = 0; i < sectores.length; i++ )
				if ( sectores[i] == Estado.LIBRE )
					libre[i].signalAll();
		}
		finally
		{
			cerrojo.unlock();
		}
	}


	private void debug ( String mensaje )
	{
		final String nombre = Thread.currentThread().getName();
		System.err.println ( nombre + mensaje );
	}
}
