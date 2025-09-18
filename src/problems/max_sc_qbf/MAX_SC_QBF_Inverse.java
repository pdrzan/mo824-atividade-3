package problems.max_sc_qbf;

import solutions.Solution;
import java.io.IOException;

public class MAX_SC_QBF_Inverse extends MAX_SC_QBF {

    public MAX_SC_QBF_Inverse(String filename) throws IOException {
        super(filename);
    }

    @Override
    public Double evaluateMAXSCQBF() {
        return -super.evaluateMAXSCQBF();
    }

    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        return -super.evaluateInsertionCost(elem, sol);
    }

    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        return -super.evaluateRemovalCost(elem, sol);
    }

    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        return -super.evaluateExchangeCost(elemIn, elemOut, sol);
    }
}
