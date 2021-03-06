/*
 *  This file is part of "Tweety", a collection of Java libraries for
 *  logical aspects of artificial intelligence and knowledge representation.
 *
 *  Tweety is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License version 3 as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2016 The Tweety Project Team <http://tweetyproject.org/contact/>
 */
package net.sf.tweety.logics.pcl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.commons.Answer;
import net.sf.tweety.commons.BeliefBase;
import net.sf.tweety.commons.Formula;
import net.sf.tweety.commons.Reasoner;
import net.sf.tweety.commons.Signature;
import net.sf.tweety.logics.cl.syntax.Conditional;
import net.sf.tweety.logics.pcl.analysis.PclDefaultConsistencyTester;
import net.sf.tweety.logics.pcl.semantics.ProbabilityDistribution;
import net.sf.tweety.logics.pcl.syntax.ProbabilisticConditional;
import net.sf.tweety.logics.pl.semantics.PossibleWorld;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;
import net.sf.tweety.logics.pl.syntax.PropositionalSignature;
import net.sf.tweety.math.GeneralMathException;
import net.sf.tweety.math.equation.Equation;
import net.sf.tweety.math.opt.OptimizationProblem;
import net.sf.tweety.math.opt.Solver;
import net.sf.tweety.math.probability.Probability;
import net.sf.tweety.math.term.FloatConstant;
import net.sf.tweety.math.term.FloatVariable;
import net.sf.tweety.math.term.Logarithm;
import net.sf.tweety.math.term.Term;
import net.sf.tweety.math.term.Variable;

/**
 * This class implements a maximum entropy reasoner for probabilistic
 * conditional logic. This means, it computes the ME-distribution
 * for the given belief set and answers queries with respect to
 * this ME-distribution.
 * 
 * @author Matthias Thimm
 *
 */
@Component(service = Reasoner.class)
public class DefaultMeReasoner implements Reasoner<ProbabilisticConditional, Formula> {

	/**
	 * The ME-distribution this reasoner bases on.
	 */
	private ProbabilityDistribution<PossibleWorld> meDistribution = null;
	
	/**
	 * The signature of the reasoner.
	 */
	private Signature signature = null;
	
//	/**
//	 * Creates a new default ME-reasoner for the given knowledge base.
//	 * @param beliefBase a pcl belief set. 
//	 */
//	public DefaultMeReasoner(BeliefBase beliefBase){
//		this(beliefBase, beliefBase.getSignature());
//	}
	
	private final PclDefaultConsistencyTester tester = new PclDefaultConsistencyTester();
	
//	/**
//	 * Creates a new default ME-reasoner for the given knowledge base.
//	 * @param beliefBase a pcl belief set. 
//	 * @param signature another signature (if the probability distribution should be defined 
//	 * on that one (that one should subsume the signature of the belief base)
//	 */
//	public DefaultMeReasoner(BeliefBase beliefBase, Signature signature){
//		super(beliefBase);		
//		if(!(beliefBase instanceof PclBeliefSet))
//			throw new IllegalArgumentException("Knowledge base of class PclBeliefSet expected.");
//		// if belief set is inconsistent no reasoning is possible
//		PclDefaultConsistencyTester tester = new PclDefaultConsistencyTester();
//		if(!tester.isConsistent((PclBeliefSet)beliefBase))
//			throw new IllegalArgumentException("Knowledge base is inconsistent.");
//		if(!beliefBase.getSignature().isSubSignature(signature))
//			throw new IllegalArgumentException("Given signature is not a super-signature of the belief base's signature.");
//		this.signature = signature;
//	}
	
	/**
	 * Returns the ME-distribution this reasoner bases on.
	 * @return the ME-distribution this reasoner bases on.
	 */
	public ProbabilityDistribution<PossibleWorld> getMeDistribution(BeliefBase<ProbabilisticConditional> beliefBase){
		if(this.meDistribution == null)
			this.meDistribution = this.computeMeDistribution(beliefBase);
		return this.meDistribution;
	}
	
	/**
	 * Computes the ME-distribution this reasoner bases on.
	 * @return the ME-distribution this reasoner bases on.
	 */
	private ProbabilityDistribution<PossibleWorld> computeMeDistribution(BeliefBase<ProbabilisticConditional> beliefBase){
		// construct optimization problem
		OptimizationProblem problem = new OptimizationProblem(OptimizationProblem.MINIMIZE);
		Set<PossibleWorld> worlds = PossibleWorld.getAllPossibleWorlds((PropositionalSignature) this.signature);
		Map<PossibleWorld,Variable> vars = new HashMap<PossibleWorld,Variable>();
		int cnt = 0;
		Term normConstraint = null;
		for(PossibleWorld w: worlds){
			Variable var = new FloatVariable("w" + cnt,0,1);
			vars.put(w, var);
			if(normConstraint == null)
				normConstraint = var;
			else normConstraint = normConstraint.add(var);
			cnt++;
		}
		problem.add(new Equation(normConstraint,new FloatConstant(1)));
		// add constraint imposed by conditionals
		for(ProbabilisticConditional pc: beliefBase){
			Term leftSide = null;
			Term rightSide = null;			
			if(pc.isFact()){
				for(PossibleWorld w: worlds)
					if(w.satisfies(pc.getConclusion())){
						if(leftSide == null)
							leftSide = vars.get(w);
						else leftSide = leftSide.add(vars.get(w));
					}
				rightSide = new FloatConstant(pc.getProbability().getValue());
			}else{				
				PropositionalFormula body = pc.getPremise().iterator().next();
				PropositionalFormula head_and_body = pc.getConclusion().combineWithAnd(body);
				for(PossibleWorld w: worlds){
					if(w.satisfies(head_and_body)){
						if(leftSide == null)
							leftSide = vars.get(w);
						else leftSide = leftSide.add(vars.get(w));
					}
					if(w.satisfies(body)){
						if(rightSide == null)
							rightSide = vars.get(w);
						else rightSide = rightSide.add(vars.get(w));
					}					
				}
				if(rightSide == null)
					rightSide = new FloatConstant(0);
				else rightSide = rightSide.mult(new FloatConstant(pc.getProbability().getValue()));
			}
			if(leftSide == null)
				leftSide = new FloatConstant(0);
			if(rightSide == null)
				rightSide = new FloatConstant(0);
			problem.add(new Equation(leftSide,rightSide));
		}
		// target function is the entropy
		Term targetFunction = null;
		for(PossibleWorld w: worlds){
			if(targetFunction == null)
				targetFunction = vars.get(w).mult(new Logarithm(vars.get(w)));
			else targetFunction = targetFunction.add(vars.get(w).mult(new Logarithm(vars.get(w))));			
		}
		problem.setTargetFunction(targetFunction);
		try{			
			Map<Variable,Term> solution = Solver.getDefaultGeneralSolver().solve(problem);
			// construct probability distribution
			ProbabilityDistribution<PossibleWorld> p = new ProbabilityDistribution<PossibleWorld>(this.signature);
			for(PossibleWorld w: worlds)
				p.put(w, new Probability(solution.get(vars.get(w)).doubleValue()));
			return p;					
		}catch (GeneralMathException e){
			// This should not happen as the optimization problem is guaranteed to be feasible (the knowledge base is consistent)
			throw new RuntimeException("Fatal error: Optimization problem to compute the ME-distribution is not feasible although the knowledge base seems to be consistent.");
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.tweety.Reasoner#query(net.sf.tweety.Formula)
	 */
	@Override
	public Answer query(BeliefBase<ProbabilisticConditional> beliefBase, Formula query) {
		if(!tester.isConsistent((PclBeliefSet)beliefBase))
			throw new IllegalArgumentException("Knowledge base is inconsistent.");
		
		ProbabilityDistribution<PossibleWorld> meDistribution = this.getMeDistribution(beliefBase);
		if(query instanceof ProbabilisticConditional){
			Answer answer = new Answer(beliefBase,query);
			boolean bAnswer = meDistribution.satisfies(query);
			answer.setAnswer(bAnswer);
			answer.appendText("The answer is: " + bAnswer);
			return answer;			
		}
		if(query instanceof Conditional){
			Answer answer = new Answer(beliefBase,query);
			Probability bAnswer = meDistribution.probability((Conditional)query);
			answer.setAnswer(bAnswer.doubleValue());
			answer.appendText("The answer is: " + bAnswer);
			return answer;
		}
		if(query instanceof PropositionalFormula){
			Answer answer = new Answer(beliefBase,query);
			Probability bAnswer = meDistribution.probability(query);
			answer.setAnswer(bAnswer.doubleValue());
			answer.appendText("The answer is: " + bAnswer);
			return answer;
		}			
		throw new IllegalArgumentException("Reasoning in probabilistic conditional logic is only defined for (probabilistic) conditionals and propositional queries.");
	}	
}
