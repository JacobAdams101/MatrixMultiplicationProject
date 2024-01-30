public class WalkThreadClass extends Thread
{
    private int n;
    private int m;
    private int p;

    private boolean testing;
    private boolean reduceSymmetry;
    private boolean treatAllAsSymmetric;
    private boolean expandSymmetry;

    public WalkThreadClass(int n, int m, int p, boolean testing, boolean reduceSymmetry, boolean treatAllAsSymmetric, boolean expandSymmetry)
    {
        this.n = n;
        this.m = m;
        this.p = p;

        this.testing = testing;
        this.reduceSymmetry = reduceSymmetry;
        this.treatAllAsSymmetric = treatAllAsSymmetric;
        this.expandSymmetry = expandSymmetry;
    }

    @Override
    public void run()
    {
        try
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
        catch (Exception ex)
        {
            System.err.println(ex);
        }


    }
}
