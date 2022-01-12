import java.util.Random;

/**
 * Analizador de datos. Simula el proceso realizado por los voluntarios con
 * los datos que reciben. En el SETI real, correspondería a enviar el dato
 * al voluntario, que se procesara allí, y que se devolviera el resultado
 * obtenido. Genera resultados erróneos con cierta probabilidad.
 * 
 * @author SSOO-DTE
 *
 */

public class Analizador
{
	private static final Random rand = new Random();
	private final long retardo;
	private final int probFallo;

	/**
	 * Constructor. Genera un analizador de datos de radiotelescopio.
	 * @param retardo Determina el orden de magnitud del tiempo que va
	 * a tardar en generar el resultado del análisis. Si es 1000, será
	 * del orden de segundos; si es 100, del orden de décimas de segundo.
	 * @param probFallo Probabilidad de generar un resultado erróneo.
	 * La probabilidad se da en tanto por ciento. Por ejemplo el valor 100
	 * indica que siempre se generará un resultado erróneo, mientras que 0
	 * indica que el resultado siempre será correcto.
	 */
	public Analizador ( long retardo, int probFallo )
	{
		this.retardo = retardo;
		this.probFallo = probFallo;
	}

	/**
	 * Procesa un dato. Simula el tiempo de proceso mediante una espera
	 * pasiva. Con probabilidad probFallo, generará un resultado erróneo.
	 * @param dato El dato a procesar.
	 * @return El resultado de procesar el dato.
	 */
	public int analizar ( String dato )
	{
		int resultado;
		// Simula (esperando) el tiempo de procesado 
		// de esta tarea por voluntario
		try
		{
			Thread.sleep ( dato.length() * retardo );
		} catch (InterruptedException e)
		{
			 // Sleep interrumpido: se ignora, no se reintenta
		}

		// Genera un resultado válido o uno erroneo,
		// en función de la probabilidad de error
		if ( rand.nextInt(100) < probFallo )
			resultado = rand.nextInt(Integer.MAX_VALUE); // fallo
		else
			resultado = dato.hashCode(); 	// resultado correcto
		return resultado;
	}
}
