
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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
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

        runWalk(n,m,p, ENABLE_TESTING);

    }

    public static void runWalk(int n, int m, int p, boolean enableTesting) throws Exception
    {
        MultiplicationMethod x = MultiplicationMethod.getBasicMethod(n, m, p);



        System.out.println("====== RUN RANDOM WALK ON (" + n + ", " + m + ", " + p + ") ======");

        x.printMethod();

        final boolean USE_REDUCED_SYMMETRY = true;
        final boolean USE_EXPANDED_SYMMETRY = false;

        if (USE_REDUCED_SYMMETRY)
        {
            System.out.println("====== REDUCE TO SYMMETRY =======");

            x.reduceToSymmetry();
            x.printMethod();

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
            x.printMethod();

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
        x.randomWalk(enableTesting);



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
