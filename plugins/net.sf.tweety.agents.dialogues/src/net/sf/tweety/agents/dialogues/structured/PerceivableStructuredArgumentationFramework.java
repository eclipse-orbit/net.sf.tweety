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

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.agents.Perceivable;
import net.sf.tweety.arg.saf.StructuredArgumentationFramework;
import net.sf.tweety.commons.BeliefBase;

/**
 * This class packs a structured argumentation framework into a
 * perceivable object.
 * 
 * @author Matthias Thimm
 *
 */
@Component(service = BeliefBase.class)
public class PerceivableStructuredArgumentationFramework extends StructuredArgumentationFramework implements Perceivable {
}
