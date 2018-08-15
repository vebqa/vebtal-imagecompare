package org.vebqa.vebtal.icomp;

/**
 * 
 * TransportContainer fuer Testergebnisse aus einem ImageCompare Test. 
 * @author kdoerges
 */
public class ImageCompareResult {

	public static int CHECKOK = 0;
	public static int REFERENCENOTFOUND = 1;
	public static int CURRENTNOTFOUND = 2;
	public static int IMAGEDIMENSIONSDIFFER = 3;
	public static int IMAGESDIFFER = 4;
	
    String resultFile;

    String message;
    
    int resultType;

    int differences;

    public int getDifferences() {
        return differences;
    }

    public void setDifferences(int differences) {
        this.differences = differences;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ImageCompareResult() {

    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public void setResultType(int aType) {
    	resultType = aType;
    }
    
    public int getResultType() {
    	return resultType;
    }
}
