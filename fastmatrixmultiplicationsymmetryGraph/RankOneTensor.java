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
    public int[]a;
    /**
     * B component
     * which elements of the B matrix are included
     */
    public int[]b;
    /**
     * C component
     * what is this multiplication mapped to
     */
    public int[]c;
    /**
    * Does this rank 1 tensor represent multiple tensors through the change of representative operation
    */
    public boolean hasSymmetry;

    public boolean justFlipped;

    public RankOneTensor(int[]a, int[]b, int[]c)
    {
        this(a, b, c, false, false);
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     */
    public RankOneTensor(int[]a, int[]b, int[]c, boolean hasSymmetry, boolean justFlipped)
    {
        this.a = a;
        this.b = b;
        this.c = c;

        this.hasSymmetry = hasSymmetry;

        this.justFlipped = justFlipped;
    }

    public boolean isSymmetricSingleton()
    {
        if (this.hasSymmetry == false) return false;

        for (int ix = 0; ix < a.length; ix++)
        {
            if (a[ix] != b[ix])
            {
                return false;
            }
            if (a[ix] != c[ix])
            {
                return false;
            }
        }
        return true;
    }

    public void copyFrom(RankOneTensor t)
    {
        for (int i = 0; i < a.length; i++)
        {
            this.a[i] = t.a[i];
            this.b[i] = t.b[i];
            this.c[i] = t.c[i];
        }
        this.hasSymmetry = t.hasSymmetry;
        this.justFlipped = t.justFlipped;
    }

    public RankOneTensor performExchange()
    {
        //System.out.println("EXCHANGING");
        return new RankOneTensor(b, c, a, hasSymmetry, justFlipped);
    }

    public void performExchangeInPlace()
    {
        int[]temp = a;
        a = b;
        b = c;
        c = temp;

        //System.out.println("EXCHANGING");
        //return new RankOneTensor(b, c, a, hasSymmetry);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RankOneTensor) {
            return isEqual((RankOneTensor)o);
        } else {
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
        return areMatrixEqual(this.a, t.a) && areMatrixEqual(this.b, t.b) && areMatrixEqual(this.c, t.c) && this.hasSymmetry == t.hasSymmetry;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean areMatrixEqual(int[] x, int[] y)
    {
        if (x.length != y.length) return false;

        for (int ix = 0; ix < x.length; ix++)
        {
            if (x[ix] != y[ix])
            {
                return false;
            }
        }
        return true;
    }

    public static int getEntry(int[] mat, int x, int y)
    {
        return (mat[x] >> y) & 1;
    }

    public static void setEntry(int[] mat, int x, int y, int set)
    {
        if (set == 0)
        {
            mat[x] &= ~(1 << y);
        }
        else
        {
            mat[x] |= 1 << y;
        }
    }

    /**
     *
     * @param output
     * @param mat
     * @param matName
     */
    public static void addMatrix(StringBuilder output, int[] mat, String matName)
    {
        int i;
        int j;
        output.append("(");
        for(i = 0; i < mat.length; i++)
        {
            for (j = 0; j < mat.length; j++)
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

        addMatrix(sb, a, "a");
        sb.append(" ⊗ ");
        addMatrix(sb, b, "b");
        sb.append(" ⊗ ");
        addMatrix(sb, c, "c");

        if (this.hasSymmetry)
        {
            sb.append("> Z_3");
        }

        return sb.toString();
    }





}
