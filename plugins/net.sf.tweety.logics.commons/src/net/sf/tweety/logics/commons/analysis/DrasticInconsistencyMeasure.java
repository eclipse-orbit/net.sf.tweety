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
package net.sf.tweety.logics.commons.analysis;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.commons.Formula;

/**
 * This class models the drastic inconsistency measure.
 * 
 * @author Matthias Thimm
 */
@Component(service = InconsistencyMeasure.class)
public class DrasticInconsistencyMeasure<S extends Formula> implements InconsistencyMeasure<S> {

	/** The consistency tester used for measuring. */
	private ConsistencyTester<S> consTester;
	
	public DrasticInconsistencyMeasure() {
		super();
	}

	/**
	 * Creates a new drastic inconsistency measure.
	 * @param consTester some consistency tester
	 */
	public DrasticInconsistencyMeasure(ConsistencyTester<S> consTester){
		this.consTester = consTester;
	}

	/* (non-Javadoc)
	 * @see net.sf.tweety.logics.commons.analysis.BeliefSetInconsistencyMeasure#inconsistencyMeasure(java.util.Collection)
	 */
	@Override
	public Double inconsistencyMeasure(Collection<S> formulas) {
		if(this.consTester.isConsistent(formulas)) return 0d;
		return 1d;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "drastic";
	}
}
