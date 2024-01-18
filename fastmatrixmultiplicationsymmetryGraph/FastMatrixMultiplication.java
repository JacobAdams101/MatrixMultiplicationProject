//package fastmatrixmultiplication;
/**
 *
 * @author jacob
 */
public class FastMatrixMultiplication
{
    final static int DEFAULT_N = 2;
    final static int DEFAULT_M = 2;
    final static int DEFAULT_P = 2;

    final static boolean ENABLE_TESTING = true;

    final static boolean USE_REDUCED_SYMMETRY = true;
    final static boolean TREAT_ALL_AS_SYMMETRIC = true;

    final static boolean USE_EXPANDED_SYMMETRY = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        //System.out.println(MultiplicationMethod.getBasicMethod(DEFAULT_N, DEFAULT_M, DEFAULT_P));
        //test();


        int n = DEFAULT_N;
        int m = DEFAULT_M;
        int p = DEFAULT_P;
        if (args.length == 0)
        {
            n = DEFAULT_N;
            m = DEFAULT_M;
            p = DEFAULT_P;
        }
        else if (args.length == 3)
        {
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
            p = Integer.parseInt(args[2]);
        }
        else
        {
            System.out.println("Invalid number of arguments!");
            return;
        }
        //For graph walk
        runWalk(n,m,p);
        //For graph explore
        //runGraphExplore(n,m,p);

    }

    public static void runGraphExplore(int n, int m, int p) throws Exception {
        Graph x = new Graph();

        x.exploreGraph(n, m, p, ENABLE_TESTING, USE_REDUCED_SYMMETRY, TREAT_ALL_AS_SYMMETRIC);
    }

    public static void runWalk(int n, int m, int p) throws Exception
    {
        MultiplicationMethod x = MultiplicationMethod.getBasicMethod(n, m, p);



        System.out.println("====== RUN RANDOM WALK ON (" + n + ", " + m + ", " + p + ") ======");

        System.out.println(x);



        if (USE_REDUCED_SYMMETRY)
        {
            System.out.println("====== REDUCE TO SYMMETRY =======");

            x.reduceToSymmetry(TREAT_ALL_AS_SYMMETRIC);
            System.out.println(x);
            System.out.println("======= TEST SYMMETRY REDUCTION ======");
            if (x.testValidity())
            {
                System.out.println("======= VALID =======");
            }
            else
            {
                System.out.println("======= FAIL  =======");
            }
        }

        if (USE_EXPANDED_SYMMETRY)
        {
            System.out.println("====== EXPAND BY SYMMETRY =======");

            x.expandBySymmetry();
            System.out.println(x);

            System.out.println("======= TEST SYMMETRY REDUCTION ======");
            if (x.testValidity())
            {
                System.out.println("======= VALID =======");
            }
            else
            {
                System.out.println("======= FAIL  =======");
            }
        }

        System.out.println("");
        System.out.println("====== START WALK =======");
        x.randomWalk(ENABLE_TESTING);



    }

}


/*
A * B * A
B * A * A
A * A * B

A * (B+A) * A         <-
B * A     * A
A * A     * (B+A)     <-

A     * (B+A) * A
(B+A) * A     * A     <-
A     * A     * B     <-

A     * (B+A) * A     <-
(B+A) * A     * A     <-
A     * A     * B


A * (B+A) * A  <-
(B-A) * (A+A+B) * A <-
A * A * (B-A)
=
A * (B+A) * A
(B+A) * B * A
A * A * (B+A)
}
*/
