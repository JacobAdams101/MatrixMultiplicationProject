import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AlgoData
{
    public int[] minStepsFoundForReduction; //Stores the current best number of steps for a reduction

    //Used to store the average
    public int[] sumsStepsRequiredForReduction;
    public int[] totalCasesFoundReduction;

    public int[] maxStepsFoundForReduction; //Used to store max

    public double[] minTimeSeconds; //Used to store min

    public double[] totalTimeSeconds; //Used to store max

    public double[] maxTimeSeconds; //Used to store max

    public AlgoData(int n, int m, int p)
    {
        minStepsFoundForReduction = new int[n*m*p*3];

        sumsStepsRequiredForReduction = new int[n*m*p*3];
        totalCasesFoundReduction = new int[n*m*p*3];

        maxStepsFoundForReduction = new int[n*m*p*3]; //Used to store max


        minTimeSeconds = new double[n*m*p*3];

        totalTimeSeconds = new double[n*m*p*3];

        maxTimeSeconds = new double[n*m*p*3]; //Used to store max

        for (int i = 0; i < minStepsFoundForReduction.length; i++)
        {
            minStepsFoundForReduction[i] = -1;
            minTimeSeconds[i] = -1;
        }
    }

    public boolean pushReduction(int rank, int steps, double timeSeconds)
    {
        boolean isMin = false;
        if (minStepsFoundForReduction[rank] == -1 || minStepsFoundForReduction[rank] > steps)
        {
            minStepsFoundForReduction[rank] = steps;
            isMin = true;
        }

        if (minTimeSeconds[rank] < 0 || minTimeSeconds[rank] > timeSeconds)
        {
            minTimeSeconds[rank] = timeSeconds;
        }

        sumsStepsRequiredForReduction[rank] += steps;
        totalTimeSeconds[rank] += timeSeconds;
        totalCasesFoundReduction[rank]++;

        if (steps > maxStepsFoundForReduction[rank])
        {
            maxStepsFoundForReduction[rank] = steps;
        }

        if (timeSeconds > maxTimeSeconds[rank])
        {
            maxTimeSeconds[rank] = timeSeconds;
        }


        return isMin;
    }

    public void printResults()
    {
        System.out.println(" ================================== RESULTS ================================== ");

        ArrayList<String[]> data = new ArrayList<>();
        String[] header = {"Rank", "Min Steps", "Average Steps", "Max Steps", "Min Time", "Average Time", "Max Time", "Count"};
        for (int rank = 0; rank < totalCasesFoundReduction.length; rank++)
        {
            if (totalCasesFoundReduction[rank] > 0)
            {

                double averageStep = (double)sumsStepsRequiredForReduction[rank] / (double)totalCasesFoundReduction[rank];
                double averageTime = totalTimeSeconds[rank] / (double)totalCasesFoundReduction[rank];

                System.out.println(" =====> RANK " + rank);
                System.out.println("Steps: ");
                System.out.println(" - Min Step: " + minStepsFoundForReduction[rank]);
                System.out.println(" - Avg Step: " + averageStep);
                System.out.println(" - Max Step: " + maxStepsFoundForReduction[rank]);
                System.out.println("Time: ");
                System.out.println(" - Min Time: " + minTimeSeconds[rank]);
                System.out.println(" - Avg Time: " + averageTime);
                System.out.println(" - Max Time: " + maxTimeSeconds[rank]);
                System.out.println("");
                System.out.println(" - Count: " + totalCasesFoundReduction[rank]);

                String[] line = {
                    rank+"",
                    minStepsFoundForReduction[rank]+"",
                    averageStep+"",
                    maxStepsFoundForReduction[rank]+"",
                    minTimeSeconds[rank]+"",
                    averageTime+"",
                    maxTimeSeconds[rank]+"",
                    totalCasesFoundReduction[rank]+""

                };

                data.add(line);

            }
        }

        writeCsvFile("AlgorithmResults.csv", data, header);

    }



    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    public static void writeCsvFile(String fileName, ArrayList<String[]> data, String[] header)
    {

        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter(fileName);

            // Write the CSV file header
            writeLine(header, fileWriter);


            // Write user data to the CSV file
            for (String[] line : data)
            {
                writeLine(line, fileWriter);
            }

        }
        catch (IOException e)
        {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        finally
        {

            try
            {
                fileWriter.flush();
                fileWriter.close();
            }
            catch (IOException e)
            {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }

        }
    }

    private static void writeLine(String[] line, FileWriter fileWriter) throws IOException
    {
        for (int i = 0; i < line.length; i++)
        {
            String entry = line[i];
            //Add entry
            fileWriter.append(entry);
            if (i != line.length - 1)
            { //Add comma if nessecary
                fileWriter.append(COMMA_DELIMITER);
            }
        }
        // Add a new line separator
        fileWriter.append(NEW_LINE_SEPARATOR);
    }

}