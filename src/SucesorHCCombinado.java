import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * SuccessorFunction que combina 1 o 2 operadors (unió de successors).
 * Pensat per a l'Experiment 1 (singles i parelles).
 */
public class SucesorHCCombinado implements SuccessorFunction {

    private final int op1;
    private final int op2;

    public SucesorHCCombinado(int op1) {
        this(op1, -1);
    }

    public SucesorHCCombinado(int op1, int op2) {
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public List getSuccessors(Object state) {
        ArrayList out = new ArrayList();
        out.addAll(new SucesorHCOperadorAislado(op1).getSuccessors(state));
        if (op2 != -1) {
            out.addAll(new SucesorHCOperadorAislado(op2).getSuccessors(state));
        }
        return out;
    }
}

