import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;

public class SucesorHCOperadorAislado implements SuccessorFunction {

    public static final int OP_MOVER = 1;
    public static final int OP_INTERCAMBIAR = 2;
    public static final int OP_FUSIONAR = 3;
    public static final int OP_MOVER_CERCANO = 5;

    private final int operador;

    public SucesorHCOperadorAislado(int operador) {
        if (operador != OP_MOVER
                && operador != OP_INTERCAMBIAR
                && operador != OP_FUSIONAR
                && operador != OP_MOVER_CERCANO) {
            throw new IllegalArgumentException("Operador no valido: " + operador);
        }
        this.operador = operador;
    }

    public List getSuccessors(Object state) {
        EstadoRescate actual = (EstadoRescate) state;
        ArrayList retval = new ArrayList();

        if (operador == OP_MOVER) {
            generarSucesoresMover(actual, retval);
        }
        else if (operador == OP_INTERCAMBIAR) {
            generarSucesoresIntercambiar(actual, retval);
        }
        else if (operador == OP_FUSIONAR) {
            generarSucesoresFusionar(actual, retval);
        }
        else {
            generarSucesoresMoverCercano(actual, retval);
        }

        return retval;
    }

    private void generarSucesoresMover(EstadoRescate actual, ArrayList retval) {
        int nGrupos = EstadoRescate.getNGrupos();
        int nHelicopteros = EstadoRescate.getNHelicopteros();

        for (int g = 0; g < nGrupos; g++) {
            for (int h = 0; h < nHelicopteros; h++) {
                for (int s = 0; s < actual.getNSalidas(h); s++) {
                    EstadoRescate nuevo = new EstadoRescate(actual);
                    if (nuevo.moverGrupo(g, h, s)) {
                        retval.add(new Successor("mover grupo " + g + " a h=" + h + " s=" + s, nuevo));
                    }
                }

                EstadoRescate nuevo = new EstadoRescate(actual);
                int nuevaSalida = actual.getNSalidas(h);
                if (nuevo.moverGrupo(g, h, nuevaSalida)) {
                    retval.add(new Successor("mover grupo " + g + " a h=" + h + " nueva salida", nuevo));
                }
            }
        }
    }

    private void generarSucesoresIntercambiar(EstadoRescate actual, ArrayList retval) {
        int nGrupos = EstadoRescate.getNGrupos();

        for (int g1 = 0; g1 < nGrupos; g1++) {
            for (int g2 = g1 + 1; g2 < nGrupos; g2++) {
                EstadoRescate nuevo = new EstadoRescate(actual);
                if (nuevo.intercambiarGrupo(g1, g2)) {
                    retval.add(new Successor("intercambiar grupos " + g1 + " y " + g2, nuevo));
                }
            }
        }
    }

    private void generarSucesoresFusionar(EstadoRescate actual, ArrayList retval) {
        int nHelicopteros = EstadoRescate.getNHelicopteros();

        for (int h = 0; h < nHelicopteros; h++) {
            int nSalidas = actual.getNSalidas(h);
            for (int s1 = 0; s1 < nSalidas; s1++) {
                for (int s2 = s1 + 1; s2 < nSalidas; s2++) {
                    EstadoRescate nuevo = new EstadoRescate(actual);
                    if (nuevo.fusionarSalidas(h, s1, s2)) {
                        retval.add(new Successor("fusionar salidas h=" + h + " s1=" + s1 + " s2=" + s2, nuevo));
                    }
                }
            }
        }
    }

    private void generarSucesoresMoverCercano(EstadoRescate actual, ArrayList retval) {
        int nGrupos = EstadoRescate.getNGrupos();
        int nHelicopteros = EstadoRescate.getNHelicopteros();
        int maxSalidas = 0;
        for (int h = 0; h < nHelicopteros; h++) {
            if (actual.getNSalidas(h) > maxSalidas) {
                maxSalidas = actual.getNSalidas(h);
            }
        }

        for (int g = 0; g < nGrupos; g++) {
            for (int s = 0; s <= maxSalidas; s++) {
                EstadoRescate nuevo = new EstadoRescate(actual);
                if (nuevo.moverGrupoACercano(g, s)) {
                    retval.add(new Successor("mover cercano grupo " + g + " a salida " + s, nuevo));
                }
            }
        }
    }
}
