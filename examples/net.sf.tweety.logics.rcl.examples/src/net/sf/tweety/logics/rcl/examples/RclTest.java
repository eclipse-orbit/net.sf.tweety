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
package net.sf.tweety.logics.rcl.examples;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.sf.tweety.commons.ParserException;
import net.sf.tweety.commons.TweetyConfiguration;
import net.sf.tweety.commons.TweetyLogging;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.rcl.RclBeliefSet;
import net.sf.tweety.logics.rcl.RelationalBruteForceCReasoner;
import net.sf.tweety.logics.rcl.parser.RclParser;
import net.sf.tweety.logics.rcl.semantics.RelationalRankingFunction;

public class RclTest {

	public static void main(String[] args) throws FileNotFoundException, ParserException, IOException{
		TweetyLogging.logLevel = TweetyConfiguration.LogLevel.DEBUG;
		TweetyLogging.initLogging();
		
		RclParser parser = new RclParser();
		RclBeliefSet bs = (RclBeliefSet) parser.parseBeliefBaseFromFile(args[0]);
		System.out.println("Knowledge base:\n " + bs);
		
		RelationalRankingFunction kappa = new RelationalBruteForceCReasoner(parser.getSignature(),true).getCRepresentation(bs);
		System.out.println("Simple c-representation:\n" + kappa);		
		
		System.out.println();
		System.out.println("Queries: ");
		
		FolParser pars = new FolParser();
		pars.setSignature(parser.getSignature());
		
		if(args[1] != null && !args[1].equals("")){
			BufferedReader in = new BufferedReader(new FileReader(args[1]));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println(line + "\t:\t" + kappa.rank((FolFormula)pars.parseFormula(line)));				
			}
			in.close();
		}
	}
}
