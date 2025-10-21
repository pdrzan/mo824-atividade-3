package problems.max_sc_qbf;

import problems.Evaluator;
import solutions.Solution;

import java.io.*;
import java.util.Arrays;
import java.util.BitSet;

/**
 * The MAX_SC_QBF problem is a variation of the MAX_QBF problem with
 * set cover. In this variation, all variables have to be covered. Each
 * variable in a set and the problem is to choose what set to use to cover
 * all the variables.
 *
 * @author ccavellucci, fusberti
 *
 */
public class MAX_SC_QBF implements Evaluator<Integer> {

	/**
	 * Dimension of the domain.
	 */
	public final Integer size;

	/**
	 * The array of numbers representing the domain.
	 */
	public final Double[] variables;

    /**
     * The array of variable subsets.
     */
    public Integer[][] S;

	/**
	 * The matrix A of coefficients for the MAX_SC_QBF f(x) = x'.A.x
	 */
	public Double[][] A;

    /**
     *  coverBits[i] = elements covered by set i
     */
    protected BitSet[] coverBits;

    /**
	 * The constructor for QuadracticBinaryFunction class. The filename of the
	 * input for setting matrix of coefficients A of the MAX_SC_QBF. The dimension of
	 * the array of variables x is returned from the {@link #readInput} method.
	 *
	 * @param filename
	 *            Name of the file containing the input for setting the QBF.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public MAX_SC_QBF(String filename) throws IOException {
		size = readInput(filename);
		variables = allocateVariables();
	}

	/**
	 * Evaluates the value of a solution by transforming it into a vector. This
	 * is required to perform the matrix multiplication which defines a QBF.
	 * 
	 * @param sol
	 *            the solution which will be evaluated.
	 */
	public void setVariables(Solution<Integer> sol) {

		resetVariables();
		if (!sol.isEmpty()) {
			for (Integer elem : sol) {
				variables[elem] = 1.0;
			}
		}

	}

    /*
     * (non-Javadoc)
     *
     * @see problems.Evaluator#isValid()
     */
    @Override
    public boolean isValid(Solution<Integer> sol) {
        return totalUncovered(sol) == 0;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#getDomainSize()
	 */
	@Override
	public Integer getDomainSize() {
		return size;
	}

	/**
	 * {@inheritDoc} In the case of a MAX_SC_QBF, the evaluation correspond to
	 * computing a matrix multiplication x'.A.x. A better way to evaluate this
	 * function when at most two variables are modified is given by methods
	 * {@link #evaluateInsertionMAXSCQBF(int)}, {@link #evaluateRemovalMAXSCQBF(int)} and
	 * {@link #evaluateExchangeMAXSCQBF(int,int)}.
	 * 
	 * @return The evaluation of the QBF.
	 */
    @Override
    public Double evaluate(Solution<Integer> sol) {
        setVariables(sol);
        return sol.cost = evaluateMAXSCQBF();
    }

	/**
	 * Evaluates a MAX_SC_QBF by calculating the matrix multiplication that defines the
	 * QBF: f(x) = x'.A.x .
	 * 
	 * @return The value of the QBF.
	 */
	public Double evaluateMAXSCQBF() {

		Double aux = (double) 0, sum = (double) 0;

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				aux += variables[j] * A[i][j];
			}
			sum += aux * variables[i];
			aux = (double) 0;
		}

		return sum;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
	 * solutions.Solution)
	 */
    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        setVariables(sol);
        double dQ = evaluateInsertionMAXSCQBF(elem);
        int newlyCovered = newlyCoveredBy(elem, sol);
        return dQ <= 0 && newlyCovered > 0 ? 1 : dQ;
    }

    /*
     * (non-Javadoc)
     *
     * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
     * solutions.Solution)
     */
    @Override
    public Double evaluateInsertionCost(Integer fistElem, Integer secondElem, Solution<Integer> sol) {
        setVariables(sol);
        double dQ = evaluateInsertionMAXSCQBF(fistElem, secondElem);
        int newlyCovered = newlyCoveredBy(fistElem, sol) + newlyCoveredBy(secondElem, sol);
        return dQ <= 0 && newlyCovered > 0 ? 1 : dQ;
    }

	/**
	 * Determines the contribution to the MAX_SC_QBF objective function from the
	 * insertion of an element.
	 * 
	 * @param i
	 *            Index of the element being inserted into the solution.
	 * @return Ihe variation of the objective function resulting from the
	 *         insertion.
	 */
	public Double evaluateInsertionMAXSCQBF(int i) {

		if (variables[i] == 1)
			return 0.0;

		return evaluateContributionMAXSCQBF(i);
	}

    /**
     * Determines the contribution to the MAX_SC_QBF objective function from the
     * insertion of two element.
     *
     * @param i
     *            Index of the first element being inserted into the solution.
     * @param j
     *            Index of the second element being inserted into the solution.
     * @return Ihe variation of the objective function resulting from the
     *         insertion.
     */
    public Double evaluateInsertionMAXSCQBF(int i, int j) {
        if (i == j)
            return evaluateInsertionMAXSCQBF(i);

        if (variables[i] == 1)
            return evaluateInsertionMAXSCQBF(j);

        if (variables[j] == 1)
            return evaluateInsertionMAXSCQBF(i);

        return evaluateContributionMAXSCQBF(i, j);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateRemovalCost(java.lang.Object,
	 * solutions.Solution)
	 */
    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        setVariables(sol);
        double dQ = evaluateRemovalMAXSCQBF(elem);
        int newlyUncovered = newlyUncoveredBy(elem, sol);
        return newlyUncovered > 0 ? Double.NEGATIVE_INFINITY : dQ;
    }

    /*
     * (non-Javadoc)
     *
     * @see problems.Evaluator#evaluateRemovalCost(java.lang.Object,
     * solutions.Solution)
     */
    @Override
    public Double evaluateRemovalCost(Integer firstElem, Integer secondElem, Solution<Integer> sol) {
        setVariables(sol);
        double dQ = evaluateRemovalMAXSCQBF(firstElem, secondElem);
        int newlyUncovered = newlyUncoveredBy(firstElem, sol) + newlyCoveredBy(secondElem, sol);
        return newlyUncovered > 0 ? Double.NEGATIVE_INFINITY : dQ;
    }

	/**
	 * Determines the contribution to the MAX_SC_QBF objective function from the
	 * removal of an element.
	 * 
	 * @param i
	 *            Index of the element being removed from the solution.
	 * @return The variation of the objective function resulting from the
	 *         removal.
	 */
	public Double evaluateRemovalMAXSCQBF(int i) {

		if (variables[i] == 0)
			return 0.0;

		return -evaluateContributionMAXSCQBF(i);
	}

    /**
     * Determines the contribution to the MAX_SC_QBF objective function from the
     * removal of two element.
     *
     * @param i
     *            Index of the first element being removed from the solution.
     * @param j
     *            Index of the second element being removed from the solution.
     * @return The variation of the objective function resulting from the
     *         removal.
     */
    public Double evaluateRemovalMAXSCQBF(int i, int j) {
        if (i == j)
            return evaluateRemovalMAXSCQBF(i);

        if (variables[i] == 0)
            return evaluateInsertionMAXSCQBF(j);

        if (variables[j] == 0)
            return evaluateInsertionMAXSCQBF(i);

        return -evaluateContributionMAXSCQBF(i, j);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateExchangeCost(java.lang.Object,
	 * java.lang.Object, solutions.Solution)
	 */
    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        setVariables(sol);
        double dQ = evaluateExchangeMAXSCQBF(elemIn, elemOut);
        int newlyCoveredIn = newlyCoveredBy(elemIn, sol);
        int newlyUncoveredOut = newlyUncoveredByConsideringExchange(elemIn, elemOut, sol);
        return newlyUncoveredOut > 0 ? Double.NEGATIVE_INFINITY : dQ <= 0 && newlyCoveredIn > 0 ? 1 : dQ;
    }

	/**
	 * Determines the contribution to the MAX_SC_QBF objective function from the
	 * exchange of two elements one belonging to the solution and the other not.
	 * 
	 * @param in
	 *            The index of the element that is considered entering the
	 *            solution.
	 * @param out
	 *            The index of the element that is considered exiting the
	 *            solution.
	 * @return The variation of the objective function resulting from the
	 *         exchange.
	 */
	public Double evaluateExchangeMAXSCQBF(int in, int out) {

		Double sum = 0.0;

		if (in == out)
			return 0.0;
		if (variables[in] == 1)
			return evaluateRemovalMAXSCQBF(out);
		if (variables[out] == 0)
			return evaluateInsertionMAXSCQBF(in);

		sum += evaluateContributionMAXSCQBF(in);
		sum -= evaluateContributionMAXSCQBF(out);
		sum -= (A[in][out] + A[out][in]);

		return sum;
	}

	/**
	 * Determines the contribution to the MAX_SC_QBF objective function from the
	 * insertion of an element. This method is faster than evaluating the whole
	 * solution, since it uses the fact that only one line and one column from
	 * matrix A needs to be evaluated when inserting a new element into the
	 * solution. This method is different from {@link #evaluateInsertionMAXSCQBF(int)},
	 * since it disregards the fact that the element might already be in the
	 * solution.
	 * 
	 * @param i
	 *            index of the element being inserted into the solution.
	 * @return the variation of the objective function resulting from the
	 *         insertion.
	 */
	private Double evaluateContributionMAXSCQBF(int i) {

		Double sum = 0.0;

		for (int j = 0; j < size; j++) {
			if (i != j)
				sum += variables[j] * (A[i][j] + A[j][i]);
		}
		sum += A[i][i];

		return sum;
	}

    /**
     * Determines the contribution to the MAX_SC_QBF objective function from the
     * insertion of two elements. This method is faster than evaluating the whole
     * solution, since it uses the fact that only one line and one column from
     * matrix A needs to be evaluated when inserting a new element into the
     * solution. This method is different from {@link #evaluateInsertionMAXSCQBF(int)},
     * since it disregards the fact that the element might already be in the
     * solution.
     *
     * @param i
     *            index of the element being inserted into the solution.
     * @return the variation of the objective function resulting from the
     *         insertion.
     */
    private Double evaluateContributionMAXSCQBF(int i, int j) {

        Double sum = 0.0;

        for (int t = 0; t < size; t++) {
            if (i != t)
                sum += variables[t] * (A[i][t] + A[t][i]);
            if (j != t)
                sum += variables[t] * (A[j][t] + A[j][i]);
        }
        sum += A[i][i];
        sum += A[j][j];

        return sum;
    }

	/**
	 * Responsible for setting the MAX_SC_QBF function parameters by reading the
	 * necessary input from an external file. this method reads the domain's
	 * dimension and matrix {@link #A}.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the black
	 *            box function.
	 * @return The dimension of the domain.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	protected Integer readInput(String filename) throws IOException {

		Reader fileInst = new BufferedReader(new FileReader(filename));
		StreamTokenizer stok = new StreamTokenizer(fileInst);

		stok.nextToken();
		int n = (int) stok.nval;
		A = new Double[n][n];
        S = new Integer[n][];

        for (int i = 0; i < n; i++) {
            stok.nextToken();
            S[i] = new Integer[(int) stok.nval];
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < S[i].length; j++) {
                stok.nextToken();
                S[i][j] = (int) stok.nval;
            }
        }

		// If instances have full matrix not only non-null values
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				stok.nextToken();
				A[i][j] = stok.nval;
				if (j>i)
					A[j][i] = 0.0;
			}
		}

        // Convert S to BitSet (each set Si has a BitSet with size dim(Si), which
        // indicates which variables it covers)
        boolean hasZero = false;
        outer:
        for (int i = 0; i < n; i++) {
            for (int v : S[i]) {
                if (v == 0) { hasZero = true; break outer; }
            }
        }
        int shift = hasZero ? 0 : -1; // if the variables are named starting in 1, shift is -1, 0 otherwise

        coverBits = new BitSet[n];
        for (int i = 0; i < n; i++) {
            BitSet bs = new BitSet(n);
            for (int v : S[i]) {
                int idx = v + shift;
                if (0 <= idx && idx < n) bs.set(idx);
            }
            coverBits[i] = bs;
        }

		return n;

	}

    /** Union of the sets covereds by indexes in sol. */
    public BitSet coveredOf(Solution<Integer> sol) {
        BitSet covered = new BitSet(size);
        for (Integer idx : sol) {
            covered.or(coverBits[idx]);
        }
        return covered;
    }

    /** Count current cover */
    public int[] coverCountOf(Solution<Integer> sol) {
        int[] cc = new int[size];
        for (Integer i : sol) {
            BitSet bs = coverBits[i];
            for (int k = bs.nextSetBit(0); k >= 0; k = bs.nextSetBit(k + 1)) cc[k]++;
        }
        return cc;
    }

    /** How many elements becomes covered when inserting elem */
    protected int newlyCoveredBy(Integer elem, Solution<Integer> sol) {
        BitSet uncovered = coveredOf(sol);
        uncovered.flip(0, size); // vira conjunto de descobertos
        BitSet bs = (BitSet) coverBits[elem].clone();
        bs.and(uncovered);
        return bs.cardinality();
    }

    /** How many elements becomes uncovered when inserting elem */
    protected int newlyUncoveredBy(Integer elem, Solution<Integer> sol) {
        int[] cc = coverCountOf(sol);
        BitSet out = coverBits[elem];
        int t = 0;
        for (int k = out.nextSetBit(0); k >= 0; k = out.nextSetBit(k + 1)) {
            if (cc[k] == 1) t++;
        }
        return t;
    }

    /** How many elements becames uncovered when exchanging out by in*/
    protected int newlyUncoveredByConsideringExchange(Integer in, Integer out, Solution<Integer> sol) {
        int[] cc = coverCountOf(sol);
        BitSet bsOut = coverBits[out], bsIn = coverBits[in];
        int t = 0;
        for (int k = bsOut.nextSetBit(0); k >= 0; k = bsOut.nextSetBit(k + 1)) {
            if (cc[k] == 1 && !bsIn.get(k)) t++;
        }
        return t;
    }

    public int totalUncovered(Solution<Integer> sol) {
        BitSet uncovered = coveredOf(sol);
        uncovered.flip(0, size);
        return uncovered.cardinality();
    }

	/**
	 * Reserving the required memory for storing the values of the domain
	 * variables.
	 * 
	 * @return a pointer to the array of domain variables.
	 */
	protected Double[] allocateVariables() {
		Double[] _variables = new Double[size];
		return _variables;
	}

	/**
	 * Reset the domain variables to their default values.
	 */
	public void resetVariables() {
		Arrays.fill(variables, 0.0);
	}

	/**
	 * Prints matrix {@link #A}.
	 */
	public void printMatrix() {

		for (int i = 0; i < size; i++) {
			for (int j = i; j < size; j++) {
				System.out.print(A[i][j] + " ");
			}
			System.out.println();
		}

	}

	/**
	 * A main method for testing the QBF class.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
        MAX_SC_QBF problem = new MAX_SC_QBF("instances/max_sc_qbf/max_sc_qbf-n_25-k_3.txt");
	}

}
