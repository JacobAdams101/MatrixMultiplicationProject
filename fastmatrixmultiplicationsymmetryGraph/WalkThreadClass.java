public class WalkThreadClass extends Thread
{
    private int n;
    private int m;
    private int p;

    private boolean testing;
    private boolean reduceSymmetry;
    private boolean treatAllAsSymmetric;
    private boolean expandSymmetry;

    private AlgoData algoData;

    private int maxruns;
    private int runcutoff;

    public WalkThreadClass(int n, int m, int p, boolean testing, boolean reduceSymmetry, boolean treatAllAsSymmetric, boolean expandSymmetry, AlgoData algoData, int maxruns, int runcutoff)
    {
        this.n = n;
        this.m = m;
        this.p = p;

        this.testing = testing;
        this.reduceSymmetry = reduceSymmetry;
        this.treatAllAsSymmetric = treatAllAsSymmetric;
        this.expandSymmetry = expandSymmetry;

        this.algoData = algoData;

        this.maxruns = maxruns;
        this.runcutoff = runcutoff;
    }

    @Override
    public void run()
    {
        int runs = 0;
        while (runs < maxruns || maxruns == -1)
        {

            MultiplicationMethod x = MultiplicationMethod.getBasicMethod(n);



            System.out.println("====== RUN RANDOM WALK ON (" + n + ", " + m + ", " + p + ") ======");

            //System.out.println(x);



            if (reduceSymmetry)
            {
                //System.out.println("====== REDUCE TO SYMMETRY =======");

                x.reduceToSymmetry(treatAllAsSymmetric);
                //System.out.println(x);

                //System.out.println("======= TEST SYMMETRY REDUCTION ======");
                /*
                if (x.testValidity())
                {
                    System.out.println("======= VALID =======");
                }
                else
                {
                    System.out.println("======= FAIL  =======");
                }
                */

            }

            if (expandSymmetry)
            {
                //System.out.println("====== EXPAND BY SYMMETRY =======");

                x.expandBySymmetry();
                /*
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
                */
            }

            //System.out.println("");
            //System.out.println("====== START WALK =======");
            try
            {
                x.randomWalk(testing, algoData, 0, 54, runcutoff, 500000);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            runs++;
        }


    }
}
