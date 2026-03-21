import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;


public class SucesorHC2 implements SuccessorFunction {

    public List getSuccessors(Object state) {
        EstadoRescate actual = (EstadoRescate) state;
        ArrayList retval = new ArrayList();

        int nGrupos = EstadoRescate.getNGrupos();

        // intercambiar dos grupos
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
