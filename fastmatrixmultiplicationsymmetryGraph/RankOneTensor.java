//package fastmatrixmultiplication;

/**
 *
 * @author jacob
 */
public class RankOneTensor
{
    /**
     * A component
     * which elements of the A matrix are included
     */
    public long a;
    /**
     * B component
     * which elements of the B matrix are included
     */
    public long b;
    /**
     * C component
     * what is this multiplication mapped to
     */
    public long c;

    public int maxIndex = 0;

    public int size;
    /**
    * Does this rank 1 tensor represent multiple tensors through the change of representative operation
    */
    public boolean hasSymmetry;
    public boolean hasMatSymmetry;

    public boolean justFlipped;

    public RankOneTensor(long a, long b, long c, int size)
    {
        this(a, b, c, size, false, false, false);
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     */
    public RankOneTensor(long a, long b, long c, int size, boolean hasSymmetry, boolean justFlipped, boolean hasMatSymmetry)
    {
        this.a = a;
        this.b = b;
        this.c = c;

        this.size = size;

        this.hasSymmetry = hasSymmetry;

        this.hasMatSymmetry = hasMatSymmetry;

        this.justFlipped = justFlipped;

        updateMaxIndex();
    }

    public void updateMaxIndex()
    {
        maxIndex = 0;
        for (int ix = 0; ix < size; ix++)
        {
            for (int iy = 0; iy < size; iy++)
            {
                if (ix > maxIndex)
                {
                    if (getEntry(a, ix, iy) == 1 || getEntry(b, ix, iy) == 1 || getEntry(c, ix, iy) == 1)
                    {
                        maxIndex = ix;
                    }
                }
                if (iy > maxIndex)
                {
                    if (getEntry(a, ix, iy) == 1 || getEntry(b, ix, iy) == 1 || getEntry(c, ix, iy) == 1)
                    {
                        maxIndex = iy;
                    }
                }
            }
        }
    }

    public boolean isSymmetricSingleton()
    {
        if (this.hasSymmetry == false) return false;

        if (a != b)
        {
            return false;
        }
        if (a != c)
        {
            return false;
        }

        return true;
    }

    public void copyFrom(RankOneTensor t)
    {
        this.a = t.a;
        this.b = t.b;
        this.c = t.c;
        this.size = t.size;
        this.hasSymmetry = t.hasSymmetry;
        this.justFlipped = t.justFlipped;
        this.hasMatSymmetry = t.hasMatSymmetry;
    }

    public RankOneTensor performExchange()
    {
        //System.out.println("EXCHANGING");
        return new RankOneTensor(b, c, a, size, hasSymmetry, justFlipped, hasMatSymmetry);
    }

    public void performExchangeInPlace()
    {
        long temp = a;
        a = b;
        b = c;
        c = temp;

        //System.out.println("EXCHANGING");
        //return new RankOneTensor(b, c, a, hasSymmetry);
    }

    public void performMatExchangeInPlace(long mat, long matInv)
    {
        a = matmult(mat, a, matInv);
        b = matmult(mat, b, matInv);
        c = matmult(mat, c, matInv);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof RankOneTensor)
        {
            return isEqual((RankOneTensor)o);
        }
        else
        {
            return false;
        }
    }
    /*
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.deepHashCode(this.a);
        hash = 97 * hash + Arrays.deepHashCode(this.b);
        hash = 97 * hash + Arrays.deepHashCode(this.c);
        hash = 97 * hash + (this.hasSymmetry ? 1 : 0);
        return hash;
    }
    */

    public boolean isEqual(RankOneTensor t)
    {
        return this.a == t.a && this.b == t.b && this.c == t.c && this.hasSymmetry == t.hasSymmetry;
    }


    public static long getEntry(long mat, int x, int y)
    {
        int index = (x*8) + y;
        return (mat >> index) & 1;
    }

    public static int getArrEntry(int[] mat, int x, int y)
    {
        return (mat[x] >> y) & 1;
    }

    public static long setEntry(long mat, int x, int y, long set)
    {
        int index = (x*8) + y;
        if (set == 0)
        {
            mat &= ~(1L << index);
        }
        else
        {
            mat |= 1L << index;
        }

        return mat;
    }

    public static void setArrEntry(int[] mat, int x, int y, long set)
    {

        if (set == 0)
        {
            mat[x] &= ~(1L << y);
        }
        else
        {
            mat[x] |= 1L << y;
        }
    }

    /**
     *
     * @param output
     * @param mat
     * @param matName
     */
    public static void addMatrix(StringBuilder output, long mat, int size, String matName)
    {
        int i;
        int j;
        output.append("(");
        for(i = 0; i < size; i++)
        {
            for (j = 0; j < size; j++)
            {
                if (getEntry(mat, i, j) != 0)
                {
                    if (getEntry(mat, i, j) > 0)
                    {
                        output.append("+");
                    }
                    else
                    {
                        output.append("-");
                    }
                    output.append(matName);
                    output.append("_");
                    output.append(i);
                    output.append(",");
                    output.append(j);
                    output.append("  ");
                }
            }
        }
        output.append(")");
    }

    public static long getMat(int n)
    {
        long ret = 0;
        for (int i = 0; i < n-1; i++)
        {
            ret = setEntry(ret, i+1, i, 1);
        }
        for (int i = 0; i < n; i++)
        {
            ret = setEntry(ret, i, n-1, 1);
        }
        return ret;
    }

    public static long getMatInv(int n)
    {
        long ret = 0;
        for (int i = 0; i < n-1; i++)
        {
            ret = setEntry(ret, i, i+1, 1);
        }
        for (int i = 0; i < n; i++)
        {
            ret = setEntry(ret, i, 0, 1);
        }
        return ret;
    }

    public static long matmult(long a, long b, long c)
    {
        long ret = 0;

        ret = matmult(a, b);
        ret = matmult(ret, c);

        return ret;
    }


    public static long matmult(long a, long b)
    {
        long ret = 0;
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                long sum = 0;
                for (int k = 0; k < 8; k++)
                {
                    sum = sum ^ (getEntry(a, i, k) & getEntry(b, k, j));
                }
                ret = setEntry(ret, i, j, sum);
            }
        }
        return ret;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if (this.hasSymmetry)
        {
            sb.append("<");
        }

        addMatrix(sb, a, size, "a");
        sb.append(" ⊗ ");
        addMatrix(sb, b, size, "b");
        sb.append(" ⊗ ");
        addMatrix(sb, c, size, "c");

        if (this.hasSymmetry)
        {
            sb.append("> Z_3");
        }

        if (this.hasMatSymmetry)
        {
            sb.append(" MAT");
        }

        return sb.toString();
    }





}
