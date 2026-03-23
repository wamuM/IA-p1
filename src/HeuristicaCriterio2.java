import aima.search.framework.HeuristicFunction;

/**
 * Heurística 2: Minimizar la suma total de tiempos MÁS el tiempo hasta
 * que el último grupo de prioridad 1 (heridos) es rescatado.
 *
 * h(estado) = tiempoTotal + peso * tiempoUltimoPrioridad1
 *
 * El parámetro "peso" permite ponderar la importancia del criterio P1
 * respecto al criterio general. Por defecto peso=1 (ambos sumados).
 *
 * Con peso > 1 se da más importancia a rescatar heridos rápidamente.
 *
 * DIFERENCIA CON CRITERIO 1:
 * - Criterio 1: optimiza la eficiencia global de todos los rescates.
 * - Criterio 2: además penaliza que los heridos (P1) tarden mucho,
 *   independientemente del tiempo total. Puede llevar a soluciones
 *   donde los heridos se rescatan antes aunque el tiempo total sea mayor.
 */
public class HeuristicaCriterio2 implements HeuristicFunction {
    private double peso;

    public HeuristicaCriterio2(double peso) {
        this.peso = peso;
    }

    public double getHeuristicValue(Object state) {
        EstadoRescate e = (EstadoRescate) state;
        return e.calcularTiempoTotal() + peso * e.calcularTiempoPrioridad1();
    }
}
