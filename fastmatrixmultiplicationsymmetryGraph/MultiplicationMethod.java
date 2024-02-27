//package fastmatrixmultiplication;


import java.sql.SQLOutput;
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
     * @return
     */
    public static MultiplicationMethod getBasicMethod(int size)
    {
        MultiplicationMethod ret = new MultiplicationMethod();


        int i;
        int j;
        int k;

        for (i = 0; i < size; i++)
        {
            for (j = 0; j < size; j++)
            {
                for (k = 0; k < size; k++)
                {
                    long a = 0, b = 0, c = 0;

                    a = RankOneTensor.setEntry(a, i, j, 1);
                    b = RankOneTensor.setEntry(b, j, k, 1);
                    c = RankOneTensor.setEntry(c, k, i, 1);

                    ret.tensors.add(new RankOneTensor(a,b,c, size));
                }
            }
        }

        boolean addT_n = false;

        if (addT_n)
        {
            for (i = 0; i < size; i++)
            {
                for (j = 0; j < size; j++)
                {
                    for (k = 0; k < size; k++)
                    {
                        long a = 0, b = 0, c = 0;

                        a = RankOneTensor.setEntry(a, i, i, 1);
                        b = RankOneTensor.setEntry(b, j, j, 1);
                        c = RankOneTensor.setEntry(c, k, k, 1);

                        ret.tensors.add(new RankOneTensor(a,b,c, size));
                    }
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
    public void randomWalk(boolean enableTesting, AlgoData algoData, int rankToStopAt, int rankToLookForSingleton, int numStepsToStopAt, int plusTransformAfterNumSteps) throws Exception
    {

        //System.out.println("Plus Transition after: " + plusTransformAfterNumSteps);
        int reductions = 0;
        int flips = 0;

        int steps = 0;
        int stepsSinceLastReductionOrTransition = 0;

        int singletonCollapses = 0;

        markTensorsAsChangeUnchanged(true);

        long startTime = System.currentTimeMillis();

        int rank;
        int tensorRank;

        int minRank = getExpandedRank();

        do
        {
            boolean hasReduced = randomStep();


            //System.out.println("STEP");

            rank = getExpandedRank();
            tensorRank = getTensorRank();


            steps++; //whether a reduction or a flip was made exactly one step was made
            flips++; //A flip was made
            stepsSinceLastReductionOrTransition++;
            if (hasReduced)
            {
                steps++; //whether a reduction or a flip was made exactly one step was made
                reductions++; //increase reductions
                stepsSinceLastReductionOrTransition = 0;



                long currentTime = System.currentTimeMillis();

                double duration = (currentTime - startTime) * 0.001d;

                if (rank < minRank)
                {
                    int previousBestRank = minRank;
                    minRank = rank;
                    if (algoData.pushReduction(rank, previousBestRank, steps, duration))
                    {
                        minRank = rank;
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

            }

            if (stepsSinceLastReductionOrTransition > plusTransformAfterNumSteps)
            {
                stepsSinceLastReductionOrTransition = 0;
                plusTransition();
                steps++; //A plus transition has happened
                //System.out.println("===== PLUS TRANSITION =====");
                /*
                if (enableTesting)
                {
                    System.out.println("===== TEST CASE POST PLUS TRANSITION =====");
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
                */

            }


            if (rank <= rankToLookForSingleton)
            {
                if (lookForSingleton())
                {

                    rank = getExpandedRank();
                    tensorRank = getTensorRank();

                    singletonCollapses++;

                    long currentTime = System.currentTimeMillis();

                    double duration = (currentTime - startTime) * 0.001d;
                    if (rank < minRank)
                    {
                        int previousBestRank = minRank;
                        minRank = rank;
                        if (algoData.pushReduction(rank, previousBestRank, steps, duration))
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
        while (rank > rankToStopAt && steps < numStepsToStopAt);

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

    int MAX_SIZE = 100;
    int[][][] indexOfCommon = new int [MAX_SIZE][MAX_SIZE][4];
    int[] setSize = new int [MAX_SIZE];
    long[] commonMatrix = new long [MAX_SIZE];
    boolean[] hasSymmetry = new boolean [MAX_SIZE];

    /**
     * Exhaustive search for a reduction. Also applies that reduction
     * @return Returns true is a reduction is found
     */
    public boolean tryReduce()
    {


        int numSets;

        //See if those subsets are linearly dependant over on other matrix A B C (can't be the same as the first obvs)
        //If I can find one return true (indicating to random walk to not do a flip)
        numSets = getMatchingSelections(Selection.A, indexOfCommon, setSize, commonMatrix, hasSymmetry); //Find subsets of A that have dimension 1 (all look the same)
        if (searchForLinearDependance(indexOfCommon, setSize, numSets, Selection.A)) return true;

        numSets = getMatchingSelections(Selection.B, indexOfCommon, setSize, commonMatrix, hasSymmetry); //Find subsets of B that have dimension 1 (all look the same)
        if (searchForLinearDependance(indexOfCommon, setSize, numSets, Selection.B)) return true;

        numSets = getMatchingSelections(Selection.C, indexOfCommon, setSize, commonMatrix, hasSymmetry); //Find subsets of C that have dimension 1 (all look the same)
        if (searchForLinearDependance(indexOfCommon, setSize, numSets, Selection.C)) return true;

        //If can't find one return false (indicating to random walk to do a flip instead)
        return false;
    }
    /**
     *
     * @param indexOfCommon
     * @param ignoreMatrix
     * @return
     */
    public boolean searchForLinearDependance(int[][][] indexOfCommon, int[] setSize, int numSets, Selection ignoreMatrix)
    {
        for (int i = 0; i < numSets; i++)
        {
            int[][] listOfSameCommon = indexOfCommon[i];
            int listOfSameCommonSize = setSize[i];
            /*
            System.out.println("List: ===========");
            for (int pos = 0; pos < listOfSameCommonSize; pos++)
            {
                System.out.println("Index: " + listOfSameCommon[pos][0] + "Flip: " + listOfSameCommon[pos][1]+listOfSameCommon[pos][2]+listOfSameCommon[pos][3]);
            }
            */
            //See if the current set selection contains tensors that have just been flipped (justFlipped = true)
            //If not that implies we checked this exact set last time
            boolean uncheckedSet = false; //Stores if we have already checked this set before
            for (int index = 0; index < listOfSameCommonSize; index++)
            {
                if (tensors.get(listOfSameCommon[index][0]).justFlipped)
                {
                    uncheckedSet = true;
                    break; //Found something unchecked so we can stop
                }
            }
            if (uncheckedSet == false)
            {
                continue;
            }

            //assume we haven't included symmetric and non symetric tensors in the same set
            //boolean isUsingSymmetry = false; //Stores if symmetry is being used
            boolean isUsingSymmetry = tensors.get(listOfSameCommon[0][0]).hasSymmetry; //Stores if symmetry is being used

            //need to write code to perform SYMMETRY on TENSORS

            int[] spin = new int[listOfSameCommonSize];

            for (int digit = 0; digit < spin.length; digit++)
            {
                spin[digit] = 0;
            }

            if (isUsingSymmetry)
            {
                int MAXCHANGEOFREPRESENTATIVES = (int)Math.pow(3, listOfSameCommonSize);

                //flip SYMMETRY on tensors
                int spinCount = 0;
                TestSpin:
                while (spinCount < MAXCHANGEOFREPRESENTATIVES)
                {

                    //Rotate tensors in order (like counting in base 3, the discard the unused ones)
                    int digit = 0;
                    if (spin.length > 0)
                    {
                        boolean carryNextDigit;

                        spinCount++;
                        do
                        {
                            carryNextDigit = false;

                            spin[digit]++;
                            tensors.get(listOfSameCommon[digit][0]).performExchangeInPlace();
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
                        if (listOfSameCommon[digit][spin[digit]+1] != 1)
                        {
                            continue TestSpin;
                        }
                    }

                    //see if valid combination
                    if (checkCaseForLinearDependance(ignoreMatrix, listOfSameCommon, listOfSameCommonSize))
                    {
                        return true;
                    }
                }
            }
            else
            {
                if (checkCaseForLinearDependance(ignoreMatrix, listOfSameCommon, listOfSameCommonSize)) return true;
            }
        }

        return false;
    }

    public boolean checkCaseForLinearDependance(Selection ignoreMatrix, int[][] nextToTest, int size)
    {
        /*
        Note:
        IgnoreMatrix is the matrix postion of DIMENSION 1 (all equal)

        */

        //Found list to test... now test it
        if (ignoreMatrix != Selection.A)
        {
            int[] combination = testLinearDependence(nextToTest, size, Selection.A);
            if (combination != null)
            {

                int tIndex = combination[combination.length-1]; //Last item of array is unrelated data and instead stores tIndex

                int tensorIndexToRemove = nextToTest[tIndex][0]; //Pick tIndex of nextToTest to remove
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

                    if (nextToTestIndex < size && k == nextToTest[nextToTestIndex][0])
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

                reconstructMultiplicationScheme(ignoreMatrix, Selection.A, nextToTest, size, tensorIndexToRemove, a, b);

                return true;
            }
        }
        if (ignoreMatrix != Selection.B)
        {
            int[] combination = testLinearDependence(nextToTest, size, Selection.B);
            if (combination != null)
            {
                int tIndex = combination[combination.length-1]; //Last item of array is unrelated data and instead stores tIndex

                int tensorIndexToRemove = nextToTest[tIndex][0]; //Pick tIndex of nextToTest to remove
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

                    if (nextToTestIndex < size && k == nextToTest[nextToTestIndex][0])
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

                reconstructMultiplicationScheme(ignoreMatrix, Selection.B, nextToTest, size, tensorIndexToRemove, a, b);

                return true;
            }
        }
        if (ignoreMatrix != Selection.C)
        {
            int[] combination = testLinearDependence(nextToTest, size, Selection.C);
            if (combination != null)
            {
                int tIndex = combination[combination.length-1]; //Last item of array is unrelated data and instead stores tIndex

                int tensorIndexToRemove = nextToTest[tIndex][0]; //Pick tIndex of nextToTest to remove
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

                    if (nextToTestIndex < size && k == nextToTest[nextToTestIndex][0])
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

                reconstructMultiplicationScheme(ignoreMatrix, Selection.C, nextToTest, size, tensorIndexToRemove, a, b);

                return true;
            }
        }

        return false;
    }

    public void reconstructMultiplicationScheme(Selection dimensionOneOver, Selection linearlyDependantOver, int[][] listOfDependant, int dependantSize, int tensorIndexToRemove, int[] a, int[] b)
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

        int size = t.size;

        long ROWMASK = 0;

        for (int i = 0; i < size; i++)
        {
            ROWMASK = ROWMASK << 1;
            ROWMASK |= 1;
        }

        for (int index = 0; index < dependantSize; index++)
        {
            int i = listOfDependant[index][0];
            if (i != tensorIndexToRemove)
            {
                //A * B * C + ab(C_t)

                long matrixToAugment = 0;
                long tmatrixToAugment = 0;
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
                //int[] augmented = new int[tensors.get(i).size];
                long augmented = 0;

                for (int j = 0; j < size; j++)
                {
                    int A = a[i] == 0 ? 0 : Integer.MAX_VALUE;
                    int B = b[i] == 0 ? 0 : Integer.MAX_VALUE;

                    augmented |= (((matrixToAugment >> (j*8)) ^ (A & B & (tmatrixToAugment >> (j*8)))) & ROWMASK) << (j*8);

                    //augmented[j] = matrixToAugment[j] ^ (A & B & tmatrixToAugment[j]); //augmented[j][k] = (matrixToAugment[j][k] + (a[i]*b[i]*tmatrixToAugment[j][k])) % 2; //In the field F_2
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
    public int[] testLinearDependence(int[][] indexes, int indexSize, Selection matrixToTest)
    {
        int[] matToReduce;

        int size = tensors.get(0).size;


        matToReduce = new int[size*size];
        for (int i = 0; i < indexSize; i++)
        {
            writeTensorToMatrix(matToReduce, tensors.get(indexes[i][0]), matrixToTest, i);
        }

        //Find linear dependencies by using Gaussian Elimination

        gaussian(matToReduce, indexSize); //Perform gaussian elimination

        //Test for linear dependency
        if (testDependancy(matToReduce, indexSize))
        {

            int[] solution = null; //Stores solution

            //Need to find relevant t vector and way of making t vector
            int tIndex = 0;


            while (solution == null && tIndex < indexSize) //Loop until a valid index is found
            {
                //Write t to remove
                writeTensorToMatrix(matToReduce, tensors.get(indexes[tIndex][0]), matrixToTest, 0);

                int col = 1;
                for (int i = 0; i < indexSize; i++)
                {
                    if (i != tIndex)
                    {
                        writeTensorToMatrix(matToReduce, tensors.get(indexes[i][0]), matrixToTest, col);
                        col++;
                    }
                }

                //contruct required combination of tensors for linear dependancy
                solution = fullgaussian(matToReduce, indexSize-1); //tIndex = 0
                if (solution == null)
                { //If I couldn't form a linear dependancy without tensor at index.get(tIndex)
                    tIndex++; //Try next to index
                }
                else
                {
                    solution[solution.length-1] = tIndex; //write which tensor to remove
                }
            }

            return solution;
        }
        return null;
    }


    private static void writeTensorToMatrix(int[] mat, RankOneTensor tensor, Selection matrixToTest, int writeToCol)
    {
        long matReference;

        switch (matrixToTest)
        {
            case A:
                matReference = tensor.a;
                break;
            case B:
                matReference = tensor.b;
                break;
            case C:
                matReference = tensor.c;
                break;
            default:
                matReference = 0;
                break;
        }

        //long temp = matReference;


        int count = 0;
        int size = tensor.size;
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                RankOneTensor.setArrEntry(mat, count, writeToCol, matReference&1);

                matReference = matReference >> 1;
                count++;
            }
            matReference = matReference >> 8-tensor.size;
        }
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
            if (RankOneTensor.getArrEntry(mat, max, k+1) == 0)
            {
                for (int i = k + 1; i < mat.length; i++)
                {
                    if (RankOneTensor.getArrEntry(mat, i, k+1) == 1)
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
                if (RankOneTensor.getArrEntry(mat, i, k+1) == 1)
                {
                    mat[i] = mat[i] ^ mat[k];
                }
            }
        }
        int[] solution = new int[width+1]; //Technically this is less rows than columns but it works, plus 1 is for extra data later
        for (int i = N - 1; i >= 0; i--)
        {
            int sum = 0;
            for (int j = i + 1; j < N; j++)
            {
                sum += RankOneTensor.getArrEntry(mat, i, j+1) * solution[j];
            }
            if (RankOneTensor.getArrEntry(mat, i, i+1) == 0)
            {
                //System.out.println("GAUSSIAN FAILED (DIVIDE BY ZERO)");
                return null;
            }
            solution[i] = ((mat[i]&1) + sum) % 2; //Don't need division on F2, Addition is the same as subtraction
        }

        //Need to check all lines below still work
        for (int i = N; i < mat.length; i++)
        {
            if ((mat[i]&1) != 0)
            {
                //System.out.println("GAUSSIAN FAILED (TOO MANY TERMS)");
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
            if (RankOneTensor.getArrEntry(mat, max, k) == 0)
            {
                for (int i = k + 1; i < mat.length; i++)
                {
                    if (RankOneTensor.getArrEntry(mat, i, k) == 1)
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

                if (RankOneTensor.getArrEntry(mat, i, k) == 1)
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
    public int getMatchingSelections(Selection lookingAtMatrix, int[][][] result, int[] setSize, long[] commonMatrix, boolean[] hasSymmetry)
    {
        //ArrayList<ArrayList<int[]>>result = new ArrayList<>();
        //ArrayList<Long> commonMatrix = new ArrayList<>();
        //ArrayList<Boolean> hasSymmetry = new ArrayList<>();

        int numSets = 0;

        for (int i = 0; i < tensors.size(); i++)
        {
            RankOneTensor t = tensors.get(i);
            long selection = 0;
            long selectionSpin1 = 0;
            long selectionSpin2 = 0;
            boolean symmetric = tensors.get(i).hasSymmetry;

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


            //for (int j = 0; j < commonMatrix.size(); j++)
            for (int j = 0; j < numSets; j++)
            {
                if (symmetric == hasSymmetry[j])
                {
                    int[] current = result[j][setSize[j]];

                    boolean foundMatchHere = false;

                    current[1] = 0;
                    current[2] = 0;
                    current[3] = 0;

                    long m = commonMatrix[j];
                    if (m == selection)
                    {
                        foundMatch = true;
                        foundMatchHere = true;

                        current[0] = i;
                        current[1] = 1;
                    }

                    if (t.hasSymmetry)
                    {

                        if (m == selectionSpin1)
                        {
                            foundMatch = true;
                            foundMatchHere = true;

                            current[0] = i;
                            current[2] = 1;
                        }
                        if (m == selectionSpin2)
                        {
                            foundMatch = true;
                            foundMatchHere = true;

                            current[0] = i;
                            current[3] = 1;
                        }
                    }

                    if (foundMatchHere)
                    {
                        setSize[j]++;
                    }
                }
            }

            if (foundMatch == false)
            { //If common matrix item is not already contained

                //int[] entry = new int[4];
                //entry[0] = i;
                //entry[1] = 1;
                //entry[2] = 0;
                //entry[3] = 0;

                //ArrayList<int[]> locationIndex = new ArrayList<>();
                //locationIndex.add(entry); //Add current position to start new group

                result[numSets][0][0] = i;
                result[numSets][0][1] = 1;
                result[numSets][0][2] = 0;
                result[numSets][0][3] = 0;

                commonMatrix[numSets] = selection;
                hasSymmetry[numSets] = symmetric;
                setSize[numSets] = 1;

                numSets++;


                //result.add(locationIndex);
                //commonMatrix.add(selection);
                //hasSymmetry.add(symmetric);
            }

        }
        return numSets;
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
        RankOneTensor xTensor;
        RankOneTensor yTensor;

        RankOneTensor xOriginal = new RankOneTensor(0,0,0, 0);
        RankOneTensor yOriginal = new RankOneTensor(0,0,0, 0);

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
                    if (xTensor.a == yTensor.a)
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
                    if (xTensor.b == yTensor.b)
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
                    if (xTensor.c == yTensor.c)
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
                if (xTensor.a == yTensor.a)
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
                if (xTensor.b == yTensor.b)
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
                if (xTensor.c == yTensor.c)
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
        return false;

    }

    public void flipTensors(RankOneTensor xTensor, RankOneTensor yTensor, int selectedflip)
    {
        //Flip tensors

        switch (selectedflip)
        {
            case 1: //A is the same
                xTensor.b = xTensor.b ^ yTensor.b;
                yTensor.c = yTensor.c ^ xTensor.c;
                break;
            case 2: //B is the same
                xTensor.a = xTensor.a ^ yTensor.a;
                yTensor.c = yTensor.c ^ xTensor.c;
                break;
            case 3: //C is the same
                xTensor.a = xTensor.a ^ yTensor.a;
                yTensor.b = yTensor.b ^ xTensor.b;
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
        int MAX_SIZE = 100;
        int[] potentialFlip = new int [MAX_SIZE];
        int[] changeRepresentativeBy = new int [MAX_SIZE];
        int pos = 0;

        RankOneTensor xTensor = null;
        RankOneTensor yTensor = null;

        int xIndex;
        int yIndex;

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
                    if (xTensor.a == yTensor.a)
                    {
                        potentialFlip[pos] = 1;
                        changeRepresentativeBy[pos] = i;
                        pos++;
                    }
                    if (xTensor.b == yTensor.b)
                    {
                        potentialFlip[pos] = 2;
                        changeRepresentativeBy[pos] = i;
                        pos++;
                    }
                    if (xTensor.c == yTensor.c)
                    {
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
                if (xTensor.a == yTensor.a)
                {
                    potentialFlip[pos] = 1;
                    changeRepresentativeBy[pos] = 0;
                    pos++;
                }
                if (xTensor.b == yTensor.b)
                {
                    potentialFlip[pos] = 2;
                    changeRepresentativeBy[pos] = 0;
                    pos++;
                }
                if (xTensor.c == yTensor.c)
                {
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
            yTensor.performExchangeInPlace();
        }

        //Flip tensors

        flipTensors(xTensor, yTensor, selectedflip);
        //xTensor.justFlipped = true;
        //yTensor.justFlipped = true;
    }


    public void plusTransition()
    {


        int xIndex, yIndex;
        RankOneTensor xTensor, yTensor, zTensor;
        do
        {
            xIndex = randomInt(tensors.size());
            yIndex = randomInt(tensors.size()-1);
            if (yIndex >= xIndex)
            {
                yIndex++;
            }

            xTensor = tensors.get(xIndex);
            yTensor = tensors.get(yIndex);
        }
        while (xTensor.hasSymmetry != yTensor.hasSymmetry);


        zTensor = new RankOneTensor(0, 0, 0, xTensor.size, xTensor.hasSymmetry, false);

        long ax = xTensor.a, ay = yTensor.a;
        long bx = xTensor.b, by = yTensor.b;
        long cx = xTensor.c, cy = yTensor.c;

        xTensor.a = ax;
        xTensor.b = bx ^ by;
        xTensor.c = cx;

        yTensor.a = ax;
        yTensor.b = by;
        yTensor.c = cy ^ cx;

        zTensor.a = ax ^ ay;
        zTensor.b = by;
        zTensor.c = cy;

        xTensor.justFlipped = true;
        yTensor.justFlipped = true;
        zTensor.justFlipped = true;

        tensors.add(zTensor);

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
        int size = t.size;
        long a = t.a;
        for (int ai = 0; ai < size; ai++)
        {
            for (int aj = 0; aj < size; aj++)
            {
                if (RankOneTensor.getEntry(a, ai, aj) == 1)
                {
                    long b = t.b;
                    for (int bi = 0; bi < size; bi++)
                    {
                        for (int bj = 0; bj < size; bj++)
                        {
                            if (RankOneTensor.getEntry(b, bi, bj) == 1)
                            {
                                long c = t.c;
                                for (int ci = 0; ci < size; ci++)
                                {
                                    for (int cj = 0; cj < size; cj++)
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
                        [m.tensors.get(0).size]
                        [m.tensors.get(0).size]
                        [m.tensors.get(0).size]
                        [m.tensors.get(0).size]
                        [m.tensors.get(0).size]
                        [m.tensors.get(0).size];

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
        int[][][][][][] testAgainst = constructResult(getBasicMethod(tensors.get(0).size));
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
