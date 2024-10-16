import java.util.HashMap;
import java.util.Map;

/**
 * The VariableParser class handles converting a string of variables into a map of variables with their values.
 */
public class VariableParser {

    // Converts the string of variables into a map of variables with their values
    public static Map<String, double[]> parseVariableValues(String variableValuesFunction) throws Exception {
        String[] variableParts = variableValuesFunction.split(",");
        Map<String, double[]> variableMap = new HashMap<>();

        for (String part : variableParts) {
            String[] details = part.split(":");

            // Check that the format is correct (must have exactly 4 parts)
            if (details.length != 4) {
                throw new Exception("Invalid variable format: " + part);
            }

            String variableName = details[0];
            try {
                double start = Double.parseDouble(details[1]);
                double step = Double.parseDouble(details[2]);
                double end = Double.parseDouble(details[3]);

                // Ensure that step is greater than 0
                if (step <= 0) {
                    throw new Exception("Step must be greater than 0 for variable: " + variableName);
                }

                // Generate the sequence of values for the variable
                double[] values = generateValues(start, step, end);
                variableMap.put(variableName, values);

            } catch (NumberFormatException e) {
                throw new Exception("Invalid numeric value for variable: " + details[0]);
            }
        }

        return variableMap;
    }

    // Generates a sequence of values from start to end with the given step
    private static double[] generateValues(double start, double step, double end) {
        int size = (int) Math.ceil((end - start) / step) + 1;
        double[] values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = start + i * step;
        }
        return values;
    }
}
