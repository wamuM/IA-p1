import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.SuccessorFunction;
import aima.search.informed.HillClimbingSearch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Experiment 1 (Pràctica de cerca local):
 * Determinar quin conjunt d'operadors dona millors resultats per la heurística del criteri 1,
 * amb escenari fix: 5 centres, 1 helicòpter per centre, 100 grups. Algorisme: Hill Climbing.
 *
 * Execució (Windows / MINGW):
 *   make exp1-w
 *
 * Execució (Linux):
 *   make exp1
 *
 * Arguments opcionals:
 *   --reps N              (default 30)
 *   --seed S              (default 0)  seed base per generar seeds dels experiments
 *   --init {tiempo|salidas|randomP|random} (default tiempo)
 */
public class Experimento1Operadores {

    // Escenari fix del punt 1
    private static final int N_CENTROS = 5;
    private static final int N_HELICOPTEROS_POR_CENTRO = 1;
    private static final int N_GRUPOS = 100;

    private enum InitStrategy { tiempo, salidas, randomP, random }

    public static void main(String[] args) throws Exception {
        int reps = 30;
        long baseSeed = 0L;
        InitStrategy init = InitStrategy.tiempo;
        boolean includeIsolated = false;

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
                case "--isolated":
                    includeIsolated = true;
                    break;
                default:
                    // ignore unknown flags to keep it simple
                    break;
            }
        }

        System.out.println("=== EXPERIMENT 1: Operadors (HC + Criteri 1) ===");
        System.out.println("Escenari: centres=" + N_CENTROS
                + ", helis/centre=" + N_HELICOPTEROS_POR_CENTRO
                + ", grups=" + N_GRUPOS);
        System.out.println("Inicialització: " + init);
        System.out.println("Repeticions: " + reps + " (baseSeed=" + baseSeed + ")");
        System.out.println();

        HeuristicFunction h = new HeuristicaCriterio1();
        GoalTest goal = new GoalState(); // sempre false (búsqueda local)

        List<Integer> seeds = generateSeeds(reps, baseSeed);

        // 1) SINGLES
        System.out.println("## Operadors individuals (singles)");
        runTable(buildSingles(), seeds, init, goal, h);
        System.out.println();

        // 2) PARELLES
        System.out.println("## Parelles d'operadors (combinacions de 2)");
        runTable(buildPairs(), seeds, init, goal, h);

        System.out.println();
        System.out.println("Nota: millor = menor temps (criteri 1).");
        System.out.println("Si vols més estabilitat, puja --reps (p.ex. 50 o 100).");
    }

    private static Map<String, SuccessorFunction> buildSingles() {
        Map<String, SuccessorFunction> m = new LinkedHashMap<>();
        m.put("moure", new SucesorHCCombinado(SucesorHCOperadorAislado.OP_MOVER));
        m.put("intercanviar", new SucesorHCCombinado(SucesorHCOperadorAislado.OP_INTERCAMBIAR));
        m.put("fusionar", new SucesorHCCombinado(SucesorHCOperadorAislado.OP_FUSIONAR));
        m.put("moure_cercano", new SucesorHCCombinado(SucesorHCOperadorAislado.OP_MOVER_CERCANO));
        return m;
    }

    private static Map<String, SuccessorFunction> buildPairs() {
        Map<String, SuccessorFunction> m = new LinkedHashMap<>();
        int[] ops = new int[] {
                SucesorHCOperadorAislado.OP_MOVER,
                SucesorHCOperadorAislado.OP_INTERCAMBIAR,
                SucesorHCOperadorAislado.OP_FUSIONAR,
                SucesorHCOperadorAislado.OP_MOVER_CERCANO
        };
        String[] names = new String[] { "moure", "intercanviar", "fusionar", "moure_cercano" };

        for (int i = 0; i < ops.length; i++) {
            for (int j = i + 1; j < ops.length; j++) {
                m.put(names[i] + " + " + names[j], new SucesorHCCombinado(ops[i], ops[j]));
            }
        }
        return m;
    }

    private static void runTable(Map<String, SuccessorFunction> operatorSets,
                                 List<Integer> seeds,
                                 InitStrategy init,
                                 GoalTest goal,
                                 HeuristicFunction h) throws Exception {

        System.out.printf("%-28s  %10s  %10s  %10s  %10s%n",
                "Operadors", "mitjana", "millor", "pitjor", "ms_mitja");
        System.out.printf("%-28s  %10s  %10s  %10s  %10s%n",
                "----------------------------", "----------", "----------", "----------", "----------");

        for (Map.Entry<String, SuccessorFunction> entry : operatorSets.entrySet()) {
            String name = entry.getKey();
            SuccessorFunction succ = entry.getValue();

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
                double score = result.calcularTiempoTotal();

                stats.add(score);
                tstats.addNanos(t1 - t0);
            }

            System.out.printf("%-28s  %10.2f  %10.2f  %10.2f  %10.0f%n",
                    name, stats.mean(), stats.min, stats.max, tstats.meanMillis());
        }
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
        // Fem seeds “pseudoaleatòries” però reproduïbles
        Random r = new Random(baseSeed);
        ArrayList<Integer> out = new ArrayList<>(reps);
        for (int i = 0; i < reps; i++) {
            out.add(r.nextInt(Integer.MAX_VALUE));
        }
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

