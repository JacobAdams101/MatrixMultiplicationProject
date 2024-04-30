public class WalkThreadClass extends Thread
{
    private int n;
    private int m;
    private int p;

    private boolean testing;
    private boolean reduceSymmetry;
    private boolean treatAllAsSymmetric;
    private boolean expandSymmetry;

    private int plusTransitionAfter;
    private int lookForSingletonAtRank;

    private AlgoData algoData;

    private int maxruns;
    private int runcutoffatsteps;
    private int runcutoffatrank;



    public WalkThreadClass(int n, int m, int p, boolean testing, boolean reduceSymmetry, boolean treatAllAsSymmetric, boolean expandSymmetry, int plusTransitionAfter, int lookForSingletonAtRank, AlgoData algoData, int maxruns, int runcutoffatsteps, int runcutoffatrank)
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
        this.runcutoffatsteps = runcutoffatsteps;

        this.runcutoffatrank = runcutoffatrank;

        this.plusTransitionAfter = plusTransitionAfter;

        this.lookForSingletonAtRank = lookForSingletonAtRank;
    }

    public MultiplicationMethod loadScheme(int scrambleSteps)
    {
        //Put a list of strings representing a string in here to load a scheme
        String[] scheme = {
            "<(+a_0,0  +a_3,3  ) ⊗ (+b_0,2  +b_0,4  +b_1,1  +b_2,2  +b_3,1  +b_3,2  +b_3,4  ) ⊗ (+c_1,0  +c_1,1  +c_1,3  +c_2,2  +c_4,0  )> Z_3",
            "(+a_4,4  ) ⊗ (+b_4,4  ) ⊗ (+c_4,4  )",
            "<(+a_2,2  +a_2,4  +a_4,2  +a_4,4  ) ⊗ (+b_0,3  +b_3,3  +b_4,3  ) ⊗ (+c_1,1  +c_1,2  +c_3,1  +c_3,2  )> Z_3",
            "<(+a_0,0  +a_1,0  +a_2,0  ) ⊗ (+b_0,1  +b_0,2  +b_0,3  +b_0,4  +b_3,1  +b_3,2  +b_3,3  +b_3,4  ) ⊗ (+c_1,0  +c_1,3  )> Z_3",
            "<(+a_2,4  ) ⊗ (+b_3,0  +b_3,1  +b_3,3  +b_4,0  +b_4,1  +b_4,3  ) ⊗ (+c_1,2  +c_2,2  +c_4,2  )> Z_3",
            "<(+a_3,3  ) ⊗ (+b_0,2  +b_0,4  +b_1,2  +b_2,2  +b_3,4  ) ⊗ (+c_2,0  +c_2,3  +c_4,0  +c_4,3  )> Z_3",
            "<(+a_1,3  ) ⊗ (+b_0,0  +b_3,0  +b_4,0  ) ⊗ (+c_0,1  +c_0,2  +c_1,1  +c_1,2  +c_4,1  +c_4,2  )> Z_3",
            "(+a_0,0  +a_1,1  +a_2,2  +a_3,3  ) ⊗ (+b_0,0  +b_1,1  +b_2,2  +b_3,3  ) ⊗ (+c_0,0  +c_1,1  +c_2,2  +c_3,3  )",
            "<(+a_1,0  +a_1,2  +a_1,4  +a_2,0  +a_2,2  +a_2,4  ) ⊗ (+b_2,1  +b_2,2  ) ⊗ (+c_1,1  +c_3,1  )> Z_3",
            "<(+a_0,0  +a_0,2  +a_0,4  +a_3,0  +a_3,2  +a_3,4  +a_4,0  ) ⊗ (+b_0,0  +b_0,3  +b_1,1  +b_2,1  ) ⊗ (+c_1,1  +c_1,2  +c_1,3  +c_1,4  +c_3,1  +c_3,2  +c_3,4  )> Z_3",
            "<(+a_0,0  +a_0,2  +a_0,4  +a_2,0  +a_2,2  +a_2,4  +a_4,0  +a_4,2  +a_4,4  ) ⊗ (+b_0,3  +b_3,3  ) ⊗ (+c_1,0  +c_1,3  +c_3,0  +c_3,3  )> Z_3",
            "<(+a_0,1  +a_0,2  +a_2,1  +a_2,2  +a_3,1  +a_3,2  +a_3,4  ) ⊗ (+b_2,2  +b_2,3  +b_2,4  +b_4,2  +b_4,3  +b_4,4  ) ⊗ (+c_1,0  +c_2,0  )> Z_3",
            "<(+a_0,0  +a_0,1  +a_0,3  ) ⊗ (+b_1,1  +b_1,2  +b_1,3  +b_1,4  ) ⊗ (+c_4,0  )> Z_3",
            "<(+a_2,4  ) ⊗ (+b_3,0  +b_3,1  +b_3,3  +b_4,0  +b_4,2  +b_4,4  ) ⊗ (+c_4,2  )> Z_3",
            "<(+a_2,4  ) ⊗ (+b_4,1  +b_4,2  +b_4,3  ) ⊗ (+c_1,1  +c_2,2  +c_4,2  )> Z_3",
            "<(+a_0,0  +a_0,4  +a_1,4  +a_2,4  +a_3,0  +a_3,4  +a_4,0  ) ⊗ (+b_1,1  +b_1,2  +b_2,1  +b_2,2  +b_4,1  +b_4,2  ) ⊗ (+c_2,0  +c_4,0  )> Z_3",
            "<(+a_2,0  +a_2,2  +a_2,4  +a_4,0  +a_4,2  +a_4,4  ) ⊗ (+b_0,3  +b_3,3  ) ⊗ (+c_1,0  +c_1,1  +c_1,2  +c_1,3  +c_3,0  +c_3,1  +c_3,2  +c_3,3  )> Z_3",
            "<(+a_1,0  +a_1,1  +a_1,3  +a_2,0  +a_2,1  +a_2,3  ) ⊗ (+b_3,1  +b_3,2  +b_3,4  ) ⊗ (+c_2,2  )> Z_3",
            "<(+a_3,3  ) ⊗ (+b_0,2  +b_0,4  +b_3,2  +b_3,4  ) ⊗ (+c_0,0  +c_0,3  +c_1,0  +c_1,3  +c_2,0  +c_2,3  )> Z_3",
            "<(+a_4,4  ) ⊗ (+b_1,1  +b_2,1  +b_4,1  ) ⊗ (+c_1,4  )> Z_3",
            "<(+a_1,3  +a_2,0  +a_4,0  ) ⊗ (+b_0,0  +b_3,0  +b_3,4  +b_4,0  ) ⊗ (+c_0,2  +c_1,2  +c_2,1  +c_4,1  +c_4,2  )> Z_3",
            "<(+a_0,4  ) ⊗ (+b_2,1  +b_2,2  +b_2,3  +b_2,4  +b_4,1  +b_4,2  +b_4,3  +b_4,4  ) ⊗ (+c_1,0  +c_2,0  +c_4,0  )> Z_3",
            "<(+a_0,2  +a_0,4  +a_1,2  +a_2,2  ) ⊗ (+b_2,3  +b_2,4  +b_4,3  ) ⊗ (+c_3,0  +c_3,1  +c_3,3  +c_4,0  )> Z_3",
            "<(+a_3,0  +a_3,4  +a_4,0  ) ⊗ (+b_0,2  +b_1,2  +b_2,2  ) ⊗ (+c_2,0  +c_2,3  +c_4,0  +c_4,3  )> Z_3",
            "<(+a_0,2  +a_0,4  +a_1,0  +a_2,0  ) ⊗ (+b_0,3  +b_2,1  +b_2,2  +b_3,3  ) ⊗ (+c_1,0  +c_1,3  +c_3,1  )> Z_3",
            "<(+a_0,0  +a_0,2  +a_0,4  +a_3,0  +a_3,2  +a_3,4  ) ⊗ (+b_0,0  +b_0,3  ) ⊗ (+c_1,1  +c_1,2  +c_1,3  +c_1,4  +c_3,1  +c_3,2  +c_3,3  +c_3,4  )> Z_3",
            "<(+a_0,2  +a_0,4  ) ⊗ (+b_0,3  +b_2,3  +b_2,4  +b_3,3  ) ⊗ (+c_3,0  +c_3,1  +c_3,3  )> Z_3",
            "<(+a_1,4  +a_2,4  ) ⊗ (+b_2,1  +b_2,2  +b_4,1  +b_4,2  ) ⊗ (+c_1,1  +c_2,0  +c_4,0  )> Z_3",
            "<(+a_0,2  +a_0,4  +a_1,2  +a_1,4  +a_2,2  +a_2,4  ) ⊗ (+b_2,1  +b_2,2  +b_4,3  ) ⊗ (+c_3,1  )> Z_3",
            "<(+a_1,3  +a_2,3  +a_4,3  ) ⊗ (+b_3,4  ) ⊗ (+c_0,2  +c_1,2  +c_4,2  )> Z_3",
            "<(+a_1,1  +a_1,2  +a_1,4  +a_3,1  +a_3,2  +a_3,4  ) ⊗ (+b_0,0  +b_0,2  +b_0,4  +b_3,0  +b_3,2  +b_3,4  +b_4,0  +b_4,2  +b_4,4  ) ⊗ (+c_1,1  +c_2,1  )> Z_3",
            "<(+a_0,1  +a_0,2  +a_0,4  +a_2,1  +a_2,2  +a_3,1  +a_3,2  +a_3,4  ) ⊗ (+b_1,0  +b_1,1  +b_1,3  +b_2,2  +b_2,3  +b_2,4  +b_4,2  +b_4,3  +b_4,4  ) ⊗ (+c_1,0  +c_2,0  )> Z_3",
            "<(+a_0,0  +a_1,0  +a_1,3  +a_2,0  +a_3,3  ) ⊗ (+b_0,0  +b_0,2  +b_0,4  +b_3,1  +b_3,2  +b_3,3  +b_3,4  ) ⊗ (+c_1,1  +c_2,2  )> Z_3",
            "<(+a_1,1  +a_2,1  +a_4,3  ) ⊗ (+b_1,1  +b_1,2  +b_3,1  +b_3,2  +b_3,4  ) ⊗ (+c_2,2  +c_4,2  +c_4,4  )> Z_3",
            "<(+a_0,1  +a_0,3  +a_0,4  +a_1,4  +a_2,4  +a_3,0  +a_3,4  +a_4,0  ) ⊗ (+b_1,1  +b_1,2  ) ⊗ (+c_2,0  +c_4,0  )> Z_3"
        };

        MultiplicationMethod x = MultiplicationMethod.getMethodFromString(n, scheme, true);

        for (int j = 0; j < scrambleSteps; j++)
        {
            x.plusTransition();
            for (int i = 0; i < 1000000; i++)
            {
                x.findAndMakeFlip();
            }
        }

        if (x.testValidity())
        {
            System.out.println("======= VALID =======");
        }
        else
        {
            System.out.println("======= FAIL  =======");
            return null;
        }

        return x;
    }

    public MultiplicationMethod getStandardScheme(int n)
    {
        return MultiplicationMethod.getBasicMethod(n);
    }

    @Override
    public void run()
    {
        int runs = 0;
        while (runs < maxruns || maxruns == -1)
        {




            MultiplicationMethod x = getStandardScheme(n);
            //MultiplicationMethod x = loadScheme(2);



            System.out.println("====== RUN RANDOM WALK ON (" + n + ", " + m + ", " + p + ") ======");
            /*
            System.out.println(x);
            if (x.testValidity())
            {
                System.out.println("======= VALID =======");
            }
            else
            {
                System.out.println("======= FAIL  =======");
            }
            */

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
                x.randomWalk(testing, algoData, runcutoffatrank, lookForSingletonAtRank, runcutoffatsteps, plusTransitionAfter, 2);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            runs++;
        }


    }
}
