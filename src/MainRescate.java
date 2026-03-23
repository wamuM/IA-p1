import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.*;



public class MainRescate {

    // Parámetros del escenario
    private static final int N_CENTROS_BASE = 5;
    private static final int N_HELICOPTEROS_BASE = 1;
    private static final int N_GRUPOS_BASE = 100;
    private static final int N_REPETICIONES = 1;

    // Parámetros de la heurística
    private final int PESO = 1;

    // Parámetros SimulatedAnnealing
    private static final int STEPS = 3000;
    private static final int STITER = 100;
    private static final int K = 125;
    private static final double LAMB = 0.0001;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Selecciona el algoritmo:");
        System.out.println("1. Hill Climbing");
        System.out.println("2. Simulated Annealing");

        int opcion = scanner.nextInt();
        scanner.close();

        if (opcion == 1) experimentoHillClimbing();
        else if (opcion == 2) experimentoSimulatedAnnealing();
        else System.out.println("Opcion no valida");
    }

    //Ejecutar Hill Climbing
    private static void experimentoHillClimbing() throws Exception {

        long startTime = System.currentTimeMillis();

        for (int seed = 0; seed < N_REPETICIONES; seed++) {
            EstadoRescate estado = new EstadoRescate(N_GRUPOS_BASE, N_CENTROS_BASE, N_HELICOPTEROS_BASE, seed);

            Problem problem = new Problem(estado, new SucesorHC3(), new GoalState(), new HeuristicaCriterio1());
            //Problem problemA = new Problem(estadoA, new SucesorHC3(), new GoalState(), new HeuristicaCriterio2(PESO));

            //Problem problemA = new Problem(estadoA, new SucesorHC2(), new GoalState(), new HeuristicaCriterio1());
            //Problem problemA = new Problem(estadoA, new SucesorHC2(), new GoalState(), new HeuristicaCriterio2(PESO));

            //Problem problemA = new Problem(estadoA, new SucesorHC1(), new GoalState(), new HeuristicaCriterio1());
            //Problem problemA = new Problem(estadoA, new SucesorHC1(), new GoalState(), new HeuristicaCriterio2(PESO));

            estado.genSolTiempo(seed);
            //estadoA.genSolSalidas(seed);
            //estadoA.genSolRandomPrioridad(seed);
            //estadoA.genSolRandom(seed);

            HillClimbingSearch hc = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, hc);
            hc.search(problem);
            EstadoRescate resultado = (EstadoRescate) hc.getGoalState();

            System.out.println();
            //printActions(agent.getActions());
            //printInstrumentation(agent.getInstrumentation());
            printSolucion(resultado);
            System.out.println(hc.getGoalState());

            long estimatedTime = System.currentTimeMillis() - startTime;
            System.out.println("Completado en: " + estimatedTime + " milisegundos");

            System.out.println("Seed: " + seed + "\t Tiempo: " + resultado.calcularTiempoTotal() + " min");

        }

    }

    //Ejecutar Simulated Annealing
    private static void experimentoSimulatedAnnealing() throws Exception {

        long startTime = System.currentTimeMillis();

        for (int seed = 0; seed < N_REPETICIONES; seed++) {
            EstadoRescate estado = new EstadoRescate(N_GRUPOS_BASE, N_CENTROS_BASE, N_HELICOPTEROS_BASE, seed);

            Problem problem = new Problem(estado, new SucesorSA3(), new GoalState(), new HeuristicaCriterio1());
            //Problem problemA = new Problem(estadoA, new SucesorSA3(), new GoalState(), new HeuristicaCriterio2(PESO));

            //Problem problemA = new Problem(estadoA, new SucesorSA2(), new GoalState(), new HeuristicaCriterio1());
            //Problem problemA = new Problem(estadoA, new SucesorSA2(), new GoalState(), new HeuristicaCriterio2(PESO));

            //Problem problemA = new Problem(estadoA, new SucesorSA1(), new GoalState(), new HeuristicaCriterio1());
            //Problem problemA = new Problem(estadoA, new SucesorSA1(), new GoalState(), new HeuristicaCriterio2(PESO));

            estado.genSolTiempo(seed);
            //estadoA.genSolSalidas(seed);
            //estadoA.genSolRandomPrioridad(seed);
            //estadoA.genSolRandom(seed);

            SimulatedAnnealingSearch sa = new SimulatedAnnealingSearch(STEPS, STITER, K, LAMB);
            SearchAgent agent = new SearchAgent(problem, sa);
            sa.search(problem);
            EstadoRescate resultado = (EstadoRescate) sa.getGoalState();

            //printInstrumentation(agent.getInstrumentation());
            printSolucion(resultado);
            System.out.println(sa.getGoalState());

            long estimatedTime = System.currentTimeMillis() - startTime;
            System.out.println("Completado en: " + estimatedTime + " milisegundos");

            System.out.println("Seed: " + seed + "\t Tiempo: " + resultado.calcularTiempoTotal() + " min");

        }
    }

    private static void printInstrumentation(Properties properties) {
        for (Object o : properties.keySet()) {
            String key = (String) o;
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }

    }

    private static void printActions(List actions) {
        for (Object o : actions) {
            String action = (String) o;
            System.out.println(action);
        }
    }

    public static void printSolucion(EstadoRescate estado) {
        for(int i = 0; i < estado.getNHelicopteros(); ++i) {
            System.out.println("Helicoptero " +i + " (centro " + i/estado.getNHelicopterosCentro() + "):");
            if (estado.getNSalidas(i) == 0) {
                System.out.println("  Sin salidas");
                continue;
            }
            for(int j = 0; j < estado.getNSalidas(i); ++j) {
                System.out.print("  Salida " + j + ": ");
                for (int g = 0; g < estado.getNGrupos(); g++) {
                    if (estado.getHelicoptero(g) == i && estado.getSalida(g) == j) {
                        System.out.print("Grupo " + g + " (P" + estado.getGrupos().get(g).getPrioridad() + ", " + estado.getGrupos().get(g).getNPersonas() + " personas) ");
                    }
                }
                System.out.println();
            }
        }
    }


}
