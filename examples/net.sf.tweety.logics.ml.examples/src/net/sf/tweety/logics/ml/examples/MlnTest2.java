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
package net.sf.tweety.logics.ml.examples;

import java.io.IOException;

import net.sf.tweety.commons.ParserException;
import net.sf.tweety.commons.util.Pair;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.FolSignature;
import net.sf.tweety.logics.ml.MarkovLogicNetwork;
import net.sf.tweety.logics.ml.NaiveMlnReasoner;
import net.sf.tweety.logics.ml.analysis.AggregatingCoherenceMeasure;
import net.sf.tweety.logics.ml.analysis.AggregatingDistanceFunction;
import net.sf.tweety.logics.ml.analysis.MaxAggregator;
import net.sf.tweety.logics.ml.syntax.MlnFormula;
import net.sf.tweety.math.probability.Probability;

public class MlnTest2 {

	public static Pair<MarkovLogicNetwork,FolSignature> Nixon1() throws ParserException, IOException{
		Predicate quaker = new Predicate("quaker",1);
		Predicate republican = new Predicate("republican",1);
		Predicate pacifist = new Predicate("pacifist",1);
		
		FolSignature sig = new FolSignature();
		sig.add(quaker);
		sig.add(republican);
		sig.add(pacifist);
		
		sig.add(new Constant("d1"));
		
		FolParser parser = new FolParser();
		parser.setSignature(sig);
		
		MarkovLogicNetwork mln = new MarkovLogicNetwork();
				
		mln.add(new MlnFormula((FolFormula)parser.parseFormula("!quaker(X)|| pacifist(X)"), new Probability(0.95)));		
		mln.add(new MlnFormula((FolFormula)parser.parseFormula("!republican(X) || !pacifist(X)"), new Probability(0.95))); 

		return new Pair<MarkovLogicNetwork,FolSignature>(mln,sig);
	}

	public static Pair<MarkovLogicNetwork,FolSignature> Nixon2() throws ParserException, IOException{
		Predicate quaker = new Predicate("quaker",1);
		Predicate republican = new Predicate("republican",1);
		Predicate president = new Predicate("president",1);
		
		FolSignature sig = new FolSignature();
		sig.add(quaker);
		sig.add(republican);
		sig.add(president);
		
		sig.add(new Constant("nixon"));
		
		FolParser parser = new FolParser();
		parser.setSignature(sig);
		
		MarkovLogicNetwork mln = new MarkovLogicNetwork();
		
		mln.add(new MlnFormula((FolFormula)parser.parseFormula("quaker(nixon) && republican(nixon) && president(nixon)"))); // p = 1
		 

		return new Pair<MarkovLogicNetwork,FolSignature>(mln,sig);
	}
	
	public static Pair<MarkovLogicNetwork,FolSignature> Nixon3() throws ParserException, IOException{
		Predicate actor = new Predicate("actor",1);
		Predicate president = new Predicate("president",1);
		
		FolSignature sig = new FolSignature();
		sig.add(actor);
		sig.add(president);
		
		sig.add(new Constant("reagan"));
		
		FolParser parser = new FolParser();
		parser.setSignature(sig);
		
		MarkovLogicNetwork mln = new MarkovLogicNetwork();
		
		mln.add(new MlnFormula((FolFormula)parser.parseFormula("!president(X) || actor(X)"), new Probability(0.9)));		
		mln.add(new MlnFormula((FolFormula)parser.parseFormula("president(reagan) && actor(reagan)"))); // p = 1
		 

		return new Pair<MarkovLogicNetwork,FolSignature>(mln,sig);
	}
	
	public static void main(String[] args) throws ParserException, IOException{
		AggregatingCoherenceMeasure measure = new AggregatingCoherenceMeasure(new AggregatingDistanceFunction(new MaxAggregator()),new MaxAggregator());
		Pair<MarkovLogicNetwork,FolSignature> ex1 = Nixon1();
		Pair<MarkovLogicNetwork,FolSignature> ex2 = Nixon2();
		Pair<MarkovLogicNetwork,FolSignature> ex3 = Nixon3();
		MarkovLogicNetwork mln1 = ex1.getFirst();
		FolSignature sig1 = ex1.getSecond();
		MarkovLogicNetwork mln2 = ex2.getFirst();
		FolSignature sig2 = ex2.getSecond();
		MarkovLogicNetwork mln3 = ex3.getFirst();
		FolSignature sig3 = ex3.getSecond();
		NaiveMlnReasoner reasoner1 = new NaiveMlnReasoner(sig1);
		NaiveMlnReasoner reasoner2 = new NaiveMlnReasoner(sig2);
		NaiveMlnReasoner reasoner3 = new NaiveMlnReasoner(sig3);
		reasoner1.setTempDirectory("/Users/mthimm/Desktop/tmp");
		reasoner2.setTempDirectory("/Users/mthimm/Desktop/tmp");
		reasoner3.setTempDirectory("/Users/mthimm/Desktop/tmp");
		System.out.println("#1: Measure " + measure.toString() + ", coherence value " + measure.coherence(mln1, reasoner1, sig1));
		System.out.println("#2: Measure " + measure.toString() + ", coherence value " + measure.coherence(mln2, reasoner2, sig2));
		System.out.println("#3: Measure " + measure.toString() + ", coherence value " + measure.coherence(mln3, reasoner3, sig3));
		System.out.println();
		
		MarkovLogicNetwork mergedMln = new MarkovLogicNetwork();
		mergedMln.addAll(mln1);
		mergedMln.addAll(mln2);
		//mergedMln.addAll(mln3);
		FolSignature mergedSig = new FolSignature();
		mergedSig.addSignature(sig1);
		mergedSig.addSignature(sig2);
		//mergedSig.addSignature(sig3);
		NaiveMlnReasoner mergedReasoner = new NaiveMlnReasoner(mergedSig);
		mergedReasoner.setTempDirectory("/Users/mthimm/Desktop/tmp");
		System.out.println("Merged: Measure " + measure.toString() + ", coherence value " + measure.coherence(mergedMln, mergedReasoner, mergedSig));
	}
}
