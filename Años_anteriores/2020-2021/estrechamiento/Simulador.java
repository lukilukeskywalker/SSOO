package estrechamiento;

public final class Simulador
{
	public static void main ( String[] args )
	throws InterruptedException
	{
		final int C = 4; /* Número máximo de vehículos circulando por el tramo estrecho */
		final int M = 5; /* Número máximo de vehiculos que acceden cuando hay esperando en sentido contrario */
		final int V = M * C; /* Número de vehículos con el que se hará la simulación */
		final Estrechamiento e = new Estrechamiento ( C, M ); /* Estrechamiento a simular */
		final Thread[] vehiculos = new Thread[V]; /* Hilos que representan los vehículos */

		for ( int i = 0; i < V; i++ )
		{
			final Vehiculo v = new Vehiculo ( e );
			vehiculos[i] = new Thread ( v );
			vehiculos[i].start();
		}
		for ( int i = 0; i < V; i++ )
			vehiculos[i].join();
	}
}
