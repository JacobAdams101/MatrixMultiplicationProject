//package fastmatrixmultiplication;


import java.util.ArrayList;

/**
 *
 * @author jacob
 */
public class MultiplicationMethod
{
    /**
     *
     */
    private ArrayList<RankOneTensor>tensors;

    /**
     *
     * @return
     */
    public ArrayList<RankOneTensor> getTensors()
    {
        return tensors;
    }


    @Override
    public boolean equals(Object e)
    {
        if (e instanceof MultiplicationMethod)
        {
            MultiplicationMethod mm = (MultiplicationMethod)e;

            ArrayList<RankOneTensor> mmTensors = new ArrayList<>();

            mmTensors.addAll(mm.tensors);

            for (RankOneTensor t : tensors)
            {
                int index = mmTensors.indexOf(t);
                if (index == -1)
                { //Something not in list
                    return false; //Return false
                }
                else
                {
                    mmTensors.remove(index);
                }
            }
            return mmTensors.isEmpty(); //If scheme is not empty then not equal

        }
        else
        {
            return false;
        }
    }

    /**
     *
     */
    public MultiplicationMethod()
    {
        tensors = new ArrayList<>();
    }

    public MultiplicationMethod(ArrayList<RankOneTensor>t)
    {
        tensors = new ArrayList<>();
        tensors.addAll(t);
    }

    public void markTensorsAsChangeUnchanged(boolean set)
    {
        for (RankOneTensor t : tensors)
        {
            t.justFlipped = set;
        }
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
                    int[] a = new int[rowsA];
                    int[] b = new int[rowsB];
                    int[] c = new int[colsC]; //Transpose

                    RankOneTensor.setEntry(a, i, j, 1);
                    RankOneTensor.setEntry(b, j, k, 1);
                    RankOneTensor.setEntry(c, k, i, 1);

                    ret.tensors.add(new RankOneTensor(a,b,c));
                }
            }
        }

        return ret;
    }




    public void expandBySymmetry()
    {
        final int ORIGINAL_TENSOR_SIZE = tensors.size();
        for (int i = 0; i < ORIGINAL_TENSOR_SIZE; i++)
        {
            RankOneTensor t0 = tensors.get(i);
            RankOneTensor t1 = t0.performExchange();
            if (t0.isEqual(t1))
            { //If t0 is the same as t1 it implies t0.a = t1.a = t0.b  AND  t0.b = t1.b = t0.c SO t0.a = t0.b = t0.c
            //So no point continuing
                continue; //Skip iteration
            }
            RankOneTensor t2 = t1.performExchange();

            tensors.add(t1);
            tensors.add(t2);
        }
    }

    public void reduceToSymmetry(boolean makeAllSymmetric)
    {
        int i;
        for (i = 0; i < tensors.size(); i++)
        {
            RankOneTensor t0 = tensors.get(i);

            RankOneTensor t1 = t0.performExchange();

            if (t0.isEqual(t1))
            { //If t0 is the same as t1 it implies t0.a = t1.a = t0.b  AND  t0.b = t1.b = t0.c SO t0.a = t0.b = t0.c
            //So no point continuing
                continue; //Skip iteration
            }

            RankOneTensor t2 = t1.performExchange();

            int t1Location = -1;
            int t2Location = -1;

            for (int j = i+1; j < tensors.size(); j++)
            {
                RankOneTensor test = tensors.get(j);

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
            for (RankOneTensor t : tensors)
            {
                t.hasSymmetry = true;
            }
        }
    }


    /**
     *
     * @return
     */
    public int getExpandedRank()
    {
        int rank = 0;
        for (RankOneTensor t : tensors)
        {
            if (t.hasSymmetry)
            {
                rank += 3;
            }
            else
            {
                rank += 1;
            }
        }
        return rank;
    }

    /**
     *
     * @return
     */
    public int getTensorRank()
    {
        return tensors.size();
    }


    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (RankOneTensor t : tensors)
        {
            sb.append(t);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     *
     */
    public void randomWalk(boolean enableTesting, AlgoData algoData) throws Exception
    {
        //Scanner sc = new Scanner(System.in);
        final int MAX_REDUCTIONS = -1;
        int reductions = 0;
        int flips = 0;

        int steps = 0;

        int singletonCollapses = 0;

        markTensorsAsChangeUnchanged(true);

        long startTime = System.currentTimeMillis();

        do
        {
            boolean hasReduced = randomStep();
            //System.out.println("STEP");


            steps++; //whether a reduction or a flip was made exactly one step was made
            flips++; //A flip was made
            if (hasReduced)
            {
                steps++; //whether a reduction or a flip was made exactly one step was made
                reductions++; //increase reductions

                int rank = getExpandedRank();
                int tensorRank = getTensorRank();

                long currentTime = System.currentTimeMillis();

                double duration = (currentTime - startTime) * 0.001d;


                if (algoData.pushReduction(rank, steps, duration))
                {
                    System.out.println("===== METHOD WITH RANK " + rank + " [ TENSOR RANK " + tensorRank +" ] (" + reductions + " REDUCTIONS, " + flips + " FLIPS, " + steps +" STEPS): =====");
                    System.out.println("Duration: " + duration);
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
            else
            {
                //flips++; //A flip was made
                if (reductions >= 100)
                {
                    if (lookForSingleton())
                    {
                        int rank = getExpandedRank();
                        int tensorRank = getTensorRank();

                        singletonCollapses++;

                        long currentTime = System.currentTimeMillis();

                        double duration = (currentTime - startTime) * 0.001d;

                        if (algoData.pushReduction(rank, steps, duration))
                        {

                            System.out.println("===== " + singletonCollapses + "SINGLETON METHOD WITH RANK " + getExpandedRank() + " [ TENSOR RANK " + tensorRank +" ] (" + reductions + " REDUCTIONS, " + flips + " FLIPS, " + steps +" STEPS): =====");
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
                }
            }
        }
        while (MAX_REDUCTIONS == -1 || reductions < MAX_REDUCTIONS); //MAX_REDUCTIONS == -1 Means don't stop

    }

    public boolean lookForSingleton()
    {
        for (RankOneTensor t : tensors)
        {
            if (t.isSymmetricSingleton())
            {
                t.hasSymmetry = false;
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public boolean randomStep()
    {
        //Always try to reduce first
        boolean result = lookForReductionInFlipNeighbours(); //Look for a reduction either at this step or at a neighbour

        if (result == false)
        { //If I can't reduce the set, perform one flip
            //Failed to find a flip so don't both searching these tensors anymore
            markTensorsAsChangeUnchanged(false);
            //Make a flip
            makeFlip();
        }
        return result;

    }

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
        /*
        System.out.println("====== REDUCEABLE IN SPIN LAND =======");
        System.out.println(this);
        System.out.println("=============LISTS OF SPIN ============");
        for (int i = 0; i < indexOfCommon.size(); i++)
        {
            System.out.println("List:");
            for (int j = 0; j < indexOfCommon.get(i).size(); j++)
            {
                System.out.println(indexOfCommon.get(i).get(j)[0] + " spin: " + indexOfCommon.get(i).get(j)[1] + ":" + indexOfCommon.get(i).get(j)[2] + ":" + indexOfCommon.get(i).get(j)[3]);
            }
        }
        */

        for (int i = 0; i < indexOfCommon.size(); i++)
        {
            ArrayList<int[]> listOfSameCommon = indexOfCommon.get(i);

            //See if the current set selection contains tensors that have just been flipped (justFlipped = true)
            //If not that implies we checked this exact set last time
            boolean uncheckedSet = false; //Stores if we have already checked this set before
            for (int index = 0; index < listOfSameCommon.size(); index++)
            {
                if (tensors.get(listOfSameCommon.get(index)[0]).justFlipped)
                {
                    uncheckedSet = true;
                    break; //Found something unchecked so we can stop
                }
            }
            if (uncheckedSet == false)
            {
                continue;
            }

            //See if we have accidently included a set containing both symmetric and non symmetric
            //boolean isUsingSymmetry = false; //Stores if symmetry is being used
            boolean isUsingSymmetry = tensors.get(listOfSameCommon.get(0)[0]).hasSymmetry; //Stores if symmetry is being used
            /*
            if (listOfSameCommon.isEmpty() == false)
            {
                isUsingSymmetry = tensors.get(listOfSameCommon.get(0)[0]).hasSymmetry;

                for (int index = 1; index < nextToTest.size(); index++)
                {
                    if (tensors.get(nextToTest.get(index)[0]).hasSymmetry != isUsingSymmetry)
                    { //If different systems don't use
                        continue TestTensors;
                    }
                }

            }
            */


            //need to write code to perform SYMMETRY on TENSORS

            int[] spin = new int[listOfSameCommon.size()];

            for (int digit = 0; digit < spin.length; digit++)
            {
                spin[digit] = 0;
            }

            //System.out.println("==================================================> IM STUPID");

            if (isUsingSymmetry)
            {
                int MAXCHANGEOFREPRESENTATIVES = (int)Math.pow(3, listOfSameCommon.size());
                //flip SYMMETRY on tensors


                //System.out.println("==================================================> Bob: " + bob);

                int spinCount = 0;
                TestSpin:
                while (spinCount < MAXCHANGEOFREPRESENTATIVES)
                {

                    //Rotate tensors in order (like counting in base 3, the discard the unused ones)
                    /*
                    int digit = 0;
                    if (spin.length > 0)
                    {
                        boolean carryNextDigit;

                        spinCount++;
                        int digitSize = 1;
                        do
                        {
                            carryNextDigit = false;
                            boolean invalidDigit;
                            do
                            {
                                spin[digit]++;
                                tensors.get(listOfSameCommon.get(digit)[0]).performExchangeInPlace();

                                if (spin[digit] >= 3)
                                {
                                    spin[digit] = 0;
                                    carryNextDigit = true;
                                }
                                invalidDigit = listOfSameCommon.get(digit)[spin[digit]] != 1;
                                if (invalidDigit)
                                {
                                    spinCount += digitSize;
                                }
                            }
                            while (invalidDigit);

                            digit++;
                            digitSize *= 3;
                        }
                        while(digit < spin.length && carryNextDigit);
                    }
                    else
                    {
                        spinCount++;
                    }
                    */
                    int digit = 0;
                    if (spin.length > 0)
                    {
                        boolean carryNextDigit;

                        spinCount++;
                        do
                        {
                            carryNextDigit = false;

                            spin[digit]++;
                            tensors.get(listOfSameCommon.get(digit)[0]).performExchangeInPlace();
                            //System.out.println("Spin");

                            if (spin[digit] >= 3)
                            {
                                spin[digit] = 0;
                                carryNextDigit = true;
                            }

                            digit++;
                        }
                        while(digit < spin.length && carryNextDigit);
                    }
                    else
                    {
                        spinCount++;
                    }

                    for (digit = 0; digit < spin.length; digit++)
                    {
                        if (listOfSameCommon.get(digit)[spin[digit]+1] != 1)
                        {
                            continue TestSpin;
                        }
                    }
                    /*
                    System.out.println("======= SPINS ========");
                    for ( digit = 0; digit < spin.length; digit++)
                    {
                        System.out.print(spin[digit] + ", ");
                        //spin[digit] = 0;
                    }
                    System.out.println("");
                    */
                    //see if valid combination

                    if (checkCaseForLinearDependance(ignoreMatrix, listOfSameCommon)) {
                        return true;
                    }
                    //System.out.println("==================================================> Bob: " + bob);
                }
                /*
                for (int digit = 0; digit < spin.length; digit++)
                {
                    System.out.print(spin[digit] + ", ");
                    //spin[digit] = 0;
                }
                System.out.println("YAYAYAYAYAYAYA");
                */

                //System.out.println("==================================================> Bob: " + bob);
            }
            else
            {
                if (checkCaseForLinearDependance(ignoreMatrix, listOfSameCommon)) return true;
            }
        }

        return false;
    }

    public boolean checkCaseForLinearDependance(Selection ignoreMatrix, ArrayList<int[]> nextToTest)
    {
        /*
        Note:
        IgnoreMatrix is the matrix postion of DIMENSION 1 (all equal)

        */

        //Found list to test... now test it
        if (ignoreMatrix != Selection.A)
        {
            int[] combination = testLinearDependence(nextToTest, Selection.A);
            if (combination != null)
            {
                int tensorIndexToRemove = nextToTest.get(0)[0]; //I think because of how this is implemented I can pick any vector
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
                        b[k] = -1; //I think because of how this is implemented all vectors are used
                    }
                }

                reconstructMultiplicationScheme(ignoreMatrix, Selection.A, nextToTest, tensorIndexToRemove, a, b);

                return true;
            }
        }
        if (ignoreMatrix != Selection.B)
        {
            int[] combination = testLinearDependence(nextToTest, Selection.B);
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
            int[] combination = testLinearDependence(nextToTest, Selection.C);
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

        return false;
    }

    public void reconstructMultiplicationScheme(Selection dimensionOneOver, Selection linearlyDependantOver, ArrayList<int[]> listOfDependant, int tensorIndexToRemove, int[] a, int[] b)
    {
        //System.out.println("Size: " + listOfDependant.size());

        //Reconstruct multiplication scheme
        //Find Tensor index to remove

        Selection augmentOver = Selection.A;

        //Find missing component
        while (dimensionOneOver == augmentOver || linearlyDependantOver == augmentOver)
        {
            augmentOver = Selection.values()[augmentOver.ordinal()+1];
        }

        RankOneTensor t = tensors.get(tensorIndexToRemove);
        for (int index = 0; index < listOfDependant.size(); index++)
        {
            int i = listOfDependant.get(index)[0];
            if (i != tensorIndexToRemove)
            {
                //A * B * C + ab(C_t)

                int[] matrixToAugment = null;
                int[] tmatrixToAugment = null;
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
                int[] augmented = new int[matrixToAugment.length];

                for (int j = 0; j < augmented.length; j++)
                {
                    int A = a[i] == 0 ? 0 : Integer.MAX_VALUE;
                    int B = b[i] == 0 ? 0 : Integer.MAX_VALUE;

                    augmented[j] = matrixToAugment[j] ^ (A & B & tmatrixToAugment[j]); //augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
                }

                switch (augmentOver)
                {
                    case A: //Update A
                        tensors.get(i).a = augmented;
                        break;
                    case B: //Update B
                        tensors.get(i).b = augmented;
                        break;
                    case C: //Update C
                        tensors.get(i).c = augmented;
                        break;
                    default:
                        break;
                }
            }
        }
        tensors.remove(tensorIndexToRemove);
    }
    /**
     *
     * @param indexes
     * @param matrixToTest
     * @return
     */
    public int[] testLinearDependence(ArrayList<int[]> indexes, Selection matrixToTest)
    {
        int[] matToReduce;
        int[] matReference = null;

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

        matToReduce = new int[matReference.length*matReference.length];
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


            int count = 0;
            for (int x = 0; x < matReference.length; x++)
            {
                for (int y = 0; y < matReference.length; y++)
                {
                    RankOneTensor.setEntry(matToReduce, count, i, RankOneTensor.getEntry(matReference, x, y));
                    count++;
                }
            }
        }

        //Find linear dependencies by using Gaussian Elimination


        //int[][] matPreReduction = cloneMat(matToReduce);

        int[] matPreReduction = new int[matToReduce.length];
        System.arraycopy(matToReduce, 0, matPreReduction, 0, matPreReduction.length);


        //Perform gaussian elimination
        gaussian(matToReduce, indexes.size());

        //Test for linear dependency
        if (testDependancy(matToReduce, indexes.size()))
        {
            /*
            System.out.println("====== MAT TO TEST: " + matrixToTest + " =======");
            System.out.println("====== REDUCEABLE =======");
            System.out.println(this);
            System.out.println("======= REDUCING ========");

            for (int i = 0; i < indexes.size(); i++)
            {
                System.out.println(tensors.get(indexes.get(i)[0]));
            }
            */


            //Need to find relevant t vector and way of making t vector

            //int tIndex = 0;

            //int[] solution = fullgaussian(removeColumn(matPreReduction, tIndex), getCol(matPreReduction, tIndex), indexes.size());
            int[] solution = fullgaussian(matPreReduction, indexes.size()-1); //tIndex = 0


            return solution;
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
    public boolean testDependancy(int[] mat, int width)
    {
        //If the rank is smaller than the number of vectors we know there must be a linear dependancy
        return rank(mat) < width;
    }
    /**
     *
     * @param mat
     * @return
     */
    public int rank(int[] mat)
    {
        int count = 0;
        for (int row = 0; row < mat.length; row++)
        {
            if (mat[row] != 0) count++;
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
    * @return
    */
    public static int[] fullgaussian(int[] mat, int width)
    {
        //RHS is actually the 0th bit
        int N = min(mat.length, width);
        for (int k = 0; k < N; k++)
        {
            // find pivot row
            int max = k;
            if (RankOneTensor.getEntry(mat, max, k+1) == 0)
            {
                for (int i = k + 1; i < mat.length; i++)
                {
                    if (RankOneTensor.getEntry(mat, i, k+1) == 1)
                    {
                        max = i;
                        break;
                    }
                }
            }

            // swap row in A matrix
            int temp = mat[k];
            mat[k] = mat[max];
            mat[max] = temp;


            //row k swapped with row max

            // pivot within A
            for (int i = k + 1; i < mat.length; i++)
            {
                if (RankOneTensor.getEntry(mat, i, k+1) == 1)
                {
                    mat[i] = mat[i] ^ mat[k];
                }
            }
        }
        int[] solution = new int[width]; //Technically this is less rows than columns but it works
        for (int i = N - 1; i >= 0; i--)
        {
            int sum = 0;
            for (int j = i + 1; j < N; j++)
            {
                sum += RankOneTensor.getEntry(mat, i, j+1) * solution[j];
            }
            if (RankOneTensor.getEntry(mat, i, i+1) == 0)
            {
                System.out.println("GAUSSIAN FAILED (DIVIDE BY ZERO bean)");
                return null;
            }
            solution[i] = ((mat[i]&1) + sum) % 2; //Don't need division on F2, Addition is the same as subtraction
        }

        //Need to check all lines below still work
        for (int i = N; i < mat.length; i++)
        {
            if ((mat[i]&1) != 0)
            {
                System.out.println("GAUSSIAN FAILED (TOO MANY TERMS bean)");
                return null;
            }
        }

        //System.out.println("GAUSSIAN SUCCESS!");

        return solution;
    }

    /**
    * Guassian elimination on a feild of 2 elements
     * @param mat
    */
    public static void gaussian(int[] mat, int width)
    {
        int N = min(mat.length, width);
        for (int k = 0; k < N; k++)
        {
            // find pivot row
            int max = k;
            if (RankOneTensor.getEntry(mat, max, k) == 0)
            {
                for (int i = k + 1; i < mat.length; i++)
                {
                    if (RankOneTensor.getEntry(mat, i, k) == 1)
                    {
                        max = i;
                        break;
                    }
                }
            }

            // swap row in A matrix
            int temp = mat[k];
            mat[k] = mat[max];
            mat[max] = temp;
            //row k swapped with row max


            // pivot within A
            for (int i = k + 1; i < mat.length; i++)
            {
                //Factor not needed for this size
                //double factor = mat[i][k] / mat[k][k]; //mat[k][k] should be 1

                if (RankOneTensor.getEntry(mat, i, k) == 1)
                {
                    mat[i] = mat[i] ^ mat[k];
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
        ArrayList<int[]> commonMatrix = new ArrayList<>();

        for (int i = 0; i < tensors.size(); i++)
        {
            RankOneTensor t = tensors.get(i);
            int[] selection = null;
            int[] selectionSpin1 = null;
            int[] selectionSpin2 = null;

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


            for (int j = 0; j < commonMatrix.size(); j++)
            {
                boolean foundMatchHere = false;
                int[] entry = new int[4];
                entry[0] = i;

                int[] m = commonMatrix.get(j);
                if (RankOneTensor.areMatrixEqual(m, selection))
                {
                    foundMatch = true;
                    foundMatchHere = true;

                    entry[1] = 1;
                }

                if (t.hasSymmetry)
                {

                    if (RankOneTensor.areMatrixEqual(m, selectionSpin1))
                    {
                        foundMatch = true;
                        foundMatchHere = true;

                        entry[2] = 1;
                    }
                    if (RankOneTensor.areMatrixEqual(m, selectionSpin2))
                    {
                        foundMatch = true;
                        foundMatchHere = true;

                        entry[3] = 1;
                    }
                }
                if (foundMatchHere)
                {
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
            /*
            else
            { //If it's already contained append this index to the list
                result.get(containsMatrixAtIndex).add(i);
            }
            */

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

    public boolean lookForReductionInFlipNeighbours()
    {
        //Continue if no reduction found
        //markTensorsAsChangeUnchanged(false); //No reduction found on any rank 1 tensor so dont bother looking again until flipped

        RankOneTensor xTensor = null;
        RankOneTensor yTensor = null;

        int SIZE = tensors.get(0).a.length;

        RankOneTensor xOriginal = new RankOneTensor(new int[SIZE], new int[SIZE], new int[SIZE]);
        RankOneTensor yOriginal = new RankOneTensor(new int[SIZE], new int[SIZE], new int[SIZE]);

        int xIndex;
        int yIndex;

        for (xIndex = 0; xIndex < tensors.size()-1; xIndex++) {
        for (yIndex = xIndex+1; yIndex < tensors.size(); yIndex++) {
            /*
            if (yIndex == xIndex)
            {
                yIndex++;
            }
            */
            //Get pair of tensors
            xTensor = tensors.get(xIndex);
            yTensor = tensors.get(yIndex);
            //Check they are a valid pair (in terms of symmetry)
            if (xTensor.hasSymmetry != yTensor.hasSymmetry)
            {
                continue;
            }

            //Create copies of them to revert to later
            xOriginal.copyFrom(xTensor);
            yOriginal.copyFrom(yTensor);


            if (yTensor.hasSymmetry)
            { //If the rank 1 tensor decomposition does not have any symmetry then no chnage of representative allowed
                for (int i = 0; i <= 2; i++)
                {
                    //Verify flip can take place
                    if (RankOneTensor.areMatrixEqual(xTensor.a, yTensor.a))
                    {
                        flipTensors(xTensor, yTensor, 1);

                        //Mark two fliped tensors to notify reduction code to check
                        xTensor.justFlipped = true;
                        yTensor.justFlipped = true;

                        //Look for a reduction
                        if (tryReduce())
                        {
                            return true; //Finish function
                        }

                    }
                    if (RankOneTensor.areMatrixEqual(xTensor.b, yTensor.b))
                    {
                        flipTensors(xTensor, yTensor, 2);

                        //Mark two fliped tensors to notify reduction code to check
                        xTensor.justFlipped = true;
                        yTensor.justFlipped = true;

                        //Look for a reduction
                        if (tryReduce())
                        {
                            return true; //Finish function
                        }

                    }
                    if (RankOneTensor.areMatrixEqual(xTensor.c, yTensor.c))
                    {
                        flipTensors(xTensor, yTensor, 3);

                        //Mark two fliped tensors to notify reduction code to check
                        xTensor.justFlipped = true;
                        yTensor.justFlipped = true;

                        //Look for a reduction
                        if (tryReduce())
                        {
                            return true; //Finish function
                        }


                    }
                    yOriginal.performExchangeInPlace();
                    //If no reduction found, revert rank 1 tensors
                    xTensor.copyFrom(xOriginal);
                    yTensor.copyFrom(yOriginal);

                    //yTensor.performExchangeInPlace();
                }
                //yTensor will have flipped back at the end of this loop
            }
            else
            {
                //Verify flip can take place
                if (RankOneTensor.areMatrixEqual(xTensor.a, yTensor.a))
                {
                    flipTensors(xTensor, yTensor, 1);

                    //Mark two fliped tensors to notify reduction code to check
                    xTensor.justFlipped = true;
                    yTensor.justFlipped = true;

                    //Look for a reduction
                    if (tryReduce())
                    {
                        return true; //Finish function
                    }
                }
                if (RankOneTensor.areMatrixEqual(xTensor.b, yTensor.b))
                {
                    flipTensors(xTensor, yTensor, 2);

                    //Mark two fliped tensors to notify reduction code to check
                    xTensor.justFlipped = true;
                    yTensor.justFlipped = true;

                    //Look for a reduction
                    if (tryReduce())
                    {
                        return true; //Finish function
                    }
                }
                if (RankOneTensor.areMatrixEqual(xTensor.c, yTensor.c))
                {
                    flipTensors(xTensor, yTensor, 3);

                    //Mark two fliped tensors to notify reduction code to check
                    xTensor.justFlipped = true;
                    yTensor.justFlipped = true;

                    //Look for a reduction
                    if (tryReduce())
                    {
                        return true; //Finish function
                    }
                }

                //If no reduction found, revert rank 1 tensors
                xTensor.copyFrom(xOriginal);
                yTensor.copyFrom(yOriginal);
            }
        }
        }
        //No reduction found
        //markTensorsAsChangeUnchanged(false); //No reduction found on any rank 1 tensor so dont bother looking again until flipped
        return false;

    }

    public void flipTensors(RankOneTensor xTensor, RankOneTensor yTensor, int selectedflip)
    {
        //Flip tensors

        switch (selectedflip)
        {
            case 1: //A is the same
                for (int i = 0; i < xTensor.b.length; i++)
                {
                    xTensor.b[i] = xTensor.b[i] ^ yTensor.b[i];
                    yTensor.c[i] = yTensor.c[i] ^ xTensor.c[i];
                }
                break;
            case 2: //B is the same
                for (int i = 0; i < xTensor.a.length; i++)
                {
                    xTensor.a[i] = xTensor.a[i] ^ yTensor.a[i];
                    yTensor.c[i] = yTensor.c[i] ^ xTensor.c[i];
                }
                break;
            case 3: //C is the same
                for (int i = 0; i < xTensor.a.length; i++)
                {
                    xTensor.a[i] = xTensor.a[i] ^ yTensor.a[i];
                    yTensor.b[i] = yTensor.b[i] ^ xTensor.b[i];
                }
                break;
            default:
                break;
        }
    }

    /**
     * Performs a random flip on the current multiplication method
     * Flips are of the form
     * a * b * c  + a * B' * C' => a * (b + B') * c + a * B' * (c - C')
     * Up to re-ordering of A, B and C (and B', C' and A' respectively)
     */
    public void makeFlip()
    {

        //ArrayList<Integer> potentialFlip = new ArrayList<>();
        //ArrayList<Integer> changeRepresentativeBy = new ArrayList<>();

        int MAX_SIZE = 100;
        int[] potentialFlip = new int [MAX_SIZE];
        int[] changeRepresentativeBy = new int [MAX_SIZE];
        int pos = 0;

        RankOneTensor xTensor = null;
        RankOneTensor yTensor = null;

        int xIndex = -1;
        int yIndex = -1;

        while (pos == 0)
        {

            //Generate 2 unique and nearly evenly distributed numbers
            xIndex = randomInt(tensors.size());
            yIndex = randomInt(tensors.size()-1);
            if (yIndex >= xIndex)
            {
                yIndex++;
            }

            xTensor = tensors.get(xIndex);
            yTensor = tensors.get(yIndex);

            if (xTensor.hasSymmetry != yTensor.hasSymmetry)
            {
                continue;
            }

            pos = 0;
            //potentialFlip.clear();
            //changeRepresentativeBy.clear();


            if (yTensor.hasSymmetry)
            { //If the rank 1 tensor decomposition does not have any symmetry then no chnage of representative allowed
                for (int i = 0; i <= 2; i++)
                {
                    //Verify flip can take place
                    if (RankOneTensor.areMatrixEqual(xTensor.a, yTensor.a))
                    {
                        //potentialFlip.add(1);
                        //changeRepresentativeBy.add(i);

                        potentialFlip[pos] = 1;
                        changeRepresentativeBy[pos] = i;
                        pos++;
                    }
                    if (RankOneTensor.areMatrixEqual(xTensor.b, yTensor.b))
                    {
                        //potentialFlip.add(2);
                        //changeRepresentativeBy.add(i);

                        potentialFlip[pos] = 2;
                        changeRepresentativeBy[pos] = i;
                        pos++;
                    }
                    if (RankOneTensor.areMatrixEqual(xTensor.c, yTensor.c))
                    {
                        //potentialFlip.add(3);
                        //changeRepresentativeBy.add(i);

                        potentialFlip[pos] = 3;
                        changeRepresentativeBy[pos] = i;
                        pos++;
                    }

                    yTensor.performExchangeInPlace();
                }
                //yTensor will have flipped back at the end of this loop
            }
            else
            {
                //Verify flip can take place
                if (RankOneTensor.areMatrixEqual(xTensor.a, yTensor.a))
                {
                    //potentialFlip.add(1);
                    //changeRepresentativeBy.add(0);

                    potentialFlip[pos] = 1;
                    changeRepresentativeBy[pos] = 0;
                    pos++;
                }
                if (RankOneTensor.areMatrixEqual(xTensor.b, yTensor.b))
                {
                    //potentialFlip.add(2);
                    //changeRepresentativeBy.add(0);

                    potentialFlip[pos] = 2;
                    changeRepresentativeBy[pos] = 0;
                    pos++;
                }
                if (RankOneTensor.areMatrixEqual(xTensor.c, yTensor.c))
                {
                    //potentialFlip.add(3);
                    //changeRepresentativeBy.add(0);

                    potentialFlip[pos] = 3;
                    changeRepresentativeBy[pos] = 0;
                    pos++;
                }
            }
        }

        int index = randomInt(pos);

        //Pick a random flip
        int selectedflip = potentialFlip[index];
        int representativeSpinBy = changeRepresentativeBy[index];

        for (int i = 0; i < representativeSpinBy; i++)
        {
            //yTensor = yTensor.performExchange();
            yTensor.performExchangeInPlace();
        }

        //Flip tensors

        flipTensors(xTensor, yTensor, selectedflip);
        //xTensor.justFlipped = true;
        //yTensor.justFlipped = true;
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

    private void sumTensor(RankOneTensor t, int[][][][][][] result)
    {
        int a[] = t.a;
        for (int ai = 0; ai < a.length; ai++)
        {
            for (int aj = 0; aj < a.length; aj++)
            {
                if (RankOneTensor.getEntry(a, ai, aj) == 1)
                {
                    int b[] = t.b;
                    for (int bi = 0; bi < b.length; bi++)
                    {
                        for (int bj = 0; bj < b.length; bj++)
                        {
                            if (RankOneTensor.getEntry(b, bi, bj) == 1)
                            {
                                int c[] = t.c;
                                for (int ci = 0; ci < c.length; ci++)
                                {
                                    for (int cj = 0; cj < c.length; cj++)
                                    {
                                        if (RankOneTensor.getEntry(c, ci, cj) == 1)
                                        {
                                            result[ai][aj][bi][bj][ci][cj] = (result[ai][aj][bi][bj][ci][cj] + 1) % 2;
                                        }
                                        else if (RankOneTensor.getEntry(c, ci, cj) != 0)
                                        {
                                            System.out.println("BIG ERROR WOW YOU SCREWED UP REALLY BADLY HERE");
                                        }
                                    }
                                }
                            }
                            else if (RankOneTensor.getEntry(b, bi, bj) != 0)
                            {
                                System.out.println("BIG ERROR WOW YOU SCREWED UP REALLY BADLY HERE");
                            }
                        }
                    }
                }
                else if (RankOneTensor.getEntry(a, ai, aj) != 0)
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
                        [m.tensors.get(0).a.length]
                        [m.tensors.get(0).b.length]
                        [m.tensors.get(0).b.length]
                        [m.tensors.get(0).c.length]
                        [m.tensors.get(0).c.length];

        for (RankOneTensor t : m.tensors)
        {
            sumTensor(t, result);

            if (t.hasSymmetry)
            {
                RankOneTensor t1 = t.performExchange();
                RankOneTensor t2 = t1.performExchange();
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
        int[][][][][][] testAgainst = constructResult(getBasicMethod(tensors.get(0).a.length, tensors.get(0).a.length, tensors.get(0).b.length));
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
