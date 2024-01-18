
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package fastmatrixmultiplication;

import java.util.ArrayList;

/**
 *
 * @author jacob
 */
public class Graph
{
    private final ArrayList<GraphNode>NODES;

    public Graph() {
        NODES = new ArrayList<>();
    }

    public void exploreGraph(int n, int m, int p, boolean enableTesting, boolean useReducedSymmetry, boolean treatAllAsSymmetric) throws Exception
    {
        MultiplicationMethod x = MultiplicationMethod.getBasicMethod(n, m, p);
        if (useReducedSymmetry)
        {
            x.reduceToSymmetry(treatAllAsSymmetric);
            if (enableTesting)
            {
                System.out.println("===== TEST CASE POST SYMMETRY =====");
                if (x.testValidity())
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
        /*
        ArrayList<RankTensor>tensors = new ArrayList<>();

        tensors.add(new RankTensor(
                new int[][] {
                    {0, 0},
                    {0, 1}},
                new int[][] {
                    {0, 0},
                    {1, 0}},
                new int[][] {
                    {1, 1},
                    {0, 0}}));

        tensors.add(new RankTensor(
                new int[][] {
                    {0, 0},
                    {1, 0}},
                new int[][] {
                    {1, 0},
                    {0, 0}},
                new int[][] {
                    {0, 1},
                    {0, 1}}));

        tensors.add(new RankTensor(
                new int[][] {
                    {0, 1},
                    {0, 1}},
                new int[][] {
                    {1, 1},
                    {1, 1}},
                new int[][] {
                    {1, 0},
                    {0, 0}}));

        tensors.add(new RankTensor(
                new int[][] {
                    {1, 1},
                    {1, 1}},
                new int[][] {
                    {1, 1},
                    {0, 0}},
                new int[][] {
                    {1, 0},
                    {0, 0}}));

        tensors.add(new RankTensor(
                new int[][] {
                    {1, 0},
                    {0, 0}},
                new int[][] {
                    {0, 1},
                    {0, 1}},
                new int[][] {
                    {1, 0},
                    {1, 0}}));

        tensors.add(new RankTensor(
                new int[][] {
                    {1, 1},
                    {0, 0}},
                new int[][] {
                    {0, 0},
                    {0, 1}},
                new int[][] {
                    {0, 0},
                    {1, 1}}));

        tensors.add(new RankTensor(
                new int[][] {
                    {1, 1},
                    {0, 1}},
                new int[][] {
                    {1, 1},
                    {0, 1}},
                new int[][] {
                    {1, 0},
                    {0, 1}}));

        tensors.add(new RankTensor(
                new int[][] {
                    {1, 1},
                    {1, 1}},
                new int[][] {
                    {1, 1},
                    {0, 0}},
                new int[][] {
                    {1, 0},
                    {0, 1}}));

        MultiplicationMethod x = new MultiplicationMethod(tensors);
        */





        tryAddMethod(x);


        NODES.add(new GraphNode(x));
        for (int i = 0; i < NODES.size(); i++)
        {
            GraphNode gn = NODES.get(i);

            ArrayList<MultiplicationMethod> methods = gn.nodeMethod.getAllFlips();

            for (MultiplicationMethod mm : methods) {




                if (enableTesting && false)
                {
                    System.out.println("===== TEST CASE POST FLIP =====");
                    if (mm.testValidity())
                    {
                        System.out.println("======= VALID =======");
                    }
                    else
                    {
                        System.out.println("======= FLIP FAIL  =======");
                        //throw new Exception("FLIP failed!");
                        return;
                    }
                }

                tryAddMethod(mm);
            }


            methods = gn.nodeMethod.getAllReductions();


            for (MultiplicationMethod mm : methods) {
                if (enableTesting)
                {
                    System.out.println("===== TEST CASE POST REDUCTION =====");
                    if (mm.testValidity())
                    {
                        System.out.println("======= VALID =======");
                    }
                    else
                    {
                        System.out.println("======= REDUCTION FAIL  =======");
                        System.out.println(mm);
                        System.out.println("======= REDUCTION FAILED FROM  =======");
                        System.out.println(gn.nodeMethod);
                        System.out.println("======= []  =======");
                        return;

                    }
                }
                System.out.println("Did add: " + tryAddMethod(mm));
            }


            if (methods.isEmpty() == false) {
                for (MultiplicationMethod reducedMethod : methods)
                {
                    System.out.println("==== FOUND REDUCTION! ====");
                    System.out.println("==== FROM ====");
                    System.out.println(gn.nodeMethod);
                    System.out.println("==== TO ====");
                    System.out.println(reducedMethod);
                }


            }


            methods = gn.nodeMethod.getAllRepresentativeChanges();

            for (MultiplicationMethod mm : methods) {
                if (enableTesting && false)
                {
                    //System.out.println("===== TEST CASE POST REPRESENTATIVE CHANGE =====");
                    if (mm.testValidity())
                    {
                        //System.out.println("======= VALID =======");
                    }
                    else
                    {
                        System.out.println("======= REPRESENTATIVE CHANGE FAIL  =======");
                        System.out.println(mm);
                        System.out.println("======= REPRESENTATIVE FAILED FROM  =======");
                        System.out.println(gn.nodeMethod);
                        System.out.println("======= []  =======");
                        return;

                    }
                }
                tryAddMethod(mm);
            }

            double percentageComplete = ((double)i * 100d) / (double)NODES.size();
            System.out.println("Progress: " + percentageComplete + "%");
        }

        System.out.println("RESULT SIZE: " + NODES.size());
    }

    public boolean tryAddMethod(MultiplicationMethod m)
    {
        GraphNode temp = new GraphNode(m);

        if (NODES.contains(temp) == false) {
            //System.out.println("SUCCESS ADD");
            NODES.add(temp);
            return true;
        } else {
            //System.out.println("FAIL ADD");
            return false;
        }

    }
}
