package main.java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.util.Arrays;

public class JSONToXML {
    public static void main(String... s) {

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);


        //Print in JSON
//        try {
//            JsonNode jsonNode = mapper.readTree(new File("PersonalData_trimmed_all9s_3recs.json"));
//            ObjectNode object = (ObjectNode) jsonNode;
//            System.out.println(mapper.writeValueAsString(jsonNode));
//        } catch (Exception e) {
//            System.out.println(e);
//        }


        try {
            BufferedReader reader = new BufferedReader(new FileReader("PersonalData_trimmed_all9s_3recs.json"));
            String line = reader.readLine();
            File output = new File("xmlOutput.xml");
            //Empty file if it exists already
            PrintWriter pw = new PrintWriter(output);
            pw.close();

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output, true))); // append mode file writer
            //Read json objects line by line
            while(line != null){
                JsonNode tree = mapper.readTree(line);

                //Test 1: remove element at root level
                removeElement(tree,"", "MEMBER_ID");
                //Test 2: remove element at sub-root level
                removeElement(tree,"NAMES", "array");
                //Test 3: remove element at sub-root level and within an array
                removeElement(tree, "ADDRESSES/array/io.confluent.ksql.avro_schemas.KsqlDataSourceSchema_ADDRESSES",
                        "ADDRESS_TYPE");

                ObjectMapper xmlMapper = new XmlMapper();
                //Pretty Print
                xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String jsonAsXml = xmlMapper.writer().withRootName("Root").writeValueAsString(tree);
                //System.out.println(jsonAsXml);

                writer.print(jsonAsXml);
                line = reader.readLine();
            }
            //Close Reader and Writer
            reader.close();
            writer.close();
        } catch (Exception e){
            System.out.println(e);
        }

    }

    public static void removeElement(JsonNode root, String path, String fieldName){
        if(root.isArray()){

            for(JsonNode subnode: root)
                removeElement(subnode, path, fieldName);
            return;
        }

        ObjectNode object = (ObjectNode) root;

        if (!path.equals("")) {
            String[] levels = path.split("/");
            String level = levels[0];

            removeElement(object.get(level), String.join("/", Arrays.copyOfRange(levels, 1, levels.length)), fieldName);
            return;
        }
        object.remove(fieldName);
    }
}