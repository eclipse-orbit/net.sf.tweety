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
package net.sf.tweety.action.description.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import net.sf.tweety.action.description.CActionDescription;
import net.sf.tweety.action.description.syntax.CLaw;
import net.sf.tweety.action.description.syntax.DynamicLaw;
import net.sf.tweety.action.description.syntax.StaticLaw;
import net.sf.tweety.action.grounding.GroundingRequirement;
import net.sf.tweety.action.grounding.parser.GroundingRequirementsParser;
import net.sf.tweety.action.signature.ActionSignature;
import net.sf.tweety.commons.BeliefBase;
import net.sf.tweety.commons.Parser;
import net.sf.tweety.commons.ParserException;
import net.sf.tweety.logics.commons.syntax.Variable;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.AssociativeFOLFormula;
import net.sf.tweety.logics.fol.syntax.Conjunction;
import net.sf.tweety.logics.fol.syntax.Contradiction;
import net.sf.tweety.logics.fol.syntax.FOLAtom;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.Negation;
import net.sf.tweety.logics.fol.syntax.Tautology;

/**
 * This class implements a parser for causal laws in C. The BNF of such rules is given by: (starting symbol is DESC) <br>
 * <br> DESC         ::== LAW ("\n" LAW)*
 * <br> LAW         ::== STATICLAW | DYNAMICLAW 
 * <br> STATICLAW   ::== "caused" FOLFORMULA ("if" FOLFORMULA)? ("requires" REQUIREMENTS)?
 * <br> DYNAMICLAW  ::== "caused" FOLFORMULA ("if" FOLFORMULA)? "after" FOLFORMULA ("requires" REQUIREMENTS)? 
 * <br> REQUIREMENTS ::== REQUIREMENT ("," REQUIREMENT)* 
 * <br> REQUIREMENT  ::== (VARIABLENAME "<>" VARIABLENAME | VARIABLENAME "<>" CONSTANTNAME)*
 * <br>
 * where FOLFORMULA is an unquantified first-order formula without functors, <br>
 * and VARIABLENAME, CONSTANTNAME are sequences of symbols <br>
 * from {a,...,z,A,...,Z,0,...,9} with a letter at the beginning.
 * 
 * @author Sebastian Homann
 */
@Component(service = Parser.class)
public class CLawParser implements Parser<CLaw> {

  protected ActionSignature signature;

  public CLawParser() {
	super();
}

/**
   * This parser needs a valid action signature to parse causal laws.
   * @param signature
   */
  public CLawParser( ActionSignature signature ) {
    this.signature = signature;
  }

  /*
   * (non-Javadoc)
   * @see net.sf.tweety.Parser#parseBeliefBase(java.io.Reader)
   */
  @Override
  public BeliefBase<CLaw> parseBeliefBase( Reader reader )
    throws ParserException {
    CActionDescription actionDescription = new CActionDescription();

    String s = "";
    // read from the reader and separate formulas by "\n"
    try {
      int c;
      do {
        c = reader.read();
        if ( c == 10 || c == -1 ) {
          if ( !s.equals( "" ) && !s.trim().startsWith( "%" )) {
            actionDescription.add( (CLaw) this.parseFormula(  s ) );
          }
          s = "";
        } else {
          s += (char) c;
        }
      } while ( c != -1 );
    } catch ( Exception e ) {
      throw new ParserException( e );
    }
    return actionDescription;
  }

  
  /*
   * (non-Javadoc)
   * @see net.sf.tweety.Parser#parseFormula(java.io.Reader)
   */
  @Override
  public CLaw parseFormula( Reader reader )
    throws IOException, ParserException {
    String s = "";
    int c;
    do{
     c = reader.read();
     s += (char) c;
    } while(c != -1);
    return parseFormula(s);
  }

  /**
   * Parses a FolFormula from a string using the FolParser class
   * @param s
   * @return A first order formula
   * @throws ParserException
   * @throws IOException
   */
  protected FolFormula parseFolFormula( String s )
    throws ParserException, IOException {
    FolParser p = new FolParser();
    p.setSignature( signature );
    return (FolFormula) p.parseFormula( s );
  }
  
  /*
   * (non-Javadoc)
   * @see net.sf.tweety.Parser#parseFormula(java.lang.String)
   */
  @Override
  public CLaw parseFormula(String s) throws ParserException, IOException {
    s = s.trim();
    String reqString = null;
    // handle grounding requirements
    if(s.contains( " requires " )) {
      reqString = s.substring( s.indexOf( " requires " ) + 10 );
      Set<String> wrongKey = containedKeywords( reqString );
      if(!wrongKey.isEmpty())
        throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use.");
      s = s.substring( 0, s.indexOf( " requires " ) );
    }
    
    if(s.startsWith( "caused " ))
      return parseCausedFormula( s, reqString );
    if(s.startsWith( "inertial " ))
      return parseInertialFormula( s, reqString );
    if(s.startsWith( "default " ))
      return parseDefaultFormula( s, reqString );
    if(s.startsWith( "always " ))
      return parseAlwaysFormula( s, reqString );
    if(s.startsWith( "nonexecutable "))
      return parseNonexecutableFormula( s, reqString );
    if(s.contains( " causes " ))
      return parseCausesFormula( s, reqString );
    if(s.contains( " may cause "))
      return parseMayCauseFormula( s, reqString );
    
    throw new ParserException("Unsupported causal law: "+s);
  }
   
  /**
   * Parses a string containing a single causal law of the form
   *   caused A (if B)? (after C)?
   * @param s a string containing a single causal law.
   * @param reqString the grounding requirements of this law.
   * @return the corresponding causal law
   * @throws ParserException
   * @throws IOException
   */
  private CLaw parseCausedFormula(String s, String reqString) throws ParserException, IOException {
    if(!s.startsWith("caused "))
      throw new ParserException("Missing 'caused' expression in causal law: "+s);
    FolFormula afterFormula = null;
    FolFormula ifFormula = null;
    FolFormula headFormula = null;

    if(s.contains( " after " )) {
      String afterString = s.substring( s.indexOf( " after ") + 7 );
      Set<String> wrongKey = containedKeywords( afterString );
      if(!wrongKey.isEmpty())
        throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use.");
      afterFormula = parseFolFormula( afterString );
      s = s.substring( 0, s.indexOf( " after " ) );
    }
    if(s.contains( " if " )) {
      String ifString = s.substring( s.indexOf(" if ") + 4 );
      Set<String> wrongKey = containedKeywords( ifString );
      if(!wrongKey.isEmpty())
        throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use.");
      ifFormula = parseFolFormula( ifString );
      s = s.substring( 0, s.indexOf( " if ") );
    }

    String headString = s.substring( s.indexOf("caused ") + 7 );
    Set<String> wrongKey = containedKeywords( headString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    headFormula = parseFolFormula( headString );
    
    // parse grounding requirements
    Set<Variable> variables = new HashSet<Variable>();
    variables.addAll( headFormula.getUnboundVariables() );
    if(ifFormula != null)
      variables.addAll( ifFormula.getUnboundVariables() );
    if(afterFormula != null)
      variables.addAll( afterFormula.getUnboundVariables() );
    Set<GroundingRequirement> requirements = null;
    requirements = new GroundingRequirementsParser().parseRequirements( reqString, variables );
    
    if(afterFormula != null)
      return new DynamicLaw(headFormula, ifFormula, afterFormula,  requirements);
    else
      return new StaticLaw(headFormula, ifFormula, requirements);
  }
  
  /**
   * Parses a string containing a single causal law of the form
   *   inertial A
   * which is converted to the causal law
   *   caused A if A after A
   * @param s a string containing a single inertial law.
   * @param reqString the grounding requirements of this law.
   * @return the corresponding causal law
   * @throws ParserException
   * @throws IOException
   */
  private CLaw parseInertialFormula(String s, String reqString) throws ParserException, IOException {
    if(!s.startsWith("inertial "))
      throw new ParserException("Missing 'inertial' expression in causal law: "+s);
    FolFormula formula = null;
    String headString = s.substring( s.indexOf("inertial ") + 9 );
    Set<String> wrongKey = containedKeywords( headString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    formula = parseFolFormula( headString );
    if(formula instanceof AssociativeFOLFormula)
      throw new ParserException("Inertial expression contains illegal argument.");
    if(formula instanceof Negation)
      if(!((((Negation)formula).getFormula()) instanceof FOLAtom))
        throw new ParserException( "Inertial expression contains illegal argument." );
    Set<GroundingRequirement> requirements = null;
    requirements = new GroundingRequirementsParser().parseRequirements( reqString, formula.getUnboundVariables() );
    return new DynamicLaw(formula, formula, formula, requirements);
  }

  /**
   * Parses a string containing a single causal law of the form
   *   default A (if B)?
   * which is converted to the causal law
   *   caused A if A && B
   * @param s a string containing a single causal law.
   * @param reqString the grounding requirements of this law.
   * @return the corresponding causal law
   * @throws ParserException
   * @throws IOException
   */
  private CLaw parseDefaultFormula(String s, String reqString) throws ParserException, IOException {
    if(!s.startsWith("default "))
      throw new ParserException("Missing 'default' expression in causal law: "+s);
    FolFormula ifFormula = null;
    FolFormula headFormula = null;
    if(s.contains( " if " )) {
      String ifString = s.substring( s.indexOf(" if ") + 4 );
      Set<String> wrongKey = containedKeywords( ifString );
      if(!wrongKey.isEmpty())
        throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use.");
      ifFormula = parseFolFormula( ifString );
      s = s.substring( 0, s.indexOf( " if ") );
    }
    String headString = s.substring( s.indexOf("default ") + 8 );
    Set<String> wrongKey = containedKeywords( headString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    headFormula = parseFolFormula( headString );
    
    // parse grounding requirements
    Set<Variable> variables = new HashSet<Variable>();
    variables.addAll( headFormula.getUnboundVariables() );
    if(ifFormula != null)
      variables.addAll( ifFormula.getUnboundVariables() );
    Set<GroundingRequirement> requirements = null;
    requirements = new GroundingRequirementsParser().parseRequirements( reqString, variables );
    
    if(ifFormula != null) {
      Conjunction c = new Conjunction();
      c.add( headFormula );
      c.add( ifFormula );
      return new StaticLaw( headFormula, c, requirements );
    } else {
      return new StaticLaw( headFormula, headFormula, requirements );
    }
  }
  
  /**
   * Parses a string containing a single causal law of the form
   *   A causes B if C
   * which is converted to
   *   caused B if + after A && C
   * @param s a string containing a single causal law.
   * @param reqString the grounding requirements of this law.
   * @return the corresponding causal law
   * @throws ParserException
   * @throws IOException
   */
  private CLaw parseCausesFormula(String s, String reqString) throws ParserException, IOException {
    if(!s.contains( " causes " ))
      throw new ParserException("Missing 'causes' expression in causal law: "+s);
    FolFormula headFormula = null;
    FolFormula causesFormula = null;
    FolFormula ifFormula = null;

    if(s.contains( " if " )) {
      String ifString = s.substring( s.indexOf(" if ") + 4 );
      Set<String> wrongKey = containedKeywords( ifString );
      if(!wrongKey.isEmpty())
        throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use.");
      ifFormula = parseFolFormula( ifString );
      s = s.substring( 0, s.indexOf( " if ") );
    }
    String[] split = s.split( " causes " );
    String headString = split[0];
    Set<String> wrongKey = containedKeywords( headString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    headFormula = parseFolFormula( headString );
    
    String causesString = split[1];
    wrongKey = containedKeywords( causesString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    causesFormula = parseFolFormula( causesString );
    
    // parse grounding requirements
    Set<Variable> variables = new HashSet<Variable>();
    variables.addAll( headFormula.getUnboundVariables() );
    variables.addAll( causesFormula.getUnboundVariables() );
    if(ifFormula != null)
      variables.addAll( ifFormula.getUnboundVariables() );
    Set<GroundingRequirement> requirements = null;
    requirements = new GroundingRequirementsParser().parseRequirements( reqString, variables );
    
    if(ifFormula != null) {
      Conjunction c = new Conjunction();
      c.add( headFormula );
      c.add( ifFormula );
      return new DynamicLaw( causesFormula, new Tautology(), c, requirements );
    } else {
      return new DynamicLaw( causesFormula, new Tautology(), headFormula, requirements );
    }
  }
  
  /**
   * Parses a string containing a single causal law of the form
   *   always A
   * which is converted to
   *   caused - if !A
   * @param s a string containing a single causal law.
   * @param reqString the grounding requirements of this law.
   * @return the corresponding causal law
   * @throws ParserException
   * @throws IOException
   */
  private CLaw parseAlwaysFormula(String s, String reqString) throws ParserException, IOException {
    if(!s.startsWith( "always " ))
      throw new ParserException("Missing 'always' expression in causal law: "+s);
    FolFormula headFormula = null;

    Set<String> wrongKey = containedKeywords( s );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    headFormula = parseFolFormula( s );
    
    // parse grounding requirements
    Set<Variable> variables = new HashSet<Variable>();
    variables.addAll( headFormula.getUnboundVariables() );
    Set<GroundingRequirement> requirements = null;
    requirements = new GroundingRequirementsParser().parseRequirements( reqString, variables );
    
    return new StaticLaw(new Contradiction(), new Negation( headFormula ), requirements);
  }
  
  /**
   * Parses a string containing a single causal law of the form
   *   nonexecutable A if B
   * which is converted to
   *   caused - after A && B
   * @param s a string containing a single causal law.
   * @param reqString the grounding requirements of this law.
   * @return the corresponding causal law
   * @throws ParserException
   * @throws IOException
   */
  private CLaw parseNonexecutableFormula(String s, String reqString) throws ParserException, IOException {
    if(!s.startsWith("nonexecutable "))
      throw new ParserException("Missing 'nonexecutable' expression in causal law: "+s);
    FolFormula ifFormula = null;
    FolFormula headFormula = null;
    if(s.contains( " if " )) {
      String ifString = s.substring( s.indexOf(" if ") + 4 );
      Set<String> wrongKey = containedKeywords( ifString );
      if(!wrongKey.isEmpty())
        throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use.");
      ifFormula = parseFolFormula( ifString );
      s = s.substring( 0, s.indexOf( " if ") );
    }
    String headString = s.substring( s.indexOf("nonexecutable ") + 14 );
    Set<String> wrongKey = containedKeywords( headString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    headFormula = parseFolFormula( headString );
    
    // parse grounding requirements
    Set<Variable> variables = new HashSet<Variable>();
    variables.addAll( headFormula.getUnboundVariables() );
    if(ifFormula != null)
      variables.addAll( ifFormula.getUnboundVariables() );
    Set<GroundingRequirement> requirements = null;
    requirements = new GroundingRequirementsParser().parseRequirements( reqString, variables );
    
    if(ifFormula != null) {
      Conjunction c = new Conjunction();
      c.add( headFormula );
      c.add( ifFormula );
      return new DynamicLaw( new Contradiction(), c, requirements );
    } else {
      return new DynamicLaw( new Contradiction(), headFormula, requirements );
    }
  }
  
  /**
   * Parses a string containing a single causal law of the form
   *   A may cause B if C
   * which is converted to
   *   caused B if B after A && C
   * @param s a string containing a single causal law.
   * @param reqString the grounding requirements of this law.
   * @return the corresponding causal law
   * @throws ParserException
   * @throws IOException
   */
  private CLaw parseMayCauseFormula(String s, String reqString) throws ParserException, IOException {
    if(!s.contains( " may cause " ))
      throw new ParserException("Missing 'may cause' expression in causal law: "+s);
    FolFormula headFormula = null;
    FolFormula maycauseFormula = null;
    FolFormula ifFormula = null;

    if(s.contains( " if " )) {
      String ifString = s.substring( s.indexOf(" if ") + 4 );
      Set<String> wrongKey = containedKeywords( ifString );
      if(!wrongKey.isEmpty())
        throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use.");
      ifFormula = parseFolFormula( ifString );
      s = s.substring( 0, s.indexOf( " if ") );
    }
    String[] split = s.split( " may cause " );
    String headString = split[0];
    Set<String> wrongKey = containedKeywords( headString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    headFormula = parseFolFormula( headString );
    
    String maycauseString = split[1];
    wrongKey = containedKeywords( maycauseString );
    if(!wrongKey.isEmpty())
      throw new ParserException("Unexpected keyword(s) '"+wrongKey.toString()+"' in formula '"+s+"' due to wrong order or multiple use of keywords.");
    maycauseFormula = parseFolFormula( maycauseString );
    
    // parse grounding requirements
    Set<Variable> variables = new HashSet<Variable>();
    variables.addAll( headFormula.getUnboundVariables() );
    variables.addAll( maycauseFormula.getUnboundVariables() );
    if(ifFormula != null)
      variables.addAll( ifFormula.getUnboundVariables() );
    Set<GroundingRequirement> requirements = null;
    requirements = new GroundingRequirementsParser().parseRequirements( reqString, variables );
    
    if(ifFormula != null) {
      Conjunction c = new Conjunction();
      c.add( headFormula );
      c.add( ifFormula );
      return new DynamicLaw( maycauseFormula, maycauseFormula, c, requirements );
    } else {
      return new DynamicLaw( maycauseFormula, maycauseFormula, headFormula, requirements );
    }
  }
  
  /**
   * Returns the set of forbidden keywords, that are contained in a string. This is used
   * for error recognition in input strings.
   * @param s
   */
  protected Set<String> containedKeywords(String s) {
    final String[] keywords = {"caused ", " if ", " after ", " requires ", "inertial ", "default ", " causes ", "always ", "nonexecutable ", " may cause " };
    Set<String> result = new HashSet<String>();
    for(String key : keywords) {
      if(s.contains( key ))
        result.add( key );
    }
    return result;
  }
}