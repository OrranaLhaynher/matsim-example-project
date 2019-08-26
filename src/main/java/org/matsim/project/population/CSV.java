package org.matsim.project.population;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSV {
	
	static String COMMA_DELIMITER = ",";

    public static void main(String [] args) throws IOException {
		String csvFile = "C:\\Users\\orran\\Desktop\\ArtigoTeste\\DATASET_final.csv";
		String[][] data = new String[46][2];
		data = getCSVData(csvFile);

		for (int i = 0; i < 46; i++) {
			for (int j = 0; j < 2; j++) {
				System.out.println(data[i][j]);
			}
		}
	}
	
    static String[][] getCSVData(String csvFile) {
		
		BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String[] position = null;
        String person[][] = new String[46][2];

        try {

            br = new BufferedReader(new FileReader(csvFile));
            int i = 0;
            while ((line = br.readLine()) != null) {
            	
                // use comma as separator
               position = line.split(cvsSplitBy);

               person[i][0]=position[5];
               person[i][1]=position[4];
                
               i++; 
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

}
