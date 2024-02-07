//package fastmatrixmultiplicationsymmetryGraph;

/**
 *
 * @author jacob
 */
public class FastMatrixMultiplication
{
    final static int DEFAULT_N = 3;
    final static int DEFAULT_M = 3;
    final static int DEFAULT_P = 3;

    final static boolean ENABLE_TESTING = true;

    final static boolean USE_REDUCED_SYMMETRY = true;
    final static boolean TREAT_ALL_AS_SYMMETRIC = true;

    final static boolean USE_EXPANDED_SYMMETRY = false;

    final static int NUM_THREADS = 64;
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

        boolean testing = ENABLE_TESTING;
        boolean reduceSymmetry = USE_REDUCED_SYMMETRY;
        boolean treatAllAsSymmetric = TREAT_ALL_AS_SYMMETRIC;
        boolean expandSymmetry = USE_EXPANDED_SYMMETRY;

        if (args.length > 7 || (args.length < 3 && args.length != 0))
        {
            System.out.println("Invalid number of arguments!");
            return;
        }

        //FastMatrixMultiplication n m p testing reduceSymmetry treatAllAsSymmetric expandSymmetry

        if (args.length >= 3)
        {
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
            p = Integer.parseInt(args[2]);
        }
        if (args.length >= 4)
        {
            if (isTrue(args[3]))
            {
                testing = true;
            }
            else if (isFalse(args[3]))
            {
                testing = false;
            }
            else
            {
                System.out.println("Invalid argument! (Need true or false)");
                return;
            }
        }
        if (args.length >= 5)
        {
            if (isTrue(args[4]))
            {
                reduceSymmetry = true;
            }
            else if (isFalse(args[4]))
            {
                reduceSymmetry = false;
            }
            else
            {
                System.out.println("Invalid argument! (Need true or false)");
                return;
            }
        }
        if (args.length >= 6)
        {
            if (isTrue(args[5]))
            {
                treatAllAsSymmetric = true;
            }
            else if (isFalse(args[5]))
            {
                treatAllAsSymmetric = false;
            }
            else
            {
                System.out.println("Invalid argument! (Need true or false)");
                return;
            }
        }

        if (args.length >= 7)
        {
            if (isTrue(args[6]))
            {
                expandSymmetry = true;
            }
            else if (isFalse(args[6]))
            {
                expandSymmetry = false;
            }
            else
            {
                System.out.println("Invalid argument! (Need true or false)");
                return;
            }
        }


        //For graph walk
        runWalk(n,m,p, testing, reduceSymmetry, treatAllAsSymmetric, expandSymmetry, NUM_THREADS);
        //For graph explore
        //runGraphExplore(n,m,p, testing, reduceSymmetry, treatAllAsSymmetric, expandSymmetry);

    }

    private static boolean isTrue(String x)
    {
        return x.trim().toLowerCase().equals("true") || x.trim().toLowerCase().equals("t") || x.trim().toLowerCase().equals("1");
    }

    private static boolean isFalse(String x)
    {
        return x.trim().toLowerCase().equals("false") || x.trim().toLowerCase().equals("f") || x.trim().toLowerCase().equals("0");
    }


    public static void runWalk(int n, int m, int p, boolean testing, boolean reduceSymmetry, boolean treatAllAsSymmetric, boolean expandSymmetry, int numThreads) throws Exception
    {

        int[] minStepsFoundForReduction = new int[n*m*p*3]; //Stores the current best number of steps for a reduction
        for (int i = 0; i < minStepsFoundForReduction.length; i++)
        {
            minStepsFoundForReduction[i] = -1;
        }

        WalkThreadClass walkThreads[] = new WalkThreadClass[numThreads];
        for (int i = 0; i < walkThreads.length; i++)
        {
            walkThreads[i] = new WalkThreadClass(n, m, p, testing, reduceSymmetry, treatAllAsSymmetric, expandSymmetry, minStepsFoundForReduction);
        }

        for (int i = 0; i < walkThreads.length; i++)
        {
            walkThreads[i].start();
        }

        for (int i = 0; i < walkThreads.length; i++)
        {
            walkThreads[i].join();
        }
        /*
        MultiplicationMethod x = MultiplicationMethod.getBasicMethod(n, m, p);



        System.out.println("====== RUN RANDOM WALK ON (" + n + ", " + m + ", " + p + ") ======");

        System.out.println(x);



        if (reduceSymmetry)
        {
            System.out.println("====== REDUCE TO SYMMETRY =======");

            x.reduceToSymmetry(treatAllAsSymmetric);
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

        if (expandSymmetry)
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
        x.randomWalk(testing);

        */

    }

}
