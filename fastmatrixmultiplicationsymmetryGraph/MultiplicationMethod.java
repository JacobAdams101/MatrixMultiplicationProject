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
    public ArrayList<RankOneTensor> getTensors() {
        return tensors;
    }


    @Override
    public boolean equals(Object e) {
        if (e instanceof MultiplicationMethod) {
            MultiplicationMethod mm = (MultiplicationMethod)e;

            ArrayList<RankOneTensor> mmTensors = new ArrayList<>();

            mmTensors.addAll(mm.tensors);

            for (RankOneTensor t : tensors)
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
    public void randomWalk(boolean enableTesting, int[] minStepsFoundForReduction) throws Exception
    {
        //Scanner sc = new Scanner(System.in);
        final int MAX_REDUCTIONS = -1;
        int reductions = 0;
        int flips = 0;

        int steps = 0;

        int singletonCollapses = 0;


        do
        {
            boolean hasReduced = randomStep();
            //System.out.println("STEP");


            steps++; //whether a reduction or a flip was made exactly one step was made
            if (hasReduced)
            {
                int rank = getExpandedRank();
                int tensorRank = getTensorRank();

                reductions++; //increase reductions

                if (minStepsFoundForReduction[rank] == -1 || minStepsFoundForReduction[rank] > steps)
                {
                    minStepsFoundForReduction[rank] = steps;
                    System.out.println("===== METHOD WITH RANK " + rank + " [ TENSOR RANK " + tensorRank +" ] (" + reductions + " REDUCTIONS, " + flips + " FLIPS, " + steps +" STEPS): =====");
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
                flips++; //A flip was made
                if (reductions >= 100)
                {
                    if (lookForSingleton())
                    {
                        int rank = getExpandedRank();
                        int tensorRank = getTensorRank();

                        singletonCollapses++;

                        if (minStepsFoundForReduction[rank] == -1 || minStepsFoundForReduction[rank] > steps)
                        {
                            minStepsFoundForReduction[rank] = steps;

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
        boolean result = tryReduce(); //Look for a reduction

        if (result == false)
        { //If I can't reduce the set, perform one flip
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
                    temp /= 2;

                }


                boolean isUsingSymmetry = false; //Stores if symmetry is being used

                if (nextToTest.isEmpty() == false)
                {
                    isUsingSymmetry = tensors.get(nextToTest.get(0)[0]).hasSymmetry;
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


                if (isUsingSymmetry)
                {
                    int MAXCHANGEOFREPRESENTATIVES = (int)Math.pow(3, nextToTest.size());
                    //flip SYMMETRY on tensors
                    int spinCount = 0;
                    while (spinCount < MAXCHANGEOFREPRESENTATIVES)
                    {

                        //Rotate tensors in order (like counting in base 3, the discard the unused ones)

                        int digit = 0;
                        if (spin.length > 0)
                        {
                            boolean nextDigit;
                            do
                            {
                                nextDigit = false;
                                do
                                {
                                    spin[digit]++;
                                    spinCount++;

                                    tensors.get(nextToTest.get(digit)[0]).performExchangeInPlace();

                                    if (spin[digit] >= 3)
                                    {
                                        spin[digit] = 0;
                                        nextDigit = true;
                                    }
                                }
                                while (nextToTest.get(digit)[spin[digit]] != 1);
                                digit++;
                            }
                            while(digit < spin.length && nextDigit);
                        }
                        else
                        {
                            spinCount++;
                        }

                        //see if valid combination



                        if (checkCaseForLinearDependance(ignoreMatrix, nextToTest)) return true;
                    }
                }
                else
                {
                    checkCaseForLinearDependance(ignoreMatrix, nextToTest);
                }
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
        //Reconstruct multiplication scheme
        //Find Tensor index to remove

        Selection augmentOver = Selection.A;

        //Find missing component
        while (dimensionOneOver == augmentOver || linearlyDependantOver == augmentOver)
        {
            augmentOver = Selection.values()[augmentOver.ordinal()+1];
        }

        RankOneTensor t = tensors.get(tensorIndexToRemove);
        for (int i = 0; i < tensors.size(); i++)
        {
            if (i != tensorIndexToRemove)
            {
                if (listOfDependant.contains(i))
                { //If in set that had a linear dependency

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

                    //Update A
                    if (augmentOver == Selection.A)
                    {
                        tensors.get(i).a = augmented;
                    }

                    //Update B
                    if (augmentOver == Selection.B)
                    {
                        tensors.get(i).b = augmented;
                    }

                    //Update C
                    if (augmentOver == Selection.C)
                    {
                        tensors.get(i).c = augmented;
                    }
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
            //Need to find relevant t vector and way of making t vector

            int tIndex = 0;

            //int[] solution = fullgaussian(removeColumn(matPreReduction, tIndex), getCol(matPreReduction, tIndex), indexes.size());
            int[] solution = fullgaussian(matPreReduction, indexes.size()-1);


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
            for (int i = k + 1; i < mat.length; i++)
            {
                if (RankOneTensor.getEntry(mat, i, k+1) > RankOneTensor.getEntry(mat, max, k+1))
                {
                    max = i;
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

        return solution;
    }

    /**
    * Guassian elimination on a feild of 2 elements
     * @param mat
    */
    public static void oldgaussian(int[][] mat)
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

    public static void gaussian(int[] mat, int width)
    {
        int N = min(mat.length, width);
        for (int k = 0; k < N; k++)
        {
            // find pivot row
            int max = k;
            for (int i = k + 1; i < mat.length; i++)
            {
                if (RankOneTensor.getEntry(mat, i, k) > RankOneTensor.getEntry(mat, max, k))
                {
                    max = i;
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

                int[] entry = new int[4];
                entry[0] = i;

                int[] m = commonMatrix.get(j);
                if (RankOneTensor.areMatrixEqual(m, selection))
                {
                    foundMatch = true;

                    entry[1] = 1;

                    break;
                }

                if (RankOneTensor.areMatrixEqual(m, selectionSpin1))
                {
                    foundMatch = true;

                    entry[2] = 1;

                    break;
                }
                if (RankOneTensor.areMatrixEqual(m, selectionSpin2))
                {
                    foundMatch = true;

                    entry[3] = 1;

                    break;
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

        RankOneTensor xTensor = null;
        RankOneTensor yTensor = null;

        int xIndex = -1;
        int yIndex = -1;

        while (potentialFlip.isEmpty())
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

            RankOneTensor temp = yTensor;

            int maxChangeRepCount = 0; //If the rank 1 tensor decomposition does not have any symmetry then no chnage of representative allowed



            if (temp.hasSymmetry)
            {
                maxChangeRepCount = 2;
            }

            for (int i = 0; i <= maxChangeRepCount; i++)
            {

                //Verify flip can take place
                if (RankOneTensor.areMatrixEqual(xTensor.a, temp.a))
                {
                    potentialFlip.add(1);
                    changeRepresentativeBy.add(i);
                }
                if (RankOneTensor.areMatrixEqual(xTensor.b, temp.b))
                {
                    potentialFlip.add(2);
                    changeRepresentativeBy.add(i);
                }
                if (RankOneTensor.areMatrixEqual(xTensor.c, temp.c))
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

        for (int i = 0; i < representativeSpinBy; i++)
        {
            yTensor = yTensor.performExchange();
        }



        //Flip tensors

        switch (selectedflip)
        {
            case 1: //A is the same

                //A component
                //No xTensor change
                //No yTensor change

                //B component
                for (int i = 0; i < xTensor.b.length; i++)
                {
                    xTensor.b[i] = xTensor.b[i] ^ yTensor.b[i];
                }
                //No yTensor change

                //C component
                //No xTensor change
                for (int i = 0; i < yTensor.c.length; i++)
                {
                    yTensor.c[i] = yTensor.c[i] ^ xTensor.c[i];
                }

                break;
            case 2: //B is the same

                //A component
                for (int i = 0; i < xTensor.a.length; i++)
                {
                    xTensor.a[i] = xTensor.a[i] ^ yTensor.a[i];
                }
                //No yTensor change

                //B component
                //No xTensor change
                //No yTensor change

                //C component
                //No xTensor change
                for (int i = 0; i < yTensor.c.length; i++)
                {
                    yTensor.c[i] = yTensor.c[i] ^ xTensor.c[i];
                }

                break;
            case 3: //C is the same

                //A component
                for (int i = 0; i < xTensor.a.length; i++)
                {
                    xTensor.a[i] = xTensor.a[i] ^ yTensor.a[i];
                }
                //no yTensorChange

                //B component
                //No xTensor change

                for (int i = 0; i < yTensor.b.length; i++)
                {
                    yTensor.b[i] = yTensor.b[i] ^ xTensor.b[i];
                }

                //C component
                //No change

                break;
            default:
                break;
        }
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
