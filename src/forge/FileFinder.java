package forge;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFinder {
       
    private Pattern p = null;
    private Matcher m = null;
    private long totalLength = 0;
    private int filesNumber = 0;
    private long directoriesNumber = 0;
    private final int FILES = 0;
    private final int DIRECTORIES = 1;
    private ArrayList<String> fileNames;
    private ArrayList<String> fName;
   
   
    public FileFinder() {
    }
    
    public List<File> findFiles(String startPath, String mask)
            throws Exception {
    	fileNames = new ArrayList<String>();
    	fName = new ArrayList<String>();
        return findWithFull(startPath, mask, FILES);
    }


    public long getDirectorySize() {
        return totalLength;
    }

    public int getFilesNumber() {
        return filesNumber;
    }

    public long getDirectoriesNumber() {
        return directoriesNumber;
    }

    private boolean accept(String name) {
       
        if(p == null) {
          
            return true;
        }
      
        m = p.matcher(name);
        
        if(m.matches()) {
            return true;
        }
        else {
            return false;
        }
    }
    
   
  
   
    private List<File> findWithFull(String startPath, String mask, int objectType)    throws Exception {
		
		if(startPath == null || mask == null) {
		    throw new Exception("Error");
		}
		File topDirectory = new File(startPath);
		if(!topDirectory.exists()) {
		    throw new Exception("Error");
		}
		
		if(!mask.equals("")) {
		    p = Pattern.compile(mask,
		            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		}
		filesNumber = 0;
		directoriesNumber = 0;
		totalLength = 0;
		ArrayList<File> res = new ArrayList<File>(100);
		
		searchWithFull(topDirectory, res, objectType);
		p = null;        
		return res;
		}
 
    
private void searchWithFull(File topDirectory, List<File> res, int objectType) {
        
        File[] list = topDirectory.listFiles();            
        
        for(int i = 0; i < list.length; i++) {
           
            if(list[i].isDirectory()) {
             
                if(objectType != FILES && accept(list[i].getName())) {
                   
                    directoriesNumber++;
                    res.add(list[i]);
                }
                
                searchWithFull(list[i], res, objectType);
            }
            
            else {
               
                if(objectType != DIRECTORIES && accept(list[i].getName())) {
                	if(list[i].getName().contains("full")){
                	if(fileNames.size()==0){	
                		fileNames.add(list[i].getName());                		
                		filesNumber++;
                        totalLength += list[i].length();
                        res.add(list[i]);
                	}
                	fName.add(list[i].getName());
                	if(fileNames.size()>=1){                	 
                		if(Collections.indexOfSubList(fileNames, fName)==-1){ 
                			fileNames.add(list[i].getName());
                			filesNumber++;
                            totalLength += list[i].length();
                            res.add(list[i]);
                		}
                		fName.remove(0);
                }}}
            }
        }
    }
    
}
