import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generador de sucesores para Simulated Annealing.
 *
 * Para SA se genera UN ÚNICO sucesor escogiendo aleatoriamente
 * un operador y sus parámetros. Esto es esencial para que SA
 * funcione correctamente y sea eficiente.
 *
 * Operadores disponibles (configurables):
 *   OP1 - Mover un grupo a una salida existente (mismo u otro helicóptero)
 *   OP2 - Intercambiar dos grupos de distintas salidas
 *   OP3 - Mover un grupo a una nueva salida
 */
public class SucesorSA3 implements SuccessorFunction {

    public List getSuccessors(Object state) {

        ArrayList retval = new ArrayList();
        EstadoRescate actual = (EstadoRescate) state;
        Random rand = new Random();

        int nGrupos = EstadoRescate.getNGrupos();
        int nHelicopteros = EstadoRescate.getNHelicopteros();

        // Probabilidades proporcionales al número de sucesores posibles
        double totalMove = nGrupos * nHelicopteros;
        double totalSwap = nGrupos * (nGrupos - 1.0) / 2.0;
        double total = totalMove + totalSwap;
        double probMove = totalMove / total;

        EstadoRescate nuevo = new EstadoRescate(actual);

        if (rand.nextDouble() > probMove) {

            // Operador swap
            int g1 = rand.nextInt(nGrupos);
            int g2 = rand.nextInt(nGrupos);
            int count = 0;
            while (g1 == g2 || !nuevo.intercambiarGrupo(g1, g2)) {
                if (count > 2 * nGrupos * nGrupos) {
                    // demasiados intentos, cambia a mover
                    int g = rand.nextInt(nGrupos);
                    int h = rand.nextInt(nHelicopteros);
                    int s = rand.nextInt(actual.getNSalidas(h) + 1);
                    while (!nuevo.moverGrupo(g, h, s)) {
                        g = rand.nextInt(nGrupos);
                        h = rand.nextInt(nHelicopteros);
                        s = rand.nextInt(actual.getNSalidas(h) + 1);
                    }
                    retval.add(new Successor("mover grupo " + g, nuevo));
                    return retval;
                }
                g1 = rand.nextInt(nGrupos);
                g2 = rand.nextInt(nGrupos);
                count++;
            }
            retval.add(new Successor("intercambiar " + g1 + " y " + g2, nuevo));
        }
        else {
            // Operador move
            int g = rand.nextInt(nGrupos);
            int h = rand.nextInt(nHelicopteros);
            int s = rand.nextInt(actual.getNSalidas(h) + 1);
            int count = 0;
            while (!nuevo.moverGrupo(g, h, s)) {
                if (count > nGrupos * nHelicopteros) {
                    // demasiados intentos, cambia a swap
                    int g1 = rand.nextInt(nGrupos);
                    int g2 = rand.nextInt(nGrupos);
                    while (g1 == g2 || !nuevo.intercambiarGrupo(g1, g2)) {
                        g1 = rand.nextInt(nGrupos);
                        g2 = rand.nextInt(nGrupos);
                    }
                    retval.add(new Successor("intercambiar " + g1 + " y " + g2, nuevo));
                    return retval;
                }
                g = rand.nextInt(nGrupos);
                h = rand.nextInt(nHelicopteros);
                s = rand.nextInt(actual.getNSalidas(h) + 1);
                count++;
            }
            retval.add(new Successor("mover grupo " + g, nuevo));
        }
        return retval;
    }
    /*
    public List getSuccessors(Object state) {

        ArrayList retval = new ArrayList();
        EstadoRescate actual = (EstadoRescate) state;
        Random rand = new Random();

        int nGrupos = EstadoRescate.getNGrupos();
        int nHelicopteros = EstadoRescate.getNHelicopteros();
        int operador = rand.nextInt(2);

        EstadoRescate nuevo = new EstadoRescate(actual);

        if (operador == 0) {

            // Operador move
            int g, h, s;
            do {
                g = rand.nextInt(nGrupos);
                h = rand.nextInt(nHelicopteros);
                s = rand.nextInt(actual.getNSalidas(h) + 1);
            } while (!nuevo.moverGrupo(g, h, s));

            retval.add(new Successor("mover grupo " + g, nuevo));
        }
        else {
            // Operador swap
            int g1 = rand.nextInt(nGrupos);
            int g2;
            do{
              g2 = rand.nextInt(nGrupos);
             } while (g1 == g2 && !nuevo.intercambiarGrupos(g1, g2));
             retval.add(new Successor("intercambiar " + g1 + " y " + g2, nuevo));
        }
        return retval;
    }
     */
}
