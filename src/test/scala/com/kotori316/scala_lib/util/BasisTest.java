package com.kotori316.scala_lib.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cats.kernel.CommutativeGroup;
import cats.kernel.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scala.jdk.javaapi.CollectionConverters;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SuppressWarnings("serial")
    // Just a test class
class BasisTest {

    Basis<Integer> integerBasis;
    Group<Integer> integerGroup;
    Basis<Pair> pairBasis;
    Group<Pair> pairGroup;

    @BeforeEach
    void setUp() {
        integerBasis = () -> CollectionConverters.asScala(Collections.singletonList(1)).toList();
        integerGroup = new CommutativeGroup<Integer>() {
            @Override
            public Integer inverse(Integer integer) {
                return -integer;
            }

            @Override
            public Integer empty() {
                return 0;
            }

            @Override
            public Integer combine(Integer x, Integer y) {
                return x + y;
            }
        };
        pairBasis = () -> CollectionConverters.asScala(Arrays.asList(new Pair(1, 0), new Pair(0, 1))).toList();
        pairGroup = new CommutativeGroup<Pair>() {
            @Override
            public Pair inverse(Pair pair) {
                return new Pair(-pair.x, -pair.y);
            }

            @Override
            public Pair empty() {
                return Pair.ZERO;
            }

            @Override
            public Pair combine(Pair x, Pair y) {
                return new Pair(x.x + y.x, x.y + y.y);
            }

        };
    }

    @Test
    void nextTuple() {
        List<Pair> expected = Arrays.asList(new Pair(1, 0), new Pair(-1, 0), new Pair(0, 1), new Pair(0, -1));
        assertIterableEquals(expected, CollectionConverters.asJava(Basis.next(Pair.ZERO, pairBasis, pairGroup)));
        assertIterableEquals(expected.stream().map(p -> pairGroup.combine(p, new Pair(1, 0))).collect(Collectors.toList()),
            CollectionConverters.asJava(Basis.next(new Pair(1, 0), pairBasis, pairGroup)));
    }

    @Test
    void nextNum() {
        assertIterableEquals(Arrays.asList(1, -1), CollectionConverters.asJava(Basis.next(0, integerBasis, integerGroup)));
        assertIterableEquals(Arrays.asList(2, 0), CollectionConverters.asJava(Basis.next(1, integerBasis, integerGroup)));
    }

    static class Pair {
        static final Pair ZERO = new Pair(0, 0);
        final int x, y;

        Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Pair{" +
                "x=" + x +
                ", y=" + y +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return x == pair.x &&
                y == pair.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}