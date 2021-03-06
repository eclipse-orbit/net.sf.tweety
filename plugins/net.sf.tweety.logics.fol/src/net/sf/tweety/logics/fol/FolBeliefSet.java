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
package net.sf.tweety.logics.fol;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.commons.BeliefBase;
import net.sf.tweety.commons.BeliefSet;
import net.sf.tweety.commons.Signature;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.FolSignature;

/**
 * This class models a first-order knowledge base, i.e. a set of formulas in
 * first-order logic.
 * 
 * @author Matthias Thimm
 *
 */
@Component(service = BeliefBase.class)
public class FolBeliefSet extends BeliefSet<FolFormula> {

	/**
	 * Creates a new and empty first-order knowledge base.
	 */
	public FolBeliefSet() {
		super();
	}

	/**
	 * Creates a new first-order knowledge base with the given set of formulas.
	 * 
	 * @param formulas
	 */
	public FolBeliefSet(Set<FolFormula> formulas) {
		super(formulas);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.tweety.kr.BeliefBase#getSignature()
	 */
	@Override
	public Signature getSignature() {
		FolSignature sig = new FolSignature();
		sig.addAll(formulas);
		return sig;
	}
}
