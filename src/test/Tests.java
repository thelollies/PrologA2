package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import jpl.Atom;
import jpl.Query;
import jpl.Term;
import jpl.Util;
import jpl.Variable;

import org.junit.BeforeClass;
import org.junit.Test;

public class Tests {

	@BeforeClass
	public static void setupOnce(){
		new Query("[tanks]").hasSolution();
		new Query("[squaremap]").hasSolution();
	}

	@Test
	public void makeTanks() {
		// Can create a tank
		assertNotNull(new Query("tank(red, one)").oneSolution());

		// Cannot create a tank with an invalid city
		assertNull(new Query("tank(red, notacity)").oneSolution());

		// Cannot create a tank with an invalid team
		assertNull(new Query("tank(invalidteam, one)").oneSolution());
	}

	@Test
	public void tankMovement(){

		// Get squaremap's initial tanks positions
		// Black tanks on 6/11/12, Red on 2/3/8
		List<TestTank> positions = initialPositions();

		// Shows that the tank is in six and one is empty
		assertFalse(positions.contains(new TestTank(true, "one")));
		assertTrue(positions.contains(new TestTank(true, "six")));


		// Check that you can move to an empty adjacent city
		Query q1 = new Query("moveToPoint", 
				new Term[]{new Atom("six"), new Atom("one"), 
				tanksToTerm(positions), new Variable("Result")});

		// Tank is in new position and Tank is not in old position
		List<TestTank> validMove = termToTanks((Term)q1.oneSolution().get("Result"));
		assertTrue(validMove.contains(new TestTank(true, "one")));
		assertFalse(validMove.contains(new TestTank(true, "six")));

		// Check that you cannot move to an occupied city
		Query q2 = new Query("moveToPoint", 
				new Term[]{new Atom("six"), new Atom("eleven"), 
				tanksToTerm(positions), new Variable("Result")});
		assertNull(q2.oneSolution());

		// Check that you cannot move to a city that is not adjacent 
		Query q3 = new Query("moveToPoint", 
				new Term[]{new Atom("six"), new Atom("thirteen"), 
				tanksToTerm(positions), new Variable("Result")});
		assertNull(q3.oneSolution());

		// Check that From must have a tank in that location
		Query q4 = new Query("moveToPoint", 
				new Term[]{new Atom("one"), new Atom("four"), 
				tanksToTerm(positions), new Variable("Result")});
		assertNull(q4.oneSolution());
	}

	/**
	 * Tests whether an attack on an adjacent enemy tank succeeds and
	 * attacking a non adjacent/friendly/non-existant tank or attacking from
	 * a city with no tank in it is invalid.
	 */
	@Test
	public void tankAttack(){
		// Get squaremap's initial tanks positions
		// Black tanks on 6/11/12, Red on 2/3/8
		List<TestTank> positions = initialPositions();

		// Move a black tank such that a red one is within range
		Query q0 = new Query("moveToPoint", 
				new Term[]{new Atom("six"), new Atom("one"), 
				tanksToTerm(positions), new Variable("Result")});
		List<TestTank> attackReady = termToTanks((Term)q0.oneSolution().get("Result"));

		// Friendly tank at one, enemy at two
		assertTrue(attackReady.contains(new TestTank(true, "one")));
		assertTrue(attackReady.contains(new TestTank(false, "two")));

		Query q1 = new Query("fireOnPoint", 
				new Term[]{new Atom("one"), new Atom("two"),
				tanksToTerm(attackReady), new Variable("Result")});

		List<TestTank> firedOn = termToTanks((Term)q1.oneSolution().get("Result"));

		// Enemy adjacent tank can be killed
		assertFalse(firedOn.contains(new TestTank(false, "two")));

		Query q2 = new Query("fireOnPoint", 
				new Term[]{new Atom("one"), new Atom("three"),
				tanksToTerm(attackReady), new Variable("Result")});

		// Cannot attack non-adjacent
		assertNull(q2.oneSolution());

		Query q3 = new Query("fireOnPoint", 
				new Term[]{new Atom("eleven"), new Atom("twelve"),
				tanksToTerm(attackReady), new Variable("Result")});

		// Cannot attack friendly
		assertNull(q3.oneSolution());

		Query q4 = new Query("fireOnPoint", 
				new Term[]{new Atom("one"), new Atom("four"),
				tanksToTerm(attackReady), new Variable("Result")});

		// Cannot attack non-existant
		assertNull(q4.oneSolution());

		Query q5 = new Query("fireOnPoint", 
				new Term[]{new Atom("four"), new Atom("two"),
				tanksToTerm(attackReady), new Variable("Result")});

		// Cannot attack from city with no tank
		assertNull(q5.oneSolution());
	}

	// Tests whether adjacent checking is valid using expected results
	// Also checks whether adjacent(city1, city2) implies adjacent(city2, city1)
	@Test
	public void adjacent(){
		assertNotNull(new Query("adjacent(one, two)").oneSolution());
		assertNotNull(new Query("adjacent(two, one)").oneSolution());
		assertNull(new Query("adjacent(one, seven)").oneSolution());
		assertNull(new Query("adjacent(seven, one)").oneSolution());
	}

	// Tests whether the correct number of killable tanks is calculated
	@Test
	public void tanksKillable(){
		// Get squaremap's initial tanks positions
		// Black tanks on 6/11/12, Red on 2/3/8
		List<TestTank> positions = initialPositions();

		Query q = new Query("tanksKillable", 
				new Term[]{new Atom("six"), tanksToTerm(positions), 
				new Variable("Move"), new Variable("Killable")});
		
		// Check that correctly calculates two tanks are killable
		assertEquals(((Term)q.oneSolution().get("Killable")).intValue(), 2);
	}
	
	// Tests the correctness of win checking
	@Test
	public void win(){
		List<TestTank> oneOfEach = Arrays.asList(new TestTank[]{
				new TestTank(true, "eleven"),
				new TestTank(false, "three")});
		
		List<TestTank> blackOnly = oneOfEach.subList(0, 1);
		List<TestTank> redOnly = oneOfEach.subList(1, 2);
		
		// Neither team has won when there is one of each tank left
		assertNull(new Query("won", new Term[]{new Atom("black"), tanksToTerm(oneOfEach)}).oneSolution());
		assertNull(new Query("won", new Term[]{new Atom("red"), tanksToTerm(oneOfEach)}).oneSolution());
		
		// Black wins when no reds left
		assertNotNull(new Query("won", new Term[]{new Atom("black"), tanksToTerm(blackOnly)}).oneSolution());
		
		// Red wins when no blacks left
		assertNotNull(new Query("won", new Term[]{new Atom("red"), tanksToTerm(redOnly)}).oneSolution());

		// Check that win condition doesn't just check for all tanks of one type
		assertNull(new Query("won", new Term[]{new Atom("red"), tanksToTerm(blackOnly)}).oneSolution());
		assertNull(new Query("won", new Term[]{new Atom("black"), tanksToTerm(redOnly)}).oneSolution());
	}

	// Test the addOne predicate to check it actually adds a tank
	@Test
	public void addOne(){
		List<TestTank> initialPositions = initialPositions();
		Query q = new Query("addOne", 
				new Term[]{new Atom("black"), tanksToTerm(initialPositions),
				new Variable("Result")});
		
		List<TestTank> oneAdded = termToTanks((Term)q.oneSolution().get("Result"));
		
		// One has been added
		assertEquals(initialPositions.size() + 1, oneAdded.size());
		
		oneAdded.removeAll(initialPositions);
		// Shows that original tanks were kept, only new one remains
		assertEquals(1, oneAdded.size());
		
		// Shows correct team added
		assertTrue(oneAdded.get(0).friendly);
		
	}
	
	// Gives list containing tanks as specified in initialPositions
	private List<TestTank> initialPositions(){
		Query tankList = new Query("initialTankPositions", new Variable("T"));
		Hashtable<?, ?> result = tankList.oneSolution();
		return termToTanks((Term)result.get("T"));
	}

	// Converts a term to a list of tanks
	private List<TestTank> termToTanks(Term tankTerm){
		Term[] tankArray = tankTerm.toTermArray();

		List<TestTank> tanks = new ArrayList<TestTank>();

		for(Term t : tankArray){
			Term[] arg = t.args();
			boolean friendly = arg[0].toString().equals("black");
			String city = arg[1].toString();
			tanks.add(new TestTank(friendly, city));
		}
		return tanks;
	}

	// Converts a list of tanks to a term
	private Term tanksToTerm(List<TestTank> tanks){
		StringBuilder tankString = new StringBuilder("[");

		for(int i = 0; i < tanks.size(); i++){
			TestTank t = tanks.get(i);
			String team = t.friendly ? "black" : "red";
			String city = t.city;
			String comma = i + 1 != tanks.size() ? ", " : ""; 
			tankString.append(String.format("tank(%s, %s)%s", team, city, comma));	
		}
		tankString.append("]");
		return Util.textToTerm(tankString.toString());
	}

	// Dummy tank object for testing
	private class TestTank{
		public final boolean friendly;
		public final String city;

		public TestTank(boolean friendly, String city){
			this.friendly = friendly;
			this.city = city;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof TestTank)) return false;

			TestTank tank = (TestTank) obj;
			return tank.friendly == friendly && tank.city.equals(city);
		}
	}
}
