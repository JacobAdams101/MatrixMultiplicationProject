
/**
 *
 * @author jacob
 */
public class RankTensor
{
    /**
     *
     */
    public int[][]a;
    /**
     *
     */
    public int[][]b;
    /**
     *
     */
    public int[][]c;

    public boolean hasSymmetry;

    public RankTensor(int[][]a, int[][]b, int[][]c)
    {
        this(a, b, c, false);
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     */
    public RankTensor(int[][]a, int[][]b, int[][]c, boolean hasSymmetry)
    {
        this.a = a;
        this.b = b;
        this.c = c;

        this.hasSymmetry = hasSymmetry;
    }

    public RankTensor performExchange()
    {
        return new RankTensor(b, c, a, hasSymmetry);
    }

    public boolean isEqual(RankTensor t)
    {
        return areMatrixEqual(this.a, t.a) && areMatrixEqual(this.b, t.b) && areMatrixEqual(this.c, t.c);
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean areMatrixEqual(int[][] x, int[][] y)
    {
        if (x.length != y.length) return false;
        if (x[0].length != y[0].length) return false;

        for (int ix = 0; ix < x.length; ix++)
        {
            for (int iy = 0; iy < x[0].length; iy++)
            {
                if (x[ix][iy] != y[ix][iy])
                {
                    return false;
                }
            }
        }
        return true;
    }
    /**
     *
     * @param output
     * @param mat
     * @param matName
     */
    public static void addMatrix(StringBuilder output, int[][] mat, String matName)
    {
        int i;
        int j;
        output.append("(");
        for(i = 0; i < mat.length; i++)
        {
            for (j = 0; j < mat[0].length; j++)
            {
                if (mat[i][j] != 0)
                {
                    if (mat[i][j] > 0)
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
