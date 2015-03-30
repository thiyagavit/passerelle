package fr.soleil.passerelle.testUtils.dataProviders;

import org.testng.annotations.DataProvider;

public class ComparisonProvider {

    public static final String NAME = "Comparison provider";

    @DataProvider(name = NAME)
    public static Object[][] getParametres() {
        return new Object[][] { //

                // TEST ON STRING
                { "aaaa", ">", "bbb", "0.0", false },// 0
                { "a", ">", "aa", "0.0", false },// 1
                { "ccc", ">", "ccc", "0.0", false }, // 2
                { "aA", ">", "aa", "0.0", false },// 3 ignore case
                { "b", ">", "a", "0.0", true },// 4

                { "aaaa", ">=", "bbb", "0.0", false },// 5
                { "a", ">=", "aa", "0.0", false },// 6
                { "ccc", ">=", "ccc", "0.0", true }, // 7
                { "aA", ">=", "aa", "0.0", true },// 8
                { "b", ">", "a", "0.0", true },// 9

                { "aaaa", "<", "bbb", "0.0", true },// 10
                { "a", "<", "aa", "0.0", true },// 11
                { "ccc", "<", "ccc", "0.0", false }, // 12
                { "aA", "<", "aa", "0.0", false },// 13 ignore case
                { "b", "<", "a", "0.0", false },// 14

                { "aaaa", "<=", "bbb", "0.0", true },// 15
                { "a", "<=", "aa", "0.0", true },// 16
                { "ccc", "<=", "ccc", "0.0", true }, // 17
                { "aA", "<=", "aa", "0.0", true },// 18 ignore case
                { "b", "<", "a", "0.0", false },// 19

                { "aaaa", "==", "bbb", "0.0", false },// 20
                { "a", "==", "aa", "0.0", false },// 21
                { "ccc", "==", "ccc", "0.0", true }, // 22
                { "aA", "==", "aa", "0.0", true },// 23 ignore case

                { "aaaa", "!=", "bbb", "0.0", true },// 24
                { "a", "!=", "aa", "0.0", true },// 25
                { "ccc", "!=", "ccc", "0.0", false }, // 26
                { "aA", "!=", "aa", "0.0", false },// 27 ignore case

                // TEST ON DOUBLE
                { "0.1", ">", "1.2", "0.0", false },// 28
                { "0.1", ">", "1.2", "0.1", false },// 29
                { "0.1", ">", "0.1", "0.0", false },// 30
                { "1.2", ">", "0.1", "0.0", true }, // 31
                { "0.1", ">", "1.2", "1.1", true },// 32
                { "0.1", ">", "0.1", "0.1", true },// 33

                { "0.1", ">=", "1.2", "0.0", false },// 34
                { "0.1", ">=", "1.2", "0.1", false },// 35
                { "0.1", ">=", "0.1", "0.0", true },// 36
                { "1.2", ">=", "0.1", "0.0", true }, // 37
                { "0.1", ">=", "1.2", "1.1", true },// 38
                { "0.1", ">=", "0.1", "0.1", true },// 39

                { "0.1", "<", "1.2", "0.0", true },// 40
                { "0.1", "<", "1.2", "0.1", true },// 41
                { "0.1", "<", "0.1", "0.0", false },// 42
                { "1.2", "<", "0.1", "0.0", false }, // 43
                { "0.1", "<", "1.2", "1.1", true },// 44
                { "0.1", "<", "0.1", "0.1", true },// 45

                { "0.1", "<=", "1.2", "0.0", true },// 46
                { "0.1", "<=", "1.2", "0.1", true },// 47
                { "0.1", "<=", "0.1", "0.0", true },// 48
                { "1.2", "<=", "0.1", "0.0", false }, // 49
                { "0.1", "<=", "1.2", "1.1", true },// 50
                { "0.1", "<=", "0.1", "0.1", true },// 51

                // test DOUBLE --STRING and STRING --DOUBLE
                { "1.2", ">", "bbb", "100.0", false },// 51
                { "bbb", ">", "1.2", "100.0", true },// 52

                { "1.2", ">=", "bbb", "100.0", false },// 53
                { "bbb", ">=", "1.2", "100.0", true },// 54

                { "1.2", "<", "bbb", "100.0", true },// 55
                { "bbb", "<", "1.2", "100.0", false },// 56

                { "1.2", "<=", "bbb", "100.0", true },// 57
                { "bbb", "<=", "1.2", "100.0", false },// 58

                { "1.2", "!=", "bbb", "100.0", true },// 59
                { "bbb", "!=", "1.2", "100.0", true },// 60

        };
    }
}
