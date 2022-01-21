package estrechamiento;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Estrechamiento
{
	private static final Random r = new Random(); /* generador de números aleatorios */
	private static final long tiempoDeCruce = r.nextLong() % 1000; /* Cuánto cuesta atravesar el estrechamiento */
	private final int C; /* número de vehículos que caben en el tramo estrecho */
	private final int M; /* número máximo de vehículos que entran antes de cambiar el sentido de paso */
	private int sentidoActual; /* sentido en el que pueden pasar los vehículos en este momento */
	private final int[] esperando; /* Cuántos vehículos hay esperando en cada sentido */
	private int dentro; /* Cuántos vehículos hay pasando el tramo estrecho */
	private int permitidos; /* Cuántos han pasado habiendo vehículos esperando en sentido contrario */
	private final Lock cerrojo; /* Para implementar una región crítica con varias condiciones de espera */
	private final Condition[] esperar; /* Variables de condición para esperar en los dos sentidos */

	/**
	 * Crea un estrechamiento con una certa capacidad y un número máximo de
	 * vehículos que pueden accedr al estrechamiento una vez que hay algún
	 * vehículo esperando en el sentido contrario.
	 * @param C Número máximo de vehículos que pueden estar circulando por el
	 * estrechamiento en un momento dado.
	 * @param M Máximo número de vehículos que pueden accedr al estrechamiento
	 * una vez que hay algún vehículo esperando en el sentido contrario.
	 */
	public Estrechamiento ( final int C, final int M )
	{
		this.C = C;
		this.M = M;
		esperando = new int[2];
		esperando[0] = 0;
		esperando[1] = 0;
		sentidoActual = 0;
		dentro = 0;
		permitidos = 0;
		cerrojo = new ReentrantLock();
		esperar = new Condition[2];
		esperar[0] = cerrojo.newCondition();
		esperar[1] = cerrojo.newCondition();
	}

	/**
	 * Devuelve el sentido contrario a aquel en el que están circulando
	 * los vehículos en un momento dado.
	 * @return El sentido contrario al actual.
	 */
	private int sentidoContrario ()
	{
		return ( sentidoActual + 1 ) % 2;
	}

	/**
	 * Determina si se dan las condiciones para que un vehículo acceda
	 * al tramo estrecho o, por el contrario, debe esperar.
	 * @param sentido El sentido en el que circula el vehículo que
	 * invoca al método.
	 * @return true si el vehículo puede pasar y false en caso contrario.
	 */
	private boolean puedePasar ( final int sentido )
	{
		return
			sentido == sentidoActual && dentro < C && permitidos < M
			||
			sentido != sentidoActual && dentro == 0 && esperando[sentidoActual] == 0;
	}

	/**
	 * Método bloqueante que regula el acceso al tramo estrecho de la calzada.
	 * Si el vehículo no puede acceder en las condiciones actuales, se queda
	 * bloqueado hasta que pueda acceder y, en ese momento, se desbloquea.
	 * @param sentido El sentido en el que circula el vehículo que ejecuta el método.
	 */
	public void acceder ( final int sentido )
	{
		cerrojo.lock();
		try
		{
			esperando[sentido]++;
			while ( !puedePasar(sentido) )
				esperar[sentido].await();
			esperando[sentido]--;
			sentidoActual = sentido;
			dentro++;
			if ( esperando[sentidoContrario()] > 0 )
				permitidos++;
		}
		catch ( InterruptedException e )
		{
			System.err.println ( "Hilo interrumpido" );
			esperando[sentido]--;
		}
		finally
		{
			cerrojo.unlock();
		}
	}

	/**
	 * Se ejecuta cada vez que un vehículo abandona el estrechamiento.
	 * Cuando un vehículo abandona el estrechamiento, podría acceder a
	 * él un vehículo que circule en su miemo destino, si se cumplen
	 * las condiciones para que acceda al estrechamiento, o puede acceder
	 * un vehículo del otro sentido si ya no queda ningún vehículo
	 * circulando en el tramo estrecho.
	 * @param sentido El sentido en el que circula el vehículo que ejecuta el método.
	 */
	public void salir ( final int sentido )
	{
		cerrojo.lock();
		try
		{
			dentro--;
			if ( dentro == 0 )
			{
				permitidos = 0;
				sentidoActual = sentidoContrario();
				esperar[sentidoActual].signalAll();
			}
			else if ( permitidos < M )
				esperar[sentido].signalAll();
		}
		finally
		{
			cerrojo.unlock();
		}
	}

	/**
	 * Realiza una espera de un tiempo inferior a un segundo para
	 * simular el tiempo que tarda un vehículo en atravesar el
	 * tramo estrecho.
	 */
	public void atravesar ()
	{
		try
		{
			Thread.sleep(tiempoDeCruce);
		} catch (InterruptedException e) {
			System.err.println ( "Interrupción en atravesar ignorada" );
		}
	}
}
