package edu.jsu.mcis.cs425;

import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Main {
    
    public static final String CS_FILENAME = "cs_fa23.html";

    public static void main(String[] args) {
        
        Main m = new Main();
        
        /* Open and Read HTML Input File */
        
        String html = null;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + CS_FILENAME)));
            String line = reader.readLine().trim();
            
            StringBuilder s = new StringBuilder();
            
            do {
                s.append(line);
                line = reader.readLine();
            }
            while (line != null);
            
            html = s.toString();
            
        }
        catch (IOException e) { e.printStackTrace(); }

        System.out.println(m.getCourseListAsCSV(html));
        
    }

    public String getCourseListAsCSV(String html) {
        
        /* Open New CSVWriter (for CSV output); Create CSV Header Row */
        
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, ',', '"', '\\', "\r\n");
        csv.writeNext(new String[] { "crn", "subjectid", "num", "section", "scheduletype", "instructor", "start", "end", "days", "where" }, false);
        
        /* Parse HTML Search Results */
        
        Document document = Jsoup.parse(html);
        
        /* Select Main Search Results Table */
        
        Element resultstable = document.select("div.pagebodydiv > table.datadisplaytable").get(0);
        
        /* Print Number of Course Records Found (for diagnostic purposes) */
        
        System.err.println("Course(s) Found: " + (resultstable.select("th.ddtitle")).size());
        
        /* Process Courses */
        //ArrayList<HashMap> courseInfo

        Elements rows = resultstable.select("tr");
        
        for(Element row : rows){
            Elements rowheader = row.select("th.ddtitle");
            Elements rowdata = row.select("td.dddefault");
            
            if(!rowheader.isEmpty()){
                String[] courseInformation = rowheader.select("th.ddtitle > a").text().split(" - ");
                String crn = courseInformation[1];
                String[] courseIdNum = courseInformation[2].split(" ");
                String subjectId = courseIdNum[0];
                String courseNum = courseIdNum[1];
                String section = courseInformation[3];
                csv.writeNext(new String[] { crn, subjectId, courseNum, section,}, false);
            }
            else if(!rowdata.isEmpty()){
                for(Element singleton : rowdata){
                    String rawText = singleton.html();                    
                    if(rawText.contains(" Credit")){
                        int indexOfCredit = rawText.indexOf(" Credit");
                        int startIndex = Math.max(0, indexOfCredit - 5);
                        double creditHours = Double.parseDouble(rawText.substring(startIndex, indexOfCredit));
                       //System.err.println("Credit Hours: " + creditHours);
                    }
                }
                
                Elements childData = rowdata.select("tr > td.dddefault");
                
                for(int i = 0; i < childData.size(); ++i){
                    Element dataFormat = childData.get(i);
                    String rawText = dataFormat.html();
                    String Class = dataFormat.getElementsByIndexEquals(0).text();
                    String[] classTimes = dataFormat.getElementsByIndexEquals(1).text().split(" - ");
                    String startTime = classTimes[0];
                    //String endTime = classTimes[1];
                    String daysofWeek = dataFormat.getElementsByIndexEquals(2).text();
                    String classType = dataFormat.getElementsByIndexEquals(3).text();
                    String schedulety = dataFormat.getElementsByIndexEquals(5).text();
                    String instructor = dataFormat.getElementsByIndexEquals(6).text();
                    
                    //System.err.println("Index: " + i + ": " + rawText);
                    //System.err.println(instructor);
                }
            }  
        }

        /* Return Output Data as a CSV String */
        
        return writer.toString();
    }
}
        
 