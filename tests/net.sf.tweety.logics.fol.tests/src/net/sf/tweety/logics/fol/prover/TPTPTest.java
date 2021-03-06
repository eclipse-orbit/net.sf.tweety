package net.sf.tweety.logics.fol.prover;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.tweety.commons.util.Shell;
import net.sf.tweety.logics.fol.FolBeliefSet;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.writer.FolWriter;
import net.sf.tweety.logics.fol.writer.TptpWriter;

/**
 * JUnitTest to test TPTP priter and Eprover implemnetation
 * 
 * @author Nils Geilen
 *
 */

public class TPTPTest {

	static FolTheoremProver e;
	FolWriter printer = new TptpWriter();

	@BeforeClass
	public static void init() {
		if (System.getProperty("os.name").matches("Win.*")) {
			System.out.println("Initializing Eprover Test for Windows");
			e = new EProver("C:/app/E/PROVER/eprover.exe", Shell.getCygwinShell("C:/cygwin64/bin/bash.exe"));
		} else {
			System.out.println("Initializing Eprover Test for Unix");
			e = new EProver("/home/nils/app/E/PROVER/eprover", Shell.getNativeShell());
			// e = new
			// EProver("/Users/mthimm/Projects/misc_bins/eprover/eprover");
		}
	}

	@Test
	public void test1() throws Exception {
		FolParser parser = new FolParser();
		String source = "type(a) \n type(b) \n type(c) \n" + "a \n !b";
		FolBeliefSet b = parser.parseBeliefBase(source);
		printer.printBase(b);
		System.out.println(printer);
		assertFalse(e.query(b, (FolFormula) parser.parseFormula("b")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("a")).getAnswerBoolean());
		assertFalse(e.query(b, (FolFormula) parser.parseFormula("c")).getAnswerBoolean());
		assertFalse(e.query(b, (FolFormula) parser.parseFormula("!c")).getAnswerBoolean());
	}

	@Test
	public void test2() throws Exception {
		FolParser parser = new FolParser();
		String source = "Animal = {horse, cow, lion} \n" + "type(Tame(Animal)) \n" + "type(Ridable(Animal)) \n"
				+ "Tame(cow) \n" + "!Tame(lion) \n" + "Ridable(horse) \n" + "forall X: (!Ridable(X) || Tame(X)) \n";
		FolBeliefSet b = parser.parseBeliefBase(source);
		printer.printBase(b);
		System.out.println(printer);
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("Tame(cow)")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("exists X: (Tame(X))")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("Tame(horse)")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("!Ridable(lion)")).getAnswerBoolean());
	}

	@Test
	public void test3() throws Exception {
		FolParser parser = new FolParser();
		String source = "Animal = {horse, cow, lion} \n" + "Plant = {grass, tree} \n" + "type(Eats(Animal, Plant)) \n"
				+ "forall X: (!Eats(X,tree)) \n" + "Eats(cow, grass) \n"
				+ "forall X: (!Eats(cow, X) || Eats(horse, X)) \n" + "exists X: (Eats(lion, X))";
		FolBeliefSet b = parser.parseBeliefBase(source);
		printer.printBase(b);
		System.out.println(printer);
		assertFalse(e.query(b, (FolFormula) parser.parseFormula("Eats(lion, tree)")).getAnswerBoolean());
		assertFalse(e.query(b, (FolFormula) parser.parseFormula("!Eats(lion, grass)")).getAnswerBoolean());
		// is not true according to the solver
		// assertTrue(e.query(b, (FolFormula)parser.parseFormula("Eats(lion,
		// grass)")));
		assertFalse(e.query(b, (FolFormula) parser.parseFormula("Eats(horse, tree)")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("!Eats(horse, tree)")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("Eats(horse, grass)")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("exists X: (forall Y: (!Eats(Y, X)))")).getAnswerBoolean());
		assertFalse(e.query(b, (FolFormula) parser.parseFormula("forall X: (forall Y: (Eats(Y, X)))")).getAnswerBoolean());
		assertTrue(e.query(b, (FolFormula) parser.parseFormula("!(forall X: (forall Y: (Eats(Y, X))))")).getAnswerBoolean());
	}

}
