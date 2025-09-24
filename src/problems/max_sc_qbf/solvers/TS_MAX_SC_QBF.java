package problems.max_sc_qbf.solvers;

import metaheuristics.tabusearch.AbstractTS;
import problems.max_sc_qbf.MAX_SC_QBF_Inverse;
import solutions.Solution;

import java.io.IOException;
import java.util.*;


/**
 * Metaheuristic TS (Tabu Search) for obtaining an optimal solution to a QBF
 * (Quadractive Binary Function -- {@link #QuadracticBinaryFunction}).
 * Since by default this TS considers minimization problems, an inverse QBF
 *  function is adopted.
 * 
 * @author ccavellucci, fusberti
 */
public class TS_MAX_SC_QBF extends AbstractTS<Integer> {
	
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
	public TS_MAX_SC_QBF(Integer tenure, Integer timeLimit, Integer consecutiveBetterSolutionsToIntensification, String filename, Double portionCL, boolean isFirstImprovement, boolean isWithIntensification) throws IOException {
		super(new MAX_SC_QBF_Inverse(filename), tenure, timeLimit, consecutiveBetterSolutionsToIntensification, portionCL, isFirstImprovement, isWithIntensification);
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
        return new ArrayList<>(CL.subList(0, (int) (portionCL * CL.size())));
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

        Collections.shuffle(CL);

	}

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#intensification()
     */
    @Override
    public Solution<Integer> intensification() {

        Double minDeltaCost;
        Integer firstBestCandIn = null, secondBestCandIn = null;
        Integer firstBestCandOut = null, secondBestCandOut = null;

        minDeltaCost = Double.POSITIVE_INFINITY;

        // Evaluate insertions
        for (Integer firstCandIn : CL) {
            for (Integer secondCandIn : CL) {
                if (Objects.equals(firstCandIn, secondCandIn)) continue;

                Double deltaCost = ObjFunction.evaluateInsertionCost(firstCandIn, secondCandIn, sol);
                if ((!TL.contains(firstCandIn) && !TL.contains(secondCandIn)) || sol.cost + deltaCost < bestSol.cost) {
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;

                        firstBestCandIn = firstCandIn;
                        secondBestCandIn = secondCandIn;
                    }
                }
            }
        }

        for (Integer firstCandOut : sol) {
            for (Integer secondCandOut : sol) {
                if (firstCandOut.equals(secondCandOut)) continue;

                Double deltaCost = ObjFunction.evaluateRemovalCost(firstCandOut, secondCandOut, sol);
                if ((!TL.contains(firstCandOut) && !TL.contains(secondCandOut)) || sol.cost + deltaCost < bestSol.cost) {
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;

                        firstBestCandOut = firstCandOut;
                        secondBestCandOut = secondCandOut;

                        firstBestCandIn = null;
                        secondBestCandIn = null;
                    }
                }
            }
        }

        // Implement the best non-tabu move
        TL.poll();
        TL.poll();
        if (firstBestCandOut != null && secondBestCandOut != null) {
            sol.remove(firstBestCandOut);
            sol.remove(secondBestCandOut);
            CL.add(firstBestCandOut);
            CL.add(secondBestCandOut);
            TL.add(firstBestCandOut);
            TL.add(secondBestCandOut);
        } else {
            TL.add(fake);
            TL.add(fake);
        }

        TL.poll();
        TL.poll();
        if (firstBestCandIn != null && secondBestCandIn != null) {
            sol.add(firstBestCandIn);
            sol.add(secondBestCandIn);
            CL.remove(firstBestCandIn);
            CL.remove(secondBestCandIn);
            TL.add(firstBestCandIn);
            TL.add(secondBestCandIn);
        } else {
            TL.add(fake);
            TL.add(fake);
        }

        ObjFunction.evaluate(sol);

        return null;

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
	 * The local search operator developed for the MAX_SC_QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> neighborhoodMove() {

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		minDeltaCost = Double.POSITIVE_INFINITY;
		updateCL();

        ArrayList<Integer> CLPortion = makeCLPortion();

        ArrayList<Integer> insertionCandidates = new ArrayList<>();
        ArrayList<Integer> removalCandidates = new ArrayList<>();
        ArrayList<ArrayList<Integer>> exchangeCandidates = new ArrayList<>();

		// Evaluate insertions
		for (Integer candIn : CLPortion) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
			if (!TL.contains(candIn) || sol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
                    insertionCandidates.add(candIn);
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
                    removalCandidates.add(candOut);
                }
            }
        }

        // Evaluate exchanges
        for (Integer candIn : CLPortion) {
            for (Integer candOut : sol) {
                Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
                if ((!TL.contains(candIn) && !TL.contains(candOut)) || sol.cost + deltaCost < bestSol.cost) {
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = candOut;
                        ArrayList<Integer> exchangeCadidate = new ArrayList<>();
                        exchangeCadidate.add(bestCandIn);
                        exchangeCadidate.add(bestCandOut);
                        exchangeCandidates.add(exchangeCadidate);
                    }
                }
            }
        }

        if (isFirstImprovement && (!insertionCandidates.isEmpty() || !removalCandidates.isEmpty() || !exchangeCandidates.isEmpty())) {
            Random random = new Random();
            int index = random.nextInt(insertionCandidates.size() + removalCandidates.size() + exchangeCandidates.size());

            if (index < insertionCandidates.size()) {
                bestCandIn = insertionCandidates.get(index);
                bestCandOut = null;
            } else if (index < insertionCandidates.size() + removalCandidates.size()) {
                index -= insertionCandidates.size();
                bestCandIn = null;
                bestCandOut = removalCandidates.get(index);
            } else {
                index -= insertionCandidates.size() + removalCandidates.size();
                bestCandIn = exchangeCandidates.get(index).getFirst();
                bestCandOut = exchangeCandidates.get(index).getLast();
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
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();

		TS_MAX_SC_QBF tabusearch = new TS_MAX_SC_QBF(50, 10, 3, "instances/max_sc_qbf/max_sc_qbf-n_400-k_5.txt", 1., true, true);

		Solution<Integer> bestSol = tabusearch.solve();

		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;

        System.out.println("maxVal = " + bestSol);
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
	}

}
