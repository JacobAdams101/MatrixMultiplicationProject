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

    private int edgeConstraint;
    private int stepsSinceLastEdgeExpansion;
    private int scaleAt;


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

    public boolean increaseEdgeConstraint(boolean earlyExpansion)
    {

        stepsSinceLastEdgeExpansion = 0;
        edgeConstraint++;
        scaleAt *= 2;
        if (edgeConstraint > tensors.get(0).size)
        {
            edgeConstraint = tensors.get(0).size;
            return false;
        }
        else
        {
            System.out.println("Expanding Edge to now: " + edgeConstraint + ", EarlyExpansion: " + earlyExpansion);
            return true;
        }


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

        stepsSinceLastEdgeExpansion = 0;

        markTensorsAsChangeUnchanged(true);

        long startTime = System.currentTimeMillis();

        int rank;
        int tensorRank;

        int minRank = getExpandedRank();

        edgeConstraint = 2;

        scaleAt = 100000;

        do
        {
            int state = randomStep();
            if (state == -1)
            {
                System.out.println("===== NO VALID STEPS FOUND! =====");
                break;
            }
            boolean hasReduced = state == 1;

            stepsSinceLastEdgeExpansion++;
            if (stepsSinceLastEdgeExpansion > scaleAt)
            {
                increaseEdgeConstraint(false);
            }


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
    public int randomStep()
    {
        //Always try to reduce first
        int result = lookForReductionInFlipNeighbours(); //Look for a reduction either at this step or at a neighbour

        switch(result)
        {
            case 0: //No reduction performed AND no flips can be performed
                //No valid flips exist either so expand edge constraint early
                //System.out.println("Early Issue");
                if (increaseEdgeConstraint(true))
                {
                    return 0; //No reduction found
                }
                else
                {
                    return -1; //Can't flip anymore
                }

            case 1: //No reduction performed but flips do exist
                //If I can't reduce the set, perform one random flip
                //Failed to find a flip so don't both searching these tensors anymore
                markTensorsAsChangeUnchanged(false);
                //Make a flip
                //Make flip using random flip chosen from searching for a reduction
                makeFlipFromSymmetry(randomFlipXIndex, randomFlipYIndex, randomFlipSelected, randomFlipSpinBy);
                //makeFlip();
                return 0; //No reduction found
            case 2: //Reduction performed
                //Return true saying that a reduction was performed
                return 1; //Reduction found
            default:
                return -1; //Error
        }


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
            int[] combination = newTestLinearDependence(nextToTest, size, Selection.A);
            if (combination != null)
            {
                int tIndex = combination[combination.length-1]; //Last item of array is unrelated data and instead stores tIndex

                int tensorIndexToRemove = nextToTest[tIndex][0]; //Pick tIndex of nextToTest to remove

                int nextToTestIndex = 0;

                int[] b = new int[tensors.size()];
                for (int k = 0; k < b.length; k++)
                {

                    if (nextToTestIndex < size && k == nextToTest[nextToTestIndex][0])
                    {
                        b[k] = combination[nextToTestIndex];
                        nextToTestIndex++;
                    }
                    else
                    {
                        b[k] = -1; //I think because of how this is implemented all vectors are used
                    }
                }

                reconstructMultiplicationScheme(ignoreMatrix, Selection.A, nextToTest, size, tensorIndexToRemove, b);

                return true;
            }
        }
        if (ignoreMatrix != Selection.B)
        {
            int[] combination = newTestLinearDependence(nextToTest, size, Selection.B);
            if (combination != null)
            {
                int tIndex = combination[combination.length-1]; //Last item of array is unrelated data and instead stores tIndex

                int tensorIndexToRemove = nextToTest[tIndex][0]; //Pick tIndex of nextToTest to remove

                int nextToTestIndex = 0;

                int[] b = new int[tensors.size()];
                for (int k = 0; k < b.length; k++)
                {

                    if (nextToTestIndex < size && k == nextToTest[nextToTestIndex][0])
                    {
                        b[k] = combination[nextToTestIndex];
                        nextToTestIndex++;
                    }
                    else
                    {
                        b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                    }
                }

                reconstructMultiplicationScheme(ignoreMatrix, Selection.B, nextToTest, size, tensorIndexToRemove, b);

                return true;
            }
        }
        if (ignoreMatrix != Selection.C)
        {
            int[] combination = newTestLinearDependence(nextToTest, size, Selection.C);
            if (combination != null)
            {
                int tIndex = combination[combination.length-1]; //Last item of array is unrelated data and instead stores tIndex

                int tensorIndexToRemove = nextToTest[tIndex][0]; //Pick tIndex of nextToTest to remove

                int nextToTestIndex = 0;

                int[] b = new int[tensors.size()];
                for (int k = 0; k < b.length; k++)
                {

                    if (nextToTestIndex < size && k == nextToTest[nextToTestIndex][0])
                    {
                        b[k] = combination[nextToTestIndex];
                        nextToTestIndex++;
                    }
                    else
                    {
                        b[k] = -1; //I *THINK* because of how this is implemented all vectors are used
                    }
                }

                reconstructMultiplicationScheme(ignoreMatrix, Selection.C, nextToTest, size, tensorIndexToRemove, b);

                return true;
            }
        }

        return false;
    }

    public void reconstructMultiplicationScheme(Selection dimensionOneOver, Selection linearlyDependantOver, int[][] listOfDependant, int dependantSize, int tensorIndexToRemove, int[] b)
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
                long augmented = 0;

                for (int j = 0; j < size; j++)
                {
                    //int A = a[i] == 0 ? 0 : Integer.MAX_VALUE;
                    int B = b[i] == 0 ? 0 : Integer.MAX_VALUE;

                    //augmented |= (((matrixToAugment >> (j*8)) ^ (A & B & (tmatrixToAugment >> (j*8)))) & ROWMASK) << (j*8);

                    augmented |= (((matrixToAugment >> (j*8)) ^ (B & (tmatrixToAugment >> (j*8)))) & ROWMASK) << (j*8);

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
    public int[] newTestLinearDependence(int[][] indexes, int indexSize, Selection matrixToTest)
    {
        int i;
        int j;

        long[] matToReduce = new long[indexSize];
        long[] solution = new long[indexSize];

        for (i = 0; i < indexSize; i++)
        {
            switch (matrixToTest)
            {
                case A:
                    matToReduce[i] = tensors.get(indexes[i][0]).a;
                    break;
                case B:
                    matToReduce[i] = tensors.get(indexes[i][0]).b;
                    break;
                case C:
                    matToReduce[i] = tensors.get(indexes[i][0]).c;
                    break;
                default:
                    matToReduce[i] = 0;
                    break;
            }
            solution[i] = 1 << i;
        }


        newGaussian(matToReduce, solution);
        for (i = 0; i < indexSize; i++)
        {
            if (matToReduce[i] == 0)
            {
                int[] result = new int[indexSize+1];
                result[indexSize] = i;
                int count = 0;
                for (j = 0; j < indexSize; j++)
                {
                    if ((solution[i]&(1<<j))!=0 && i!=j)
                    {
                        result[j] = 1;
                    }
                }

                return result;
            }
        }
        return null;
    }

    public static void newGaussian(long[] mat, long[] solution)
    {
      for (int k = 0; k < mat.length; k++)
      {
        int pivotColIndex = k;
        long pivotCol = mat[pivotColIndex];
        long solCol = solution[pivotColIndex];
        long pivotBit = pivotCol&(-pivotCol);
        for (int i = k+1; i < mat.length; i++)
        {
          if ((mat[i] & pivotBit) != 0)
          {
            mat[i] ^= pivotCol;
            solution[i] ^= solCol;
          }
        }
      }
      for (int k = 0; k < mat.length; k++)
      {
        int pivotColIndex = mat.length-1-k;
        long pivotCol = mat[pivotColIndex];
        long solCol = solution[pivotColIndex];
        long pivotBit = pivotCol&(-pivotCol);
        for (int i = k+1; i < mat.length; i++)
        {
          if ((mat[mat.length-1-i] & pivotBit) != 0)
          {
            mat[mat.length-1-i] ^= pivotCol;
            solution[mat.length-1-i] ^= solCol;
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

    int randomFlipXIndex;
    int randomFlipYIndex;
    int randomFlipSelected;
    int randomFlipSpinBy;
    double flipsFound;

    public void flipFound(int xIndex, int yIndex, int selectedFlip, int spinBy)
    {
        flipsFound += 1.0;

        if (Math.random() < 1.0 / flipsFound || flipsFound < 1.5) //Make sure if makes this flip on the first go
        {
            //System.out.println("Values: " + flipsFound);
            randomFlipXIndex = xIndex;
            randomFlipYIndex = yIndex;
            randomFlipSelected = selectedFlip;
            randomFlipSpinBy = spinBy;
        }
    }

    public int lookForReductionInFlipNeighbours()
    {
        RankOneTensor xTensor;
        RankOneTensor yTensor;

        RankOneTensor xOriginal = new RankOneTensor(0,0,0,0);
        RankOneTensor yOriginal = new RankOneTensor(0,0,0,0);

        int xIndex;
        int yIndex;

        boolean foundFlip = false;

        flipsFound = 0.0;

        for (xIndex = 0; xIndex < tensors.size()-1; xIndex++) {
        for (yIndex = xIndex+1; yIndex < tensors.size(); yIndex++) {

            //Get pair of tensors
            xTensor = tensors.get(xIndex);
            yTensor = tensors.get(yIndex);
            //Check they are a valid pair (in terms of symmetry)
            if (xTensor.hasSymmetry != yTensor.hasSymmetry)
            {
                continue;
            }

            if (xTensor.maxIndex >= edgeConstraint || yTensor.maxIndex >= edgeConstraint)
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
                        foundFlip = true;
                        flipFound(xIndex, yIndex, 0, i);

                        for (int j = 0; j <= 1; j++)
                        {
                            flipTensors(xTensor, yTensor, j);

                            //Mark two fliped tensors to notify reduction code to check
                            xTensor.justFlipped = true;
                            yTensor.justFlipped = true;

                            //Look for a reduction
                            if (tryReduce())
                            {
                                return 2; //Finish function
                            }
                            //If no reduction found, revert rank 1 tensors
                            xTensor.copyFrom(xOriginal);
                            yTensor.copyFrom(yOriginal);
                        }

                    }
                    if (xTensor.b == yTensor.b)
                    {
                        foundFlip = true;
                        flipFound(xIndex, yIndex, 2, i);

                        for (int j = 0; j <= 1; j++)
                        {
                            flipTensors(xTensor, yTensor, 2+j);

                            //Mark two fliped tensors to notify reduction code to check
                            xTensor.justFlipped = true;
                            yTensor.justFlipped = true;

                            //Look for a reduction
                            if (tryReduce())
                            {
                                return 2; //Finish function
                            }
                            //If no reduction found, revert rank 1 tensors
                            xTensor.copyFrom(xOriginal);
                            yTensor.copyFrom(yOriginal);
                        }

                    }
                    if (xTensor.c == yTensor.c)
                    {
                        foundFlip = true;
                        flipFound(xIndex, yIndex, 4, i);

                        for (int j = 0; j <= 1; j++)
                        {
                            flipTensors(xTensor, yTensor, 4+j);

                            //Mark two fliped tensors to notify reduction code to check
                            xTensor.justFlipped = true;
                            yTensor.justFlipped = true;

                            //Look for a reduction
                            if (tryReduce())
                            {
                                return 2; //Finish function
                            }
                            //If no reduction found, revert rank 1 tensors
                            xTensor.copyFrom(xOriginal);
                            yTensor.copyFrom(yOriginal);
                        }


                    }
                    yOriginal.performExchangeInPlace();
                    //If no reduction found, revert rank 1 tensors
                    xTensor.copyFrom(xOriginal);
                    yTensor.copyFrom(yOriginal);

                }
                //yTensor will have flipped back at the end of this loop
            }
            else
            {
                //Verify flip can take place
                if (xTensor.a == yTensor.a)
                {
                    foundFlip = true;
                    flipFound(xIndex, yIndex, 0, 0);

                    for (int j = 0; j <= 1; j++)
                    {
                        flipTensors(xTensor, yTensor, j);

                        //Mark two fliped tensors to notify reduction code to check
                        xTensor.justFlipped = true;
                        yTensor.justFlipped = true;

                        //Look for a reduction
                        if (tryReduce())
                        {
                            return 2; //Finish function
                        }
                        //If no reduction found, revert rank 1 tensors
                        xTensor.copyFrom(xOriginal);
                        yTensor.copyFrom(yOriginal);
                    }
                }
                if (xTensor.b == yTensor.b)
                {
                    foundFlip = true;
                    flipFound(xIndex, yIndex, 2, 0);

                    for (int j = 0; j <= 1; j++)
                    {
                        flipTensors(xTensor, yTensor, 2+j);

                        //Mark two fliped tensors to notify reduction code to check
                        xTensor.justFlipped = true;
                        yTensor.justFlipped = true;

                        //Look for a reduction
                        if (tryReduce())
                        {
                            return 2; //Finish function
                        }
                        //If no reduction found, revert rank 1 tensors
                        xTensor.copyFrom(xOriginal);
                        yTensor.copyFrom(yOriginal);
                    }
                }
                if (xTensor.c == yTensor.c)
                {
                    foundFlip = true;
                    flipFound(xIndex, yIndex, 4, 0);

                    for (int j = 0; j <= 1; j++)
                    {
                        flipTensors(xTensor, yTensor, 4+j);

                        //Mark two fliped tensors to notify reduction code to check
                        xTensor.justFlipped = true;
                        yTensor.justFlipped = true;

                        //Look for a reduction
                        if (tryReduce())
                        {
                            return 2; //Finish function
                        }
                        //If no reduction found, revert rank 1 tensors
                        xTensor.copyFrom(xOriginal);
                        yTensor.copyFrom(yOriginal);
                    }
                }

                //If no reduction found, revert rank 1 tensors
                //xTensor.copyFrom(xOriginal);
                //yTensor.copyFrom(yOriginal);
            }
        }
        }
        if (foundFlip)
        { //Flip found
            return 1;
        }
        //No reduction found and no flip found either
        return 0;

    }
    /**
     * Performs a random flip on the current multiplication method
     * Flips are of the form
     * a * b * c  + a * B' * C' => a * (b + B') * c + a * B' * (c - C')
     * Up to re-ordering of A, B and C (and B', C' and A' respectively)
     */
    public void flipTensors(RankOneTensor xTensor, RankOneTensor yTensor, int selectedflip)
    {
        //Flip tensors

        //int whichWayRound = (int)(Math.random()*2);

        switch (selectedflip)
        {
            case 0: //A is the same
                xTensor.b = xTensor.b ^ yTensor.b;
                yTensor.c = yTensor.c ^ xTensor.c;
                break;
            case 1: //A is the same
                yTensor.b = xTensor.b ^ yTensor.b;
                xTensor.c = yTensor.c ^ xTensor.c;
                break;
            case 2: //B is the same
                xTensor.a = xTensor.a ^ yTensor.a;
                yTensor.c = yTensor.c ^ xTensor.c;
                break;
            case 3: //A is the same
                yTensor.a = xTensor.a ^ yTensor.a;
                xTensor.c = yTensor.c ^ xTensor.c;
                break;
            case 4: //C is the same
                xTensor.a = xTensor.a ^ yTensor.a;
                yTensor.b = yTensor.b ^ xTensor.b;
                break;
            case 5: //A is the same
                yTensor.a = xTensor.a ^ yTensor.a;
                xTensor.b = yTensor.b ^ xTensor.b;
                break;
            default:
                break;
        }
    }

    public void makeFlipFromSymmetry(int xIndex, int yIndex, int selectedflip, int representativeSpinBy)
    {
        RankOneTensor xTensor = tensors.get(xIndex);
        RankOneTensor yTensor = tensors.get(yIndex);

        for (int i = 0; i < representativeSpinBy; i++)
        {
            yTensor.performExchangeInPlace();
        }

        flipTensors(xTensor, yTensor, selectedflip+((int)(Math.random()*2.0)));
        xTensor.justFlipped = true;
        yTensor.justFlipped = true;
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
