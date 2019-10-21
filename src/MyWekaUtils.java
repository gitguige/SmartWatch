
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


/**
 *
 * @author mm5gg
 */
public class MyWekaUtils {

    public static double classify(String arffData, int option) throws Exception {
		StringReader strReader = new StringReader(arffData);
		Instances instances = new Instances(strReader);
		strReader.close();
		instances.setClassIndex(instances.numAttributes() - 1);
		
		Classifier classifier;
		if(option==1)
			classifier = new J48(); // Decision Tree classifier
		else if(option==2)			
			classifier = new RandomForest();
		else if(option == 3)
			classifier = new SMO();  //This is a SVM classifier
		else 
			return -1;
		
		classifier.buildClassifier(instances); // build classifier
		
		Evaluation eval = new Evaluation(instances);
		eval.crossValidateModel(classifier, instances, 10, new Random(1), new Object[] { });
		
		return eval.pctCorrect();
	}
    
    
    public static String[][] readCSV(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        ArrayList<String> lines = new ArrayList();
        String line;

        while ((line = br.readLine()) != null) {
            lines.add(line);;
        }


        if (lines.size() == 0) {
            System.out.println("No data found");
            return null;
        }

        int lineCount = lines.size();

        String[][] csvData = new String[lineCount][];
        String[] vals;
        int i, j;
        for (i = 0; i < lineCount; i++) {            
                csvData[i] = lines.get(i).split(",");            
        }
        
        return csvData;

    }

    public static String csvToArff(String[][] csvData, int[] featureIndices) throws Exception {
        int total_rows = csvData.length;
        int total_cols = csvData[0].length;
        int fCount = featureIndices.length;
        String[] attributeList = new String[fCount + 1];
        int i, j;
        for (i = 0; i < fCount; i++) {
            attributeList[i] = csvData[0][featureIndices[i]];
        }
        attributeList[i] = csvData[0][total_cols - 1];

        String[] classList = new String[1];
        classList[0] = csvData[1][total_cols - 1];

        for (i = 1; i < total_rows; i++) {
            classList = addClass(classList, csvData[i][total_cols - 1]);
        }

        StringBuilder sb = getArffHeader(attributeList, classList);

        for (i = 1; i < total_rows; i++) {
            for (j = 0; j < fCount; j++) {
                sb.append(csvData[i][featureIndices[j]]);
                sb.append(",");
            }            
            sb.append(csvData[i][total_cols - 1]);
            sb.append("\n");
        }

        return sb.toString();
    }

    private static StringBuilder getArffHeader(String[] attributeList, String[] classList) {
        StringBuilder s = new StringBuilder();
        s.append("@RELATION wada\n\n");

        int i;
        for (i = 0; i < attributeList.length - 1; i++) {
            s.append("@ATTRIBUTE ");
            s.append(attributeList[i]);
            s.append(" numeric\n");
        }

        s.append("@ATTRIBUTE ");
        s.append(attributeList[i]);
        s.append(" {");
        s.append(classList[0]);

        for (i = 1; i < classList.length; i++) {
            s.append(",");
            s.append(classList[i]);
        }
        s.append("}\n\n");
        s.append("@DATA\n");
        return s;
    }

    private static String[] addClass(String[] classList, String className) {
        int len = classList.length;
        int i;
        for (i = 0; i < len; i++) {
            if (className.equals(classList[i])) {
                return classList;
            }
        }

        String[] newList = new String[len + 1];
        for (i = 0; i < len; i++) {
            newList[i] = classList[i];
        }
        newList[i] = className;

        return newList;
    }
    
    public static void main(String[] args) {
//    	MyWekaUtils MyWekaUtils1= new MyWekaUtils();
//    	File directory = new File("./");
//    	System.out.println(directory.getAbsolutePath());
    	try {
            //  first get the full features file
//    		String[][] csvData = MyWekaUtils.readCSV("./resources/old_data/features_1000ms.csv");  
    		String[][] csvData = MyWekaUtils.readCSV("./resources/features_12_3000ms.csv");  
    		
    		double best_accuracy = 0,pre_best_accuracy=0;
    		int best_index = 0,best_feature=0;  
    		int select_num=0,unselect_num=12;
    		int[] features_select = new int[12];
            int[] features_unselect = {0, 1, 2, 3, 4, 5,6,7,8,9,10,11}; // all the features
//            int[] features = {0, 1, 2, 3, 4, 5}; // all the features
//			int[] features = {0, 2, 5}; // use mean_x, mean_y, std_z 
            
//            //manual option, only for debug===============================================
//            int[] features = {0, 1, 2, 3, 4, 5}; // all the features
//            String arffData = MyWekaUtils.csvToArff(csvData, features);
////            System.out.println(arffData);
//            
//            double accuracy = MyWekaUtils.classify(arffData, 1);
//            System.out.println(accuracy);
            
            //automatic option===========================================================
            while(select_num <12) {
//	            for(int k=0;k<unselect_num;k++)
//	            	System.out.println(features_unselect[k]);

            	int[] features= new int[select_num+1];
            	int[] feature_temp = new int[11-select_num];
            	for(int j=0;j<select_num;j++)
            		features[j] = features_select[j]; //copy the selected feature
            	
	            for(int i=0;i<unselect_num;i++) {
	            	features[select_num] = features_unselect[i];	  //add a new feature   
	            	System.out.println("features=");
	            	for(int k=0;k<features.length;k++)
	            		System.out.println(features[k]);
	            	
		            String arffData = MyWekaUtils.csvToArff(csvData, features);
//		            System.out.println(arffData);
		            
		            double accuracy = MyWekaUtils.classify(arffData, 1);
//		            System.out.println(accuracy);
		            if(accuracy>best_accuracy) {
		            	best_accuracy = accuracy;
		            	best_index = i;
		            	best_feature = features_unselect[i];
		            } 
		            System.out.println("Accuracy="+accuracy);
	            }
	            if(best_accuracy>1.01*pre_best_accuracy) { //update only when improvement is larger than 1%
		            System.out.println("Best_feature="+best_feature);
		            System.out.println("Best_accuracy="+best_accuracy);
	            	System.out.println("Accuracy improvement="+100*(best_accuracy/pre_best_accuracy-1)+"%");
	            	pre_best_accuracy = best_accuracy;
		            features_select[select_num] = best_feature;
		            select_num++;	            
	            	
		            for(int k =0;k<best_index;k++)
		            	feature_temp[k] = features_unselect[k];
		            for(int k=best_index+1;k<unselect_num;k++)
		            	feature_temp[k-1] = features_unselect[k];		                  
		            features_unselect = feature_temp.clone(); //remove the selected feature	
		            
		            unselect_num--;
	            }
	            else {
		            System.out.println("Best_feature="+best_feature);
		            System.out.println("Best_accuracy="+best_accuracy);
	            	
	            	System.out.println("Stop iteration! Accuracy improvement="+100*(best_accuracy/pre_best_accuracy-1)+"%");
	            	break;
	            }          
            }
            
            System.out.println("Selected features=");
            for(int i=0;i<select_num;i++)
            	System.out.println(features_select[i]);
                        
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
