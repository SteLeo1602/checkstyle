/*
RightCurly
option = ALONE_OR_SINGLELINE
tokens = LITERAL_CASE

*/

// Java17
package com.puppycrawl.tools.checkstyle.checks.blocks.rightcurly;

public class InputRightCurlyCaseBlocksWithSwitchRuleAloneOrSingleline {

    public void testt0() {
        int mode = 0;
        switch (mode) {
            case 1 -> {int x = 1;}   // ok, single line
            case 2 -> {int x = 0;}
            case 3 -> System.out.println("x is 3");
        }
    }

    public static void test0() {
        int mode = 0;
        switch (mode) {
            case 1 -> {
                int
            x = 1;} // violation '}' at column 19 should be alone on a line'
            case 2 -> {
                int x = 0;
            }
            case 3 -> System.out.println("x is 3");
        }
    }

    public static void test() {
        int mode = 0;
        switch (mode) {
            case 1 -> {
                int x = 1;

            } default -> {          // violation '}' at column 13 should be alone on a line'
                int x = 0;
            }
        }
    }

    public static void test1() {
        int k = 0;
        switch (k) {
            case 1 -> {


           int x = 1; } case 2 -> {       // violation '}' at column 23 should be alone on a line'
                int x = 2;}     // violation '}' at column 27 should be alone on a line'

        }
    }

    public static void test2() {
         int k = 0;
         switch (k) {
            case 1 -> {int x = 1;

            }case 2 -> throw new RuntimeException();
            // violation above '}' at column 13 should be alone on a line'
         }
    }

    public static void test3() {
         int k = 0;
         switch (k) {
            case 1 -> {
                 int x = 1;
            }
            case 2 -> {
                    int x = 2;
            } }      // violation '}' at column 13 should be alone on a line'
    }

    public static void test4() {
        int mode = 0;
        switch (mode) { case 0 -> {int x = 1;} default -> {int x = 0;} }
        // violation above '}' at column 46 should be alone on a line'
    }

    public static void test5() {
        int mode = 0;
        switch (mode) {
            case 0 -> {
                int x = 1;
            } case 1 -> {int x = 2;}  // violation '}' at column 13 should be alone on a line'
        }
    }

    public static void test8() {
        int mode = 0;
        int x = 0;
        switch (mode) {
            case 0 -> throw new RuntimeException();
            case 1 -> x = 1;
            case 2 -> {x = 1; }      // ok, single line
            case 3 -> {x = 5; } }   // violation '}' at column 31 should be alone on a line'
    }
}
