
//package fastmatrixmultiplication;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author jacob
 */
public class GraphNode
{
    private static int idGen;
    static {
        idGen = 0;
    }
    private static int nextID() {
        return idGen++;
    }

    private final int ID;

    public final MultiplicationMethod nodeMethod;

    private final ArrayList<GraphNode>neighbours;

    public GraphNode(MultiplicationMethod nodeMethod) {
        this.nodeMethod = new MultiplicationMethod(nodeMethod.getTensors());
        neighbours = new ArrayList<>();
        this.ID = nextID();
    }

    public int getID() {
        return ID;
    }

    @Override
    public boolean equals(Object e) {
        if (e instanceof GraphNode) {
            return nodeMethod.equals(((GraphNode) e).nodeMethod);
        } else {
            return false;
        }
    }
    /*
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.nodeMethod);
        return hash;
    }
    */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(nodeMethod);
        sb.append("\nNeighbours:\n");

        for (GraphNode gn : neighbours) {
            sb.append(gn.getID());
            sb.append(", ");
        }
        sb.append("\n");

        return sb.toString();
    }
}
