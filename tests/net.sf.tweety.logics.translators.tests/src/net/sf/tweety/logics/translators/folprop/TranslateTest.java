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
package net.sf.tweety.logics.translators.folprop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.tweety.logics.commons.error.LanguageException;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.fol.syntax.Conjunction;
import net.sf.tweety.logics.fol.syntax.Disjunction;
import net.sf.tweety.logics.fol.syntax.FOLAtom;
import net.sf.tweety.logics.pl.syntax.Proposition;

/**
 * Tests the FOLPropTranslator
 * @author Tim Janus
 */
public class TranslateTest {

	private static FOLPropTranslator translator = new FOLPropTranslator();
	
	@Test
	public void testPropToFOLAtomTranslation() {
		Proposition prop = new Proposition("test");
		FOLAtom atom = translator.toFOL(prop);
		assertEquals(prop.getName(), atom.getName());
		assertEquals(0, atom.getArguments().size());
	}
	
	@Test
	public void testFOLToPropAtomTranslation() {
		FOLAtom atom = new FOLAtom(new Predicate("test"));
		Proposition prop = translator.toPropositional(atom);
		assertEquals(atom.getName(), prop.getName());
	}
	
	@Test(expected=LanguageException.class)
	public void testFOLToPropAtomTranslationFAILCauseArgs() {
		FOLAtom atom = new FOLAtom(new Predicate("is_male", 1), new Constant("bob"));
		translator.toPropositional(atom);
	}
	
	@Test
	public void testDisjunctionFOLtoProp() {
		Disjunction dis = new Disjunction();
		dis.add(new FOLAtom(new Predicate("a")));
		dis.add(new FOLAtom(new Predicate("b")));
		net.sf.tweety.logics.pl.syntax.Disjunction td = translator.toPropositional(dis);
		assertEquals(2, td.size());
		assertEquals(true, td.contains(new Proposition("a")));
		assertEquals(true, td.contains(new Proposition("b")));
	}
	
	@Test
	public void testNestedConjunction() {
		net.sf.tweety.logics.pl.syntax.Conjunction con = new net.sf.tweety.logics.pl.syntax.Conjunction();
		con.add(new Proposition("a"));
		net.sf.tweety.logics.pl.syntax.Disjunction nested = new net.sf.tweety.logics.pl.syntax.Disjunction();
		nested.add(new Proposition("b"));
		nested.add(new Proposition("c"));
		con.add(nested);
		
		Conjunction tc = translator.toFOL(con);
		assertEquals(2, tc.size());
		assertEquals(true, tc.contains(new FOLAtom(new Predicate("a"))));
		assertEquals(translator.toFOL(nested), tc.get(1));
	}
}
