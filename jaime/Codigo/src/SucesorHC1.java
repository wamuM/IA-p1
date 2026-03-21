import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;


public class SucesorHC1 implements SuccessorFunction {

    public List getSuccessors(Object state) {
        EstadoRescate actual = (EstadoRescate) state;
        ArrayList retval = new ArrayList();

        int nGrupos = EstadoRescate.getNGrupos();
        int nHelicopteros = EstadoRescate.getNHelicopteros();

        // mover grupo a salida existente o nueva
        for (int g = 0; g < nGrupos; g++) {
            for (int h = 0; h < nHelicopteros; h++) {
                for (int s = 0; s < actual.getNSalidas(h); s++) {
                    EstadoRescate nuevo = new EstadoRescate(actual);
                    if (nuevo.moverGrupo(g, h, s)) {
                        retval.add(new Successor("mover grupo " + g + " a h=" + h + " s=" + s, nuevo));
                    }
                }

                EstadoRescate nuevo = new EstadoRescate(actual);
                if (nuevo.moverGrupo(g, h, actual.getNSalidas(h))) {
                    retval.add(new Successor("mover grupo " + g + " a h=" + h + " nueva salida", nuevo));
                }
            }
        }

        return retval;
    }
}
