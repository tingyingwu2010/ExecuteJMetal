//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import myJMetal.UCB.UCB;
import myJMetal.UCB.UCB_set;
import myJMetal.UCB.WeightFunction;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;

/**
 * Class implementing the MOEA/D-DRA algorithm described in :
 * Q. Zhang,  W. Liu,  and H Li, The Performance of a New Version of
 * MOEA/D on CEC09 Unconstrained MOP Test Instances, Working Report CES-491,
 * School of CS & EE, University of Essex, 02/2009
 *
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MOEADDRAUCBHybrid extends MOEADDRA {
    private String name = "";
    private Map<String, String> parameters;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String str) {
        name = str;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }



    public MOEADDRAUCBHybrid(Problem<DoubleSolution> problem,
										int populationSize, int resultPopulationSize,
										nt maxEvaluations, MutationOperator<DoubleSolution> mutation,
										CrossoverOperator<DoubleSolution> crossover,
										FunctionType functionType, String dataDirectory,
										double neighborhoodSelectionProbability,
										int maximumNumberOfReplacedSolutions, int neighborSize) {
        super(problem, populationSize, resultPopulationSize, maxEvaluations, mutation, crossover, functionType, dataDirectory, neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions, neighborSize);
    }



  @Override
  public void run() {
    initializePopulation() ;
    initializeUniformWeight();
    initializeNeighborhood();
    initializeIdealPoint() ;

    int generation = 0 ;
    evaluations = populationSize ;

    UCB_set hh; //HyperHeuristic
    hh = new UCB_set((int)(0.5*populationSize), //Window Size
                             (maxEvaluations/6),//Init
                             0,                 //End
                             1000,              //Step
                             5.0,               //C
                             1.0);              //D

    hh.addSelector("set", new UCB(new Integer[]{1,2,3,4,5,6,7,8,9,10}, WeightFunction.Linear));

    ucb_configuration(0);//hybrid : initial configuration found with IRace

    int count_apply =0;

    //Begin Evolutionary process
    do {

      int[] permutation = new int[populationSize];

      MOEADUtils.randomPermutation(permutation, populationSize);

      for (int i = 0; i < populationSize; i++) {
        int subProblemId = permutation[i];
        frequency[subProblemId]++;

        if(hh.isWorking(evaluations, maxEvaluations)&&(!hh.isWSfull() || (evaluations-populationSize)%hh.maxStep==0)){
            hh.selectOperators();
            Integer op = (Integer)hh.getOperator("set");
            ucb_configuration(op);
            count_apply++;
        }

        NeighborType neighborType = chooseNeighborType() ;
        List<DoubleSolution> parents = parentSelection(subProblemId, neighborType, differentialEvolutionCrossover.getVariant());

        //Fitness of element to after DE execution
        hh.setX(fitnessFunction(population.get(subProblemId), lambda[subProblemId]));

        differentialEvolutionCrossover.setCurrentSolution(population.get(subProblemId));
        List<DoubleSolution> children = differentialEvolutionCrossover.execute(parents);

        DoubleSolution child = children.get(0) ;
        mutationOperator.execute(child);
        problem.evaluate(child);


        if(hh.isWorking(evaluations, maxEvaluations)&& (!hh.isWSfull() || (evaluations-populationSize)%hh.maxStep==0)){
            //Fitness of element to before DE execution
            hh.setY(fitnessFunction(child, lambda[subProblemId]));
            //Adjust the sliding window
            hh.adjustSlidingWindow();
            //Calculate the credit assignment useing a Decaying Factor
            hh.creditAssignment();
        }

        evaluations++;
        updateIdealPoint(child);
        updateNeighborhood(child, subProblemId, neighborType);


        /*
        //The MOEADDRA implements 'HistoricAlgorithm' and it has some static function
        //Warning! This method could be very heavy to process!
        //Will store data from Quality Indicators at each 100 generations
        //  to create the graphics like https://github.com/LucasLP/Multi-Objective-Algorithms-Comparison/blob/master/examples/PlotInPNGFile/PlotInPNGFile_DTLZ7.png


        if(HistoricAlgorithm.testToCalculate(evaluations,maxEvaluations)){
            HistoricAlgorithm.calculateIndicators(evaluations, maxEvaluations, problem.getName(), population,history);
        }

        /**/

      }

      generation++;
      if (generation % draTime == 0) {
        utilityFunction();
      }
    } while (evaluations < maxEvaluations);
    /**
    //HH informations
    System.out.println("=============");
    System.out.println(hh.info());
    hh.printHistory("set");
    /**/
		//Save list of operator's sequence
      ((UCB) hh.getUCB("set")).printOperatorsUsed(this.getName(),problem.getName());
  }





  private void ucb_configuration(Integer i){
      switch(i){
            case 0://HYBRID CONFIGURATION
                      //delta  nr  CR   F     variant    dratime
                setConfig(0.52, 3, 0.11, 0.65, "rand/1/bin");//300k: hybrid
                break;
            case 1://DEFAULT MOEAD CONFIGURATION
                setConfig(0.9, 2, 1.0, 0.5, "rand/1/bin");
                break;
            case 2:
                setConfig(0.95, 2, 0.4, 0.37, "rand/1/bin");
                break;
            case 3:
                setConfig(0.8, 3, 0.8, 0.7, "current-to-rand/2/bin");
                break;
            case 4:
                setConfig(1.0, 1, 0.6, 0.7, "current-to-rand/1/bin");
                break;
            case 5:
                setConfig(0.6, 2, 0.4, 0.1, "rand/1/bin");
                break;
            case 6:
                setConfig(0.95, 1, 1.0, 0.4, "rand/2/bin");
                break;
            case 7:
                setConfig(0.3, 6, 0.2, 0.8, "current-to-rand/1/bin");
                break;
            case 8:
                setConfig(0.75, 10, 0.8, 0.1, "current-to-rand/2/bin");
                break;
            case 9:
                setConfig(1.0, 2, 1.0, 0.7, "rand/1/bin");
                break;
            case 10:
                setConfig(1.0, 3, 1.0, 0.9, "rand/1/bin");
                break;
      }
  }


  private void setConfig(Double delta, Integer nr,Double Cr, Double F, String variant) {
        neighborhoodSelectionProbability = delta;
        maximumNumberOfReplacedSolutions = nr;

        differentialEvolutionCrossover.setCr(Cr);
        differentialEvolutionCrossover.setF(F);
        differentialEvolutionCrossover.setVariant(variant);
    }



    @Override
   protected List<DoubleSolution> parentSelection(int subProblemId, NeighborType neighborType, String variant) {
     List<DoubleSolution> parents = null;
     if (variant.equals("rand/1/bin") || variant.equals("rand/1/exp")
             || variant.equals("current-to-rand/1/bin")){//|| variant.equals("best/1/bin") || variant.equals("best/1/exp")) {
         List<Integer> matingPool = matingSelection(subProblemId, 3, neighborType);
         parents = new ArrayList<>(3);
         parents.add(population.get(matingPool.get(0)));
         parents.add(population.get(matingPool.get(1)));
         parents.add(population.get(matingPool.get(2)));
     }else if (variant.equals("rand/2/bin") ){
         List<Integer> matingPool = matingSelection(subProblemId, 4, neighborType);
         parents = new ArrayList<>(4);
         parents.add(population.get(matingPool.get(0)));
         parents.add(population.get(matingPool.get(1)));
         parents.add(population.get(matingPool.get(2)));
         parents.add(population.get(matingPool.get(3)));
     } else if(variant.equals("current-to-rand/2/bin")) {
         List<Integer> matingPool = matingSelection(subProblemId, 5, neighborType);
         parents = new ArrayList<>(5);
         parents.add(population.get(matingPool.get(0)));
         parents.add(population.get(matingPool.get(1)));
         parents.add(population.get(matingPool.get(2)));
         parents.add(population.get(matingPool.get(3)));
         parents.add(population.get(matingPool.get(4)));
     }
     return parents;
    }


      protected List<DoubleSolution> parentSelection_2(int subProblemId, NeighborType neighborType) {
        List<Integer> matingPool = matingSelection(subProblemId, 1, neighborType);

        List<DoubleSolution> parents = new ArrayList<>(2);

        parents.add(population.get(matingPool.get(0)));
        parents.add(population.get(subProblemId));

        return parents ;
      }
}
