package org.matsim.project.population;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSV {
	
	static String COMMA_DELIMITER = ",";

    public static void main(String [] args) throws IOException {
		String csvFile = "C:\\Users\\orran\\Desktop\\ArtigoTeste\\dataset.csv";
        String[][] data = new String[149][3];
        int m = 149;
        int n = 3;
		data = getCSVData(csvFile, m, n);

		for (int i = 0; i < 149; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.println(data[i][j]);
			}
		}
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

        /*int end = person.length;
        for(int m = 0; m < end; m++){
            for(int n = 0; n < 3; n++){
                set.add(person[m][0]);
            }
        }
        Iterator<String> it = set.iterator();
        for(int k=0; k<person.length; k++){
            person[k][0] = it.next();
            System.out.println(person[k][0]);
        }*/

        return person;
    }

}
