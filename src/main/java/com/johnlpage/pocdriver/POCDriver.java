package com.johnlpage.pocdriver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.cli.ParseException;
import org.bson.BsonBinaryWriter;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

public class POCDriver {

    public static void main(String[] args) {

        POCTestOptions testOpts;
        LogManager.getLogManager().reset();
        Logger logger = LoggerFactory.getLogger(POCDriver.class);

        logger.info("MongoDB Proof Of Concept - Load Generator");
        try {
            testOpts = new POCTestOptions(args);
            // Quit after displaying help message
            if (testOpts.helpOnly) {
                return;
            }

            if (testOpts.arrayupdates > 0 && (testOpts.arraytop < 1 || testOpts.arraynext < 1)) {
                logger.error("You must specify an array size to update arrays");
                return;
            }
            if (testOpts.printOnly) {
                printTestDocument(testOpts);
                return;
            }

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return;
        }

        POCTestResults testResults = new POCTestResults(testOpts);
        LoadRunner runner = new LoadRunner(testOpts);
        runner.RunLoad(testOpts, testResults);
    }

    private static void printTestDocument(final POCTestOptions testOpts) {
        //Sets up sample data don't remove
        TestRecord tr;
        int[] arr = new int[2];
        arr[0] = testOpts.arraytop;
        arr[1] = testOpts.arraynext;
        tr = new TestRecord(testOpts.numFields, testOpts.depth, testOpts.textFieldLen,
                1, 12345678, testOpts.NUMBER_SIZE, arr, testOpts.blobSize, testOpts.locationCodes);
        //System.out.println(tr);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(tr.internalDoc.toJson());

        String json = gson.toJson(je);
        StringBuilder newJson = getStringBuilder(json);

        //This is actual output not logging 
        System.out.println(newJson);

        //Thanks to Ross Lawley for this bit of black magic
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonBinaryWriter binaryWriter = new BsonBinaryWriter(buffer);
        new DocumentCodec().encode(binaryWriter, tr.internalDoc, EncoderContext.builder().build());
        int length = binaryWriter.getBsonOutput().getSize();

        System.out.printf("Documents are %.2f KB each as BSON%n", (float) length / 1024);
    }

    private static StringBuilder getStringBuilder(String json) {
        StringBuilder newJson = new StringBuilder();
        int arrays = 0;

        // Collapse inner newlines
        boolean inquotes = false;
        for (int c = 0; c < json.length(); c++) {
            char inChar = json.charAt(c);
            if (inChar == '[') {
                arrays++;
            }
            if (inChar == ']') {
                arrays--;
            }
            if (inChar == '"') {
                inquotes = !inquotes;
            }

            if (arrays > 1 && inChar == '\n') {
                continue;
            }
            if (arrays > 1 && !inquotes && inChar == ' ') {
                continue;
            }
            newJson.append(json.charAt(c));
        }
        return newJson;
    }

}
