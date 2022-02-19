package at.unterhuber.test_runner.git;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class AprioriTest {
    private Apriori apriori;

    @BeforeEach
    public void setup() {
        List<Set<Integer>> transactions = List.of(
                Set.of(1, 2, 3),
                Set.of(2, 3, 4),
                Set.of(2, 3),
                Set.of(1, 3)
        );
        apriori = new Apriori(transactions, 0.5, 0.8);
    }

    public void assertCombination(Set<Integer> left, Set<Integer> right, Apriori.Combination<Integer> combination) {
        Assertions.assertEquals(left, combination.getLeftSet());
        Assertions.assertEquals(right, combination.getRightSet());
    }

    @Test
    public void testApriori() {
        List<Apriori.Combination<Integer>> combinations = apriori.find();
        Assertions.assertEquals(2, combinations.size());
        assertCombination(Set.of(1), Set.of(3), combinations.get(0));
        assertCombination(Set.of(2), Set.of(3), combinations.get(1));
    }
}
