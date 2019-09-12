package org.matsim.project.population;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class IdDuplicate {

    public static void main(String [] args) throws IOException {
		String csvFile = "C:\\Users\\orran\\Desktop\\ArtigoTeste\\RouteAnalysis1\\dataset_before.csv";
        String[][] data = new String[60][3];
        int m = 60;
        int n = 3;
		data = CSV.getCSVData(csvFile, m, n);
        //findDuplicatesUsingJava8(data);
            String id = getDuplicates(data);
            System.out.println(id);
		/*for (int i = 0; i < 149; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.println(data[i][j]);
			}
		}*/
    }

    public static String getDuplicates(String[][] array) {
		Set<String> set = new HashSet<String>();
        String[] vet = new String[60];
        for (int i = 0; i < array.length; i++) {
            vet[i] = array[i][0];
        }

        for (String name : vet) {
            if (set.add(name) == false) {
                return name;
            }
       }
        return null;

    }

    private static boolean findDuplicate(String string, String[][] position) {
        /*String[] duplicates = new String[120];
        for (int i = 0; i < position.length; i++) {
            for (int j = i + 1 ; j < position.length; j++) {
                if (position[i][0].equals(position[j][0])) {
                    String duplicate = position[i][0];
                }
            }
        }
        return duplicates;*/
        Set<String> set = new HashSet<String>();


        for (String[] name : position) {
            if (set.add(name[0]) == false) {
              return true;
            }
       }
       return false;
    }

    public static <T extends Comparable<T>> void getDuplicates(T[][] array) {
		Set<T> dupes = new HashSet<T>();

        for (T[] i : array) {
            if (!dupes.add(i[0])) {
                System.out.println("Duplicate element in array is : " + i[0]);
            }
        }

    }

    private static boolean checkForDuplicates(String e, String[] array){
		// create an empty set
        Set<String> set = new HashSet<String>();

		// do for every element in the array
		for ( String pos : array)
		{
			// return true if duplicate is found
			if (set.contains(pos))
				return true;
			// insert current element into a set
			if (e != null)
                //System.out.println(pos);
				set.add(pos);
            

		}

		// no duplicate found
		return false;
	}
    

}