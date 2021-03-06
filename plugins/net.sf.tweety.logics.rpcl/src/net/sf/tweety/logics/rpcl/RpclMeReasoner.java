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
package net.sf.tweety.logics.rpcl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.tweety.commons.Answer;
import net.sf.tweety.commons.BeliefBase;
import net.sf.tweety.commons.Formula;
import net.sf.tweety.commons.Reasoner;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.fol.semantics.HerbrandBase;
import net.sf.tweety.logics.fol.semantics.HerbrandInterpretation;
import net.sf.tweety.logics.fol.syntax.Conjunction;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.FolSignature;
import net.sf.tweety.logics.pcl.semantics.ProbabilityDistribution;
import net.sf.tweety.logics.rcl.syntax.RelationalConditional;
import net.sf.tweety.logics.rpcl.semantics.RpclProbabilityDistribution;
import net.sf.tweety.logics.rpcl.semantics.RpclSemantics;
import net.sf.tweety.logics.rpcl.syntax.RelationalProbabilisticConditional;
import net.sf.tweety.math.GeneralMathException;
import net.sf.tweety.math.equation.Equation;
import net.sf.tweety.math.equation.Statement;
import net.sf.tweety.math.opt.OptimizationProblem;
import net.sf.tweety.math.opt.ProblemInconsistentException;
import net.sf.tweety.math.opt.Solver;
import net.sf.tweety.math.probability.Probability;
import net.sf.tweety.math.term.FloatConstant;
import net.sf.tweety.math.term.FloatVariable;
import net.sf.tweety.math.term.IntegerConstant;
import net.sf.tweety.math.term.Logarithm;
import net.sf.tweety.math.term.Term;
import net.sf.tweety.math.term.Variable;

/**
 * General ME-reasoner for RPCL.
 * 
 * @author Matthias Thimm
 */
@Component(service = Reasoner.class)
public class RpclMeReasoner implements Reasoner<RelationalProbabilisticConditional, Formula> {
	
	/**
	 * Logger.
	 */
	static private Logger log = LoggerFactory.getLogger(RpclMeReasoner.class);
	
	/**
	 * Integer constant for standard inference.
	 */
	public static final int STANDARD_INFERENCE = 1;
	
	/**
	 * Integer constant for lifted inference.
	 */
	public static final int LIFTED_INFERENCE = 2;

	/**
	 * The semantics used for this reasoner.
	 */
	private RpclSemantics semantics;
	
	/**
	 * The signature for this reasoner.
	 */
	private FolSignature signature;
	
	/**
	 * The ME-distribution for this reasoner.
	 */
	private  ProbabilityDistribution<?> meDistribution;
	
	/**
	 * Whether this reasoner should use lifted inference for reasoning.
	 */
	private int inferenceType;
	
	public RpclMeReasoner() {
		super();
	}

	/**
	 * Creates a new reasoner for the given knowledge base.
	 * @param beliefBase a knowledge base.
	 * @param semantics the semantics for this reasoner.
	 * @param signature the fol signature for this reasoner.
	 * @param inferenceType one of RpclMeReasoner.STANDARD_INFERENCE or RpclMeReasoner.LIFTED_INFERENCE 
	 */
	public RpclMeReasoner(RpclSemantics semantics, FolSignature signature, int inferenceType){
		super();
		log.trace("Creating instance of 'RpclMeReasoner'.");
		if(inferenceType != RpclMeReasoner.STANDARD_INFERENCE && inferenceType != RpclMeReasoner.LIFTED_INFERENCE){
			log.error("The inference type must be either 'standard' or 'lifted'.");
			throw new IllegalArgumentException("The inference type must be either 'standard' or 'lifted'.");
		}
		this.signature = signature;
		this.semantics = semantics;
		this.inferenceType = inferenceType;
//		if(!(beliefBase instanceof RpclBeliefSet)){
//			log.error("Knowledge base of class 'RpclBeliefSet' expected but encountered '" + beliefBase.getClass() + "'.");
//			throw new IllegalArgumentException("Knowledge base of class 'RpclBeliefSet' expected but encountered '" + beliefBase.getClass() + "'.");
//		}
//		RpclBeliefSet beliefSet = (RpclBeliefSet) beliefBase;
//		if(!beliefSet.getSignature().isSubSignature(signature)){
//			log.error("Signature must be super-signature of the belief set's signature.");
//			throw new IllegalArgumentException("Signature must be super-signature of the belief set's signature.");
//		}
//		if(inferenceType == RpclMeReasoner.LIFTED_INFERENCE)
//			for(Predicate p: ((FolSignature)beliefSet.getSignature()).getPredicates())
//				if(p.getArity()>1){
//					log.error("Lifted inference only applicable for signatures containing only unary predicates.");
//					throw new IllegalArgumentException("Lifted inference only applicable for signatures containing only unary predicates.");
//				}
		log.trace("Finished creating instance of 'RpclReasoner'.");
	}
	
	/**
	 * Creates a new reasoner for the given knowledge base.
	 * @param beliefBase a knowledge base.
	 * @param semantics the semantics for this reasoner.
	 * @param signature the fol signature for this reasoner.
	 */
	public RpclMeReasoner(RpclSemantics semantics, FolSignature signature){
		this(semantics, signature, RpclMeReasoner.STANDARD_INFERENCE);
	}
	
	/**
	 * Returns the inference type of this reasoner, i.e. one of
	 * RpclMeReasoner.STANDARD_INFERENCE or RpclMeReasoner.LIFTED_INFERENCE 
	 * @return the inference type of this reasoner.
	 */
	public int getInferenceType(){
		return this.inferenceType;
	}
	
	/**
	 * Returns the ME-distribution this reasoner bases on.
	 * @return the ME-distribution this reasoner bases on.
	 */
	public ProbabilityDistribution<?> getMeDistribution(BeliefBase<RelationalProbabilisticConditional> beliefBase) throws ProblemInconsistentException{
		if(this.meDistribution == null)
			this.meDistribution = this.computeMeDistribution(beliefBase);
		return this.meDistribution;		
	}

	/**
	 * Computes the ME-distribution for this reasoner's knowledge base. 
	 * @return the ME-distribution for this reasoner's knowledge base.
	 */	
	private ProbabilityDistribution<?> computeMeDistribution(BeliefBase<RelationalProbabilisticConditional> beliefBase) throws ProblemInconsistentException{		
		RpclBeliefSet kb = (RpclBeliefSet) beliefBase;
		log.info("Computing ME-distribution using \"" + this.semantics.toString() + "\" and " + ((this.inferenceType==RpclMeReasoner.LIFTED_INFERENCE)?("lifted"):("standard")) + " inference for the knowledge base " + kb.toString() + ".");
		// TODO extract common parts from the following if/else
		log.info("Constructing optimization problem for finding the ME-distribution.");
		if(this.inferenceType == RpclMeReasoner.LIFTED_INFERENCE){
			// determine equivalence classes of the knowledge base
			Set<Set<Constant>> equivalenceClasses = kb.getEquivalenceClasses(this.getSignature());
			// determine the reference worlds needed to represent a probability distribution on the knowledge base.
			Set<ReferenceWorld> worlds = ReferenceWorld.enumerateReferenceWorlds(this.getSignature().getPredicates(), equivalenceClasses);
			
			/*  int numberOfInterpretations = 0;
			 
			for(ReferenceWorld w: worlds)
				numberOfInterpretations += w.spanNumber();*/
			
			// Generate Variables for the probability of each reference world,
			// range constraints for probabilities, and construct normalization sum
			Map<ReferenceWorld,FloatVariable> worlds2vars = new HashMap<ReferenceWorld,FloatVariable>();
			// check for empty kb
			if(kb.size() == 0)
				return CondensedProbabilityDistribution.getUniformDistribution(this.semantics, this.getSignature(), equivalenceClasses);
			int i=0;
			// We first construct the necessary constraints for the optimization problem
			Set<Statement> constraints = new HashSet<Statement>();
			Term normalization_sum = null;
			for(ReferenceWorld world: worlds){
				// variables representing probabilities should be in [0,1]
				FloatVariable v = new FloatVariable("X"+i++,0,1);
				worlds2vars.put(world, v);			
				// add term for normalization sum
				Term t = new FloatConstant(world.spanNumber()).mult(v);
				if(normalization_sum == null)
					normalization_sum = t;
				else normalization_sum = normalization_sum.add(t);
			}
			// add normalization constraint for probabilities
			Statement norm = new Equation(normalization_sum,new FloatConstant(1));
			constraints.add(norm);
			//for each conditional, add the corresponding constraint		
			// TODO remove conditionals with probability 0 or 1		
			for(RelationalProbabilisticConditional r: kb)
				constraints.add(this.semantics.getSatisfactionStatement(r, this.signature, worlds2vars));	
			// optimize for entropy
			OptimizationProblem problem = new OptimizationProblem(OptimizationProblem.MAXIMIZE);
			problem.addAll(constraints);
			Term targetFunction = null;
			for(ReferenceWorld w: worlds2vars.keySet()){
				Term t = new IntegerConstant(-w.spanNumber()).mult(worlds2vars.get(w).mult(new Logarithm(worlds2vars.get(w))));
				if(targetFunction == null)
					targetFunction = t;
				else targetFunction = targetFunction.add(t);
			}
			problem.setTargetFunction(targetFunction);			
			try{
				Map<Variable,Term> solution = Solver.getDefaultGeneralSolver().solve(problem);				
				CondensedProbabilityDistribution p = new CondensedProbabilityDistribution(this.semantics,this.getSignature());
				for(ReferenceWorld w: worlds2vars.keySet()){
					net.sf.tweety.math.term.Constant c = solution.get(worlds2vars.get(w)).value();
					Double value = new Double(c.doubleValue());
					p.put(w, new Probability(value));			
				}
				return p;
			}catch(GeneralMathException e){
				log.error("The knowledge base " + kb + " is inconsistent.");
				throw new ProblemInconsistentException();				
			}
		}else{
			// get interpretations
			Set<HerbrandInterpretation> worlds = new HerbrandBase(this.signature).allHerbrandInterpretations();
			// Generate Variables for the probability of each world,
			// range constraints for probabilities, and construct normalization sum
			Map<HerbrandInterpretation,FloatVariable> worlds2vars = new HashMap<HerbrandInterpretation,FloatVariable>();
			// check for empty kb
			if(kb.size() == 0)
				return RpclProbabilityDistribution.getUniformDistribution(this.semantics, this.getSignature());
			int i=0;
			// We first construct the necessary constraints for the optimization problem
			Set<Statement> constraints = new HashSet<Statement>();
			Term normalization_sum = null;
			for(HerbrandInterpretation world: worlds){
				// variables representing probabilities should be in [0,1]
				FloatVariable v = new FloatVariable("X"+i++,0,1);
				worlds2vars.put(world, v);			
				if(normalization_sum == null)
					normalization_sum = v;
				else normalization_sum = normalization_sum.add(v);
			}
			// add normalization constraint for probabilities
			Statement norm = new Equation(normalization_sum,new FloatConstant(1));
			constraints.add(norm);
			//for each conditional, add the corresponding constraint		
			// TODO remove conditionals with probability 0 or 1		
			for(RelationalProbabilisticConditional r: kb)
				constraints.add(this.semantics.getSatisfactionStatement(r, this.signature, worlds2vars));	
			// optimize for entropy
			OptimizationProblem problem = new OptimizationProblem(OptimizationProblem.MAXIMIZE);
			problem.addAll(constraints);
			Term targetFunction = null;
			for(HerbrandInterpretation w: worlds2vars.keySet()){
				Term t = new IntegerConstant(-1).mult(worlds2vars.get(w).mult(new Logarithm(worlds2vars.get(w))));
				if(targetFunction == null)
					targetFunction = t;
				else targetFunction = targetFunction.add(t);
			}
			problem.setTargetFunction(targetFunction);			
			try{
				Map<Variable,Term> solution = Solver.getDefaultGeneralSolver().solve(problem);
				RpclProbabilityDistribution p = new RpclProbabilityDistribution(this.semantics,this.getSignature());
				for(HerbrandInterpretation w: worlds2vars.keySet()){
					net.sf.tweety.math.term.Constant c = solution.get(worlds2vars.get(w)).value();
					Double value = new Double(c.doubleValue());
					p.put(w, new Probability(value));			
				}
				return p;
			}catch(GeneralMathException e){
				log.error("The knowledge base " + kb + " is inconsistent.");
				throw new ProblemInconsistentException();				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.tweety.kr.Reasoner#query(net.sf.tweety.kr.Formula)
	 */
	@Override
	public Answer query(BeliefBase<RelationalProbabilisticConditional> beliefBase, Formula query) {
		if(!(query instanceof FolFormula) && !(query instanceof RelationalConditional))
			throw new IllegalArgumentException("Reasoning in relational probabilistic conditional logic is only defined for first-order queries or relational conditionals.");
		
		if(!beliefBase.getSignature().isSubSignature(signature)){
			log.error("Signature must be super-signature of the belief set's signature.");
			throw new IllegalArgumentException("Signature must be super-signature of the belief set's signature.");
		}

		if(inferenceType == RpclMeReasoner.LIFTED_INFERENCE)
			for(Predicate p: ((FolSignature)beliefBase.getSignature()).getPredicates())
				if(p.getArity()>1){
					log.error("Lifted inference only applicable for signatures containing only unary predicates.");
					throw new IllegalArgumentException("Lifted inference only applicable for signatures containing only unary predicates.");
				}
		
		ProbabilityDistribution<?> meDistribution = this.getMeDistribution(beliefBase);		
		Probability prob;
		if(query instanceof FolFormula){
			prob = meDistribution.probability(query);
		}else{
			FolFormula premise = new Conjunction(((RelationalConditional)query).getPremise());
			FolFormula premiseConclusion = premise.combineWithAnd(((RelationalConditional)query).getConclusion());
			Probability prob1 = meDistribution.probability(premise);
			Probability prob2 = meDistribution.probability(premiseConclusion);
			if(prob1.doubleValue() == 0)
				prob = new Probability(0d);
			else prob = prob2.divide(prob1);
		}				
		Answer answer = new Answer(beliefBase, query);			
		answer.setAnswer(prob.getValue());
		answer.appendText("The probability of the query is " + prob + ".");
		return answer;		
	}

	/**
	 * Returns the signature of this reasoner.
	 * @return the signature of this reasoner.
	 */
	public FolSignature getSignature(){
		return this.signature;
	}	
}
