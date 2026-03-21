import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;


public class SucesorHC3 implements SuccessorFunction {

    public List getSuccessors(Object state) {
        EstadoRescate actual = (EstadoRescate) state;
        ArrayList retval = new ArrayList();

        int nGrupos = EstadoRescate.getNGrupos();
        int nHelicopteros = EstadoRescate.getNHelicopteros();

        // Operador 1: mover grupo a salida existente o nueva
        for (int g = 0; g < nGrupos; g++) {
            for (int h = 0; h < nHelicopteros; h++) {
                for (int s = 0; s < actual.getNSalidas(h); s++) {
                    EstadoRescate nuevo = new EstadoRescate(actual);
                    if (nuevo.moverGrupo(g, h, s)) {
                        retval.add(new Successor("mover grupo " + g + " a h=" + h + " s=" + s, nuevo));
                    }
                }

            }
        }

        // Operador 2: intercambiar dos grupos
        for (int g1 = 0; g1 < nGrupos; g1++) {
            for (int g2 = g1 + 1; g2 < nGrupos; g2++) {
                EstadoRescate nuevo = new EstadoRescate(actual);
                if (nuevo.intercambiarGrupo(g1, g2)) {
                    retval.add(new Successor("intercambiar grupos " + g1 + " y " + g2, nuevo));
                }
            }
        }

        return retval;
    }
}
