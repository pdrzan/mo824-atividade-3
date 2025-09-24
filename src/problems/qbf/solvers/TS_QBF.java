package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import metaheuristics.tabusearch.AbstractTS;
import problems.qbf.QBF_Inverse;
import solutions.Solution;



/**
 * Metaheuristic TS (Tabu Search) for obtaining an optimal solution to a QBF
 * (Quadractive Binary Function -- {@link #QuadracticBinaryFunction}).
 * Since by default this TS considers minimization problems, an inverse QBF
 *  function is adopted.
 * 
 * @author ccavellucci, fusberti
 */
public class TS_QBF extends AbstractTS<Integer> {
	
	private final Integer fake = new Integer(-1);

	/**
	 * Constructor for the TS_QBF class. An inverse QBF objective function is
	 * passed as argument for the superclass constructor.
	 * 
	 * @param tenure
	 *            The Tabu tenure parameter.
	 * @param timeLimit
	 *            The number of seconds which the TS will be executed.
     * @param consecutiveBetterSolutionsToIntensification
     *            The number of consecutive better solutions to trigger intensification.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
     * @param portionCL
     *            The portion of Candidate List that will be considered in local
     *            search.
     * @param isFirstImprovement
     *            Decides if the local search will be first-improvement
     * @param isWithIntensification
     *            Decides if it will use intensification strategy
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
	public TS_QBF(Integer tenure, Integer timeLimit, Integer consecutiveBetterSolutionsToIntensification, String filename, Double portionCL, boolean isFirstImprovement, boolean isWithIntensification) throws IOException {
		super(new QBF_Inverse(filename), tenure, timeLimit, consecutiveBetterSolutionsToIntensification, portionCL, isFirstImprovement, isWithIntensification);
	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeCL()
	 */
	@Override
	public ArrayList<Integer> makeCL() {

		ArrayList<Integer> _CL = new ArrayList<Integer>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = new Integer(i);
			_CL.add(cand);
		}

		return _CL;

	}

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#makeCLPortion()
     */
    @Override
    public ArrayList<Integer> makeCLPortion() {

        Collections.shuffle(CL);
        ArrayList<Integer> CLPortion = new ArrayList<>(CL.subList(0, (int) (portionCL * CL.size())));

        return CLPortion;
    }

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeRCL()
	 */
	@Override
	public ArrayList<Integer> makeRCL() {

		ArrayList<Integer> _RCL = new ArrayList<Integer>();

		return _RCL;

	}
	
	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeTL()
	 */
	@Override
	public ArrayDeque<Integer> makeTL() {

		ArrayDeque<Integer> _TS = new ArrayDeque<Integer>(2*tenure);
		for (int i=0; i<2*tenure; i++) {
			_TS.add(fake);
		}

		return _TS;

	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#updateCL()
	 */
	@Override
	public void updateCL() {

		// do nothing

	}

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#intensification()
     */
    @Override
    public Solution<Integer> intensification() {

        return sol;

    }

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> neighborhoodMove() {

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		minDeltaCost = Double.POSITIVE_INFINITY;
		updateCL();
		// Evaluate insertions
		for (Integer candIn : CL) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
			if (!TL.contains(candIn) || sol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
		}
		// Evaluate removals
		for (Integer candOut : sol) {
			Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
			if (!TL.contains(candOut) || sol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
		}
		// Evaluate exchanges
		for (Integer candIn : CL) {
			for (Integer candOut : sol) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || sol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
		}
		// Implement the best non-tabu move
		TL.poll();
		if (bestCandOut != null) {
			sol.remove(bestCandOut);
			CL.add(bestCandOut);
			TL.add(bestCandOut);
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (bestCandIn != null) {
			sol.add(bestCandIn);
			CL.remove(bestCandIn);
			TL.add(bestCandIn);
		} else {
			TL.add(fake);
		}
		ObjFunction.evaluate(sol);
		
		return null;
	}

	/**
	 * A main method used for testing the TS metaheuristic.
	 * 
	 */
	public static void main(String[] args) {
		if (args.length < 5) {
			printUsage();
			return;
		}

		try {
			// Required parameters
			final String filename          = args[0];                // ex: instances/qbf/qbf100
			final int    tenure            = Integer.parseInt(args[1]);  // ex: 20
			final int    timeLimitSec      = Integer.parseInt(args[2]);  // ex: 1000
			final String mode              = args[3].toLowerCase();      // standard|prob_ts|intensify|prob_plus_intensify
			final boolean firstImprovement = Boolean.parseBoolean(args[4]);

			// Defaults for optional parameters
			double  portionCL             = 1.0;   // prob_ts
			int     intensEvery           = 0;
			int     intensLength          = 0;
			boolean forceBestOnIntens     = false;
			boolean useProbTS             = false;
			boolean withIntensification   = false;

			int idx = 5;

			switch (mode) {
				case "standard":
					break;

				case "prob_ts":
					if (args.length <= idx)
						throw new IllegalArgumentException("Mode prob_ts requires <portionCL>.");
					portionCL = Double.parseDouble(args[idx++]);
					if (portionCL <= 0.0 || portionCL > 1.0)
						throw new IllegalArgumentException("portionCL must be in (0,1].");
					useProbTS = true;
					break;

				case "intensify":
					if (args.length <= idx + 1)
						throw new IllegalArgumentException("Mode intensify requires <every> <length> [forceBest].");
					intensEvery  = Integer.parseInt(args[idx++]);
					intensLength = Integer.parseInt(args[idx++]);
					if (intensEvery <= 0 || intensLength <= 0)
						throw new IllegalArgumentException("<every> and <length> must be > 0.");
					if (args.length > idx) forceBestOnIntens = Boolean.parseBoolean(args[idx++]);
					withIntensification = true;
					break;

				case "prob_plus_intensify":
					if (args.length <= idx + 2)
						throw new IllegalArgumentException("Mode prob_plus_intensify requires <portionCL> <every> <length> [forceBest].");
					portionCL = Double.parseDouble(args[idx++]);
					if (portionCL <= 0.0 || portionCL > 1.0)
						throw new IllegalArgumentException("portionCL must be in (0,1].");
					intensEvery  = Integer.parseInt(args[idx++]);
					intensLength = Integer.parseInt(args[idx++]);
					if (intensEvery <= 0 || intensLength <= 0)
						throw new IllegalArgumentException("<every> and <length> must be > 0.");
					if (args.length > idx) forceBestOnIntens = Boolean.parseBoolean(args[idx++]);
					useProbTS           = true;
					withIntensification = true;
					break;

				default:
					throw new IllegalArgumentException("Unknown mode: " + mode);
			}

			long t0 = System.currentTimeMillis();

			// Intensification triggers after finding 3 consecutive better solutions (can be changed in the constructor call below)
			TS_QBF ts = new TS_QBF(tenure, timeLimitSec, 3, filename, portionCL, firstImprovement, withIntensification);
			// Solve
			Solution<Integer> bestSol = ts.solve();

			long t1 = System.currentTimeMillis();
			System.out.println("Best solution found: " + bestSol);
			System.out.printf("Time (s): %.3f%n", (t1 - t0) / 1000.0);

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			printUsage();
		}
	}

	private static void printUsage() {
		System.out.println("Uso:");
		System.out.println("  java -cp bin problems.qbf.solvers.TS_QBF <instance> <tenure> <timelimit_sec> <mode> <firstImprovement> [params...]");
		System.out.println();
		System.out.println("Modos:");
		System.out.println("  standard");
		System.out.println("  prob_ts <portionCL (0,1]>");
		System.out.println("  intensify <every> <length> [forceBest(true/false)]");
		System.out.println("  prob_plus_intensify <portionCL> <every> <length> [forceBest(true/false)]");
		System.out.println();
		System.out.println("Exemplos:");
		System.out.println("  java -cp bin problems.qbf.solvers.TS_QBF instances/qbf/qbf100 20 60 standard false");
		System.out.println("  java -cp bin problems.qbf.solvers.TS_QBF instances/qbf/qbf100 20 60 prob_ts false 0.25");
		System.out.println("  java -cp bin problems.qbf.solvers.TS_QBF instances/qbf/qbf100 20 60 intensify false 100 10 true");
		System.out.println("  java -cp bin problems.qbf.solvers.TS_QBF instances/qbf/qbf100 20 60 prob_plus_intensify false 0.25 150 15 true");
	}

}
