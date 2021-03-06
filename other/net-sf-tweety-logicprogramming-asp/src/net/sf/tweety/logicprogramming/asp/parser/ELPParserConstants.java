/*
 *  This file is part of "Tweety", a collection of Java libraries for
 *  logical aspects of artificial intelligence and knowledge representation.
 *
 *  Tweety is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3 as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/* Generated By:JavaCC: Do not edit this line. ELPParserConstants.java */
package net.sf.tweety.logicprogramming.asp.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface ELPParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int RAUTE = 5;
  /** RegularExpression Id. */
  int NOT = 6;
  /** RegularExpression Id. */
  int INT = 7;
  /** RegularExpression Id. */
  int MAXINT = 8;
  /** RegularExpression Id. */
  int AGGREG = 9;
  /** RegularExpression Id. */
  int LBRA = 10;
  /** RegularExpression Id. */
  int RBRA = 11;
  /** RegularExpression Id. */
  int COLON = 12;
  /** RegularExpression Id. */
  int PIPE = 13;
  /** RegularExpression Id. */
  int NAME = 14;
  /** RegularExpression Id. */
  int COMP = 15;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"#\"",
    "\"not\"",
    "\"int\"",
    "\"maxint\"",
    "<AGGREG>",
    "\"{\"",
    "\"}\"",
    "\":\"",
    "\"|\"",
    "<NAME>",
    "<COMP>",
    "\" v \"",
    "\".\"",
    "\":-\"",
    "\",\"",
    "\"(\"",
    "\")\"",
    "\"=\"",
    "\"~\"",
    "\"-\"",
  };

}
