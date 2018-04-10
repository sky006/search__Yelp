/*
 *By: QC
 *Last modified: 3/16/2018 
 */
package fileProcess;

public class Main {
	public static void main(String args[]) {
//		SearchIndex.search("hamburgers");
		String opType=args[0];
		if(opType.equals("Preprocess")) {
			System.out.println("Preprocess");
			String dataPath=args[1];
			String foodWordsFile=args[2];
			Preprocess.process(dataPath,foodWordsFile);
			System.out.println("Preprocess Finished");
		}else if(opType.equals("seperateIndex")) {
			System.out.println("SeperateIndex");
			String indexPath=args[1];
			String outIndexPath=args[2];
			seperateIndex.seperate(indexPath,outIndexPath);
			System.out.println("SeperateIndex Finished");
		}else {
			System.out.println("Wrong arguments");
		}
	}
}
