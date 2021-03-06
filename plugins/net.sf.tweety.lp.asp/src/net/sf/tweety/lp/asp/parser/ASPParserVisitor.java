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
/* Generated By:JavaCC: Do not edit this line. ASPParserVisitor.java Version 5.0 */
package net.sf.tweety.lp.asp.parser;

public interface ASPParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTAnswerSetList node, Object data);
  public Object visit(ASTAnswerSet node, Object data);
  public Object visit(ASTProgram node, Object data);
  public Object visit(ASTRule node, Object data);
  public Object visit(ASTHead node, Object data);
  public Object visit(ASTElementLst node, Object data);
  public Object visit(ASTAggregate node, Object data);
  public Object visit(ASTSymbolicSet node, Object data);
  public Object visit(ASTArithmetic node, Object data);
  public Object visit(ASTComparative node, Object data);
  public Object visit(ASTDefAtom node, Object data);
  public Object visit(ASTAtom node, Object data);
  public Object visit(ASTFunctionalTerm node, Object data);
  public Object visit(ASTSetTerm node, Object data);
  public Object visit(ASTListTail node, Object data);
  public Object visit(ASTListTerm node, Object data);
  public Object visit(ASTTermLst node, Object data);
  public Object visit(ASTTerm node, Object data);
  public Object visit(ASTSimpleTerm node, Object data);
  public Object visit(ASTArithmeticInteger node, Object data);
  public Object visit(ASTNumber node, Object data);
  public Object visit(ASTIdLst node, Object data);
  public Object visit(ASTSpecId node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTCompareOp node, Object data);
  public Object visit(ASTArithmeticOp node, Object data);
}
/* JavaCC - OriginalChecksum=4938ba66312c508aebcaab4f0945b006 (do not edit this line) */
