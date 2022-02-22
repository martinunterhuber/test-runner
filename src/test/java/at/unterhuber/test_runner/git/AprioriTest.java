package at.unterhuber.test_runner.git;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class AprioriTest {
    private Apriori apriori;

    public void assertCombination(Set<Integer> left, Set<Integer> right, Apriori.Combination<Integer> combination) {
        Assertions.assertEquals(left, combination.getLeftSet());
        Assertions.assertEquals(right, combination.getRightSet());
    }

    @Test
    public void testApriori1() {
        List<Set<Integer>> transactions = List.of(
                Set.of(1, 2, 3),
                Set.of(2, 3, 4),
                Set.of(2, 3),
                Set.of(1, 3)
        );
        apriori = new Apriori(transactions, 0.5, 0.8);
        List<Apriori.Combination<Integer>> combinations = apriori.find();
        Assertions.assertEquals(2, combinations.size());
        assertCombination(Set.of(1), Set.of(3), combinations.get(0));
        assertCombination(Set.of(2), Set.of(3), combinations.get(1));
    }

    @Test
    public void testApriori2() {
        List<Set<Integer>> transactions = List.of(
                Set.of(1, 2),
                Set.of(1, 2, 3),
                Set.of(1, 2),
                Set.of(1, 2, 3)
        );
        apriori = new Apriori(transactions, 0.7, 0.5);
        List<Apriori.Combination<Integer>> combinations = apriori.find();
        Assertions.assertEquals(2, combinations.size());
        assertCombination(Set.of(1), Set.of(2), combinations.get(0));
        assertCombination(Set.of(2), Set.of(1), combinations.get(1));
    }

    @Test
    public void testApriori3() {
        List<Set<Integer>> transactions = List.of(
                Set.of(1, 2, 3),
                Set.of(1, 2, 3, 4),
                Set.of(1, 2, 3, 5),
                Set.of(1, 4),
                Set.of(2, 5),
                Set.of(3, 4, 5)
        );
        apriori = new Apriori(transactions, 0.5, 0.8);
        List<Apriori.Combination<Integer>> combinations = apriori.find();
        Assertions.assertEquals(3, combinations.size());
        assertCombination(Set.of(1, 2), Set.of(3), combinations.get(0));
        assertCombination(Set.of(1, 3), Set.of(2), combinations.get(1));
        assertCombination(Set.of(2, 3), Set.of(1), combinations.get(2));
    }

    @Test
    public void testApriori4() {
        List<Set<Integer>> transactions = List.of(
                Set.of(1, 2, 3),
                Set.of(1, 2, 3),
                Set.of(2, 3),
                Set.of(2, 4, 5),
                Set.of(3, 6, 7)
        );
        apriori = new Apriori(transactions, 0.3, 0.8);
        List<Apriori.Combination<Integer>> combinations = apriori.find();
        Assertions.assertEquals(5, combinations.size());
        assertCombination(Set.of(1), Set.of(2), combinations.get(0));
        assertCombination(Set.of(1), Set.of(3), combinations.get(1));
        assertCombination(Set.of(1), Set.of(2, 3), combinations.get(2));
        assertCombination(Set.of(1, 2), Set.of(3), combinations.get(3));
        assertCombination(Set.of(1, 3), Set.of(2), combinations.get(4));
    }
}
