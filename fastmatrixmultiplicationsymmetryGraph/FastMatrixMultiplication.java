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

    final static boolean USE_REDUCED_SYMMETRY = false;
    final static boolean TREAT_ALL_AS_SYMMETRIC = false;

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

        boolean testing = ENABLE_TESTING;
        boolean reduceSymmetry = USE_REDUCED_SYMMETRY;
        boolean treatAllAsSymmetric = TREAT_ALL_AS_SYMMETRIC;
        boolean expandSymmetry = USE_EXPANDED_SYMMETRY;

        if (args.length > 7 || (args.length < 3 && args.length != 0))
        {
            System.out.println("Invalid number of arguments!");
            return;
        }


        if (args.length >= 3)
        {
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
            p = Integer.parseInt(args[2]);
        }
        if (args.length >= 4)
        {
            if (args[3].trim().toLowerCase().equals("true") || args[3].trim().toLowerCase().equals("t"))
            {
                testing = true;
            }
            else if (args[3].trim().toLowerCase().equals("false") || args[3].trim().toLowerCase().equals("f"))
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
            if (args[4].trim().toLowerCase().equals("true") || args[4].trim().toLowerCase().equals("t"))
            {
                reduceSymmetry = true;
            }
            else if (args[4].trim().toLowerCase().equals("false") || args[4].trim().toLowerCase().equals("f"))
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
            if (args[5].trim().toLowerCase().equals("true") || args[5].trim().toLowerCase().equals("t"))
            {
                treatAllAsSymmetric = true;
            }
            else if (args[5].trim().toLowerCase().equals("false") || args[5].trim().toLowerCase().equals("f"))
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
            if (args[6].trim().toLowerCase().equals("true") || args[6].trim().toLowerCase().equals("t"))
            {
                expandSymmetry = true;
            }
            else if (args[6].trim().toLowerCase().equals("false") || args[6].trim().toLowerCase().equals("f"))
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
        runWalk(n,m,p, testing, reduceSymmetry, treatAllAsSymmetric, expandSymmetry);
        //For graph explore
        //runGraphExplore(n,m,p, testing, reduceSymmetry, treatAllAsSymmetric, expandSymmetry);

    }

    public static void runGraphExplore(int n, int m, int p, boolean testing, boolean reduceSymmetry, boolean treatAllAsSymmetric, boolean expandSymmetry) throws Exception {
        Graph x = new Graph();

        x.exploreGraph(n, m, p, testing, reduceSymmetry, treatAllAsSymmetric);
    }

    public static void runWalk(int n, int m, int p, boolean testing, boolean reduceSymmetry, boolean treatAllAsSymmetric, boolean expandSymmetry) throws Exception
    {
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



    }

}
