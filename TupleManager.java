import it.units.project.expression.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The TupleManager class is responsible for building value tuples
 * for variables, based on two main modes: "GRID" and "LIST".
 *
 * In "GRID" mode, the Cartesian product of all possible variable values is constructed.
 * In "LIST" mode, the values of the variables are combined element by element.
 */
public class TupleManager {

    // Builds value tuples based on the type (GRID or LIST)
    public static List<Map<Variable, Double>> buildValueTuples(Map<String, double[]> variableMap, String valuesKind) throws Exception {
        List<Map<Variable, Double>> tuples = new ArrayList<>();
        List<String> variableNames = new ArrayList<>(variableMap.keySet());
        List<double[]> variableValues = new ArrayList<>(variableMap.values());

        if ("GRID".equalsIgnoreCase(valuesKind)) {
            // Builds the Cartesian product of the variables
            buildGridTuples(variableNames, variableValues, 0, new HashMap<>(), tuples);
        } else if ("LIST".equalsIgnoreCase(valuesKind)) {
            // Builds the union of variables element by element
            buildListTuples(variableNames, variableValues, tuples);
        } else {
            throw new Exception("Unsupported ValuesKind type: " + valuesKind);
        }

        return tuples;
    }

    // Building tuples using the Cartesian product
    private static void buildGridTuples(List<String> variableNames, List<double[]> variableValues, int index, Map<Variable, Double> currentTuple, List<Map<Variable, Double>> resultTuples) {
        if (index == variableNames.size()) {
            resultTuples.add(new HashMap<>(currentTuple));
            return;
        }

        // Get the variable name and its possible values
        String variableName = variableNames.get(index);
        double[] values = variableValues.get(index);
        Variable variable = new Variable(variableName);

        for (double value : values) {
            currentTuple.put(variable, value);
            // Recursively call the method to consider the next variable
            buildGridTuples(variableNames, variableValues, index + 1, currentTuple, resultTuples);
        }
    }

    // Building tuples element by element (LIST)
    private static void buildListTuples(List<String> variableNames, List<double[]> variableValues, List<Map<Variable, Double>> resultTuples) throws Exception {
        // Check that all variables have the same number of values
        int listLength = variableValues.get(0).length;
        for (double[] values : variableValues) {
            if (values.length != listLength) {
                throw new Exception("Lists do not have the same length");
            }
        }
        // Build the tuples for each element in the lists
        for (int i = 0; i < listLength; i++) {
            Map<Variable, Double> tuple = new HashMap<>();
            for (int j = 0; j < variableNames.size(); j++) {
                Variable variable = new Variable(variableNames.get(j));
                double[] values = variableValues.get(j);
                tuple.put(variable, values[i]);
            }
            resultTuples.add(tuple);
        }
    }
}
