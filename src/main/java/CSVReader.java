import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private static final char DEFAULT_CSV_SEPARATOR = ',';

    public static List<List<String>> parseCsv(InputStream csvInput) {
        return parseCsv(csvInput, DEFAULT_CSV_SEPARATOR);
    }

    public static List<List<String>> parseCsv(InputStream csvInput, char csvSeparator) {

        // Prepare.
        BufferedReader csvReader = null;
        List<List<String>> csvList = new ArrayList<List<String>>();
        String csvRecord = null;

        // Process records.
        try {
            csvReader = new BufferedReader(new InputStreamReader(csvInput, "UTF-8"));
            while ((csvRecord = csvReader.readLine()) != null) {
                csvList.add(parseCsvRecord(csvRecord, csvSeparator));
            }
        } catch (IOException e) {
            throw new RuntimeException("Reading CSV failed.", e);
        } finally {
            if (csvReader != null)
                try {
                    csvReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return csvList;
    }

    private static List<String> parseCsvRecord(String record, char csvSeparator) {

        // Prepare.
        boolean quoted = false;
        StringBuilder fieldBuilder = new StringBuilder();
        List<String> fields = new ArrayList<String>();

        // Process fields.
        for (int i = 0; i < record.length(); i++) {
            char c = record.charAt(i);
            fieldBuilder.append(c);

            if (c == '"') {
                quoted = !quoted; // Detect nested quotes.
            }

            if ((!quoted && c == csvSeparator) // The separator ..
                    || i + 1 == record.length()) // .. or, the end of record.
            {
                String field = fieldBuilder.toString() // Obtain the field, ..
                        .replaceAll(csvSeparator + "$", "") // .. trim ending separator, ..
                        .replaceAll("^\"|\"$", "") // .. trim surrounding quotes, ..
                        .replace("\"\"", "\""); // .. and un-escape quotes.
                fields.add(field.trim()); // Add field to List.
                fieldBuilder = new StringBuilder(); // Reset.
            }
        }

        return fields;
    }
}
