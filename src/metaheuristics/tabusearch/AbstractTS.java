/**
 * 
 */
package metaheuristics.tabusearch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

import problems.Evaluator;
import solutions.Solution;

/**
 * Abstract class for metaheuristic Tabu Search. It considers a minimization problem.
 * 
 * @author ccavellucci, fusberti
 * @param <E>
 *            Generic type of the candidate to enter the solution.
 */
public abstract class AbstractTS<E> {

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = true;

	/**
	 * a random number generator
	 */
	static Random rng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<E> ObjFunction;

	/**
	 * the best solution cost
	 */
	protected Double bestCost;

	/**
	 * the incumbent solution cost
	 */
	protected Double cost;

	/**
	 * the best solution
	 */
	protected Solution<E> bestSol;

	/**
	 * the incumbent solution
	 */
	protected Solution<E> sol;

	/**
	 * the tabu tenure.
	 */
	protected Integer tenure;

    /**
     * the portion of CL to be considered on local
     * search
     */
    protected Double portionCL;

    /**
     * if it uses fist-improvement strategy on search.
     * if not, uses best-improvement strategy
     */
    protected boolean isFirstImprovement;

    /**
     * if it uses an intensification strategy
     */
    protected boolean isWithIntensification;

    /**
     * time limit in second for execution
     */
    protected Integer timeLimit;

    /**
     * consecutive iterations that a better solutions was found
     */
    protected Integer consecutiveBetterSolutions;

    /**
     * how many consecutive better solutions to do an intensification
     */
    protected Integer consecutiveBetterSolutionsToIntensification;

    /**
	 * the Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> CL;

	/**
	 * the Restricted Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> RCL;
	
	/**
	 * the Tabu List of elements to enter the solution.
	 */
	protected ArrayDeque<E> TL;

	/**
	 * Creates the Candidate List, which is an ArrayList of candidate elements
	 * that can enter a solution.
	 * 
	 * @return The Candidate List.
	 */
	public abstract ArrayList<E> makeCL();

    /**
     * Gets the portion of Candidate List that will be considered in local
     * search. This is part of the probabilist Tabu Search.
     *
     * @return The portion of Candidate List to be considered
     */
    public abstract ArrayList<E> makeCLPortion();

	/**
	 * Creates the Restricted Candidate List, which is an ArrayList of the best
	 * candidate elements that can enter a solution. 
	 * 
	 * @return The Restricted Candidate List.
	 */
	public abstract ArrayList<E> makeRCL();
	
	/**
	 * Creates the Tabu List, which is an ArrayDeque of the Tabu
	 * candidate elements. The number of iterations a candidate
	 * is considered tabu is given by the Tabu Tenure {@link #tenure}
	 * 
	 * @return The Tabu List.
	 */
	public abstract ArrayDeque<E> makeTL();

	/**
	 * Updates the Candidate List according to the incumbent solution
	 * {@link #sol}. In other words, this method is responsible for
	 * updating the costs of the candidate solution elements.
	 */
	public abstract void updateCL();

	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * candidate solution element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<E> createEmptySol();

	/**
	 * The TS local search phase is responsible for repeatedly applying a
	 * neighborhood operation while the solution is getting improved, i.e.,
	 * until a local optimum is attained. When a local optimum is attained
	 * the search continues by exploring moves which can make the current 
	 * solution worse. Cycling is prevented by not allowing forbidden
	 * (tabu) moves that would otherwise backtrack to a previous solution.
	 * 
	 * @return A local optimum solution.
	 */
	public abstract Solution<E> neighborhoodMove();

    /**
     * Do an intensification phase with more moves possibilities than {@link #neighborhoodMove()}.
     * This is useful to run when we believe that we are searching a space with good potential
     * to find good solutions.
     *
     * @return A local optimum solution.
     */
    public abstract Solution<E> intensification();

	/**
	 * Constructor for the AbstractTS class.
	 * 
	 * @param objFunction
	 *            The objective function being minimized.
	 * @param tenure
	 *            The Tabu tenure parameter. 
	 * @param timeLimit
	 *            The of seconds which the TS will be executed.
     * @param consecutiveBetterSolutionsToIntensification
     *            The number of consecutive better solutions to trigger intensification.
     * @param portionCL
     *            The portion of Candidate List that will be considered in local
     *            search.
     * @param isFirstImprovement
     *            Decides if the local search will be first-improment
     * @param isWithIntensification
     *            Decides if it will use intensification strategy
	 */
	public AbstractTS(Evaluator<E> objFunction, Integer tenure, Integer timeLimit, Integer consecutiveBetterSolutionsToIntensification, Double portionCL, boolean isFirstImprovement, boolean isWithIntensification) {
		this.ObjFunction = objFunction;
		this.tenure = tenure;
        this.timeLimit = timeLimit;
        this.consecutiveBetterSolutionsToIntensification = consecutiveBetterSolutionsToIntensification;
        this.portionCL = portionCL;
        this.isFirstImprovement = isFirstImprovement;
        this.isWithIntensification = isWithIntensification;
	}

	/**
	 * The TS constructive heuristic, which is responsible for building a
	 * feasible solution by selecting in a greedy fashion, candidate
	 * elements to enter the solution.
	 * 
	 * @return A feasible solution to the problem being minimized.
	 */
	public Solution<E> constructiveHeuristic() {

		CL = makeCL();
		RCL = makeRCL();
		sol = createEmptySol();
		cost = Double.POSITIVE_INFINITY;

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {
			Double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			cost = ObjFunction.evaluate(sol);
			updateCL();

            if (CL.isEmpty()) break;

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (E c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, sol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance.
			 */
			for (E c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, sol);
				if (deltaCost <= minCost) {
					RCL.add(c);
				}
			}

			/* Choose a candidate randomly from the RCL */
			int rndIndex = rng.nextInt(RCL.size());
			E inCand = RCL.get(rndIndex);

			CL.remove(inCand);
			sol.add(inCand);

			ObjFunction.evaluate(sol);
			RCL.clear();
		}

		return sol;
	}

	/**
	 * The TS mainframe. It consists of a constructive heuristic followed by
	 * a loop, in which each iteration a neighborhood move is performed on
	 * the current solution. The best solution is returned as result.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<E> solve() {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();

		bestSol = createEmptySol();
		constructiveHeuristic();
		TL = makeTL();
        int iteration = 0;
        consecutiveBetterSolutions = 0;

		while (endTime - startTime <= timeLimit * 1000) {
			neighborhoodMove();
            if (isWithIntensification && consecutiveBetterSolutions >= consecutiveBetterSolutionsToIntensification) {
                intensification();
            }

			if (bestSol.cost > sol.cost) {
                consecutiveBetterSolutions++;
				bestSol = new Solution<E>(sol);
				if (verbose)
					System.out.println("(Iter. " + iteration + ") BestSol = " + bestSol);
			} else {
                consecutiveBetterSolutions = 0;
            }

            endTime = System.currentTimeMillis();
            iteration++;
		}

		return bestSol;
	}

	/**
	 * A standard stopping criteria for the constructive heuristic is to repeat
	 * until the incumbent solution improves by inserting a new candidate
	 * element.
	 * 
	 * @return true if the criteria is met.
	 */
	public Boolean constructiveStopCriteria() {
		return cost < sol.cost && ObjFunction.isValid(sol);
	}

}
