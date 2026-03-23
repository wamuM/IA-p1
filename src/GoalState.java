import aima.search.framework.GoalTest;

/**
 * Comprobador de estado final para búsqueda local.
 *
 * En búsqueda local (Hill Climbing, Simulated Annealing) no existe
 * un "estado final" que se pueda verificar durante la búsqueda, ya
 * que el algoritmo termina por criterio de parada (no hay mejora /
 * número máximo de iteraciones).
 *
 * Por tanto, esta función siempre devuelve false.
 */
public class GoalState implements GoalTest {

    public boolean isGoalState(Object state){
        return((EstadoRescate) state).is_goal();
    }
}
