package org.matsim.project.population;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class CSV {

    public static void main(String [] args) throws IOException {
		String csvFile = "C:\\Users\\orran\\Desktop\\Minicurso\\uniquepoints\\dataset_before.csv";
        /*String[][] data = new String[296][3];
        int m = 296;
        int n = 3;
        data = getCSVData(csvFile, m, n);*/
        getDuplicates(csvFile);

		/*for (int i = 0; i < 296; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.println(data[i][j]);
			}
		}*/
	}
	
    public static String[][] getCSVData(String csvFile, int i, int j) {
		
		BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String[] position = null;
        String person[][] = new String[i][j];
        //Set<String> set = new HashSet<String>();

        try {

            br = new BufferedReader(new FileReader(csvFile));
            int p = 0;
            while ((line = br.readLine()) != null) {
            	
                // use comma as separator
                position = line.split(cvsSplitBy);
                person[p][0]=position[1];
                person[p][1]=position[5];
                person[p][2]=position[4];
  
                p++; 
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return person;
    }

    public static void getDuplicates(String csvFile) throws IOException {
          
        // BufferedReader object for input.txt 
        BufferedReader br = new BufferedReader(new FileReader(csvFile)); 
        FileWriter csvWriter = new FileWriter("C:\\Users\\orran\\Desktop\\Minicurso\\duplicates_before.csv");
        FileWriter csvWriter1 = new FileWriter("C:\\Users\\orran\\Desktop\\Minicurso\\nduplicates_before.csv");
          
         String line = br.readLine(); 
          
        // set store unique values 
        HashSet<String> hs = new HashSet<String>(); 
        String[] position = new String[149];
        String cvsSplitBy = ",";
              
        // loop for each line of input.txt 
        while(line != null) { 
            // write only if not 
            // present in hashset 
            position = line.split(cvsSplitBy);

            if(hs.add(position[1])){ 
                csvWriter1.append(String.join(",", line)); 
                csvWriter1.append("\n");
            }else{
                csvWriter.append(String.join(",", line)); 
                csvWriter.append("\n");
            }
            line = br.readLine();  
            csvWriter.flush();
            csvWriter1.flush(); 
        } 

        br.close();
        csvWriter.close();
        csvWriter1.close();

        System.out.println("File operation performed successfully"); 

    }
}
