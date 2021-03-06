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
package net.sf.tweety.logics.ml;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.commons.BeliefBase;
import net.sf.tweety.commons.Reasoner;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.ml.syntax.MlnFormula;

/**
 * This MLN reasoner takes another MLN reasoner and performs several iterations
 * with this one and takes the average result as result.
 * 
 * @author Matthias Thimm
 */
@Component(service = Reasoner.class)
public class IteratingMlnReasoner extends AbstractMlnReasoner{ 

	/** The reasoner inside this reasoner. */
	private AbstractMlnReasoner reasoner;
	
	/** The number of iterations. */
	private long numberOfIterations;

	/** For archiving previous results. */
	private Map<FolFormula,Double> archive;
	
	public IteratingMlnReasoner() {
		super();
	}

	/**
	 * Creates a new IteratingMlnReasoner for the given MLN reaasoner.
	 * @param reasoner some MLN reasoner.
	 * @param numberOfIterations the number of iterations for the reasoner 
	 */
	public IteratingMlnReasoner(AbstractMlnReasoner reasoner, long numberOfIterations){
		super(reasoner.getSignature());
		this.reasoner = reasoner;
		this.numberOfIterations = numberOfIterations;
		this.archive = new HashMap<FolFormula,Double>();		
	}
	
	/* (non-Javadoc)
	 * @see net.sf.tweety.logics.markovlogic.AbstractMlnReasoner#reset()
	 */
	public void reset(){
		this.reasoner.reset();
		this.archive.clear();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.tweety.logics.markovlogic.AbstractMlnReasoner#doQuery(net.sf.tweety.logics.firstorderlogic.syntax.FolFormula)
	 */
	@Override
	protected double doQuery(BeliefBase<MlnFormula> beliefBase, FolFormula query) {
		if(this.archive.containsKey(query))
			return this.archive.get(query);
		double resultSum = 0;
		for(long i = 0; i < this.numberOfIterations; i++){
			this.reasoner.reset();
			resultSum += this.reasoner.doQuery(beliefBase, query);
		}
		double result = resultSum/this.numberOfIterations;
		this.archive.put(query, result);
		return result;
	}

}