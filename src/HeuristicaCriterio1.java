import aima.search.framework.HeuristicFunction;

/**
 * Heurística 1: Minimizar la suma total de tiempos empleados por todos
 * los helicópteros en rescatar a todos los grupos.
 *
 * h(estado) = suma de tiempos de todas las salidas de todos los helicópteros
 *
 * Al ser una función de minimización, el valor devuelto es directamente
 * el tiempo total (AIMA minimiza cuando se usa con HillClimbing/SA con
 * la convención de que buscamos el mínimo).
 *
 * NOTA: Las clases del AIMA minimizan la heurística en la búsqueda local.
 */
public class HeuristicaCriterio1 implements HeuristicFunction {

    public double getHeuristicValue(Object state) {
        EstadoRescate e = (EstadoRescate) state;
        return e.calcularTiempoTotal();
    }
}
