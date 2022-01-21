import java.util.Random;

public class Coche implements Runnable
{
	private static final Random random = new Random();

	private final Rotonda rotonda;
	private final int entrada;
	private final int salida;
	private final long tiempoEnLaCalle;
	private final long tiempoDeGiro;

	public static void circular ( long tiempo )
	throws InterruptedException
	{
		Thread.sleep ( tiempo );
	}

	public Coche ( Rotonda rotonda, int entrada, int salida )
	{
		this.rotonda = rotonda;
		this.entrada = entrada;
		this.salida = salida;
		tiempoEnLaCalle = Math.abs ( random.nextLong() % 1000 );
		tiempoDeGiro = Math.abs ( random.nextLong() % 100 );
	}

	@Override
	public void run ()
	{
		try
		{
			comportamiento();
		}
		catch ( InterruptedException e )
		{
			System.out.println ( e.getMessage() );
		}
	}

	private void comportamiento()
	throws InterruptedException
	{
		Coche.circular ( tiempoEnLaCalle ); // llegar a la rotonda
		debug ( "va a entrar en la rotonda por la entrada " + entrada );
		rotonda.usar ( entrada, salida, tiempoDeGiro );
		debug ( "ha salido de la rotonda por la salida " + salida );
		circular ( tiempoEnLaCalle ); // continuar al destino
	}

	private void debug ( String mensaje )
	{
		final String nombre = Thread.currentThread().getName();
		System.err.println ( nombre + mensaje );
	}
}
