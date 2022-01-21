import java.util.Random;

public final class Simulador
{
	private static final Random random = new Random();

	private Simulador ()
	{
	}

	public static void main ( String[] args )
	throws InterruptedException
	{
		final int numCoches = Integer.parseInt ( args[0] );
		final int numCalles = Integer.parseInt ( args[1] );
		final Coche[] coches = new Coche[numCoches];
		final Thread[] hilos = new Thread[numCoches];
		final Rotonda rotonda = new Rotonda ( numCalles );

		for ( int i = 0; i < numCoches; i++ )
		{
			final int e = random.nextInt ( numCalles );
			final int s = random.nextInt ( numCalles );
			coches[i] = new Coche ( rotonda, e, s );
		}

		for ( int i = 0; i < numCoches; i++ )
		{
			hilos[i] = new Thread ( coches[i] );
			hilos[i].setName ( "coche-" + i + ": " );
			hilos[i].start();
		}

		for ( int i = 0; i < numCoches; i++ )
			hilos[i].join();
	}
}
