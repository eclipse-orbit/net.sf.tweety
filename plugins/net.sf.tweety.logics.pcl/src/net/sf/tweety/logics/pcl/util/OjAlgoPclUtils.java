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
package net.sf.tweety.logics.pcl.util;

import java.util.Set;

import org.ojalgo.access.Access2D;
import org.ojalgo.constant.BigMath;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;

import net.sf.tweety.logics.pcl.PclBeliefSet;
import net.sf.tweety.logics.pcl.syntax.ProbabilisticConditional;
import net.sf.tweety.logics.pl.semantics.PossibleWorld;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

/**
 * Provides some utility functions for solving Pcl specific reasoning problems with ojAlgo.
 * 
 * @author NicoPotyka
 *
 */
public class OjAlgoPclUtils {

	
	/**
	 * Add probability normalization constraint to model 
	 * (all probabilities have to sum to 1). 
	 * @param model
	 */
	public static void addProbabilityNormalizationConstraint(ExpressionsBasedModel model) {
		
		Expression tmpExpr = model.addExpression("Normalization").level(BigMath.ONE);
		for(int i=0; i<model.countVariables(); i++) {
			tmpExpr.setLinearFactor(i, BigMath.ONE);
		}
		
	}
	

	/**
	 * Create (non-negative) variables for the probabilities of possible worlds. 
	 * @param noWorlds
	 * @return
	 */
	public static Variable[] createVariables(int noWorlds) {
	
		Variable[] tmpVariables = new Variable[noWorlds];		
		for(int i=0; i<noWorlds; i++) {
			tmpVariables[i] = new Variable(""+i).lower(BigMath.ZERO);
		}
		
		return tmpVariables;
	}
	


	/**
	 * Create constraint matrix for a set of PCL conditionals over a set of possible worlds.
	 * @param beliefSet
	 * @param worlds
	 * @return
	 */
	public static PrimitiveMatrix createConstraintMatrix(PclBeliefSet beliefSet, Set<PossibleWorld> worlds) {
		
		int i = 0;
		
		Access2D.Builder<PrimitiveMatrix> aBuilder = PrimitiveMatrix.FACTORY.getBuilder(beliefSet.size(), worlds.size());
		
		for(ProbabilisticConditional c: beliefSet) {
			
			int j = 0; 
			double p = c.getProbability().doubleValue();
			PropositionalFormula conclusion = c.getConclusion();
			
			if(c.isFact()) {
				
				for(PossibleWorld w: worlds) {
					if(w.satisfies(conclusion)) {
						aBuilder.set(i, j, 1-p);
					}
					else {
						aBuilder.set(i, j, -p);
					}
					
					j++;
				}
				
			}
			else {
				
				PropositionalFormula premise = c.getPremise().iterator().next();
				
				for(PossibleWorld w: worlds) {
					
					if(w.satisfies(premise)) {
						
						if(w.satisfies(conclusion)) {
							aBuilder.set(i, j, 1-p);
						}
						else {
							aBuilder.set(i, j, -p);
						}
						
					}

					j++;
				}
			}
			
			i++;
		}
		
		return aBuilder.build();
	}
	

	
}
