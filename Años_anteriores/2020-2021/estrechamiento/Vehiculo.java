package estrechamiento;

import java.util.Random;

public class Vehiculo implements Runnable
{
	private static final Random r = new Random(); /* generador de números aleatorios */ 
	private final Estrechamiento estrechamiento;
	private final int sentido;

	/**
	 * Crea un vehículo que accederá al estrechamiento en un sentido elegido al azar.
	 * @param e el estrechamiento al que accederá el vehículo.
	 */
	public Vehiculo ( final Estrechamiento e )
	{
		estrechamiento = e;
		sentido = r.nextInt ( 2 );
	}

	/**
	 * Espera un tiempo aleatorio inferior a un segundo.
	 */
	private void esperar ()
	{
		try
		{
			final long aleatorio = r.nextLong();
			final long tiempo = (aleatorio > 0) ? aleatorio : -aleatorio; 
			Thread.sleep ( tiempo % 1000 );
		} catch (InterruptedException e) {
			System.err.println ( "Interrumpción ignorada en la espera inicial" );
		}
	}

	@Override
	public void run ()
	{
		esperar();
		debug ( "intenta acceder al estrechamiento" );
		estrechamiento.acceder ( sentido );
		debug ( "ha accedido al estrechamiento" );
		estrechamiento.atravesar();
		debug ( "ha atravesado el estrechamiento" );
		estrechamiento.salir ( sentido );
		debug ( "ha salido del tramo estrecho" );
	}
	
	private void debug ( String m )
	{
		final String nombre = Thread.currentThread().getName();
		final String mensaje = nombre + " que va en sentido " + sentido + " " + m;
		System.out.println ( mensaje );
	}
}
