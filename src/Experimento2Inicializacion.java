import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.SuccessorFunction;
import aima.search.informed.HillClimbingSearch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Experiment 2:
 * Determinar quina estratègia d'inicialització dona millors resultats
 * mantenint fixos:
 * - Escenari: 5 centres, 1 helicòpter/centre, 100 grups
 * - Algorisme: Hill Climbing
 * - Heurística: Criteri 1
 * - Operadors: mover + intercanviar
 *
 * Execució (Windows/MINGW):
 *   make exp2-w
 *
 * Execució (Linux):
 *   make exp2
 *
 * Arguments opcionals:
 *   --reps N      (default 30)
 *   --seed S      (default 0)
 *   --only-init {tiempo|salidas|randomP|random}
 */
public class Experimento2Inicializacion {

    private static final int N_CENTROS = 5;
    private static final int N_HELICOPTEROS_POR_CENTRO = 1;
    private static final int N_GRUPOS = 100;

    private enum InitStrategy { tiempo, salidas, randomP, random }

    public static void main(String[] args) throws Exception {
        int reps = 30;
        long baseSeed = 0L;
        InitStrategy onlyInit = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--reps":
                    reps = Integer.parseInt(args[++i]);
                    break;
                case "--seed":
                    baseSeed = Long.parseLong(args[++i]);
                    break;
                case "--only-init":
                    onlyInit = InitStrategy.valueOf(args[++i]);
                    break;
                default:
                    break;
            }
        }

        System.out.println("=== EXPERIMENT 2: Inicialització (HC + Criteri 1) ===");
        System.out.println("Escenari: centres=" + N_CENTROS
                + ", helis/centre=" + N_HELICOPTEROS_POR_CENTRO
                + ", grups=" + N_GRUPOS);
        System.out.println("Operadors fixats: mover + intercanviar");
        System.out.println("Repeticions: " + reps + " (baseSeed=" + baseSeed + ")");
        System.out.println();

        Map<String, InitStrategy> strategies = buildInitStrategies(onlyInit);
        List<Integer> seeds = generateSeeds(reps, baseSeed);

        HeuristicFunction h = new HeuristicaCriterio1();
        GoalTest goal = new GoalState();
        SuccessorFunction succ = new SucesorHCCombinado(
                SucesorHCOperadorAislado.OP_MOVER,
                SucesorHCOperadorAislado.OP_INTERCAMBIAR
        );

        System.out.println("inicialitzacio;mitjana;millor;pitjor;ms_mitja");

        for (Map.Entry<String, InitStrategy> e : strategies.entrySet()) {
            String name = e.getKey();
            InitStrategy init = e.getValue();

            Stats stats = new Stats();
            TimeStats tstats = new TimeStats();

            for (int seed : seeds) {
                EstadoRescate estado = new EstadoRescate(N_GRUPOS, N_CENTROS, N_HELICOPTEROS_POR_CENTRO, seed);
                applyInitStrategy(estado, init, seed);

                Problem p = new Problem(estado, succ, goal, h);
                HillClimbingSearch hc = new HillClimbingSearch();

                long t0 = System.nanoTime();
                hc.search(p);
                long t1 = System.nanoTime();

                EstadoRescate result = (EstadoRescate) hc.getGoalState();
                stats.add(result.calcularTiempoTotal());
                tstats.addNanos(t1 - t0);
            }

            System.out.printf(Locale.US, "%s;%.2f;%.2f;%.2f;%.0f%n",
                    name, stats.mean(), stats.min, stats.max, tstats.meanMillis());
        }

        System.out.println();
        System.out.println("Nota: millor = menor temps (criteri 1).");
    }

    private static Map<String, InitStrategy> buildInitStrategies(InitStrategy onlyInit) {
        Map<String, InitStrategy> m = new LinkedHashMap<>();

        if (onlyInit != null) {
            switch (onlyInit) {
                case tiempo:
                    m.put("genSolTiempo", InitStrategy.tiempo);
                    break;
                case salidas:
                    m.put("genSolSalidas", InitStrategy.salidas);
                    break;
                case randomP:
                    m.put("genSolRandomP", InitStrategy.randomP);
                    break;
                case random:
                    m.put("genSolRandom", InitStrategy.random);
                    break;
            }
            return m;
        }

        m.put("genSolTiempo", InitStrategy.tiempo);
        m.put("genSolSalidas", InitStrategy.salidas);
        m.put("genSolRandomP", InitStrategy.randomP);
        m.put("genSolRandom", InitStrategy.random);
        return m;
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

