package org.matsim.project.router;

import java.util.List;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.analysis.plot.Plot;


public class NSGAII {

    public static void main(String[] args) {
        // configure and run this experiment
        NondominatedPopulation result = new Executor()
                .withProblemClass(Problem.class)
                .withAlgorithm("NSGAII")
                .withMaxEvaluations(1000)
                .distributeOnAllCores()
                .run();

        new Plot()
            .add("NSGAII", result)
            .show();

        List<NondominatedPopulation> multiRuns = new Executor()
                .withProblem("ZDT1")
                .withAlgorithm("NSGAII")
                .withMaxEvaluations(1000)
                .distributeOnAllCores()
                .runSeeds(3);

        System.out.format("Obj1  Obj2%n");
        for (Solution solution : result) {
            System.out.format("%.5f\t%.5f%n", solution.getObjective(0),
                    solution.getObjective(1));
        }
    }
}