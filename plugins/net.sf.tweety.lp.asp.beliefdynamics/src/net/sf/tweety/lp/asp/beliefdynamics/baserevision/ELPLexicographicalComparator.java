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
package net.sf.tweety.lp.asp.beliefdynamics.baserevision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.sf.tweety.lp.asp.syntax.Program;
import net.sf.tweety.lp.asp.syntax.Rule;

/**
 * This comparator imposes a total order on the set of extended logic programs by use
 * of the lexicographical order given as follows:
 * A program A is less than a program B iff the smallest rule of A is smaller than
 * the smallest rule of B or if both are equal if the second smallest rule of A
 * is smaller than the second smallest rule of B and so on.
 * 
 * @author Sebastian Homann
 */
public class ELPLexicographicalComparator implements Comparator<Program> {

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Program arg0, Program arg1) {
		ArrayList<Rule> lhs = new ArrayList<Rule>(arg0);
		Collections.sort(lhs);
		ArrayList<Rule> rhs = new ArrayList<Rule>(arg1);
		Collections.sort(rhs);
		
		// compare element-wise and return first comparator value != 0
		Iterator<Rule> lhsiter = lhs.iterator();
		Iterator<Rule> rhsiter = rhs.iterator();
		while(lhsiter.hasNext() && rhsiter.hasNext()) {
			int cmp = lhsiter.next().compareTo(rhsiter.next());
			if(cmp != 0) {
				return cmp;
			}
		}
		// all elements equal, shorter programs first
		if(lhs.size() < rhs.size()) {
			return -1;
		}
		if(lhs.size() > rhs.size()) {
			return 1;
		}
		return 0;
	}

}
