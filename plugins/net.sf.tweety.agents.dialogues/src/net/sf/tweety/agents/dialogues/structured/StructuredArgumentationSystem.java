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
package net.sf.tweety.agents.dialogues.structured;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.tweety.agents.Agent;
import net.sf.tweety.agents.Environment;
import net.sf.tweety.agents.Executable;
import net.sf.tweety.agents.MultiAgentSystem;
import net.sf.tweety.agents.Perceivable;
import net.sf.tweety.arg.dung.semantics.Extension;
import net.sf.tweety.arg.dung.syntax.Argument;
import net.sf.tweety.arg.dung.syntax.Attack;

/**
 * This class represents a structured argumentation system, i.e. a set of agents
 * that argue within some given protocol based on structured argumentation frameworks.
 * 
 * @author Matthias Thimm
 */
public class StructuredArgumentationSystem extends MultiAgentSystem<SasAgent> {

	/**
	 * This class models an environment for structured argumentation systems.
	 * @author Matthias Thimm
	 */
	private static class SasEnvironment implements Environment {

		/**
		 * The common view of all agents.
		 */
		private PerceivableStructuredArgumentationFramework commonView;
		
		/**
		 * The underlying structured argumentation framework.
		 */
		private PerceivableStructuredArgumentationFramework saf;
		
		/**
		 * Creates a new SasEnvironment based on the given SAF.
		 * @param saf a perceivable structured argumentation framework.
		 */
		public SasEnvironment(PerceivableStructuredArgumentationFramework saf){
			this.saf = saf;
			this.commonView = new PerceivableStructuredArgumentationFramework();
		}
		
		/* (non-Javadoc)
		 * @see net.sf.tweety.agents.Environment#execute(net.sf.tweety.agents.Executable)
		 */
		@Override
		public Set<Perceivable> execute(Executable action) {
			if(!(action instanceof Extension))
				throw new IllegalArgumentException("SasEnvironment expects action of type 'Extension'.");
			Extension e = (Extension) action;
			if(!(this.saf.containsAll(e)))
				throw new IllegalArgumentException("Action contains unknown arguments.");
			this.commonView.addAll(e);
			for(Argument a: this.commonView){
				for(Argument b: this.commonView)
					if(saf.isAttackedBy(a, b))
						this.commonView.add(new Attack(b,a));
			}				
			return this.getPercepts(null);
		}

	
		/* (non-Javadoc)
		 * @see net.sf.tweety.agents.Environment#execute(java.util.Collection)
		 */
		@Override
		public Set<Perceivable> execute(Collection<? extends Executable> actions) {
			for(Executable action: actions)
				this.execute(action);
			return this.getPercepts(null);
		}

		/* (non-Javadoc)
		 * @see net.sf.tweety.agents.Environment#getPercepts(net.sf.tweety.agents.Agent)
		 */
		@Override
		public Set<Perceivable> getPercepts(Agent agent) {
			Set<Perceivable> percepts = new HashSet<Perceivable>();
			percepts.add(this.commonView);
			return percepts;
		}

		/* (non-Javadoc)
		 * @see net.sf.tweety.agents.Environment#reset()
		 */
		@Override
		public boolean reset() {
			this.commonView = new PerceivableStructuredArgumentationFramework();
			return true;
		}
		
	}
	
	/**
	 * Creates a new StructuredArgumentationSystem
	 * @param saf the underlying perceivable structured argumentation framework
	 */
	public StructuredArgumentationSystem(PerceivableStructuredArgumentationFramework saf) {
		this(saf,new HashSet<SasAgent>());
	}
	
	/**
	 * Creates a new StructuredArgumentationSystem with the given collection of agents.
	 * @param saf the underlying perceivable structured argumentation framework
	 * @param agents a collection of agents.
	 */
	public StructuredArgumentationSystem(PerceivableStructuredArgumentationFramework saf, Collection<? extends SasAgent> agents){
		super(new SasEnvironment(saf),agents);
	}
}
