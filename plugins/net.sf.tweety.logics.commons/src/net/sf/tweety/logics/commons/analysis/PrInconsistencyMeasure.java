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
import java.util.HashSet;

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.commons.Formula;

/**
 * This class models the P inconsistency measure from e.g. [Grant,Hunter,2011a].
 * It takes as inconsistency value the number of formulas that are in some 
 * minimal inconsistency subset.
 * 
 * @author Matthias Thimm
 */
@Component(service = InconsistencyMeasure.class)
public class PrInconsistencyMeasure<S extends Formula> implements InconsistencyMeasure<S> {

	/** The MUs enumerator. */
	private MusEnumerator<S> enumerator;
	
	public PrInconsistencyMeasure() {
		super();
	}

	/**
	 * Creates a new drastic inconsistency measure.
	 * @param enumerator some MUs enumerator
	 */
	public PrInconsistencyMeasure(MusEnumerator<S> enumerator){
		this.enumerator = enumerator;
	}

	/* (non-Javadoc)
	 * @see net.sf.tweety.logics.commons.analysis.BeliefSetInconsistencyMeasure#inconsistencyMeasure(java.util.Collection)
	 */
	@Override
	public Double inconsistencyMeasure(Collection<S> formulas) {
		Collection<Collection<S>> mis = this.enumerator.minimalInconsistentSubsets(formulas);
		Collection<S> problematic = new HashSet<S>();
		for(Collection<S> mi: mis)
			problematic.addAll(mi);
		return new Double(problematic.size());
	}
}
