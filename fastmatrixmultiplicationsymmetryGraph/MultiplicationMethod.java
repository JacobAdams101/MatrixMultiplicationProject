//package fastmatrixmultiplication;


import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author jacob
 */
public class MultiplicationMethod
{
    /**
     *
     */
    private ArrayList<RankTensor>tensors;

    public ArrayList<RankTensor> getTensors() {
        return tensors;
    }


    @Override
    public boolean equals(Object e) {
        if (e instanceof MultiplicationMethod) {
            MultiplicationMethod mm = (MultiplicationMethod)e;

            ArrayList<RankTensor> mmTensors = new ArrayList<>();

            mmTensors.addAll(mm.tensors);

            for (RankTensor t : tensors)
            {
                int index = mmTensors.indexOf(t);
                if (index == -1) { //Something not in list
                    return false; //Return false
                } else {
                    mmTensors.remove(index);
                }
            }
            return mmTensors.isEmpty(); //If scheme is not empty then not equal

        } else {
            return false;
        }
    }
    /*
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.tensors);
        return hash;
    }
    */
    /**
     *
     */
    public MultiplicationMethod()
    {
        tensors = new ArrayList<>();
    }

    public MultiplicationMethod(ArrayList<RankTensor>t)
    {
        tensors = new ArrayList<>();
        tensors.addAll(t);
    }
    /**
     *
     * @param colsA
     * @param rowsA
     * @param colsB
     * @return
     */
    public static MultiplicationMethod getBasicMethod(int colsA, int rowsA, int colsB)
    {
        MultiplicationMethod ret = new MultiplicationMethod();

        int rowsB = colsA;

        int colsC = colsB;
        int rowsC = rowsA;

        int i;
        int j;
        int k;

        for (i = 0; i < rowsA; i++)
        {
            for (j = 0; j < colsA; j++)
            {
                for (k = 0; k < colsB; k++)
                {
                    int[][] a = new int[rowsA][colsA];
                    int[][] b = new int[rowsB][colsB];
                    //int[][] c = new int[rowsC][colsC]; //Original
                    int[][] c = new int[colsC][rowsC]; //Transpose

                    a[i][j] = 1;
                    b[j][k] = 1;
                    //c[i][k] = 1;

                    c[k][i] = 1;

                    ret.tensors.add(new RankTensor(a,b,c));
                }
            }
        }

        return ret;
    }


    /*
    [ a00  a01]    [ b00  b01].
    [ a10  a11]    [ b10  b11].

    a00 b00 c00

    a01 b10 c00
    a10
    */

    public void expandBySymmetry()
    {
        final int ORIGINAL_TENSOR_SIZE = tensors.size();
        for (int i = 0; i < ORIGINAL_TENSOR_SIZE; i++)
        {
            RankTensor t0 = tensors.get(i);
            RankTensor t1 = t0.performExchange();
            if (t0.isEqual(t1))
            { //If t0 is the same as t1 it implies t0.a = t1.a = t0.b  AND  t0.b = t1.b = t0.c SO t0.a = t0.b = t0.c
            //So no point continuing
                continue; //Skip iteration
            }
            RankTensor t2 = t1.performExchange();

            tensors.add(t1);
            tensors.add(t2);
        }
    }

    public void reduceToSymmetry(boolean makeAllSymmetric)
    {
        int i;
        for (i = 0; i < tensors.size(); i++)
        {
            RankTensor t0 = tensors.get(i);

            RankTensor t1 = t0.performExchange();

            if (t0.isEqual(t1))
            { //If t0 is the same as t1 it implies t0.a = t1.a = t0.b  AND  t0.b = t1.b = t0.c SO t0.a = t0.b = t0.c
            //So no point continuing
                continue; //Skip iteration
            }

            RankTensor t2 = t1.performExchange();

            int t1Location = -1;
            int t2Location = -1;

            for (int j = i+1; j < tensors.size(); j++)
            {
                RankTensor test = tensors.get(j);

                if (test.isEqual(t1))
                {
                    t1Location = j;
                }
                if (test.isEqual(t2))
                {
                    t2Location = j;
                }
            }

            if (t1Location != -1 && t2Location != -1)
            {
                //Remove in order
                if (t1Location > t2Location)
                {
                    tensors.remove(t1Location); //Biggest index first
                    tensors.remove(t2Location);
                }
                else
                {
                    tensors.remove(t2Location); //Biggest index first
                    tensors.remove(t1Location);
                }
                t0.hasSymmetry = true; //t0 now has hasSymmetry
            }

        }

        if (makeAllSymmetric)
        {
            for (RankTensor t : tensors)
            {
                t.hasSymmetry = true;
            }
        }
    }


    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (RankTensor t : tensors)
        {
            sb.append(t);
            sb.append("\n");
        }
        return sb.toString();
    }

    public int rank()
    {
        return tensors.size();
    }

    /**
     *
     */
    public void randomWalk(boolean enableTesting) throws Exception
    {
        //Scanner sc = new Scanner(System.in);
        final int MAX_REDUCTIONS = -1;
        int reductions = 0;

        do
        {
            boolean hasReduced = randomStep();
            //System.out.println("STEP");


            if (hasReduced)
            {
                reductions++; //increase redutions

                if (FastMatrixMultiplication.bestSoFar < reductions)
                {
                    FastMatrixMultiplication.bestSoFar = reductions;

                    System.out.println("===== METHOD WITH RANK " + tensors.size() + " (" + reductions + " REDUCTIONS): =====");
                    System.out.println(this);
                    if (enableTesting)
                    {
                        System.out.println("===== TEST CASE POST REDUCTION =====");
                        if (this.testValidity())
                        {
                            System.out.println("======= VALID =======");
                        }
                        else
                        {
                            System.out.println("======= FAIL  =======");
                            throw new Exception("Reduction failed!");
                        }
                    }
                }
            }
            //System.out.println("=========================================================");
            //System.out.println("Step");
            //System.out.println("=========================================================");

            //System.out.println("=========================================================");
            //System.out.println("Result");
            //System.out.println("=========================================================");
            //this.printMethod();
            //System.out.println("=========================================================");
            //sc.next();
        }
        while (MAX_REDUCTIONS == -1 || reductions < MAX_REDUCTIONS); //MAX_REDUCTIONS == -1 Means don't stop

    }
    /**
     *
     * @return
     */
    public boolean randomStep()
    {
        //System.out.println("===Step");
        //System.out.println("Reduce");
        //Always try to reduce first
        boolean result = tryReduce(); //Look for a reduction
        //System.out.println("Look For next idea");
        if (result == false)
        { //If can't reduce flip
            //Instead of picking whether to flip or change here do it in the flip
            /*
            if (Math.random() < 0.9)
            {
                changeRepresentitive();
            } else {
                //System.out.println("Look for flip");
                makeFlip();
            }
            */
            makeFlip();

            //System.out.println("Done");
            return false;
        }
        else
        {
            //System.out.println("Done");
            return true;
        }

    }
    /*
    public ArrayList<MultiplicationMethod> getAllReductions()
    {
        ArrayList<MultiplicationMethod> ret = new ArrayList<>();

        ArrayList<ArrayList<int[]>> indexOfCommonA = getMatchingSelections(Selection.A); //Find subsets of A that have dimension 1 (all look the same)
        //ret.addAll(searchForLinearDependanceFindAll(indexOfCommonA, Selection.A));

        ArrayList<ArrayList<int[]>> indexOfCommonB = getMatchingSelections(Selection.B); //Find subsets of B that have dimension 1 (all look the same)
        //ret.addAll(searchForLinearDependanceFindAll(indexOfCommonB, Selection.B));

        ArrayList<ArrayList<int[]>> indexOfCommonC = getMatchingSelections(Selection.C); //Find subsets of C that have dimension 1 (all look the same)
        //ret.addAll(searchForLinearDependanceFindAll(indexOfCommonC, Selection.C));

        //System.out.println("Found " + ret.size() + " possible reductions");

        return ret;
    }
    */
    /*
    public ArrayList<MultiplicationMethod>searchForLinearDependanceFindAll(ArrayList<ArrayList<Integer>> indexOfCommon, Selection ignoreMatrix)
    {
        ArrayList<MultiplicationMethod> ret = new ArrayList<>();

        for (int i = 0; i < indexOfCommon.size(); i++)
        {
            ArrayList<Integer> listOfSameCommon = indexOfCommon.get(i);
            ArrayList<Integer> nextToTest = new ArrayList<>(); //Stores a list of indexes of things to test (Similar to set "I" in the paper https://arxiv.org/pdf/2212.01175.pdf)

            final int MAX = (int)Math.pow(2, listOfSameCommon.size());
            TestTensors:
            for (int j = 0; j < MAX; j++)
            {
                nextToTest.clear();
                int temp = j;
                for (int index = 0; index < listOfSameCommon.size(); index++)
                {
                    if ((temp & 1) == 1)
                    {
                        nextToTest.add(listOfSameCommon.get(index));
                    }
                    temp = temp / 2;
                }



                if (nextToTest.isEmpty() == false)
                {
                    boolean isUsingSymmetry = tensors.get(nextToTest.get(0)).hasSymmetry;
                    for (int index = 0; index < nextToTest.size(); index++)
                    {
                        if (tensors.get(nextToTest.get(index)).hasSymmetry != isUsingSymmetry)
                        { //If different systems don't use
                            continue TestTensors;
                        }
                    }
                }




                //Found list to test... now test it
                if (ignoreMatrix != Selection.A)
                {
                    int[] combination = testLinearDependance(nextToTest, Selection.A);
                    if (combination != null)
                    {
                        int tensorIndexToRemove = nextToTest.get(0); //I *THINK* because of how this is implemented I can pick any vector
                        //Find values for A and B lists
                        int[] a = new int[tensors.size()];
                        for (int k = 0; k < a.length; k++)
                        {
                            a[k] = 1; //For two vectors in F2... if A and B have dimension 1 then either one is 0 or A=B
                        }
                        int nextToTestIndex = 0;
                        int combinationIndex = 0;
                        int[] b = new int[tensors.size()];
                        for (int k = 0; k < b.length; k++)
                        {

                            if (nextToTestIndex < nextToTest.size() && k == nextToTest.get(nextToTestIndex))
                            {
                                if (k != tensorIndexToRemove)
                                {
                                    b[k] = combination[combinationIndex];
                                    combinationIndex++;
                                }
                                nextToTestIndex++;
                            }
                            else
                            {
                                b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                            }
                        }

                        ret.add(reconstructMultiplicationSchemeForAll(ignoreMatrix, Selection.A, nextToTest, tensorIndexToRemove, a, b));
                    }
                }
                if (ignoreMatrix != Selection.B)
                {
                    int[] combination = testLinearDependance(nextToTest, Selection.B);
                    if (combination != null)
                    {
                        int tensorIndexToRemove = nextToTest.get(0); //I *THINK* because of how this is implemented I can pick any vector
                        //Find values for A and B lists
                        int[] a = new int[tensors.size()];
                        for (int k = 0; k < a.length; k++)
                        {
                            a[k] = 1; //For two vectors in F2... if A and B have dimension 1 then either one is 0 or A=B
                        }
                        int nextToTestIndex = 0;
                        int combinationIndex = 0;
                        int[] b = new int[tensors.size()];
                        for (int k = 0; k < b.length; k++)
                        {

                            if (nextToTestIndex < nextToTest.size() && k == nextToTest.get(nextToTestIndex))
                            {
                                if (k != tensorIndexToRemove)
                                {
                                    b[k] = combination[combinationIndex];
                                    combinationIndex++;
                                }
                                nextToTestIndex++;
                            }
                            else
                            {
                                b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                            }
                        }

                        ret.add(reconstructMultiplicationSchemeForAll(ignoreMatrix, Selection.B, nextToTest, tensorIndexToRemove, a, b));
                    }
                }
                if (ignoreMatrix != Selection.C)
                {
                    int[] combination = testLinearDependance(nextToTest, Selection.C);
                    if (combination != null)
                    {
                        int tensorIndexToRemove = nextToTest.get(0); //I *THINK* because of how this is implemented I can pick any vector
                        //Find values for A and B lists
                        int[] a = new int[tensors.size()];
                        for (int k = 0; k < a.length; k++)
                        {
                            a[k] = 1; //For two vectors in F2... if A and B have dimension 1 then either one is 0 or A=B
                        }
                        int nextToTestIndex = 0;
                        int combinationIndex = 0;
                        int[] b = new int[tensors.size()];
                        for (int k = 0; k < b.length; k++)
                        {

                            if (nextToTestIndex < nextToTest.size() && k == nextToTest.get(nextToTestIndex))
                            {
                                if (k != tensorIndexToRemove)
                                {
                                    b[k] = combination[combinationIndex];
                                    combinationIndex++;
                                }
                                nextToTestIndex++;
                            }
                            else
                            {
                                b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                            }
                        }

                        ret.add(reconstructMultiplicationSchemeForAll(ignoreMatrix, Selection.C, nextToTest, tensorIndexToRemove, a, b));
                    }
                }

            }
        }

        return ret;
    }
    */
    /*
    public MultiplicationMethod reconstructMultiplicationSchemeForAll(Selection dimensionOneOver, Selection linearlyDependantOver, ArrayList<Integer> listOfDependant, int tensorIndexToRemove, int[] a, int[] b)
    {
        //Reconstruct multiplication scheme
        ArrayList<RankTensor> reducedScheme = new ArrayList<>();
        //Find Tensor index to remove

        Selection augmentOver = Selection.A;

        //Find missing component
        while (dimensionOneOver == augmentOver || linearlyDependantOver == augmentOver)
        {
            augmentOver = Selection.values()[augmentOver.ordinal()+1];
        }


        RankTensor t = tensors.get(tensorIndexToRemove);
        for (int i = 0; i < tensors.size(); i++)
        {
            if (i != tensorIndexToRemove)
            {
                if (listOfDependant.contains(i))
                { //If in set that had a linear dependancy

                    //A * B * C + ab(C_t)

                    int[][] matrixToAugment = null;
                    int[][] tmatrixToAugment = null;
                    if (null != augmentOver)
                    {
                        switch (augmentOver)
                        {
                            case A:
                                matrixToAugment = tensors.get(i).a;
                                tmatrixToAugment = t.a;
                                break;
                            case B:
                                matrixToAugment = tensors.get(i).b;
                                tmatrixToAugment = t.b;
                                break;
                            case C:
                                matrixToAugment = tensors.get(i).c;
                                tmatrixToAugment = t.c;
                                break;
                            default:
                                break;
                        }
                    }
                    int[][] augmented = new int[matrixToAugment.length][matrixToAugment[0].length];

                    int[][]newA;
                    int[][]newB;
                    int[][]newC;

                    //Update A
                    if (augmentOver == Selection.A)
                    {
                        for (int j = 0; j < augmented.length; j++)
                        {
                            for (int k = 0; k < augmented[0].length; k++)
                            {
                                //augmented[j][k] = matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k]);
                                augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
                            }
                        }
                        newA = augmented;
                    }
                    else
                    {
                        newA = tensors.get(i).a;
                    }

                    //Update B
                    if (augmentOver == Selection.B)
                    {
                        for (int j = 0; j < augmented.length; j++)
                        {
                            for (int k = 0; k < augmented[0].length; k++)
                            {
                                augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
                            }
                        }
                        newB = augmented;
                    }
                    else
                    {
                        newB = tensors.get(i).b;
                    }

                    //Update C
                    if (augmentOver == Selection.C)
                    {
                        for (int j = 0; j < augmented.length; j++)
                        {
                            for (int k = 0; k < augmented[0].length; k++)
                            {
                                augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
                            }
                        }
                        newC = augmented;
                    }
                    else
                    {
                        newC = tensors.get(i).c;
                    }

                    RankTensor newTensor = new RankTensor(newA, newB, newC, tensors.get(i).hasSymmetry);
                    reducedScheme.add(newTensor);

                }
                else
                { //If in a set that did NOT have a linear dependancy
                    // A * B * C
                    reducedScheme.add(tensors.get(i));
                }
            }

        }

        return new MultiplicationMethod(reducedScheme);
    }
    */

    /**
     * Exhaustive search for a reduction. Also applies that reduction
     * @return Returns true is a reduction is found
     */
    public boolean tryReduce()
    {
        //See if those subsets are linearly dependant over on other matrix A B C (can't be the same as the first obvs)
        //If I can find one return true (indicating to random walk to not do a flip)
        ArrayList<ArrayList<int[]>> indexOfCommonA = getMatchingSelections(Selection.A); //Find subsets of A that have dimension 1 (all look the same)
        if (searchForLinearDependance(indexOfCommonA, Selection.A)) return true;

        ArrayList<ArrayList<int[]>> indexOfCommonB = getMatchingSelections(Selection.B); //Find subsets of B that have dimension 1 (all look the same)
        if (searchForLinearDependance(indexOfCommonB, Selection.B)) return true;

        ArrayList<ArrayList<int[]>> indexOfCommonC = getMatchingSelections(Selection.C); //Find subsets of C that have dimension 1 (all look the same)
        if (searchForLinearDependance(indexOfCommonC, Selection.C)) return true;

        //If can't find one return false (indicating to random walk to do a flip instead)
        return false;
    }
    /**
     *
     * @param indexOfCommon
     * @param ignoreMatrix
     * @return
     */
    public boolean searchForLinearDependance(ArrayList<ArrayList<int[]>> indexOfCommon, Selection ignoreMatrix)
    {
        for (int i = 0; i < indexOfCommon.size(); i++)
        {
            ArrayList<int[]> listOfSameCommon = indexOfCommon.get(i);
            ArrayList<int[]> nextToTest = new ArrayList<>(); //Stores a list of indexes of things to test (Similar to set "I" in the paper https://arxiv.org/pdf/2212.01175.pdf)

            final int MAX = (int)Math.pow(2, listOfSameCommon.size());
            TestTensors:
            for (int j = 0; j < MAX; j++)
            {
                nextToTest.clear();
                int temp = j;
                for (int index = 0; index < listOfSameCommon.size(); index++)
                {
                    if ((temp & 1) == 1)
                    {
                        nextToTest.add(listOfSameCommon.get(index));
                    }
                    temp = temp / 2;
                }
                /*
                =====================
                UNCOMMENT THIS
                */


                if (nextToTest.isEmpty() == false)
                {
                    boolean isUsingSymmetry = tensors.get(nextToTest.get(0)[0]).hasSymmetry;
                    for (int index = 1; index < nextToTest.size(); index++)
                    {
                        if (tensors.get(nextToTest.get(index)[0]).hasSymmetry != isUsingSymmetry)
                        { //If different systems don't use
                            continue TestTensors;
                        }
                    }
                }

                //need to write code to perform SYMMETRY on TENSORS

                int[] spin = new int[nextToTest.size()];

                for (int digit = 0; digit < spin.length; digit++)
                {
                    spin[digit] = 0;
                }


                int MAXCHANGEOFREPRESENTATIVES = (int)Math.pow(3, nextToTest.size());
                //System.out.println("WOAH HI");
                //flip SYMMETRY on tensors
                Spin:
                for (int spinCount = 0; spinCount < MAXCHANGEOFREPRESENTATIVES; spinCount++)
                {

                    //Rotate tensors in order (like counting in base 3, the discard the unused ones)
                    //System.out.println("WOAH HI x1");
                    int digit = 0;
                    if (spin.length > 0)
                    {
                        do
                        {
                            //System.out.println("WOAH HI x2");
                            spin[digit]++;
                            //System.out.println("WOAH HI x3");
                            tensors.get(nextToTest.get(digit)[0]).performExchangeInPlace();
                            //System.out.println("WOAH HI x4");
                            if (spin[digit] >= 3)
                            {
                                spin[digit] = 0;
                            }
                            //System.out.println("WOAH HI x5");
                            digit++;
                            //System.out.println("WOAH HI x6");
                        }
                        while(digit < spin.length && spin[digit-1] == 0);
                    }
                    //System.out.println("WOAH HI x10");

                    //see if valid combination

                    for (digit = 0; digit < spin.length; digit++)
                    {
                        if (nextToTest.get(digit)[spin[digit]] != 1)
                        {
                            continue Spin;
                        }
                    }



                    /*
                    System.out.println("New Test");
                    for (int index = 0; index < nextToTest.size(); index++)
                    {
                        System.out.print("Sym: " + tensors.get(nextToTest.get(index)).hasSymmetry);
                    }
                    System.out.println("");
                    System.out.println("Test it!");
                    */

                    /*
                    Note:
                    IgnoreMatrix is the matrix postion of DIMENSION 1 (all equal)

                    */

                    //Found list to test... now test it
                    if (ignoreMatrix != Selection.A)
                    {
                        int[] combination = testLinearDependance(nextToTest, Selection.A);
                        if (combination != null)
                        {
                            int tensorIndexToRemove = nextToTest.get(0)[0]; //I *THINK* because of how this is implemented I can pick any vector
                            //Find values for A and B lists
                            int[] a = new int[tensors.size()];
                            for (int k = 0; k < a.length; k++)
                            {
                                a[k] = 1; //For two vectors in F2... if A and B have dimension 1 then either one is 0 or A=B
                            }
                            int nextToTestIndex = 0;
                            int combinationIndex = 0;
                            int[] b = new int[tensors.size()];
                            for (int k = 0; k < b.length; k++)
                            {

                                if (nextToTestIndex < nextToTest.size() && k == nextToTest.get(nextToTestIndex)[0])
                                {
                                    if (k != tensorIndexToRemove)
                                    {
                                        b[k] = combination[combinationIndex];
                                        combinationIndex++;
                                    }
                                    nextToTestIndex++;
                                }
                                else
                                {
                                    b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                                }
                            }

                            reconstructMultiplicationScheme(ignoreMatrix, Selection.A, nextToTest, tensorIndexToRemove, a, b);

                            return true;
                        }
                    }
                    if (ignoreMatrix != Selection.B)
                    {
                        int[] combination = testLinearDependance(nextToTest, Selection.B);
                        if (combination != null)
                        {
                            int tensorIndexToRemove = nextToTest.get(0)[0]; //I *THINK* because of how this is implemented I can pick any vector
                            //Find values for A and B lists
                            int[] a = new int[tensors.size()];
                            for (int k = 0; k < a.length; k++)
                            {
                                a[k] = 1; //For two vectors in F2... if A and B have dimension 1 then either one is 0 or A=B
                            }
                            int nextToTestIndex = 0;
                            int combinationIndex = 0;
                            int[] b = new int[tensors.size()];
                            for (int k = 0; k < b.length; k++)
                            {

                                if (nextToTestIndex < nextToTest.size() && k == nextToTest.get(nextToTestIndex)[0])
                                {
                                    if (k != tensorIndexToRemove)
                                    {
                                        b[k] = combination[combinationIndex];
                                        combinationIndex++;
                                    }
                                    nextToTestIndex++;
                                }
                                else
                                {
                                    b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                                }
                            }

                            reconstructMultiplicationScheme(ignoreMatrix, Selection.B, nextToTest, tensorIndexToRemove, a, b);

                            return true;
                        }
                    }
                    if (ignoreMatrix != Selection.C)
                    {
                        int[] combination = testLinearDependance(nextToTest, Selection.C);
                        if (combination != null)
                        {
                            int tensorIndexToRemove = nextToTest.get(0)[0]; //I *THINK* because of how this is implemented I can pick any vector
                            //Find values for A and B lists
                            int[] a = new int[tensors.size()];
                            for (int k = 0; k < a.length; k++)
                            {
                                a[k] = 1; //For two vectors in F2... if A and B have dimension 1 then either one is 0 or A=B
                            }
                            int nextToTestIndex = 0;
                            int combinationIndex = 0;
                            int[] b = new int[tensors.size()];
                            for (int k = 0; k < b.length; k++)
                            {

                                if (nextToTestIndex < nextToTest.size() && k == nextToTest.get(nextToTestIndex)[0])
                                {
                                    if (k != tensorIndexToRemove)
                                    {
                                        b[k] = combination[combinationIndex];
                                        combinationIndex++;
                                    }
                                    nextToTestIndex++;
                                }
                                else
                                {
                                    b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                                }
                            }

                            reconstructMultiplicationScheme(ignoreMatrix, Selection.C, nextToTest, tensorIndexToRemove, a, b);

                            return true;
                        }
                    }

                }

            }
        }

        return false;
    }

    public void reconstructMultiplicationScheme(Selection dimensionOneOver, Selection linearlyDependantOver, ArrayList<int[]> listOfDependant, int tensorIndexToRemove, int[] a, int[] b)
    {
        //Reconstruct multiplication scheme
        ArrayList<RankTensor> reducedScheme = new ArrayList<>();
        //Find Tensor index to remove

        Selection augmentOver = Selection.A;

        //Find missing component
        while (dimensionOneOver == augmentOver || linearlyDependantOver == augmentOver)
        {
            augmentOver = Selection.values()[augmentOver.ordinal()+1];
        }


        RankTensor t = tensors.get(tensorIndexToRemove);
        for (int i = 0; i < tensors.size(); i++)
        {
            if (i != tensorIndexToRemove)
            {
                if (listOfDependant.contains(i))
                { //If in set that had a linear dependancy

                    //A * B * C + ab(C_t)

                    int[][] matrixToAugment = null;
                    int[][] tmatrixToAugment = null;
                    if (null != augmentOver)
                    {
                        switch (augmentOver)
                        {
                            case A:
                                matrixToAugment = tensors.get(i).a;
                                tmatrixToAugment = t.a;
                                break;
                            case B:
                                matrixToAugment = tensors.get(i).b;
                                tmatrixToAugment = t.b;
                                break;
                            case C:
                                matrixToAugment = tensors.get(i).c;
                                tmatrixToAugment = t.c;
                                break;
                            default:
                                break;
                        }
                    }
                    int[][] augmented = new int[matrixToAugment.length][matrixToAugment[0].length];

                    int[][]newA;
                    int[][]newB;
                    int[][]newC;

                    //Update A
                    if (augmentOver == Selection.A)
                    {
                        for (int j = 0; j < augmented.length; j++)
                        {
                            for (int k = 0; k < augmented[0].length; k++)
                            {
                                //augmented[j][k] = matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k]);
                                augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
                            }
                        }
                        newA = augmented;
                    }
                    else
                    {
                        newA = tensors.get(i).a;
                    }

                    //Update B
                    if (augmentOver == Selection.B)
                    {
                        for (int j = 0; j < augmented.length; j++)
                        {
                            for (int k = 0; k < augmented[0].length; k++)
                            {
                                augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
                            }
                        }
                        newB = augmented;
                    }
                    else
                    {
                        newB = tensors.get(i).b;
                    }

                    //Update C
                    if (augmentOver == Selection.C)
                    {
                        for (int j = 0; j < augmented.length; j++)
                        {
                            for (int k = 0; k < augmented[0].length; k++)
                            {
                                augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
                            }
                        }
                        newC = augmented;
                    }
                    else
                    {
                        newC = tensors.get(i).c;
                    }

                    RankTensor newTensor = new RankTensor(newA, newB, newC, tensors.get(i).hasSymmetry);
                    reducedScheme.add(newTensor);

                }
                else
                { //If in a set that did NOT have a linear dependancy
                    // A * B * C
                    reducedScheme.add(tensors.get(i));
                }
            }

        }

        tensors = reducedScheme;
    }
    /**
     *
     * @param indexes
     * @param matrixToTest
     * @return
     */
    public int[] testLinearDependance(ArrayList<int[]> indexes, Selection matrixToTest)
    {
        int[][] matToReduce;
        int[][] matReference = null;

        switch (matrixToTest)
        {
            case A:
                matReference = tensors.get(0).a;
                break;
            case B:
                matReference = tensors.get(0).b;
                break;
            case C:
                matReference = tensors.get(0).c;
                break;
            default:
                break;
        }

        matToReduce = new int[matReference.length*matReference[0].length][indexes.size()];
        for (int i = 0; i < indexes.size(); i++)
        {
            switch (matrixToTest)
            {
                case A:
                    matReference = tensors.get(indexes.get(i)[0]).a;
                    break;
                case B:
                    matReference = tensors.get(indexes.get(i)[0]).b;
                    break;
                case C:
                    matReference = tensors.get(indexes.get(i)[0]).c;
                    break;
                default:
                    break;
            }

            //System.out.println("Add: ");
            //printArray(matReference);

            int count = 0;
            for (int x = 0; x < matReference.length; x++)
            {
                for (int y = 0; y < matReference[0].length; y++)
                {
                    matToReduce[count][i] = matReference[x][y];
                    count++;
                }
            }
        }

        //Find linear dependacies by using Gaussian Elimination

        //System.out.println("Pre guass");
        //printArray(matToReduce);

        int[][] matPreReduction = cloneMat(matToReduce);

        //Perform gaussian elmination
        gaussian(matToReduce);

        //Test for linear dependancy
        if (testDependancy(matToReduce))
        {
            //System.out.println("YAY... ");
            //System.out.println("Dependant: " + matrixToTest);

            //System.out.println("post guass");
            //printArray(matToReduce);

            //Need to find relevant t vector and way of making t vector

            int tIndex = 0;

            int[] solution = fullgaussian(removeColumn(matPreReduction, tIndex), getCol(matPreReduction, tIndex));
            /*
            StringBuilder sb = new StringBuilder();
            sb.append("Solution: ");
            for (int i = 0; i < solution.length; i++)
            {
                sb.append(solution[i]);
                sb.append(", ");
            }
            System.out.println(sb);

            for (int i = 0; i < indexes.size(); i++)
            {
                System.out.println(tensors.get(indexes.get(i)));
            }
            (
            */

            return solution;
        }
        else
        {
            //System.out.println("Fail");
        }


        return null;
    }
    /**
     *
     * @param mat
     * @return
     */
    private static int[][] cloneMat(int[][] mat)
    {
        int[][] result = new int[mat.length][mat[0].length];
        for (int i = 0; i < mat.length; i++)
        {
            System.arraycopy(mat[i], 0, result[i], 0, result[i].length);
        }
        return result;
    }
    /**
     *
     * @param mat
     * @param removeCol
     * @return
     */
    private static int[][] removeColumn(int[][] mat, int removeCol)
    {
        int[][] result = new int[mat.length][mat[0].length-1];

        for (int outrow = 0; outrow < result.length; outrow++)
        {
            for (int outcol = 0, incol = 0; outcol < result[0].length; outcol++, incol++)
            {
                if (incol == removeCol)
                {
                    incol++;
                }
                //System.out.println("outrow: " + outrow + ", outcol: " + outcol + " incol: " + incol);
                result[outrow][outcol] = mat[outrow][incol];
            }
        }
        return result;
    }
    /**
     *
     * @param mat
     * @param col
     * @return
     */
    private static int[] getCol(int[][] mat, int col)
    {
        int[] result = new int[mat.length];
        for (int row = 0; row < mat.length; row++)
        {
            result[row] = mat[row][col];
        }
        return result;
    }

    /**
     *
     * @param mat
     */
    public static void printArray(int[][] mat)
    {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < mat.length; row++)
        {
            sb.append("|");
            for (int col = 0; col < mat[0].length; col++)
            {
                sb.append(" ");
                sb.append(mat[row][col]);
            }
            sb.append(" |\n");
        }
        System.out.println(sb);
    }

    public static void printArray(int[][] mat, int[] rhs)
    {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < mat.length; row++)
        {
            sb.append("|");
            for (int col = 0; col < mat[0].length; col++)
            {
                sb.append(" ");
                sb.append(mat[row][col]);
            }
            sb.append(" | ");
            sb.append(rhs[row]);
            sb.append(" |\n");
        }
        System.out.println(sb);
    }
    /**
     *
     * @param mat
     * @return
     */
    public boolean testDependancy(int[][] mat)
    {
        //If the rank is smaller than the number of vectors we know there must be a linear dependancy
        return rank(mat) < mat[0].length;
    }
    /**
     *
     * @param mat
     * @return
     */
    public int rank(int[][] mat)
    {
        int count = 0;
        for (int row = 0; row < mat.length; row++)
        {
            ColumnSearch:
            for (int col = 0; col < mat[0].length; col++)
            {
                if (mat[row][col] != 0)
                {
                    count++;
                    break ColumnSearch;
                }
            }
        }
        return count;
    }
    /**
     *
     * @param a
     * @param b
     * @return
     */
    private static int min(int a, int b)
    {
        return a < b ? a : b;
    }

    /**
    *
    * @param mat
    * @param rhs
    * @return
    */
    public static int[] fullgaussian(int[][] mat, int[]rhs)
    {
        int N = min(mat.length, mat[0].length);
        for (int k = 0; k < N; k++)
        {
            // find pivot row
            int max = k;
            for (int i = k + 1; i < mat.length; i++)
            {
                if (mat[i][k] > mat[max][k])
                {
                    max = i;
                }
            }

            // swap row in A matrix
            int[] temp = mat[k];
            mat[k] = mat[max];
            mat[max] = temp;

            // swap row in B matrix
            int temp2 = rhs[k];
            rhs[k] = rhs[max];
            rhs[max] = temp2;

            //row k swapped with row max

            // pivot within A
            for (int i = k + 1; i < mat.length; i++)
            {
                //Factor not needed for this size
                //double factor = mat[i][k] / mat[k][k]; //mat[k][k] should be 1

                if (mat[i][k] == 1)
                {
                    rhs[i] = (rhs[i] + rhs[k]) % 2; //On the field F2
                    for (int j = k; j < N; j++)
                    {
                        mat[i][j] = (mat[i][j] + mat[k][j]) % 2; //On the field F2
                    }
                }
            }
        }
        int[] solution = new int[mat[0].length]; //Technically this is less rows than columns but it works
        for (int i = N - 1; i >= 0; i--)
        {
            int sum = 0;
            for (int j = i + 1; j < N; j++)
            {
                sum += mat[i][j] * solution[j];
            }
            if (mat[i][i] == 0)
            {
                System.out.println("GAUSSIAN FAILED (DIVIDE BY ZERO)");
                printArray(mat, rhs);
                return null;
            }
            //solution[i] = (rhs[i] - sum) / mat[i][i];
            solution[i] = (rhs[i] + sum) % 2; //Don't need division on F2, Addition is the same as subtraction
        }

        //Need to check all lines below still work
        for (int i = N; i < rhs.length; i++)
        {
            if (rhs[i] != 0)
            {
                System.out.println("GAUSSIAN FAILED (TOO MANY TERMS)");
                printArray(mat, rhs);
                return null;
            }
            else
            {
                //System.out.println("YAYAYAY");
            }
        }

        return solution;
    }

    /**
    * Guassian elimination on a feild of 2 elements
     * @param mat
    */
    public static void gaussian(int[][] mat)
    {
        int N = min(mat.length, mat[0].length);
        for (int k = 0; k < N; k++)
        {
            // find pivot row
            int max = k;
            for (int i = k + 1; i < mat.length; i++)
            {
                if (mat[i][k] > mat[max][k])
                {
                    max = i;
                }
            }

            // swap row in A matrix
            int[] temp = mat[k];
            mat[k] = mat[max];
            mat[max] = temp;
            //row k swapped with row max


            // pivot within A
            for (int i = k + 1; i < mat.length; i++)
            {
                //Factor not needed for this size
                //double factor = mat[i][k] / mat[k][k]; //mat[k][k] should be 1

                if (mat[i][k] == 1)
                {
                    for (int j = k; j < N; j++)
                    {
                        mat[i][j] = (mat[i][j] + mat[k][j]) % 2;
                    }
                }
            }
        }
    }
    /**
     *
     * @param lookingAtMatrix
     * @return
     */
    public ArrayList<ArrayList<int[]>> getMatchingSelections(Selection lookingAtMatrix)
    {
        ArrayList<ArrayList<int[]>>result = new ArrayList<>();
        ArrayList<int[][]> commonMatrix = new ArrayList<>();

        for (int i = 0; i < tensors.size(); i++)
        {
            RankTensor t = tensors.get(i);
            int[][] selection = null;
            int[][] selectionSpin1 = null;
            int[][] selectionSpin2 = null;

            if (null != lookingAtMatrix)
            {
                switch (lookingAtMatrix)
                {
                    case A:
                        selection = t.a;
                        selectionSpin1 = t.b;
                        selectionSpin2 = t.c;
                        break;
                    case B:
                        selection = t.b;
                        selectionSpin1 = t.c;
                        selectionSpin2 = t.a;
                        break;
                    case C:
                        selection = t.c;
                        selectionSpin1 = t.a;
                        selectionSpin2 = t.b;
                        break;
                    default:
                        break;
                }
            }

            boolean foundMatch = false;

            int containsMatrixAtIndex = -1;
            for (int j = 0; j < commonMatrix.size(); j++)
            {

                int[] entry = new int[4];
                entry[0] = i;
                boolean needsEntry = false;

                int[][] m = commonMatrix.get(j);
                if (RankTensor.areMatrixEqual(m, selection))
                {
                    foundMatch = true;

                    needsEntry = true;
                    entry[1] = 1;

                    break;
                }

                if (RankTensor.areMatrixEqual(m, selectionSpin1))
                {
                    foundMatch = true;

                    needsEntry = true;
                    entry[2] = 1;

                    break;
                }
                if (RankTensor.areMatrixEqual(m, selectionSpin2))
                {
                    foundMatch = true;

                    needsEntry = true;
                    entry[3] = 1;

                    break;
                }

                if (needsEntry)
                {
                    //If it's already contained append this index to the list
                    result.get(j).add(entry);
                }

            }

            if (foundMatch == false)
            { //If common matrix item is not already contained

                int[] entry = new int[4];
                entry[0] = i;
                entry[1] = 1;
                entry[2] = 0;
                entry[3] = 0;

                ArrayList<int[]> locationIndex = new ArrayList<>();
                locationIndex.add(entry); //Add current position to start new group


                result.add(locationIndex);
                commonMatrix.add(selection);
            }

        }

        return result;
    }
    /**
     *
     */
    public enum Selection
    {
        A,
        B,
        C
    }

    public void changeRepresentitive()
    {
        ArrayList<Integer> potentialChange = new ArrayList<>();

        for (int i = 0; i < tensors.size(); i++)
        {
            RankTensor t = tensors.get(i);
            if (t.hasSymmetry)
            {
                potentialChange.add(i);
            }
        }
        if (potentialChange.isEmpty() == false)
        {

            int index = potentialChange.get(randomInt(potentialChange.size()));

            RankTensor tensorToChange = tensors.get(index);

            RankTensor changedTensor = tensorToChange.performExchange();

            tensors.set(index, changedTensor);
        }
    }

    public ArrayList<MultiplicationMethod>getAllRepresentativeChanges()
    {
        ArrayList<MultiplicationMethod> ret = new ArrayList<>();
        ArrayList<Integer> potentialChange = new ArrayList<>();

        for (int i = 0; i < tensors.size(); i++)
        {
            RankTensor t = tensors.get(i);
            if (t.hasSymmetry)
            {
                potentialChange.add(i);
            }
        }
        for (int index : potentialChange)
        {
            MultiplicationMethod newMethod = new MultiplicationMethod(tensors);

            RankTensor tensorToChange = newMethod.tensors.get(index);

            RankTensor changedTensor = tensorToChange.performExchange();

            newMethod.tensors.set(index, changedTensor);

            ret.add(newMethod);
        }

        //System.out.println("Found " + ret.size() + " possible representative changes");

        return ret;
    }

    public ArrayList<MultiplicationMethod> getAllFlips()
    {
        ArrayList<Integer> potentialFlip = new ArrayList<>();

        ArrayList<MultiplicationMethod>ret = new ArrayList<>();

        RankTensor xTensor = null;
        RankTensor yTensor = null;

        int xIndex;
        int yIndex;

        for (xIndex = 0; xIndex < tensors.size(); xIndex++) {
        for (yIndex = 0; yIndex < tensors.size()-1; yIndex++) {

            if (yIndex == xIndex)
            {
                yIndex++;
            }

            xTensor = tensors.get(xIndex);
            yTensor = tensors.get(yIndex);

            potentialFlip.clear();
            if (xTensor.hasSymmetry == yTensor.hasSymmetry) {
                //Verify flip can take place
                if (RankTensor.areMatrixEqual(xTensor.a, yTensor.a))
                {
                    potentialFlip.add(1);
                }
                if (RankTensor.areMatrixEqual(xTensor.b, yTensor.b))
                {
                    potentialFlip.add(2);
                }
                if (RankTensor.areMatrixEqual(xTensor.c, yTensor.c))
                {
                    potentialFlip.add(3);
                }
            }



            if (potentialFlip.isEmpty() == false)
            {
                for (int selectedflip : potentialFlip)
                {

                //Pick a random flip
                //int selectedflip = potentialFlip.get(randomInt(potentialFlip.size()));

                    int[][] xA = new int[xTensor.a.length][xTensor.a[0].length];
                    int[][] xB = new int[xTensor.b.length][xTensor.b[0].length];
                    int[][] xC = new int[xTensor.c.length][xTensor.c[0].length];

                    int[][] yA = new int[yTensor.a.length][yTensor.a[0].length];
                    int[][] yB = new int[yTensor.b.length][yTensor.b[0].length];
                    int[][] yC = new int[yTensor.c.length][yTensor.c[0].length];

                    switch (selectedflip)
                    {
                        case 1: //A is the same

                            //A component
                            for (int i = 0; i < xA.length; i++)
                            {
                                for (int j = 0; j < xA[0].length; j++)
                                {
                                    xA[i][j] = xTensor.a[i][j];
                                }
                            }
                            for (int i = 0; i < xA.length; i++)
                            {
                                for (int j = 0; j < xA[0].length; j++)
                                {
                                    yA[i][j] = yTensor.a[i][j];
                                }
                            }

                            //B component
                            for (int i = 0; i < xB.length; i++)
                            {
                                for (int j = 0; j < xB[0].length; j++)
                                {
                                    //Commented out as over the feild of two elements we can use MOD (/XOR)
                                    //xB[i][j] = xTensor.b[i][j] + yTensor.b[i][j];
                                    xB[i][j] = (xTensor.b[i][j] + yTensor.b[i][j]) % 2;
                                }
                            }
                            for (int i = 0; i < xB.length; i++)
                            {
                                for (int j = 0; j < xB[0].length; j++)
                                {
                                    yB[i][j] = yTensor.b[i][j];
                                }
                            }

                            //C component
                            for (int i = 0; i < xC.length; i++)
                            {
                                for (int j = 0; j < xC[0].length; j++)
                                {
                                    xC[i][j] = xTensor.c[i][j];
                                }
                            }
                            for (int i = 0; i < xC.length; i++)
                            {
                                for (int j = 0; j < xC[0].length; j++)
                                {
                                    //Commented out as over the feild of two elements we can use MOD (/XOR)
                                    //yC[i][j] = yTensor.c[i][j] - xTensor.c[i][j];
                                    yC[i][j] = (yTensor.c[i][j] + xTensor.c[i][j]) % 2;
                                }
                            }

                            break;
                        case 2: //B is the same

                            //A component
                            for (int i = 0; i < xA.length; i++)
                            {
                                for (int j = 0; j < xA[0].length; j++)
                                {
                                    //Commented out as over the feild of two elements we can use MOD (/XOR)
                                    //xA[i][j] = xTensor.a[i][j] + yTensor.a[i][j];
                                    xA[i][j] = (xTensor.a[i][j] + yTensor.a[i][j]) % 2;
                                }
                            }
                            for (int i = 0; i < xA.length; i++)
                            {
                                for (int j = 0; j < xA[0].length; j++)
                                {
                                    yA[i][j] = yTensor.a[i][j];
                                }
                            }

                            //B component
                            for (int i = 0; i < xB.length; i++)
                            {
                                for (int j = 0; j < xB[0].length; j++)
                                {
                                    xB[i][j] = xTensor.b[i][j];
                                }
                            }
                            for (int i = 0; i < xB.length; i++)
                            {
                                for (int j = 0; j < xB[0].length; j++)
                                {
                                    yB[i][j] = yTensor.b[i][j];
                                }
                            }

                            //C component
                            for (int i = 0; i < xC.length; i++)
                            {
                                for (int j = 0; j < xC[0].length; j++)
                                {
                                    xC[i][j] = xTensor.c[i][j];
                                }
                            }
                            for (int i = 0; i < xC.length; i++)
                            {
                                for (int j = 0; j < xC[0].length; j++)
                                {
                                    //Commented out as over the feild of two elements we can use MOD (/XOR)
                                    //yC[i][j] = yTensor.c[i][j] - xTensor.c[i][j];
                                    yC[i][j] = (yTensor.c[i][j] + xTensor.c[i][j]) % 2;
                                }
                            }

                            break;
                        case 3: //C is the same

                            //A component
                            for (int i = 0; i < xA.length; i++)
                            {
                                for (int j = 0; j < xA[0].length; j++)
                                {
                                    //Commented out as over the feild of two elements we can use MOD (/XOR)
                                    //xA[i][j] = xTensor.a[i][j] + yTensor.a[i][j];
                                    xA[i][j] = (xTensor.a[i][j] + yTensor.a[i][j]) % 2;
                                }
                            }
                            for (int i = 0; i < xA.length; i++)
                            {
                                for (int j = 0; j < xA[0].length; j++)
                                {
                                    yA[i][j] = yTensor.a[i][j];
                                }
                            }

                            //B component
                            for (int i = 0; i < xB.length; i++)
                            {
                                for (int j = 0; j < xB[0].length; j++)
                                {
                                    xB[i][j] = xTensor.b[i][j];
                                }
                            }
                            for (int i = 0; i < xB.length; i++)
                            {
                                for (int j = 0; j < xB[0].length; j++)
                                {
                                    //Commented out as over the feild of two elements we can use MOD (/XOR)
                                    //yB[i][j] = yTensor.b[i][j] - xTensor.b[i][j];
                                    yB[i][j] = (yTensor.b[i][j] + xTensor.b[i][j]) % 2;
                                }
                            }

                            //C component
                            for (int i = 0; i < xC.length; i++)
                            {
                                for (int j = 0; j < xC[0].length; j++)
                                {
                                    xC[i][j] = xTensor.c[i][j];
                                }
                            }
                            for (int i = 0; i < xC.length; i++)
                            {
                                for (int j = 0; j < xC[0].length; j++)
                                {
                                    yC[i][j] = yTensor.c[i][j];
                                }
                            }

                            break;
                        default:
                            break;
                    }

                    MultiplicationMethod flippedMethod = new MultiplicationMethod(tensors);

                    RankTensor flippedATensor = new RankTensor(xA, xB, xC, xTensor.hasSymmetry);
                    RankTensor flippedBTensor = new RankTensor(yA, yB, yC, yTensor.hasSymmetry);

                    //System.out.println("FLIPPED TENSORS:");
                    //System.out.println("");
                    //System.out.println(flippedATensor);
                    //System.out.println(flippedBTensor);
                    //System.out.println("");


                    if (xIndex > yIndex)
                    {
                        flippedMethod.tensors.remove(xIndex);
                        flippedMethod.tensors.remove(yIndex);
                    }
                    else
                    {
                        flippedMethod.tensors.remove(yIndex);
                        flippedMethod.tensors.remove(xIndex);
                    }

                    flippedMethod.tensors.add(flippedATensor);
                    flippedMethod.tensors.add(flippedBTensor);

                    ret.add(flippedMethod);

                }
            }
        }
        }

        //System.out.println("Found " + ret.size() + " possible flips");
        return ret;
    }

    /**
     * Performs a random flip on the current multiplication method
     * Flips are of the form
     * a * b * c  + a * B' * C' => a * (b + B') * c + a * B' * (c - C')
     * Up to re-ordering of A, B and C (and B', C' and A' respectively)
     */
    public void makeFlip()
    {

        ArrayList<Integer> potentialFlip = new ArrayList<>();
        ArrayList<Integer> changeRepresentativeBy = new ArrayList<>();

        RankTensor xTensor = null;
        RankTensor yTensor = null;

        int xIndex = -1;
        int yIndex = -1;

        while (potentialFlip.isEmpty())
        {
            /*
            if (Math.random() < 0.5) //Random chance to change representative
            {
                changeRepresentitive();
            }
            */


            //Generate 2 unique and nearly evenly distributed numbers
            xIndex = randomInt(tensors.size());
            yIndex = randomInt(tensors.size()-1);
            if (yIndex >= xIndex)
            {
                yIndex++;
            }

            xTensor = tensors.get(xIndex);
            yTensor = tensors.get(yIndex);

            RankTensor temp = yTensor;

            for (int i = 0; i <= 2; i++)
            {

                //Verify flip can take place
                if (RankTensor.areMatrixEqual(xTensor.a, temp.a))
                {
                    potentialFlip.add(1);
                    changeRepresentativeBy.add(i);
                }
                if (RankTensor.areMatrixEqual(xTensor.b, temp.b))
                {
                    potentialFlip.add(2);
                    changeRepresentativeBy.add(i);
                }
                if (RankTensor.areMatrixEqual(xTensor.c, temp.c))
                {
                    potentialFlip.add(3);
                    changeRepresentativeBy.add(i);
                }

                temp = temp.performExchange();
            }
        }

        int index = randomInt(potentialFlip.size());

        //Pick a random flip
        int selectedflip = potentialFlip.get(index);
        int representativeSpinBy = changeRepresentativeBy.get(index);

        //System.out.println(potentialFlip.size() + " HI " + changeRepresentativeBy.size());

        //System.out.println("TEST");

        //System.out.println("PRE SPIN");
        //System.out.println(xTensor);
        //System.out.println(yTensor);

        for (int i = 0; i < representativeSpinBy; i++)
        {

            //System.out.println("SPIN");
            yTensor = yTensor.performExchange();
            //System.out.println(yTensor);
        }

        //System.out.println("POST SPIN");
        //System.out.println(xTensor);
        //System.out.println(yTensor);

        /*

        switch (selectedflip)
        {
            case 1: //A is the same
                System.out.println("As EQUAL: " + RankTensor.areMatrixEqual(xTensor.a, yTensor.a));
                break;
            case 2:
                System.out.println("Bs EQUAL: " + RankTensor.areMatrixEqual(xTensor.b, yTensor.b));
                break;
            case 3:
                System.out.println("Cs EQUAL: " + RankTensor.areMatrixEqual(xTensor.c, yTensor.c));
                break;
            default:
                break;
        }


        */
        int[][] xA = new int[xTensor.a.length][xTensor.a[0].length];
        int[][] xB = new int[xTensor.b.length][xTensor.b[0].length];
        int[][] xC = new int[xTensor.c.length][xTensor.c[0].length];

        int[][] yA = new int[yTensor.a.length][yTensor.a[0].length];
        int[][] yB = new int[yTensor.b.length][yTensor.b[0].length];
        int[][] yC = new int[yTensor.c.length][yTensor.c[0].length];

        switch (selectedflip)
        {
            case 1: //A is the same

                //A component
                for (int i = 0; i < xA.length; i++)
                {
                    for (int j = 0; j < xA[0].length; j++)
                    {
                        xA[i][j] = xTensor.a[i][j];
                    }
                }
                for (int i = 0; i < xA.length; i++)
                {
                    for (int j = 0; j < xA[0].length; j++)
                    {
                        yA[i][j] = yTensor.a[i][j];
                    }
                }

                //B component
                for (int i = 0; i < xB.length; i++)
                {
                    for (int j = 0; j < xB[0].length; j++)
                    {
                        //Commented out as over the feild of two elements we can use MOD (/XOR)
                        //xB[i][j] = xTensor.b[i][j] + yTensor.b[i][j];
                        xB[i][j] = (xTensor.b[i][j] + yTensor.b[i][j]) % 2;
                    }
                }
                for (int i = 0; i < xB.length; i++)
                {
                    for (int j = 0; j < xB[0].length; j++)
                    {
                        yB[i][j] = yTensor.b[i][j];
                    }
                }

                //C component
                for (int i = 0; i < xC.length; i++)
                {
                    for (int j = 0; j < xC[0].length; j++)
                    {
                        xC[i][j] = xTensor.c[i][j];
                    }
                }
                for (int i = 0; i < xC.length; i++)
                {
                    for (int j = 0; j < xC[0].length; j++)
                    {
                        //Commented out as over the feild of two elements we can use MOD (/XOR)
                        //yC[i][j] = yTensor.c[i][j] - xTensor.c[i][j];
                        yC[i][j] = (yTensor.c[i][j] + xTensor.c[i][j]) % 2;
                    }
                }

                break;
            case 2: //B is the same

                //A component
                for (int i = 0; i < xA.length; i++)
                {
                    for (int j = 0; j < xA[0].length; j++)
                    {
                        //Commented out as over the feild of two elements we can use MOD (/XOR)
                        //xA[i][j] = xTensor.a[i][j] + yTensor.a[i][j];
                        xA[i][j] = (xTensor.a[i][j] + yTensor.a[i][j]) % 2;
                    }
                }
                for (int i = 0; i < xA.length; i++)
                {
                    for (int j = 0; j < xA[0].length; j++)
                    {
                        yA[i][j] = yTensor.a[i][j];
                    }
                }

                //B component
                for (int i = 0; i < xB.length; i++)
                {
                    for (int j = 0; j < xB[0].length; j++)
                    {
                        xB[i][j] = xTensor.b[i][j];
                    }
                }
                for (int i = 0; i < xB.length; i++)
                {
                    for (int j = 0; j < xB[0].length; j++)
                    {
                        yB[i][j] = yTensor.b[i][j];
                    }
                }

                //C component
                for (int i = 0; i < xC.length; i++)
                {
                    for (int j = 0; j < xC[0].length; j++)
                    {
                        xC[i][j] = xTensor.c[i][j];
                    }
                }
                for (int i = 0; i < xC.length; i++)
                {
                    for (int j = 0; j < xC[0].length; j++)
                    {
                        //Commented out as over the feild of two elements we can use MOD (/XOR)
                        //yC[i][j] = yTensor.c[i][j] - xTensor.c[i][j];
                        yC[i][j] = (yTensor.c[i][j] + xTensor.c[i][j]) % 2;
                    }
                }

                break;
            case 3: //C is the same

                //A component
                for (int i = 0; i < xA.length; i++)
                {
                    for (int j = 0; j < xA[0].length; j++)
                    {
                        //Commented out as over the feild of two elements we can use MOD (/XOR)
                        //xA[i][j] = xTensor.a[i][j] + yTensor.a[i][j];
                        xA[i][j] = (xTensor.a[i][j] + yTensor.a[i][j]) % 2;
                    }
                }
                for (int i = 0; i < xA.length; i++)
                {
                    for (int j = 0; j < xA[0].length; j++)
                    {
                        yA[i][j] = yTensor.a[i][j];
                    }
                }

                //B component
                for (int i = 0; i < xB.length; i++)
                {
                    for (int j = 0; j < xB[0].length; j++)
                    {
                        xB[i][j] = xTensor.b[i][j];
                    }
                }
                for (int i = 0; i < xB.length; i++)
                {
                    for (int j = 0; j < xB[0].length; j++)
                    {
                        //Commented out as over the feild of two elements we can use MOD (/XOR)
                        //yB[i][j] = yTensor.b[i][j] - xTensor.b[i][j];
                        yB[i][j] = (yTensor.b[i][j] + xTensor.b[i][j]) % 2;
                    }
                }

                //C component
                for (int i = 0; i < xC.length; i++)
                {
                    for (int j = 0; j < xC[0].length; j++)
                    {
                        xC[i][j] = xTensor.c[i][j];
                    }
                }
                for (int i = 0; i < xC.length; i++)
                {
                    for (int j = 0; j < xC[0].length; j++)
                    {
                        yC[i][j] = yTensor.c[i][j];
                    }
                }

                break;
            default:
                break;
        }

        RankTensor flippedATensor = new RankTensor(xA, xB, xC, xTensor.hasSymmetry);
        RankTensor flippedBTensor = new RankTensor(yA, yB, yC, yTensor.hasSymmetry);

        //System.out.println("FLIPPED TENSORS:");
        //System.out.println("");
        //System.out.println(flippedATensor);
        //System.out.println(flippedBTensor);
        //System.out.println("");


        if (xIndex > yIndex)
        {
            tensors.remove(xIndex);
            tensors.remove(yIndex);
        }
        else
        {
            tensors.remove(yIndex);
            tensors.remove(xIndex);
        }

        tensors.add(flippedATensor);
        tensors.add(flippedBTensor);
    }
    /**
     *
     * @param max
     * @return
     */
    private int randomInt(int max) //max not included
    {
        return (int)Math.floor(Math.random()*max);
    }

    private void sumTensor(RankTensor t, int[][][][][][] result)
    {
        int a[][] = t.a;
        for (int ai = 0; ai < a.length; ai++)
        {
            for (int aj = 0; aj < a[0].length; aj++)
            {
                if (a[ai][aj] == 1)
                {
                    int b[][] = t.b;
                    for (int bi = 0; bi < b.length; bi++)
                    {
                        for (int bj = 0; bj < b[0].length; bj++)
                        {
                            if (b[bi][bj] == 1)
                            {
                                int c[][] = t.c;
                                for (int ci = 0; ci < c.length; ci++)
                                {
                                    for (int cj = 0; cj < c[0].length; cj++)
                                    {
                                        if (c[ci][cj] == 1)
                                        {
                                            result[ai][aj][bi][bj][ci][cj] = (result[ai][aj][bi][bj][ci][cj] + 1) % 2;
                                        }
                                        else if (c[ci][cj] != 0)
                                        {
                                            System.out.println("BIG ERROR WOW YOU SCREWED UP REALLY BADLY HERE");
                                        }
                                    }
                                }
                            }
                            else if (b[bi][bj] != 0)
                            {
                                System.out.println("BIG ERROR WOW YOU SCREWED UP REALLY BADLY HERE");
                            }
                        }
                    }
                }
                else if (a[ai][aj] != 0)
                {
                    System.out.println("BIG ERROR WOW YOU SCREWED UP REALLY BADLY HERE");
                }
            }
        }
    }

    private int[][][][][][] constructResult(MultiplicationMethod m)
    {
        int[][][][][][] result = new int
                        [m.tensors.get(0).a.length]
                        [m.tensors.get(0).a[0].length]
                        [m.tensors.get(0).b.length]
                        [m.tensors.get(0).b[0].length]
                        [m.tensors.get(0).c.length]
                        [m.tensors.get(0).c[0].length];

        for (RankTensor t : m.tensors)
        {
            sumTensor(t, result);

            if (t.hasSymmetry)
            {
                RankTensor t1 = t.performExchange();
                RankTensor t2 = t1.performExchange();
                sumTensor(t1, result);
                sumTensor(t2, result);
            }
        }
        return result;
    }

    /**
     *
     * @return
     */
    public boolean testValidity()
    {
        int[][][][][][] result = constructResult(this);
        int[][][][][][] testAgainst = constructResult(getBasicMethod(tensors.get(0).a[0].length, tensors.get(0).a.length, tensors.get(0).b[0].length));
        boolean success = true;
        StringBuilder sb = new StringBuilder();
        for (int ci = 0; ci < result[0][0][0][0].length; ci++)
        {
            for (int cj = 0; cj < result[0][0][0][0][0].length; cj++)
            {
                sb.append("c_");
                sb.append(ci);
                sb.append(",");
                sb.append(cj);
                sb.append(" = (");
                for (int ai = 0; ai < result.length; ai++)
                {
                    for (int aj = 0; aj < result[0].length; aj++)
                    {
                        for (int bi = 0; bi < result[0][0].length; bi++)
                        {
                            for (int bj = 0; bj < result[0][0][0].length; bj++)
                            {
                                if (result[ai][aj][bi][bj][ci][cj] == 1)
                                {
                                    sb.append("a_");
                                    sb.append(ai);
                                    sb.append(",");
                                    sb.append(aj);

                                    sb.append(" * b_");
                                    sb.append(bi);
                                    sb.append(",");
                                    sb.append(bj);

                                    sb.append(") + (");
                                }
                                if (result[ai][aj][bi][bj][ci][cj] != testAgainst[ai][aj][bi][bj][ci][cj])
                                {
                                    success = false;
                                }
                            }
                        }
                    }
                }
                sb.append(")");
                sb.append("\n");
            }

        }
        if (success == false) { System.out.println(sb.toString());}
        return success;
    }

}
