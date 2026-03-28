import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.SuccessorFunction;
import aima.search.informed.HillClimbingSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Experimento 5:
 * Estudiar cómo evoluciona el tiempo de ejecución para hallar la solución aumentando
 * por separado:
 *  A) número de grupos (centros fijos)
 *  B) número de centros (grupos fijos)
 *
 * Solo Hill Climbing, manteniendo fijo:
 * - Heurística: criterio 1
 * - Operadores: mover + intercanviar
 * - Inicialización: genSolTiempo
 *
 * Defaults (según enunciado):
 *  A) centros=5, grupos=100.. (paso 50)
 *  B) grupos=100, centros=5.. (paso 5)
 *
 * Argumentos opcionales:
 *   --reps N         (default 10)
 *   --seed S         (default 0)
 *   --mode M         (default both)  M in {both,groups,centers}
 *   --groups-start G (default 100)
 *   --groups-step G  (default 50)
 *   --groups-max G   (default 500)
 *   --centers-start C (default 5)
 *   --centers-step C  (default 5)
 *   --centers-max C   (default 30)
 */
public class Experimento5EscaladoSeparado {

    private static final int HELIS_POR_CENTRO = 1;

    // Fijados por experimentos anteriores
    private static final SuccessorFunction HC_SUCC = new SucesorHCCombinado(
            SucesorHCOperadorAislado.OP_MOVER,
            SucesorHCOperadorAislado.OP_INTERCAMBIAR
    );
    private static final HeuristicFunction H = new HeuristicaCriterio1();
    private static final GoalTest GOAL = new GoalState();

    public static void main(String[] args) throws Exception {
        int reps = 10;
        long baseSeed = 0L;
        String mode = "both";

        int groupsStart = 100, groupsStep = 50, groupsMax = 500;
        int centersStart = 5, centersStep = 5, centersMax = 30;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--reps": reps = Integer.parseInt(args[++i]); break;
                case "--seed": baseSeed = Long.parseLong(args[++i]); break;
                case "--mode": mode = args[++i]; break;
                case "--groups-start": groupsStart = Integer.parseInt(args[++i]); break;
                case "--groups-step": groupsStep = Integer.parseInt(args[++i]); break;
                case "--groups-max": groupsMax = Integer.parseInt(args[++i]); break;
                case "--centers-start": centersStart = Integer.parseInt(args[++i]); break;
                case "--centers-step": centersStep = Integer.parseInt(args[++i]); break;
                case "--centers-max": centersMax = Integer.parseInt(args[++i]); break;
                default: break;
            }
        }

        List<Integer> seeds = generateSeeds(reps, baseSeed);

        System.out.println("=== EXPERIMENTO 5: Escalado por separado (solo HC) ===");
        System.out.println("Inicialización fija: genSolTiempo");
        System.out.println("Operadores fijos: mover + intercanviar");
        System.out.println("Repeticiones por punto: " + reps + " (baseSeed=" + baseSeed + ")");
        System.out.println();

        if (mode.equals("both") || mode.equals("groups")) {
            // A) Aumentar grupos con centros fijos
            System.out.println("## A) Centros fijos (5) y grupos crecientes");
            System.out.println("centros;grupos;HC_ms;HC_coste");

            int fixedCenters = 5;
            for (int grupos = groupsStart; grupos <= groupsMax; grupos += groupsStep) {
                Stats cost = new Stats();
                TimeStats ms = new TimeStats();
                for (int seed : seeds) {
                    EstadoRescate e = new EstadoRescate(grupos, fixedCenters, HELIS_POR_CENTRO, seed);
                    e.genSolTiempo(seed);
                    Problem p = new Problem(e, HC_SUCC, GOAL, H);
                    HillClimbingSearch hc = new HillClimbingSearch();
                    long t0 = System.nanoTime();
                    hc.search(p);
                    long t1 = System.nanoTime();
                    EstadoRescate r = (EstadoRescate) hc.getGoalState();
                    cost.add(r.calcularTiempoTotal());
                    ms.addNanos(t1 - t0);
                }
                System.out.printf(Locale.US, "%d;%d;%.0f;%.2f%n",
                        fixedCenters, grupos, ms.meanMillis(), cost.mean());
            }
            System.out.println();
        }

        if (mode.equals("both") || mode.equals("centers")) {
            // B) Aumentar centros con grupos fijos
            System.out.println("## B) Grupos fijos (100) y centros crecientes");
            System.out.println("centros;grupos;HC_ms;HC_coste");

            int fixedGroups = 100;
            for (int centros = centersStart; centros <= centersMax; centros += centersStep) {
                Stats cost = new Stats();
                TimeStats ms = new TimeStats();
                for (int seed : seeds) {
                    EstadoRescate e = new EstadoRescate(fixedGroups, centros, HELIS_POR_CENTRO, seed);
                    e.genSolTiempo(seed);
                    Problem p = new Problem(e, HC_SUCC, GOAL, H);
                    HillClimbingSearch hc = new HillClimbingSearch();
                    long t0 = System.nanoTime();
                    hc.search(p);
                    long t1 = System.nanoTime();
                    EstadoRescate r = (EstadoRescate) hc.getGoalState();
                    cost.add(r.calcularTiempoTotal());
                    ms.addNanos(t1 - t0);
                }
                System.out.printf(Locale.US, "%d;%d;%.0f;%.2f%n",
                        centros, fixedGroups, ms.meanMillis(), cost.mean());
            }
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

