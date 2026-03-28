import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.SuccessorFunction;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Experimento 4:
 * Estudiar cómo evoluciona el tiempo de ejecución al aumentar el tamaño del problema
 * manteniendo la proporción 5 centros : 100 grupos.
 *
 * Se usan Hill Climbing y Simulated Annealing con:
 * - Heurística: criterio 1
 * - Operadores fijados: mover + intercambiar
 * - Inicialización fijada: genSolTiempo
 * - Parámetros SA fijados (de Exp. 3): steps=6000, stiter=50, k=20, lambda=0.001
 *
 * Argumentos opcionales:
 *   --reps N      (default 10)
 *   --seed S      (default 0)
 *   --max-centros C  (default 30)  (5,10,15,...,C)
 *   --start-centros C (default 5)
 *   --step-centros S  (default 5)
 */
public class Experimento4EscaladoProporcion {

    private static final int HELIS_POR_CENTRO = 1;

    // Fijados por experimentos anteriores
    private static final SuccessorFunction HC_SUCC = new SucesorHCCombinado(
            SucesorHCOperadorAislado.OP_MOVER,
            SucesorHCOperadorAislado.OP_INTERCAMBIAR
    );
    private static final SuccessorFunction SA_SUCC = new SucesorSA3();
    private static final HeuristicFunction H = new HeuristicaCriterio1();
    private static final GoalTest GOAL = new GoalState();

    // Parámetros SA (ganador exp3)
    private static final int SA_STEPS = 6000;
    private static final int SA_STITER = 50;
    private static final int SA_K = 20;
    private static final double SA_LAMBDA = 0.001;

    public static void main(String[] args) throws Exception {
        int reps = 10;
        long baseSeed = 0L;
        int maxCentros = 30;
        int startCentros = 5;
        int stepCentros = 5;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--reps":
                    reps = Integer.parseInt(args[++i]);
                    break;
                case "--seed":
                    baseSeed = Long.parseLong(args[++i]);
                    break;
                case "--max-centros":
                    maxCentros = Integer.parseInt(args[++i]);
                    break;
                case "--start-centros":
                    startCentros = Integer.parseInt(args[++i]);
                    break;
                case "--step-centros":
                    stepCentros = Integer.parseInt(args[++i]);
                    break;
                default:
                    break;
            }
        }

        List<Integer> seeds = generateSeeds(reps, baseSeed);

        System.out.println("=== EXPERIMENTO 4: Escalado (proporción 5:100) ===");
        System.out.println("Inicialización fija: genSolTiempo");
        System.out.println("Operadores fijos: mover + intercanviar");
        System.out.println("SA params: steps=" + SA_STEPS + " stiter=" + SA_STITER + " k=" + SA_K + " lambda=" + SA_LAMBDA);
        System.out.println("Repeticiones por tamaño: " + reps + " (baseSeed=" + baseSeed + ")");
        System.out.println();

        System.out.println("centros;grupos;HC_ms;HC_coste;SA_ms;SA_coste");

        for (int centros = startCentros; centros <= maxCentros; centros += stepCentros) {
            int grupos = centros * 20; // proporción 5:100 -> 1:20

            Stats hcCost = new Stats();
            TimeStats hcMs = new TimeStats();
            Stats saCost = new Stats();
            TimeStats saMs = new TimeStats();

            for (int seed : seeds) {
                // Hill Climbing
                EstadoRescate eHC = new EstadoRescate(grupos, centros, HELIS_POR_CENTRO, seed);
                eHC.genSolTiempo(seed);
                Problem pHC = new Problem(eHC, HC_SUCC, GOAL, H);
                HillClimbingSearch hc = new HillClimbingSearch();
                long t0 = System.nanoTime();
                hc.search(pHC);
                long t1 = System.nanoTime();
                EstadoRescate rHC = (EstadoRescate) hc.getGoalState();
                hcCost.add(rHC.calcularTiempoTotal());
                hcMs.addNanos(t1 - t0);

                // Simulated Annealing
                EstadoRescate eSA = new EstadoRescate(grupos, centros, HELIS_POR_CENTRO, seed);
                eSA.genSolTiempo(seed);
                Problem pSA = new Problem(eSA, SA_SUCC, GOAL, H);
                SimulatedAnnealingSearch sa = new SimulatedAnnealingSearch(SA_STEPS, SA_STITER, SA_K, SA_LAMBDA);
                long s0 = System.nanoTime();
                sa.search(pSA);
                long s1 = System.nanoTime();
                EstadoRescate rSA = (EstadoRescate) sa.getGoalState();
                saCost.add(rSA.calcularTiempoTotal());
                saMs.addNanos(s1 - s0);
            }

            System.out.printf(Locale.US, "%d;%d;%.0f;%.2f;%.0f;%.2f%n",
                    centros, grupos,
                    hcMs.meanMillis(), hcCost.mean(),
                    saMs.meanMillis(), saCost.mean());
        }
    }

    private static List<Integer> generateSeeds(int reps, long baseSeed) {
        Random r = new Random(baseSeed);
        ArrayList<Integer> out = new ArrayList<>(reps);
        for (int i = 0; i < reps; i++) out.add(r.nextInt(Integer.MAX_VALUE));
        return out;
    }

    private static final class Stats {
        double sum = 0.0;
        int n = 0;
        void add(double x) { sum += x; n++; }
        double mean() { return n == 0 ? Double.NaN : sum / n; }
    }

    private static final class TimeStats {
        long sumNanos = 0L;
        int n = 0;
        void addNanos(long nanos) { sumNanos += nanos; n++; }
        double meanMillis() { return n == 0 ? Double.NaN : (sumNanos / 1_000_000.0) / n; }
    }
}

