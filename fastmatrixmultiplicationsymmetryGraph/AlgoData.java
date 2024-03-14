import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AlgoData
{
    public int[] minStepsFoundForReduction; //Stores the current best number of steps for a reduction

    //Used to store the average
    public int[] sumsStepsRequiredForReduction;

    public int[] maxStepsFoundForReduction; //Used to store max

    public double[] minTimeSeconds; //Used to store min

    public double[] totalTimeSeconds; //Used to store max

    public double[] maxTimeSeconds; //Used to store max

    public int[][] stepsforEach;
    public double[][] timeforEach;

    public int[] totalCasesFoundReduction;

    public AlgoData(int n, int m, int p)
    {
        minStepsFoundForReduction = new int[n*m*p*3*n];

        sumsStepsRequiredForReduction = new int[n*m*p*3*n];
        totalCasesFoundReduction = new int[n*m*p*3*n];

        maxStepsFoundForReduction = new int[n*m*p*3*n]; //Used to store max


        minTimeSeconds = new double[n*m*p*3*n];

        totalTimeSeconds = new double[n*m*p*3*n];

        maxTimeSeconds = new double[n*m*p*3*n]; //Used to store max

        stepsforEach = new int[n*m*p*3*n][1024];
        timeforEach = new double[n*m*p*3*n][1024];

        for (int i = 0; i < minStepsFoundForReduction.length; i++)
        {
            minStepsFoundForReduction[i] = -1;
            minTimeSeconds[i] = -1;
        }
    }

    public synchronized boolean pushReduction(int rank, int previousBestRank, int steps, double timeSeconds)
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

        for (int r = rank; r < previousBestRank; r++)
        {
            insert(r, steps, timeSeconds);
        }


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

    private synchronized void insert(int rank, int step, double time)
    {
        //System.out.println("Inserting: " + rank + ", " + step + ", " + time);
        int i;
        for (i = totalCasesFoundReduction[rank]-1; i > 0 ; i--)
        {
            if (stepsforEach[rank][i-1] < step)
            {
                stepsforEach[rank][i] = step;
                break;
            }
            stepsforEach[rank][i] = stepsforEach[rank][i-1];
        }
        if (i == 0)
        {
            stepsforEach[rank][0] = step;
        }

        for (i = totalCasesFoundReduction[rank]-1; i > 0 ; i--)
        {
            if (timeforEach[rank][i-1] < time)
            {
                timeforEach[rank][i] = time;
                break;
            }
            timeforEach[rank][i] = timeforEach[rank][i-1];
        }
        if (i == 0)
        {
            timeforEach[rank][0] = time;
        }
    }

    private String printSteps(int rank)
    {
        String ret = "";

        for (int i = 0; i < totalCasesFoundReduction[rank]; i++)
        {
            ret += stepsforEach[rank][i];
            if (i+1 < totalCasesFoundReduction[rank])
            {
                ret += ",";
            }
        }

        return ret;
    }

    private String printTime(int rank)
    {
        String ret = "";

        for (int i = 0; i < totalCasesFoundReduction[rank]; i++)
        {
            ret += timeforEach[rank][i];
            if (i+1 < totalCasesFoundReduction[rank])
            {
                ret += ",";
            }
        }

        return ret;
    }


    public void printResults()
    {
        System.out.println(" ================================== RESULTS ================================== ");

        ArrayList<String[]> data = new ArrayList<>();
        ArrayList<String> rawstepdata = new ArrayList<>();
        ArrayList<String> rawtimedata = new ArrayList<>();
        String[] header = {"Rank", "Min Steps", "Average Steps", "Max Steps", "Min Time", "Average Time", "Max Time", "Count"};
        String[] rawheader = {"Rank"};
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

                String rawSteps = printSteps(rank);
                String rawTime = printTime(rank);

                System.out.println("Recorded Steps: "+ rawSteps);
                System.out.println("Recorded Time: "+ rawTime);

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
                rawstepdata.add(rank+","+rawSteps);
                rawtimedata.add(rank+","+rawTime);

            }
        }

        writeCsvFile("AlgorithmResults.csv", data, header);
        writeCsvFileLine("AlgorithmRawStepResults.csv", rawstepdata, rawheader);
        writeCsvFileLine("AlgorithmRawTimeResults.csv", rawtimedata, rawheader);

    }



    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    private static void writeCsvFile(String fileName, ArrayList<String[]> data, String[] header)
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

    private static void writeCsvFileLine(String fileName, ArrayList<String> data, String[] header)
    {

        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter(fileName);

            // Write the CSV file header
            writeLine(header, fileWriter);


            // Write user data to the CSV file
            for (String line : data)
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

    private static void writeLine(String line, FileWriter fileWriter) throws IOException
    {
        //Add entry
        fileWriter.append(line);
        // Add a new line separator
        fileWriter.append(NEW_LINE_SEPARATOR);
    }


}
