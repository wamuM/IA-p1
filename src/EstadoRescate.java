import IA.Desastres.Grupos;
import IA.Desastres.Grupo;
import IA.Desastres.Centros;
import IA.Desastres.Centro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

/**
 * Representa el estado del problema de rescate con helicópteros.
 * REPRESENTACIÓN DEL ESTADO:
 - asignacion[g] = índice del grupo asigna helicoptero
 - salida[g] = índice del grupo asignarle la salida del helicoptero
 - nSalida[s] = índice helicoptero asignarle número de salidas
 */
public class EstadoRescate {

    // Constantes del problema
    public static final int MAX_PERSONAS_HELICOPTERO = 15;
    public static final int MAX_GRUPOS_POR_SALIDA = 3;
    public static final double VELOCIDAD_KMH = 100.0;
    public static final int TIEMPO_RECARGA_MIN = 10;
    public static final int TIEMPO_POR_PERSONA_MIN = 1;

    // Atributos
    private static Grupos grupos;
    private static Centros centros;
    private static int nGrupos;
    private static int nCentros;
    private static int nHelicopteros;
    private static int nHelicopterosPorCentro;

    // Representación de estado
    private int[] asignacion;
    private int[] salidas;
    private int[] nSalidas;

    // -----------------------------------------------------------------------
    // Constructores
    // -----------------------------------------------------------------------

    public EstadoRescate(int nGrupos, int nCentros, int nHelicopteros, int seed) {
        this.asignacion = new int[nGrupos];
        this.salidas = new int[nGrupos];
        for(int i = 0; i < nGrupos; ++i) {
            asignacion[i] = -1;
            salidas[i] = -1;
        }

        grupos = new Grupos(nGrupos,seed);
        centros = new Centros(nCentros,nHelicopteros, seed);

        this.nGrupos = nGrupos;
        this.nCentros = nCentros;
        this.nHelicopterosPorCentro = nHelicopteros;
        this.nHelicopteros = nCentros * nHelicopteros;
        this.nSalidas = new int[this.nHelicopteros];

        for(int i = 0; i < nHelicopteros; ++i) {
            nSalidas[i] = 0;
        }

    }

    public EstadoRescate(EstadoRescate x) {
        this.asignacion = x.asignacion.clone();
        this.salidas = x.salidas.clone();
        this.nSalidas = x.nSalidas.clone();
    }

    //Funciones extra
    private int personasEnSalida(int h, int s) {
        int total = 0;
        for(int g = 0; g < nGrupos; ++g) {
            if(asignacion[g] == h && salidas[g] == s) total += grupos.get(g).getNPersonas();
        }
        return total;
    }

    private int gruposEnSalida(int h, int s) {
        int total = 0;
        for(int g = 0; g < nGrupos; ++g) {
            if(asignacion[g] == h && salidas[g] == s) ++total;
        }
        return total;
    }

    private static double distancia(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private int mejorHelicoptero(int g) {
        double minDist = Double.MAX_VALUE;
        int maxCentro = 0;
        for(int i = 0; i < nCentros; ++i) {
            double dist = distancia(grupos.get(g).getCoordX(), grupos.get(g).getCoordY(), centros.get(i).getCoordX(), centros.get(i).getCoordY());
            if(dist < minDist) {
                minDist = dist;
                maxCentro = i;
            }
        }

        int maxH = 0;
        for(int h = maxCentro * nHelicopterosPorCentro; h <(maxCentro + 1) * nHelicopterosPorCentro; ++h) {
            if(nSalidas[h] < nSalidas[maxH]) maxH = h;
        }
        return maxH;
    }

    // Getters
    public static int getNHelicopteros() { return nHelicopteros; }
    public int getNHelicopterosCentro() { return nHelicopterosPorCentro; }
    public static int getNGrupos() { return nGrupos; }
    public int getNCentros() { return nCentros; }
    public Grupos getGrupos() { return grupos; }
    public Centros getCentros() { return centros; }

    public int getGruposEnSalida(int h, int s) { return gruposEnSalida(h,s); }
    public int getPersonasEnSalida(int h, int s) { return personasEnSalida(h,s); }

    public int getNSalidas(int h) { return nSalidas[h]; }
    public int getSalida(int g) { return salidas[g]; }
    public int getHelicoptero(int g) { return asignacion[g]; }


    //Generar soluciones iniciales

    //Generar solución random
    public void genSolRandom(int seed) {
        Random random = new Random(seed);

        for(int g = 0; g < nGrupos; ++g) {
            boolean asignado = false;
            while(!asignado) {
                int h = random.nextInt(nHelicopteros);
                if(nSalidas[h] > 0) {
                    int s = random.nextInt(nSalidas[h]);
                    if(gruposEnSalida(h, s) < MAX_GRUPOS_POR_SALIDA && personasEnSalida(h, s) + grupos.get(g).getNPersonas() <= MAX_PERSONAS_HELICOPTERO) {
                        asignado = true;
                        asignacion[g] = h;
                        salidas[g] = s;
                    }
                }
                else {
                    asignado = true;
                    asignacion[g] = h;
                    salidas[g] = 0;
                    nSalidas[h]++;
                }
            }
        }
    }

    //Generar solución random dando prioridad al grupo 1
    public void genSolRandomPrioridad(int seed) {
        Random random = new Random(seed);

        List<Integer> grupo1 = new ArrayList<>();
        List<Integer> grupo2 = new ArrayList<>();

        for(int g = 0; g < nGrupos; ++g) {
            if (grupos.get(g).getPrioridad() == 1) grupo1.add(g);
            else grupo2.add(g);
        }

        Collections.shuffle(grupo1, random);
        Collections.shuffle(grupo2, random);

        List<Integer> todosGrupos = new ArrayList<>();
        todosGrupos .addAll(grupo1);
        todosGrupos .addAll(grupo2);

        for(int g: todosGrupos ) {
            boolean asignado = false;
            while(!asignado) {
                int h = random.nextInt(nHelicopteros);
                if(nSalidas[h] > 0) {
                    int s = random.nextInt(nSalidas[h]);
                    if(gruposEnSalida(h, s) < MAX_GRUPOS_POR_SALIDA && personasEnSalida(h, s) + grupos.get(g).getNPersonas() <= MAX_PERSONAS_HELICOPTERO) {
                        asignado = true;
                        asignacion[g] = h;
                        salidas[g] = s;
                    }
                }
                else {
                    asignado = true;
                    asignacion[g] = h;
                    salidas[g] = 0;
                    nSalidas[h]++;
                }
            }
        }
    }

    //Generar solución random minimizando las salidas
    public void genSolSalidas(int seed) {
        Random random = new Random(seed);

        List<Integer> grupo1 = new ArrayList<>();
        List<Integer> grupo2 = new ArrayList<>();

        for(int g = 0; g < nGrupos; ++g) {
            if (grupos.get(g).getPrioridad() == 1) grupo1.add(g);
            else grupo2.add(g);
        }

        Collections.shuffle(grupo1, random);
        Collections.shuffle(grupo2, random);

        List<Integer> todosGrupos = new ArrayList<>();
        todosGrupos .addAll(grupo1);
        todosGrupos .addAll(grupo2);

        for(int g: todosGrupos) {
            int h = 0;
            for(int i = 0; i < nHelicopteros - 1;++i) {
                if(nSalidas[i] < nSalidas[h]) h = i;
            }

            boolean asignado = false;
            for(int s = 0; s < nSalidas[h] && !asignado; s++) {
                if (gruposEnSalida(h, s) < MAX_GRUPOS_POR_SALIDA && personasEnSalida(h, s) + grupos.get(g).getNPersonas() <= MAX_PERSONAS_HELICOPTERO) {
                    asignado = true;
                    asignacion[g] = h;
                    salidas[g] = s;
                }
            }

            if(!asignado) {
                asignacion[g] = h;
                salidas[g] = nSalidas[h];
                nSalidas[h]++;
            }
        }
    }

    //Generar solución random minimizando el tiempo
    public void genSolTiempo(int seed) {
        Random random = new Random(seed);

        List<Integer> grupo1 = new ArrayList<>();
        List<Integer> grupo2 = new ArrayList<>();

        for(int g = 0; g < nGrupos; ++g) {
            if (grupos.get(g).getPrioridad() == 1) grupo1.add(g);
            else grupo2.add(g);
        }

        List<Integer> todosGrupos = new ArrayList<>();
        todosGrupos .addAll(grupo1);
        todosGrupos .addAll(grupo2);

        for(int g: todosGrupos) {
            int h = mejorHelicoptero(g);

            boolean asignado = false;
            for(int s = 0; s < nSalidas[h] && !asignado; s++) {
                if (gruposEnSalida(h, s) < MAX_GRUPOS_POR_SALIDA && personasEnSalida(h, s) + grupos.get(g).getNPersonas() <= MAX_PERSONAS_HELICOPTERO) {
                    asignado = true;
                    asignacion[g] = h;
                    salidas[g] = s;
                }
            }

            if(!asignado) {
                asignacion[g] = h;
                salidas[g] = nSalidas[h];
                nSalidas[h]++;
            }
        }
    }

    // // OPERADORES DE BÚSQUEDA

    //Mover todos los viajes a una salida anterior si la salida queda vacía
    private void reajustarAsignacionAnterior(int h, int s) {
        if (gruposEnSalida(h, s) == 0) {
            for (int g = 0; g < nGrupos; g++) {
                if (asignacion[g] == h && salidas[g] > s) {
                    salidas[g]--;
                }
            }
            nSalidas[h]--;
        }
    }


    //Mover grupo a un helicóptero con una salida
    public boolean moverGrupo(int g, int nuevoH, int nuevaS) {
        if (asignacion[g] == nuevoH && salidas[g] == nuevaS) return false;

        if(nuevaS < nSalidas[nuevoH]) {
            if (gruposEnSalida(nuevoH, nuevaS) >= MAX_GRUPOS_POR_SALIDA) return false;
            if (personasEnSalida(nuevoH, nuevaS) + grupos.get(g).getNPersonas() > MAX_PERSONAS_HELICOPTERO) return false;
        }
        else if(nuevaS == nSalidas[nuevoH]) {
            nSalidas[nuevoH]++;
        }
        else return false;


        int viejoH = asignacion[g];
        int viejaS = salidas[g];
        asignacion[g] = nuevoH;
        salidas[g] = nuevaS;

        //if (viejoH == nuevoH && viejaS < nuevaS) salidas[g]--;

        reajustarAsignacionAnterior(viejoH, viejaS);

        return true;
    }

    //Intercambiar grupos
    public boolean intercambiarGrupo(int g1, int g2) {
        int h1 = asignacion[g1];
        int s1 = salidas[g1];
        int h2 = asignacion[g2];
        int s2 = salidas[g2];

        int personasS1SinG1 = personasEnSalida(h1,s1) - grupos.get(g1).getNPersonas();
        int personasS2SinG2 = personasEnSalida(h2,s2) - grupos.get(g2).getNPersonas();

        if (personasS1SinG1 + grupos.get(g2).getNPersonas() > MAX_PERSONAS_HELICOPTERO)
            return false;
        if (personasS2SinG2 + grupos.get(g1).getNPersonas() > MAX_PERSONAS_HELICOPTERO)
            return false;

        asignacion[g1] = h2;
        salidas[g1] = s2;
        asignacion[g2] = h1;
        salidas[g2] = s1;
        return true;
    }



    // -----------------------------------------------------------------------
    // Cálculo de tiempos
    // -----------------------------------------------------------------------

    /**
     * Calcula el tiempo total de una salida (desde el centro hasta recoger todos
     * los grupos y volver al centro).
     * Centro asignado al helicóptero h: centros[h / nHelicopterosPorCentro]
     */
    private double calcularTiempoSalida(int h, int s) {

        int[] gruposSalida = new int[nGrupos];
        int nGruposSalida = 0;
        for (int g = 0; g < nGrupos; g++) {
            if (asignacion[g] == h && salidas[g] == s) {
                gruposSalida[nGruposSalida++] = g;
            }
        }

        // Centro del helicóptero
        int centro = h / nHelicopterosPorCentro;
        double cx = centros.get(centro).getCoordX();
        double cy = centros.get(centro).getCoordY();

        // Calcular distancia mínima según número de grupos
        double minDistancia = 0.0;

        if (nGruposSalida == 1) {
            int g0 = gruposSalida[0];
            minDistancia = distancia(cx, cy, grupos.get(g0).getCoordX(), grupos.get(g0).getCoordY()) * 2;

        }
        else if (nGruposSalida == 2) {
            int g0 = gruposSalida[0], g1 = gruposSalida[1];
            double d1 = distancia(cx, cy, grupos.get(g0).getCoordX(), grupos.get(g0).getCoordY())
                    + distancia(grupos.get(g0).getCoordX(), grupos.get(g0).getCoordY(), grupos.get(g1).getCoordX(), grupos.get(g1).getCoordY())
                    + distancia(grupos.get(g1).getCoordX(), grupos.get(g1).getCoordY(), cx, cy);
            double d2 = distancia(cx, cy, grupos.get(g1).getCoordX(), grupos.get(g1).getCoordY())
                    + distancia(grupos.get(g1).getCoordX(), grupos.get(g1).getCoordY(), grupos.get(g0).getCoordX(), grupos.get(g0).getCoordY())
                    + distancia(grupos.get(g0).getCoordX(), grupos.get(g0).getCoordY(), cx, cy);
            minDistancia = Math.min(d1, d2);

        }
        else if (nGruposSalida == 3) {
            int g0 = gruposSalida[0], g1 = gruposSalida[1], g2 = gruposSalida[2];
            int[][] perms = {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
            minDistancia = Double.MAX_VALUE;
            for (int[] perm : perms) {
                int ga = gruposSalida[perm[0]], gb = gruposSalida[perm[1]], gc = gruposSalida[perm[2]];
                double d = distancia(cx, cy, grupos.get(ga).getCoordX(), grupos.get(ga).getCoordY())
                        + distancia(grupos.get(ga).getCoordX(), grupos.get(ga).getCoordY(), grupos.get(gb).getCoordX(), grupos.get(gb).getCoordY())
                        + distancia(grupos.get(gb).getCoordX(), grupos.get(gb).getCoordY(), grupos.get(gc).getCoordX(), grupos.get(gc).getCoordY())
                        + distancia(grupos.get(gc).getCoordX(), grupos.get(gc).getCoordY(), cx, cy);
                if (d < minDistancia) minDistancia = d;
            }
        }

        // Tiempo de recogida
        double tiempoRecogida = 0.0;
        for (int i = 0; i < nGruposSalida; i++) {
            int g = gruposSalida[i];
            tiempoRecogida += grupos.get(g).getNPersonas() * TIEMPO_POR_PERSONA_MIN * (grupos.get(g).getPrioridad() == 1 ? 2 : 1);
        }

        // Tiempo vuelo + recogida + recarga
        return (minDistancia / VELOCIDAD_KMH) * 60.0 + tiempoRecogida + TIEMPO_RECARGA_MIN;
    }

    /**
     * Calcula el tiempo total acumulado de un helicóptero (suma de todas sus salidas).
     */
    public double calcularTiempoHelicoptero(int h) {
        double total = 0.0;
        for (int s = 0; s < nSalidas[h]; s++) {
            total += calcularTiempoSalida(h, s);
        }
        return total - TIEMPO_RECARGA_MIN;
    }

    /**
     * Heurística 1: Calcula el tiempo total de todos los helicópteros.
     */
    public double calcularTiempoTotal() {
        double total = 0.0;
        for (int h = 0; h < nHelicopteros; h++) {
            total += calcularTiempoHelicoptero(h);
        }
        return total;
    }

    /**
     * Calcula el tiempo máximo hasta que el último grupo de prioridad 1 es rescatado.
     * Para cada helicóptero, buscamos cuándo termina la última salida que contiene un grupo P1.
     */
    public double calcularTiempoPrioridad1() {
        double maxTiempo = 0.0;
        for (int h = 0; h < nHelicopteros; h++) {
            double tiempoAcum = 0.0;
            for (int s = 0; s < nSalidas[h]; s++) {
                tiempoAcum += calcularTiempoSalida(h, s);
                if (salidaContieneP1(h, s)) {
                    if (tiempoAcum > maxTiempo) maxTiempo = tiempoAcum - TIEMPO_RECARGA_MIN;
                }
            }
        }
        return maxTiempo;
    }

    private boolean salidaContieneP1(int h, int s) {
        for (int g = 0; g < nGrupos; g++) {
            if (asignacion[g] == h && salidas[g] == s && grupos.get(g).getPrioridad() == 1)
                return true;
        }
        return false;
    }

    //Estado final
    public boolean is_goal(){
        return false;
    }
}


