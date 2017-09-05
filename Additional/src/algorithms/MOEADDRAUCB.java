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
import myJMetal.UCB.UCBWeighted;
import myJMetal.UCB.UCB_set;

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
public class MOEADDRAUCB extends MOEADDRA {
    String name = "";
    Map<String, String> parameters;

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    
    @Override
    public String getName() {
        return name;
    }

    public void setName(String str) {
        name = str;
    }

    public MOEADDRAUCB(Problem<DoubleSolution> problem, int populationSize, int resultPopulationSize, int maxEvaluations, MutationOperator<DoubleSolution> mutation, CrossoverOperator<DoubleSolution> crossover, FunctionType functionType, String dataDirectory, double neighborhoodSelectionProbability, int maximumNumberOfReplacedSolutions, int neighborSize) {
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
    
    //Versao 3 
    UCB_set hh = new UCB_set((int)(2.0*populationSize), //Window Size
                             75000,                     //Init
                             0,                     //End
                             100,                      //Step  
                             5.0,                       //C
                             1.0);                      //D
    //hh.addSelector("set", new Integer[]{1,2,3,4,5,6,7,8,9,10});
    hh.addSelector("set", new UCBWeighted(new Integer[]{1,2,3,4,5,6,7,8,9,10}));//, 11,12
    ucb_configuration(1);
      
    
    do {
      int[] permutation = new int[populationSize];
      MOEADUtils.randomPermutation(permutation, populationSize);

      for (int i = 0; i < populationSize; i++) {
  
        int subProblemId = permutation[i];
        frequency[subProblemId]++;
        
        
        if(hh.isWorking(evaluations, maxEvaluations)&&(!hh.isWSfull() || (evaluations-populationSize)%hh.maxStep==0)){
            hh.selectOperators();
           ucb_configuration((Integer)hh.getOperator("set"));
            //System.out.println((Integer)hh.getOperator("set"));
        }

        NeighborType neighborType = chooseNeighborType() ;
        //List<DoubleSolution> parents = parentSelection(subProblemId, neighborType) ;
        List<DoubleSolution> parents = parentSelection(subProblemId, neighborType, differentialEvolutionCrossover.getVariant()) ; 
        hh.setX(fitnessFunction(population.get(subProblemId), lambda[subProblemId]));
        
        differentialEvolutionCrossover.setCurrentSolution(population.get(subProblemId));
        
        List<DoubleSolution> children = differentialEvolutionCrossover.execute(parents);
        DoubleSolution child = children.get(0) ;
        mutationOperator.execute(child);
        problem.evaluate(child);
        

        if(hh.isWorking(evaluations, maxEvaluations)&& (!hh.isWSfull() || (evaluations-populationSize)%hh.maxStep==0)){
            hh.setY(fitnessFunction(child, lambda[subProblemId]));//qnto menor melhor
            hh.adjustSlidingWindow();//Adjust the sliding window
            hh.creditAssignment();//Calculate the credit assignment useing a Decaying Factor
        }
        
        evaluations++;
        updateIdealPoint(child);
        updateNeighborhood(child, subProblemId, neighborType);
        
        /*if(evaluations%50000==0){
            System.out.println("=============");
            hh.printHistory("set");
        }/**/
      }

      generation++;
      if (generation % draTime == 0) {
        utilityFunction();
      }

    } while (evaluations < maxEvaluations);
    /**
    System.out.println("=============");
    System.out.println(hh.info());
    hh.printHistory("set");
    /**/
  }
  
  
  
  
  
  private void ucb_configuration(Integer i){
      switch(i){
            case 1://DEFAULT CONFIGURATION
                neighborhoodSelectionProbability = 0.9;
                maximumNumberOfReplacedSolutions = 2;
                
                differentialEvolutionCrossover.setCr(1.0);
                differentialEvolutionCrossover.setF(0.5);
                differentialEvolutionCrossover.setVariant("rand/1/bin");
                break;
            case 2://PARTE DA CONFIGURAÇÃO ENCONTRADA PELO IRACE
                neighborhoodSelectionProbability = 0.95;
                maximumNumberOfReplacedSolutions = 2;
                
                differentialEvolutionCrossover.setCr(0.4);
                differentialEvolutionCrossover.setF(0.37);
                differentialEvolutionCrossover.setVariant("rand/1/bin");
                break;
            case 3:
                neighborhoodSelectionProbability = 0.8;
                maximumNumberOfReplacedSolutions = 3;
                
                differentialEvolutionCrossover.setCr(0.8);
                differentialEvolutionCrossover.setF(0.7);
                differentialEvolutionCrossover.setVariant("current-to-rand/2/bin");
                break;
            case 4:
                neighborhoodSelectionProbability = 1.0;
                maximumNumberOfReplacedSolutions = 1;
                
                differentialEvolutionCrossover.setCr(0.6);
                differentialEvolutionCrossover.setF(0.7);
                differentialEvolutionCrossover.setVariant("current-to-rand/1/bin");
                break;
            case 5:
                neighborhoodSelectionProbability = 0.6;
                maximumNumberOfReplacedSolutions = 2;
                
                differentialEvolutionCrossover.setCr(0.4);
                differentialEvolutionCrossover.setF(0.1);
                differentialEvolutionCrossover.setVariant("rand/1/bin");
                break;
            case 6:
                neighborhoodSelectionProbability = 0.95;
                maximumNumberOfReplacedSolutions = 1;
                
                differentialEvolutionCrossover.setCr(1.0);
                differentialEvolutionCrossover.setF(0.4);
                differentialEvolutionCrossover.setVariant("rand/2/bin");
                break;
            case 7:
                neighborhoodSelectionProbability = 0.3;
                maximumNumberOfReplacedSolutions = 6;
                
                differentialEvolutionCrossover.setCr(0.2);
                differentialEvolutionCrossover.setF(0.8);
                differentialEvolutionCrossover.setVariant("current-to-rand/1/bin");
                break;
            case 8:
                neighborhoodSelectionProbability = 0.75;
                maximumNumberOfReplacedSolutions = 10;
                
                differentialEvolutionCrossover.setCr(0.8);
                differentialEvolutionCrossover.setF(0.1);
                differentialEvolutionCrossover.setVariant("current-to-rand/2/bin");
                break;    
            case 9:
                neighborhoodSelectionProbability = 1.0;
                maximumNumberOfReplacedSolutions = 2;
                
                differentialEvolutionCrossover.setCr(1.0);
                differentialEvolutionCrossover.setF(0.7);
                differentialEvolutionCrossover.setVariant("rand/1/bin");
                break;
            case 10:
                neighborhoodSelectionProbability = 1.0;
                maximumNumberOfReplacedSolutions = 3;
                
                differentialEvolutionCrossover.setCr(1.0);
                differentialEvolutionCrossover.setF(0.9);
                differentialEvolutionCrossover.setVariant("rand/1/bin");
                break;
                
                
                
            case 11:
                neighborhoodSelectionProbability = 0.1;
                maximumNumberOfReplacedSolutions = 2;
                
                differentialEvolutionCrossover.setCr(0.3);
                differentialEvolutionCrossover.setF(0.6);
                differentialEvolutionCrossover.setVariant("rand/1/bin");
                break;
            case 12:
                neighborhoodSelectionProbability = 0.3;
                maximumNumberOfReplacedSolutions = 1;
                
                differentialEvolutionCrossover.setCr(0.80);
                differentialEvolutionCrossover.setF(0.3);
                differentialEvolutionCrossover.setVariant("current-to-rand/1/bin");
                break;
            
      }
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
      
      
}
