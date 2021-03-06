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
package net.sf.tweety.arg.dung;

import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.arg.dung.semantics.Extension;
import net.sf.tweety.arg.dung.semantics.Semantics;
import net.sf.tweety.arg.dung.semantics.StratifiedLabeling;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.commons.Answer;
import net.sf.tweety.commons.BeliefBase;
import net.sf.tweety.commons.Reasoner;

/**
 * This class implements a stratified labeling reasoner.
 * @author Matthias Thimm
 */
@Component(service = Reasoner.class)
public class StratifiedLabelingReasoner implements Reasoner<Argument, Argument> {

	/** The set of stratified labelings this reasoner is based upon. */
	private Set<StratifiedLabeling> labelings;
	
	/**
	 * The type of inference for this reasoner, either sceptical or
	 * credulous.
	 */
	private int inferenceType;
	
	/** The semantics used for this reasoner. */
	private int semantics;
	
	/**
	 * Creates a new reasoner for the given Dung theory, semantics, and inference type.
	 * @param beliefBase a Dung theory
	 * @param semantics a semantics
	 * @param inferenceType and inference type
	 */
	public StratifiedLabelingReasoner(int semantics, int inferenceType) {
		super();
//		if(!(beliefBase instanceof DungTheory))
//			throw new IllegalArgumentException("Knowledge base of class DungTheory expected.");
		if(inferenceType != Semantics.CREDULOUS_INFERENCE && inferenceType != Semantics.SCEPTICAL_INFERENCE)
			throw new IllegalArgumentException("Inference type must be either sceptical or credulous.");
		this.inferenceType = inferenceType;
		this.semantics = semantics;
	}

	/**
	 * Creates a new reasoner for the given knowledge base using sceptical inference and grounded semantics.
	 * @param beliefBase The knowledge base for this reasoner.
	 */
	public StratifiedLabelingReasoner(){
		this(Semantics.GROUNDED_SEMANTICS, Semantics.SCEPTICAL_INFERENCE);		
	}	
	
	/* (non-Javadoc)
	 * @see net.sf.tweety.Reasoner#query(net.sf.tweety.Formula)
	 */
	@Override
	public Answer query(BeliefBase<Argument> beliefBase, Argument arg) {
//		if(!(query instanceof Argument))
//			throw new IllegalArgumentException("Formula of class argument expected");
//		Argument arg = (Argument) query;
		if(this.inferenceType == Semantics.SCEPTICAL_INFERENCE){
			Answer answer = new Answer(beliefBase,arg);
			for(StratifiedLabeling e: this.getLabelings(beliefBase)){
				if(!e.satisfies(arg)){
					answer.setAnswer(false);
					answer.appendText("The answer is: false");
					return answer;
				}
			}			
			answer.setAnswer(true);
			answer.appendText("The answer is: true");
			return answer;
		}
		// so its credulous semantics
		Answer answer = new Answer(beliefBase, arg);
		for(StratifiedLabeling e: this.getLabelings(beliefBase)){
			if(e.satisfies(arg)){
				answer.setAnswer(true);
				answer.appendText("The answer is: true");
				return answer;
			}
		}			
		answer.setAnswer(false);
		answer.appendText("The answer is: false");
		return answer;
	}

	/**
	 * Returns the labelings this reasoner bases upon.
	 * @return the labelings this reasoner bases upon.
	 */
	public Set<StratifiedLabeling> getLabelings(BeliefBase<Argument> beliefBase){
		if(this.labelings == null)
			this.labelings = this.computeLabelings(beliefBase);
		return this.labelings;
	}
	
	/**
	 * Returns the inference type of this reasoner.
	 * @return the inference type of this reasoner.
	 */
	public int getInferenceType(){
		return this.inferenceType;
	}
	
	/**
	 * Computes the labelings this reasoner bases upon.
	 * @return A set of labelings.
	 */
	private Set<StratifiedLabeling> computeLabelings(BeliefBase<Argument> beliefBase){
		Set<StratifiedLabeling> labelings = new HashSet<StratifiedLabeling>();
		AbstractExtensionReasoner reasoner = AbstractExtensionReasoner.getReasonerForSemantics(this.semantics, Semantics.CREDULOUS_INFERENCE);
		Set<Extension> extensions = reasoner.getExtensions(beliefBase);
		DungTheory theory = (DungTheory) beliefBase;
		for(Extension extension: extensions){
			StratifiedLabeling labeling = new StratifiedLabeling();
			if(extension.isEmpty()){
				for(Argument arg: theory)
					labeling.put(arg, Integer.MAX_VALUE);
				labelings.add(labeling);
			}else{
				for(Argument arg: extension)
					labeling.put(arg, 0);
				Extension remainingArguments = new Extension(theory);
				remainingArguments.removeAll(extension);
				DungTheory remainingTheory = new DungTheory(theory.getRestriction(remainingArguments));
				StratifiedLabelingReasoner sReasoner = new StratifiedLabelingReasoner(this.semantics, this.inferenceType);
				for(StratifiedLabeling labeling2: sReasoner.getLabelings(remainingTheory)){
					for(Argument arg: labeling2.keySet())
						labeling2.put(arg, labeling2.get(arg) + 1);
					labeling2.putAll(labeling);
					labelings.add(labeling2);
				}
			}
		}		
		return labelings;
	}
}
