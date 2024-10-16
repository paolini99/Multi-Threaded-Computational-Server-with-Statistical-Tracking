import it.units.project.expression.Constant;
import it.units.project.expression.Node;
import it.units.project.expression.Operator;
import it.units.project.expression.Variable;

import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * The ComputationEngine class is responsible for performing computations on mathematical expressions
 * based on variables and their values. It supports various operations such as calculating minimum, maximum,
 * average, and count.
 */
public class ComputationEngine {

    // Enum defining the available computation types
    public enum ComputationKind {
        MIN, MAX, AVG, COUNT
    }

    private final ComputationKind kind;
    private final List<Node> expressions;
    private final List<Map<Variable, Double>> tuples;

    // Constructor that accepts the computation type, expressions, and variable tuples
    public ComputationEngine(ComputationKind kind, List<Node> expressions, List<Map<Variable, Double>> tuples) {
        this.kind = kind;
        this.expressions = expressions;
        this.tuples = tuples;
    }

    // Main method that performs the computation based on the requested operation type
    public double compute() throws Exception {
        if (tuples.isEmpty()) {
            throw new IllegalArgumentException("The tuple list cannot be empty");
        }

        // Selects the operation to perform
        switch (kind) {
            case MIN:
                return computeMin();
            case MAX:
                return computeMax();
            case AVG:
                return computeAvg();
            case COUNT:
                return computeCount();
            default:
                throw new IllegalArgumentException("Unsupported computation type: " + kind);
        }
    }

    // Calculates the minimum value among the expressions
    private double computeMin() throws Exception {
        return computeValue(Math::min, Double.MAX_VALUE);
    }

    // Calculates the maximum value among the expressions
    private double computeMax() throws Exception {
        return computeValue(Math::max, -Double.MAX_VALUE);
    }

    // Calculates the average of the expressions
    private double computeAvg() throws Exception {
        double sum = computeSum(); // Sums the values
        return sum / tuples.size(); // Returns the average
    }

    // Counts the number of tuples (variable values)
    private double computeCount() {
        return tuples.size();
    }

    // Sums all the values of the expressions
    private double computeSum() throws Exception {
        return computeValue(Double::sum, 0.0); // Sums the values
    }

    // Generic function to calculate the value based on the specified operation (min, max, sum)
    private double computeValue(BinaryOperator<Double> operation, double initialValue) throws Exception {
        double result = initialValue;

        // Iterates over all tuples and all expressions
        for (Map<Variable, Double> tuple : tuples) {
            for (Node expression : expressions) {
                double value = evaluateExpression(expression, tuple); // Evaluates the expression
                result = operation.apply(result, value); // Applies the operation
            }
        }
        return result;
    }

    // Evaluates an expression based on variables and their values
    private double evaluateExpression(Node expression, Map<Variable, Double> variableValues) throws Exception {
        // If the expression is a constant, return the value
        if (expression instanceof Constant) {
            return ((Constant) expression).getValue();
        }
        // If it's a variable, return the associated value
        else if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            Double value = variableValues.get(variable);
            if (value == null) {
                throw new Exception("Variable not found: " + variable.getName());
            }
            return value;
        }
        // If it's an operator, evaluate the two operands and apply the operator
        else if (expression instanceof Operator) {
            Operator op = (Operator) expression;
            List<Node> children = op.getChildren();
            if (children.size() != 2) {
                throw new Exception("Operator must have two operands");
            }
            double leftValue = evaluateExpression(children.get(0), variableValues);
            double rightValue = evaluateExpression(children.get(1), variableValues);
            return op.getType().getFunction().apply(new double[]{leftValue, rightValue});
        }
        // If the node type is unsupported, throw an exception
        else {
            throw new Exception("Unsupported node type");
        }
    }
}
