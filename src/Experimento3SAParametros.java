import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.SuccessorFunction;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Experimento 3:
 * Buscar los mejores parámetros de Simulated Annealing manteniendo fijo:
 * - Escenario: 5 centros, 1 helicóptero/centro, 100 grupos
 * - Heurística: criterio 1
 * - Operadores: los fijados (por defecto SA3 = mover + intercambiar probabilístico)
 * - Estrategia inicial: configurable por --init
 *
 * Uso (Windows/MINGW):
 *   make exp3-w
 *   java -cp "lib/*;bin" Experimento3SAParametros --reps 10 --seed 0 --init tiempo --sa 3
 */
public class Experimento3SAParametros {

    private static final int N_CENTROS = 5;
    private static final int N_HELICOPTEROS_POR_CENTRO = 1;
    private static final int N_GRUPOS = 100;

    private enum InitStrategy { tiempo, salidas, randomP, random }

    private static final int[] STEPS_GRID = {1000, 3000, 6000};
    private static final int[] STITER_GRID = {50, 100, 200};
    private static final int[] K_GRID = {20, 125, 500};
    private static final double[] LAMBDA_GRID = {0.001, 0.0001, 0.00001};

    public static void main(String[] args) throws Exception {
        int reps = 10;          // enunciado: mínimo 10
        long baseSeed = 0L;
        InitStrategy init = InitStrategy.tiempo; // placeholder; cámbialo al cerrar exp2
        int saVariant = 3;      // 1,2,3 (por defecto 3)

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--reps":
                    reps = Integer.parseInt(args[++i]);
                    break;
                case "--seed":
                    baseSeed = Long.parseLong(args[++i]);
                    break;
                case "--init":
                    init = InitStrategy.valueOf(args[++i]);
                    break;
                case "--sa":
                    saVariant = Integer.parseInt(args[++i]);
                    break;
                default:
                    break;
            }
        }

        List<Integer> seeds = generateSeeds(reps, baseSeed);
        HeuristicFunction h = new HeuristicaCriterio1();
        GoalTest goal = new GoalState();
        SuccessorFunction succ = pickSASuccessor(saVariant);

        System.out.println("=== EXPERIMENTO 3: Ajuste de parámetros SA ===");
        System.out.println("Escenario: centros=" + N_CENTROS + ", helis/centro=" + N_HELICOPTEROS_POR_CENTRO + ", grupos=" + N_GRUPOS);
        System.out.println("Heurística: criterio 1");
        System.out.println("Operadores SA: SucesorSA" + saVariant);
        System.out.println("Inicialización: " + init + " (ajusta al ganador del experimento 2)");
        System.out.println("Repeticiones: " + reps + " (baseSeed=" + baseSeed + ")");
        System.out.println();

        ArrayList<Row> rows = new ArrayList<>();

        for (int steps : STEPS_GRID) {
            for (int stiter : STITER_GRID) {
                for (int k : K_GRID) {
                    for (double lambda : LAMBDA_GRID) {
                        Stats scoreStats = new Stats();
                        TimeStats timeStats = new TimeStats();

                        for (int seed : seeds) {
                            EstadoRescate estado = new EstadoRescate(N_GRUPOS, N_CENTROS, N_HELICOPTEROS_POR_CENTRO, seed);
                            applyInitStrategy(estado, init, seed);

                            Problem p = new Problem(estado, succ, goal, h);
                            SimulatedAnnealingSearch sa = new SimulatedAnnealingSearch(steps, stiter, k, lambda);

                            long t0 = System.nanoTime();
                            sa.search(p);
                            long t1 = System.nanoTime();

                            EstadoRescate result = (EstadoRescate) sa.getGoalState();
                            double score = result.calcularTiempoTotal();

                            scoreStats.add(score);
                            timeStats.addNanos(t1 - t0);
                        }

                        rows.add(new Row(
                                steps, stiter, k, lambda,
                                scoreStats.mean(), scoreStats.min, scoreStats.max,
                                timeStats.meanMillis()
                        ));
                    }
                }
            }
        }

        rows.sort(Comparator.comparingDouble(r -> r.mean));

        System.out.println("steps;stiter;k;lambda;mitjana;millor;pitjor;ms_mitja");

        for (Row r : rows) {
            System.out.printf(Locale.US, "%d;%d;%d;%.6f;%.2f;%.2f;%.2f;%.0f%n",
                    r.steps, r.stiter, r.k, r.lambda, r.mean, r.best, r.worst, r.meanMs);
        }

        System.out.println();
        System.out.println("Top 3 (menor mitjana):");
        for (int i = 0; i < Math.min(3, rows.size()); i++) {
            Row r = rows.get(i);
            System.out.printf(Locale.US, "%d) steps=%d stiter=%d k=%d lambda=%.6f | mitjana=%.2f ms=%.0f%n",
                    i + 1, r.steps, r.stiter, r.k, r.lambda, r.mean, r.meanMs);
        }
    }

    private static SuccessorFunction pickSASuccessor(int v) {
        if (v == 1) return new SucesorSA1();
        if (v == 2) return new SucesorSA2();
        return new SucesorSA3();
    }

    private static void applyInitStrategy(EstadoRescate e, InitStrategy init, int seed) {
        switch (init) {
            case tiempo:
                e.genSolTiempo(seed);
                break;
            case salidas:
                e.genSolSalidas(seed);
                break;
            case randomP:
                e.genSolRandomPrioridad(seed);
                break;
            case random:
                e.genSolRandom(seed);
                break;
        }
    }

    private static List<Integer> generateSeeds(int reps, long baseSeed) {
        Random r = new Random(baseSeed);
        ArrayList<Integer> out = new ArrayList<>(reps);
        for (int i = 0; i < reps; i++) out.add(r.nextInt(Integer.MAX_VALUE));
        return out;
    }

    private static final class Row {
        int steps;
        int stiter;
        int k;
        double lambda;
        double mean;
        double best;
        double worst;
        double meanMs;

        Row(int steps, int stiter, int k, double lambda, double mean, double best, double worst, double meanMs) {
            this.steps = steps;
            this.stiter = stiter;
            this.k = k;
            this.lambda = lambda;
            this.mean = mean;
            this.best = best;
            this.worst = worst;
            this.meanMs = meanMs;
        }
    }

    private static final class Stats {
        double sum = 0.0;
        int n = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        void add(double x) {
            sum += x;
            n++;
            if (x < min) min = x;
            if (x > max) max = x;
        }

        double mean() {
            return n == 0 ? Double.NaN : sum / n;
        }
    }

    private static final class TimeStats {
        long sumNanos = 0L;
        int n = 0;

        void addNanos(long nanos) {
            sumNanos += nanos;
            n++;
        }

        double meanMillis() {
            return n == 0 ? Double.NaN : (sumNanos / 1_000_000.0) / n;
        }
    }
}

