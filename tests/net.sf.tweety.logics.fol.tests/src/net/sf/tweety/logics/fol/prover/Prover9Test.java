package net.sf.tweety.logics.fol.prover;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.tweety.commons.BeliefBase;
import net.sf.tweety.logics.fol.FolBeliefSet;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.writer.FolWriter;
import net.sf.tweety.logics.fol.writer.Prover9Writer;

public class Prover9Test {

	static FolTheoremProver e;
	FolWriter printer = new Prover9Writer();

	@BeforeClass
	public static void init() {
		e = new Prover9("C:\\app\\prover9\\bin\\prover9.exe");
	}

	@Test
	public void test1() throws Exception {
		FolParser parser = new FolParser();
		String source = "type(a) \n type(b) \n type(c) \n" + "a \n !b";
		BeliefBase<FolFormula> b = parser.parseBeliefBase(source);
		// printer.printBase(b);
		System.out.println(printer);
		assertFalse(e.query(b, parser.parseFormula("b")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("a")).getAnswerBoolean());
		assertFalse(e.query(b, parser.parseFormula("c")).getAnswerBoolean());
		assertFalse(e.query(b, parser.parseFormula("!c")).getAnswerBoolean());
	}

	@Test
	public void test2() throws Exception {
		FolParser parser = new FolParser();
		String source = "Animal = {horse, cow, lion} \n" + "type(Tame(Animal)) \n" + "type(Ridable(Animal)) \n"
				+ "Tame(cow) \n" + "!Tame(lion) \n" + "Ridable(horse) \n" + "forall X: (!Ridable(X) || Tame(X)) \n";
		BeliefBase<FolFormula> b = parser.parseBeliefBase(source);
		printer.printBase(b);
		System.out.println(printer);
		assertTrue(e.query(b, parser.parseFormula("Tame(cow)")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("exists X: (Tame(X))")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("Tame(horse)")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("!Ridable(lion)")).getAnswerBoolean());
	}

	@Test
	public void test3() throws Exception {
		FolParser parser = new FolParser();
		String source = "Animal = {horse, cow, lion} \n" + "Plant = {grass, tree} \n" + "type(Eats(Animal, Plant)) \n"
				+ "forall X: (!Eats(X,tree)) \n" + "Eats(cow, grass) \n"
				+ "forall X: (!Eats(cow, X) || Eats(horse, X)) \n" + "exists X: (Eats(lion, X))";
		BeliefBase<FolFormula> b = parser.parseBeliefBase(source);
		printer.printBase(b);
		System.out.println(printer);
		assertFalse(e.query(b, parser.parseFormula("Eats(lion, tree)")).getAnswerBoolean());
		assertFalse(e.query(b, parser.parseFormula("!Eats(lion, grass)")).getAnswerBoolean());
		// is not true according to the solver
		// assertTrue(e.query(b, (FolFormula)parser.parseFormula("Eats(lion,
		// grass)")));
		assertFalse(e.query(b, parser.parseFormula("Eats(horse, tree)")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("!Eats(horse, tree)")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("Eats(horse, grass)")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("exists X: (forall Y: (!Eats(Y, X)))")).getAnswerBoolean());
		assertFalse(e.query(b, parser.parseFormula("forall X: (forall Y: (Eats(Y, X)))")).getAnswerBoolean());
		assertTrue(e.query(b, parser.parseFormula("!(forall X: (forall Y: (Eats(Y, X))))")).getAnswerBoolean());
	}

	@Test
	public void test4() throws Exception {
		FolParser parser = new FolParser();
		String source = "type(a) \n type(b) \n type(c) \n" + "a \n !b";
		BeliefBase<FolFormula> b = parser.parseBeliefBase(source);
		// printer.printBase(b);
		System.out.println(printer);
		assertTrue(e.equivalent(b, parser.parseFormula("b"), parser.parseFormula("b")));
		assertFalse(e.equivalent(b, parser.parseFormula("a"), parser.parseFormula("b")));
		assertTrue(e.equivalent(b, parser.parseFormula("a && b"),
				parser.parseFormula("b && a")));

	}
}
