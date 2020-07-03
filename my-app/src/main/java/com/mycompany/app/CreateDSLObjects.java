package com.mycompany.app;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// import com.mycompany.dsl.DSL;
import com.mycompany.dsl.FeatureExtractor;
import com.mycompany.dsl.ColumnBasedTable;
import com.mycompany.dsl.Column;
import com.mycompany.dsl.ColumnType;
import com.mycompany.dsl.ColumnData;
import com.mycompany.dsl.SemType;

/**
 * This class creates objects from csv file data.
 * @author rutujarane
 * 
*/

public class CreateDSLObjects {

    static Logger logger = LogManager.getLogger(CreateDSLObjects.class.getName());
    public static String[][] readFile(String fileName){
        List<String[]> rowList = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // logger.info("Line:"+line);
                String[] lineItems = line.split(",",-1);
                rowList.add(lineItems);
            }
            br.close();

            String[][] matrix = new String[rowList.size()][];
            for (int i = 0; i < rowList.size(); i++) {
                String[] row = rowList.get(i);
                matrix[i] = row;
            }
            return matrix;
        }
        catch(Exception e){
            // Handle any I/O problems
            logger.info("ERROR: File not readable");
        }
        String[][] matrix = new String[0][0];
        return matrix;
    }

    public static void deleteFile(String fileName){
        try
        { 
            Files.deleteIfExists(Paths.get(fileName)); 
        } 
        catch(NoSuchFileException e) 
        { 
            logger.info("No such file/directory exists"); 
        } 
        catch(DirectoryNotEmptyException e) 
        { 
            logger.info("Directory is not empty."); 
        } 
        catch(IOException e) 
        { 
            logger.info("Invalid permissions."); 
        }
        logger.info("Deletion successful."); 
    }

    public static FeatureExtractor create_feature_extractor(String files[]) {
        List<ColumnBasedTable> columnBasedTableObj = new ArrayList<ColumnBasedTable>();

        // int kk=0;
        for(String file: files){
            String [][] data = readFile(file);
            if(data.length == 0){
                logger.info("Warning: file not readable "+file);
                continue;
            }
            logger.info("Read the file"+file);
            columnBasedTableObj.add(findDatatype(data,file));
            // if(kk>=1)
            //     break;
        }
        return new FeatureExtractor(columnBasedTableObj);

    }

    public static ColumnBasedTable findDatatype(String[][] data, String tableName){
        logger.info("TabName:"+tableName);
        // for(int i=0; i<data[0].length; i++){
        //     System.out.print(data[1][i] + " ");
        // }
        List<Column> columns = new ArrayList<Column>();
        for(int index=0; index<data[0].length; index++){
            List<String> colData = getColumnData(data,index);
            SemType semTypeObj = findSemType(colData.get(1));
            Hashtable<String, Float> typeStats = new Hashtable<String, Float>();
            Column columnObj = new Column(tableName, colData.get(0), semTypeObj, colData.get(2), data.length, typeStats);
            List<String> colSubList = new ArrayList<String>(colData.subList(3,colData.size()));
            columnObj.value = new ColumnData(colSubList);
            columns.add(columnObj);
            logger.info("Column Object created");
        }
        ColumnBasedTable columnBasedTableObj = new ColumnBasedTable(tableName, columns);
        return columnBasedTableObj;
    }

    public static SemType findSemType(String colName){
        String col[] = colName.split("-");
        SemType semTypeObj = new SemType(col[0],col[col.length-1]);
        return semTypeObj;
    }

    public static List<String> getColumnData(String[][] data, int index){
        List<String> column = new ArrayList<String>();
        for(int i=0; i<data.length; i++){
            column.add(data[i][index]);
        }
        return column;
    }
}