//package fastmatrixmultiplicationsymmetryGraph;

/**
 *
 * @author jacob
 */
public class FastMatrixMultiplication
{
    final static int DEFAULT_N = 4;
    final static int DEFAULT_M = 4;
    final static int DEFAULT_P = 4;

    final static boolean ENABLE_TESTING = true;

    final static boolean USE_REDUCED_SYMMETRY = true;
    final static boolean TREAT_ALL_AS_SYMMETRIC = true;

    final static boolean USE_EXPANDED_SYMMETRY = false;

    final static int PLUS_TRANSITION_AFTER = 500000;
    final static int LOOK_FOR_SINGLETON_AT_RANK = 0;

    final static int NUM_THREADS = 1;
    final static int RUNS_PER_THREAD = 1;

    final static int RUNS_STEPS_CUTOFF = 100000000;
    final static int RUNS_RANK_CUTOFF = 0;
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

        int plusTransitionAfter = PLUS_TRANSITION_AFTER;
        int lookForSingletonAtRank = LOOK_FOR_SINGLETON_AT_RANK;

        int numThreads = NUM_THREADS;
        int runsPerThread = RUNS_PER_THREAD;

        int runStepsCutOff = RUNS_STEPS_CUTOFF;
        int runRankCutOff = RUNS_RANK_CUTOFF;

        if (args.length > 13 || (args.length < 3 && args.length != 0))
        {
            System.out.println("Invalid number of arguments: " + args.length );
            return;
        }

        //FastMatrixMultiplication n m p testing reduceSymmetry treatAllAsSymmetric expandSymmetry plusTransitionAfter lookForSingletonAtRank numThreads runsPerThread runStepsCutOff runRankCutOff


        //For 2x2
        //java FastMatrixMultiplication 2 2 2 true true true false 500000 9 16 16 10000000 7
        //java FastMatrixMultiplication 2 2 2 true false false false 500000 0 16 16 10000000 7

        //For 3x3
        //java FastMatrixMultiplication 3 3 3 true true true false 500000 27 16 16 10000000 23
        //java FastMatrixMultiplication 3 3 3 true false false false 500000 0 16 16 10000000 23


        //What tests did I do
        //
        //3x3
        //java FastMatrixMultiplication 3 3 3 true true true false 5000 27 16 16 1000000 23
        //java FastMatrixMultiplication 3 3 3 true false false false 5000 0 16 16 1000000 23
        //
        //java FastMatrixMultiplication 4 4 4 true true true false 500000 51 16 16 10000000 47
        //java FastMatrixMultiplication 4 4 4 true false false false 500000 0 16 16 10000000 47

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

        if (args.length >= 8)
        {
            plusTransitionAfter = Integer.parseInt(args[7]);
        }

        if (args.length >= 9)
        {
            lookForSingletonAtRank = Integer.parseInt(args[8]);
        }

        if (args.length >= 10)
        {
            numThreads = Integer.parseInt(args[9]);
        }
        if (args.length >= 11)
        {
            runsPerThread = Integer.parseInt(args[10]);
        }

        if (args.length >= 12)
        {
            runStepsCutOff = Integer.parseInt(args[11]);
        }
        if (args.length >= 13)
        {
            runRankCutOff = Integer.parseInt(args[12]);
        }






        //For graph walk
        runWalk(n,m,p, testing, reduceSymmetry, treatAllAsSymmetric, expandSymmetry, plusTransitionAfter, lookForSingletonAtRank, numThreads, runsPerThread, runStepsCutOff, runRankCutOff);
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


    public static class ShutdownResults extends Thread
    {
        AlgoData algoDataRef;

        public ShutdownResults(AlgoData algoDataRef)
        {
            this.algoDataRef = algoDataRef;
        }
        @Override
        public void run()
        {
            algoDataRef.printResults();
        }
    }

    public static void runWalk(int n, int m, int p, boolean testing, boolean reduceSymmetry, boolean treatAllAsSymmetric, boolean expandSymmetry, int plusTransitionAfter, int lookForSingletonAtRank, int numThreads, int runsPerThread, int runStepsCutOff, int runRankCutOff) throws Exception
    {



        AlgoData algoData = new AlgoData(n, m, p);

        ShutdownResults results = new ShutdownResults(algoData);

        Runtime.getRuntime().addShutdownHook(results);


        WalkThreadClass walkThreads[] = new WalkThreadClass[numThreads];
        for (int i = 0; i < walkThreads.length; i++)
        {
            walkThreads[i] = new WalkThreadClass(n, m, p, testing, reduceSymmetry, treatAllAsSymmetric, expandSymmetry, plusTransitionAfter, lookForSingletonAtRank, algoData, runsPerThread, runStepsCutOff, runRankCutOff);
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
