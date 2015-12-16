import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author cmcammarano
 */
public class SchemeEnvironment {
	
	private static ArrayList<HashMap<SchemeSymbol, SchemeObject>> environmentFrames = new ArrayList<HashMap<SchemeSymbol, SchemeObject>>();
	private static int currentDepth = 0;
	
	/**********************************************************************************/
	// ENVIRONMENT TABLE FUNCTIONS
	/**********************************************************************************/
	private static boolean isSymbolDefinition(SchemeSymbol symbol) {
		return symbol.toString().equals("define");
	}
	
	private static void increaseDepth() {
		environmentFrames.add(new HashMap<SchemeSymbol, SchemeObject>());
		currentDepth++;
	}
	
	private static void decreaseDepth() {
		environmentFrames.remove(currentDepth);
		currentDepth--;
	}
	
	private static void defineSchemeSymbol(SchemeObject object) throws RuntimeException {
		if (currentDepth < environmentFrames.size()) {
			HashMap<SchemeSymbol, SchemeObject> frame = environmentFrames.get(currentDepth);
			SchemeSymbol symbol = SchemeSymbol.getSymbol(object.car().toString());
			
			SchemeObject obj;
			if (object.cdr().car() instanceof SchemeSymbol) {
				
				obj = checkSymbols(object.cdr());
			}

			else if (object.cdr().car() instanceof SchemePair) {
				if (isLambdaExpression((SchemeSymbol)object.cdr().car().car())) {
					obj = object.cdr().car().cdr();
				}
				
				else {
					obj = checkSymbols(object.cdr().car());
				}
			}

			else {
				obj = object.cadr();
			}
			
			if (object.cdr().cdr() instanceof SchemeNull) {
				if (!doesVariableExistAtCurrentLevel(symbol)) {
					frame.put(symbol, obj);
				}
				
				else {
					throw new RuntimeException(symbol.toString() + ": Already defined.");
				}
			}
			else {
				throw new RuntimeException("define: expected only one expression after the variable name " + symbol.toString() + ", but found 1 extra part");
			}
		}
		
		else {
			HashMap<SchemeSymbol, SchemeObject> frame = new HashMap<SchemeSymbol, SchemeObject>();
			SchemeSymbol symbol = (SchemeSymbol)object.car();
			
			SchemeObject obj;
			if (object.cdr().car() instanceof SchemeSymbol) {
				obj = checkSymbols(object.cdr());
			}

			else if (object.cdr().car() instanceof SchemePair) {
				if (isLambdaExpression((SchemeSymbol)object.cdr().car().car())) {
					obj = object.cdr().car().cdr();
				}
				
				else {
					obj = checkSymbols(object.cdr().car());
				}
			}

			else {
				obj = object.cadr();
			}
			
			if (object.cdr().cdr() instanceof SchemeNull) {
				frame.put(symbol, obj);
				environmentFrames.add(frame);
			}
			else {
				System.err.println("define: expected only one expression after the variable name " + symbol.toString() + ", but found 1 extra part");
			}
		}
	}
	
	private static boolean doesVariableExist(SchemeSymbol symbol) {
		for (int i = 0; i < environmentFrames.size(); i++) {
			if (environmentFrames.get(i).containsKey(symbol)) {
				return true;	
			}
		}
		return false;
	}
	
	private static boolean doesVariableExistAtCurrentLevel(SchemeSymbol symbol) {
		if (currentDepth < environmentFrames.size()) {
			return environmentFrames.get(currentDepth).containsKey(symbol);
		}
		return false;
	}
	
	private static SchemeObject accessSchemeSymbol(SchemeSymbol symbol) {
		SchemeObject value = null;
		for (int i = 0; i < environmentFrames.size(); i++) {
			if (environmentFrames.get(i).containsKey(symbol)) {
				value = environmentFrames.get(i).get(symbol);
			}
		}
		return value;
	}
	
	/**********************************************************************************/
	// FUNCTIONS
	/**********************************************************************************/
	private static boolean isSymbolRedefinition(SchemeSymbol symbol) {
		return symbol.toString().equals("set!");
	}
	
	private static void setOperation(SchemeObject object) throws RuntimeException {
		SchemeSymbol symbol = SchemeSymbol.getSymbol(object.car().toString());
		if (doesVariableExist(symbol)) {
			HashMap<SchemeSymbol, SchemeObject> frame = environmentFrames.get(currentDepth);
			
			SchemeObject obj;
			if (object.cdr().car() instanceof SchemeSymbol) {
				obj = checkSymbols(object.cdr());
			}

			else if (object.cdr().car() instanceof SchemePair) {
				obj = checkSymbols(object.cdr().car());
			}

			else {
				obj = object.cadr();
			}
			
			if (object.cdr().cdr() instanceof SchemeNull) {
				frame.put(symbol, obj);
			}
			
			else {
				throw new RuntimeException("define: expected only one expression after the variable name " + symbol.toString() + ", but found 1 extra part");
			}
		}
		
		else {
			throw new RuntimeException(symbol.toString() + ": Is not defined.");
		}
	}
	
	/**********************************************************************************/
	// LAMBDA EXPRESSIONS
	/**********************************************************************************/
	private static boolean isLambdaExpression(SchemeSymbol symbol) {
		return symbol.toString().equals("lambda");
	}
	
	private static boolean doesLambdaExpressionExist(SchemeSymbol symbol) {
		if (environmentFrames.size() > 0) {
			if (environmentFrames.get(0).containsKey(symbol)) {
				return true;
			}
		}
	
		return false;
	}
	
	private static SchemeObject computeLambdaExpression(SchemeObject expression, SchemeObject paramSymbols, SchemeObject paramVals) {
		
		// Map new variables to the table -- increase stack depth
		increaseDepth();
		
		// Map new parameters 
		SchemeObject currentParam = paramSymbols;
		SchemeObject currentValue = paramVals;
		while (!(currentParam instanceof SchemeNull)) {
			SchemeObject actualValue = currentValue.car();
			if (actualValue instanceof SchemeSymbol) {
				actualValue = accessSchemeSymbol((SchemeSymbol)actualValue);
			}
			
			else if (actualValue instanceof SchemePair) {
				actualValue = checkSymbols(actualValue);
			}
			
			SchemePair parameterPair = new SchemePair(currentParam.car(), new SchemePair(actualValue, SchemeNull.getNull()));
			defineSchemeSymbol(parameterPair);
			currentParam = currentParam.cdr();
			currentValue = currentValue.cdr();
		}
		
		SchemeObject result;
		if (expression instanceof SchemePair) {
			result = checkSymbols(expression);
		}
		
		else {
			if (expression instanceof SchemeSymbol) {
				result = checkSymbols(new SchemePair(expression, SchemeNull.getNull()));
			}
			
			else {
				result = expression;
			}
		}
		decreaseDepth();
		return result;
	}
	
	/**********************************************************************************/
	// Quote Function
	/**********************************************************************************/
	private static boolean isQuoteExpression(SchemeSymbol symbol) {
		return symbol.toString().equals("quote") || symbol.toString().equals("'");
	}
	
	private static SchemeObject quoteOperation(SchemeObject object) {
		SchemeObject data = object.cdr().car();
		if (data instanceof SchemeString) {
			if (object.cdr().cdr() instanceof SchemeNull) {
				return new SchemeString(data.toString());
			}
			
			else {
				throw new RuntimeException("Expected only one expression after quote, but found 1 extra part");
			}
		}
		
		else if (data instanceof SchemeNumber) {
			if (object.cdr().cdr() instanceof SchemeNull) {
				return data;
			}
			
			else {
				throw new RuntimeException("Expected only one expression after quote, but found 1 extra part");
			}
		}
		
		else if (data instanceof SchemePair) {
			return new SchemePair (SchemeSymbol.getSymbol("list"), new SchemePair(new SchemeString("'" + data.car().toString()), data.cdr()));
		}
		
		else {
			if (object.cdr().cdr() instanceof SchemeNull) {
				return new SchemeString("'" + data.toString());
			}
			
			else {
				throw new RuntimeException("Expected only one expression after quote, but found 1 extra part");
			}
		}
	}
	
	/**********************************************************************************/
	// LET FUNCTIONS
	/**********************************************************************************/
	private static boolean isLetExpression(SchemeSymbol symbol) {
		return symbol.toString().equals("let");
	}
	
	private static SchemeObject computeLetExpression(SchemeObject object) {
		SchemeObject params = object.cdr().car();
		SchemeObject expression = object.cdr().cdr();
		
		// Increase the depth of our stack
		increaseDepth();
		
		// Iterate through parameters
		SchemeObject currentParam = params;
		while (!(currentParam instanceof SchemeNull)) {
			SchemeObject actualParam = currentParam.car();
			SchemeObject actualSymbol = currentParam.car().car();
			SchemeObject actualValue = currentParam.car().cdr();
			SchemePair finalParam;
			
			if (actualValue.car() instanceof SchemeSymbol) {
				actualValue = accessSchemeSymbol((SchemeSymbol)actualValue.car());
				finalParam = new SchemePair (actualSymbol, new SchemePair(actualValue, SchemeNull.getNull()));
			}
			
			else if (actualValue.car() instanceof SchemePair) {
				actualValue = checkSymbols(actualValue.car());
				finalParam = new SchemePair (actualSymbol, new SchemePair(actualValue, SchemeNull.getNull()));
			}
			
			else {
				finalParam = new SchemePair(actualSymbol, actualValue);
			}
			
			defineSchemeSymbol(finalParam);
			currentParam = currentParam.cdr();
		}
		
		SchemeObject result = checkSymbols(expression.car());
		decreaseDepth();
		
		return result;
	}
	
	/**********************************************************************************/
	// CONDITIONAL EXPRESSIONS
	/**********************************************************************************/
	private static boolean isIfStatement(SchemeSymbol symbol) {
		return symbol.toString().equals("if");
	}
	
	private static SchemeObject evaluateIfStatement(SchemeObject object) {
		try {
			SchemeObject condition = object.cdr().car();
			SchemeObject trueStatement = object.cdr().cdr().car();
			SchemeObject falseStatement = object.cdr().cdr().cdr().car();
			
			// If more than two statements are entered, throw an exception!
			try {
				SchemeObject extraStatements = object.cdr().cdr().cdr().cdr().car();
				throw new RuntimeException("Expected a question and two answers! Received more!");
			} catch (ClassCastException e) {}
			
			SchemeObject result = null;
			SchemeObject eval;
			if (condition instanceof SchemePair) {
				eval = checkSymbols(condition);
			}

			else if (condition instanceof SchemeBoolean) {
				eval = condition;
			}

			else {
				throw new RuntimeException("if: question result is not true or false.");
			}

			if (eval instanceof SchemeBoolean) {
				if (((SchemeBoolean)eval).getValue()) {
					if (trueStatement instanceof SchemePair) {
						result = checkSymbols(trueStatement);
					}

					else {
						result = trueStatement;
					}
				}

				else {
					if (falseStatement instanceof SchemePair) {
						result = checkSymbols(falseStatement);
					}

					else {
						result = falseStatement;
					}
				}
			}

			else {
				throw new RuntimeException("if: question result is not true or false.");
			}
			return result;
		} catch (ClassCastException e) {
			throw new RuntimeException("Expected a question and two answers! Received one!");
		}
	}
	
	private static boolean isConditionalStatement(SchemeSymbol symbol) {
		return symbol.toString().equals("cond");
	}
	
	private static SchemeObject evaluateConditionalStatement(SchemeObject object) {
		SchemeObject conditionList = object.cdr();
		SchemeObject result = null;
		while (!(conditionList instanceof SchemeNull)) {
			SchemeObject statement = conditionList.car();
			SchemeObject condition = statement.car();
			SchemeObject expression = statement.cdr();
			if (condition instanceof SchemeBoolean) {
				if (((SchemeBoolean)condition).getValue()) {
					if (expression.car() instanceof SchemePair || expression.car() instanceof SchemeSymbol) {
						return checkSymbols(expression.car());
					}
					
					else {
						try {
							expression.cdr().car();
							throw new RuntimeException("Only one return value permitted per conditional statement!");
						} catch (ClassCastException e) {
							return expression.car();
						}
					}
				}
			}
			
			else if (condition instanceof SchemePair) {
				SchemeObject evaluatedCondition = checkSymbols (condition);
				if (evaluatedCondition instanceof SchemeBoolean) {
					if (((SchemeBoolean)evaluatedCondition).getValue()) {
						if (expression.car() instanceof SchemePair || expression.car() instanceof SchemeSymbol) {
							return checkSymbols(expression);
						}

						else {
							try {
								expression.cdr().car();
								throw new RuntimeException("Only one return value permitted per conditional statement!");
							} catch (ClassCastException e) {
								return expression.car();
							}
						}
					}
				}
				
				else {
					throw new RuntimeException("Invalid conditional statement: " + condition.toString());
				}
			}
			
			else {
				throw new RuntimeException("Invalid conditional statement: " + condition.toString());
			}
			conditionList = conditionList.cdr();
		}
		
		throw new RuntimeException("Invalid conditional statement!");
	}
	
	/**********************************************************************************/
	// ARITHMETIC FUNCTIONS
	/**********************************************************************************/
	private static boolean isArithmeticFunction(SchemeSymbol symbol) {
		return symbol.toString().equals("+") || symbol.toString().equals("-") || symbol.toString().equals("*");
	}
	
	private static SchemeObject computeArithmeticOperation(SchemeObject object) throws RuntimeException {
		SchemeObject computedValue;
		if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("+")) {
			if (object.cdr() instanceof SchemeNull) {
				throw new RuntimeException ("Invalid input detected! Expected at least 1 operand!");
			}
			
			else {
				computedValue = computeAdditionOperation(object.cdr());
			}
		}
		
		else if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("-")) {
			computedValue = computeSubtractionOperation(object.cdr());
		}
		
		else {
			if (object.cdr() instanceof SchemeNull) {
				throw new RuntimeException ("Invalid input detected! Expected at least 1 operand!");
			}
			
			else if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("*")) {
				computedValue = computeMultiplicationOperation(object.cdr());
			}
			
			else {
				throw new RuntimeException("Incorrect arithmetic operation.");
			}
		}
		
		return computedValue;
	}
	
	private static SchemeObject computeAdditionOperation (SchemeObject object) throws RuntimeException {
		SchemeNumber finalResult;	
		// If the current object doesn't exist, then we return 0, that's the base case.
		if (object instanceof SchemeNull) {
			return new SchemeInteger(0);
		}
		
		if (object.car() instanceof SchemeSymbol) {
			if (doesVariableExist(SchemeSymbol.getSymbol(object.car().toString()))) {
				boolean integer = false;
				SchemeNumber head = (SchemeNumber)accessSchemeSymbol(SchemeSymbol.getSymbol(object.car().toString()));
				SchemeNumber tail;
				if (!(object.cdr() instanceof SchemeNull)) {
					tail = (SchemeNumber)computeAdditionOperation(object.cdr());
				}
				
				else {
					tail = new SchemeInteger(0);
				}
				
				if (head instanceof SchemeInteger && tail instanceof SchemeInteger) {
					double total = head.doubleValue() + tail.doubleValue();
					return new SchemeInteger((int)total);
				}
				
				double total = head.doubleValue() + tail.doubleValue();
				return new SchemeDouble(total);
			}
			
			else {
				throw new RuntimeException("Undefined variable detected!");
			}
		}
		
		// Nested functions? HAH!
		if (object.car() instanceof SchemePair) {
			SchemeObject head = checkSymbols(object.car());
			SchemeObject tail = computeAdditionOperation(object.cdr());
			if (head instanceof SchemeNumber) {
				double total = ((SchemeNumber)head).doubleValue() + ((SchemeNumber)tail).doubleValue();
				if (head instanceof SchemeInteger && tail instanceof SchemeInteger) {
					return new SchemeInteger((int)total);
				}

				else {
					return new SchemeDouble(total);
				}
			}
			
			else {
				throw new RuntimeException("Invalid operation detected!");
			}
		}
		
		// If we ever encounter a double, we need to set the type to be a double.
		else if (object.car() instanceof SchemeDouble) {
			double total = ((SchemeDouble)object.car()).d;
			total += ((SchemeNumber)(computeAdditionOperation(object.cdr()))).doubleValue();
			finalResult = new SchemeDouble(total);
		}
		
		// If the entire operation is an integer
		else {
			SchemeNumber intermediate = (SchemeNumber)computeAdditionOperation(object.cdr());
			if (intermediate instanceof SchemeInteger) {
				int total = ((SchemeInteger)object.car()).i.intValue() + ((SchemeInteger)intermediate).i.intValue();
				finalResult = new SchemeInteger(total);
			}

			else {
				double total = ((SchemeNumber)object.car()).doubleValue() + intermediate.doubleValue();
				finalResult = new SchemeDouble(total);
			}
		}
			
		return finalResult;
	}
	
	private static SchemeObject computeSubtractionOperation (SchemeObject object) throws RuntimeException {
		SchemeNumber finalResult;
		if (object instanceof SchemeNull) {
			throw new RuntimeException("Not a valid input! Expected at least 1 operand!");
		}
		
		if (object.car() instanceof SchemeSymbol) {
			if (doesVariableExist(SchemeSymbol.getSymbol(object.car().toString()))) {
				boolean integer = false;
				SchemeNumber head = (SchemeNumber)accessSchemeSymbol(SchemeSymbol.getSymbol(object.car().toString()));
				SchemeNumber tail;
				if (!(object.cdr() instanceof SchemeNull)) {
					tail = (SchemeNumber)computeSubtractionOperation(object.cdr());
				}
				
				else {
					tail = new SchemeInteger(0);
				}
				
				if (head instanceof SchemeInteger && tail instanceof SchemeInteger) {
					double total = head.doubleValue() - tail.doubleValue();
					return new SchemeInteger((int)total);
				}
				
				double total = head.doubleValue() - tail.doubleValue();
				return new SchemeDouble(total);
			}
			
			else {
				throw new RuntimeException("Undefined variable detected!");
			}
		}
		
		// Nested functions? HAH!
		if (object.car() instanceof SchemePair) {
			SchemeObject head = checkSymbols(object.car());
			SchemeObject tail = computeAdditionOperation(object.cdr());
			if (head instanceof SchemeNumber) {
				double total = ((SchemeNumber)head).doubleValue() - ((SchemeNumber)tail).doubleValue();
				if (head instanceof SchemeInteger && tail instanceof SchemeInteger) {
					return new SchemeInteger((int)total);
				}

				else {
					return new SchemeDouble(total);
				}
			}
			
			else {
				throw new RuntimeException("Invalid operation detected!");
			}
		}
		
		// If the second object is null, we just return the original value.
		if (object.cdr() instanceof SchemeNull) {
			
			if (object.car() instanceof SchemeNumber) {
				finalResult = (SchemeNumber)object.car();
				return finalResult;
			}

			else {
				throw new RuntimeException("Not a valid input!");
			}
		}
		
		// If our original value is a scheme double.
		if (object.car() instanceof SchemeDouble) {
			double total = ((SchemeDouble)object.car()).d;
			SchemeObject tail = object.cdr();
			
			while (!(tail instanceof SchemeNull)) {
				if (tail.car() instanceof SchemeNumber) {
					total -= ((SchemeDouble)tail.car()).doubleValue();
				}

				else if (tail.car() instanceof SchemePair) {
					try {
						SchemeNumber val = (SchemeNumber)checkSymbols(tail.car());
						total -= val.doubleValue();
					} catch (ClassCastException e) {
						throw new RuntimeException ("Invalid input detected!");
					}
				}
				
				else if (tail.car() instanceof SchemeSymbol) {
					if (doesVariableExist(SchemeSymbol.getSymbol(tail.car().toString()))) {
						SchemeNumber value = (SchemeNumber)accessSchemeSymbol(SchemeSymbol.getSymbol(tail.car().toString()));
						total -= value.doubleValue();
					}
				}
				
				else {
					throw new RuntimeException("Not a valid input!");
				}
				
				tail = tail.cdr();
			}
			
			finalResult = new SchemeDouble(total);
		}
		
		// If the entire operation is an integer
		else {
			boolean isInteger = true;
			double total = ((SchemeNumber)object.car()).doubleValue();
			SchemeObject tail = object.cdr();
			
			while (!(tail instanceof SchemeNull)) {
				if (tail.car() instanceof SchemeNumber) {
					total -= ((SchemeNumber)tail.car()).doubleValue();
				}
				
				else if (tail.car() instanceof SchemePair) {
					try {
						SchemeNumber val = (SchemeNumber)checkSymbols(tail.car());
						total -= val.doubleValue();
					} catch (ClassCastException e) {
						throw new RuntimeException("Invalid input detected!");
					}
				}
				
				else if (tail.car() instanceof SchemeSymbol) {
					if (doesVariableExist(SchemeSymbol.getSymbol(tail.car().toString()))) {
						SchemeNumber value = (SchemeNumber)accessSchemeSymbol(SchemeSymbol.getSymbol(tail.car().toString()));
						
						if (value instanceof SchemeDouble) {
							isInteger = false;
						}
						total -= value.doubleValue();
					}
				}

				else {
					throw new RuntimeException("Not a valid input!");
				}
				
				if (tail.car() instanceof SchemeDouble) {
					isInteger = false;
				}
				
				tail = tail.cdr();
			}
			
			if (isInteger) {
				finalResult = new SchemeInteger((int)total);
			}
			
			else {
				finalResult = new SchemeDouble((int)total);
			}
		}
		
		return finalResult;
	}
	
	private static SchemeObject computeMultiplicationOperation (SchemeObject object) {
		SchemeNumber finalResult;
		
		// If the current object doesn't exist, then we return 1, that's the base case.
		if (object instanceof SchemeNull) {
			return new SchemeInteger(1);
		}
		
		if (object.car() instanceof SchemeSymbol) {
			if (doesVariableExist(SchemeSymbol.getSymbol(object.car().toString()))) {
				boolean integer = false;
				SchemeNumber head = (SchemeNumber)accessSchemeSymbol(SchemeSymbol.getSymbol(object.car().toString()));
				SchemeNumber tail;
				if (!(object.cdr() instanceof SchemeNull)) {
					tail = (SchemeNumber)computeMultiplicationOperation(object.cdr());
				}
				
				else {
					tail = new SchemeInteger(1);
				}
				
				if (head instanceof SchemeInteger && tail instanceof SchemeInteger) {
					double total = head.doubleValue() * tail.doubleValue();
					return new SchemeInteger((int)total);
				}
				
				double total = head.doubleValue() * tail.doubleValue();
				return new SchemeDouble(total);
			}
			
			else {
				throw new RuntimeException("Undefined variable detected!");
			}
		}
		
		// Nested functions? HAH! Ken Chang is a racist.
		if (object.car() instanceof SchemePair) {
			SchemeObject head = checkSymbols(object.car());
			SchemeObject tail = computeAdditionOperation(object.cdr());
			if (head instanceof SchemeNumber) {
				double total = ((SchemeNumber)head).doubleValue() * ((SchemeNumber)tail).doubleValue();
				if (head instanceof SchemeInteger && tail instanceof SchemeInteger) {
					return new SchemeInteger((int)total);
				}

				else {
					return new SchemeDouble(total);
				}
			}
			
			else {
				throw new RuntimeException("Invalid operation detected!");
			}
		}
		
		// If we ever encounter a double, we need to set the type to be a double.
		else if (object.car() instanceof SchemeDouble) {
			double total = ((SchemeDouble)object.car()).d;
			total *= ((SchemeNumber)(computeMultiplicationOperation(object.cdr()))).doubleValue();
			finalResult = new SchemeDouble(total);
		}
		
		// If the entire operation is an integer
		else {
			SchemeNumber intermediate = (SchemeNumber)computeMultiplicationOperation(object.cdr());
			
			if (intermediate instanceof SchemeInteger) {
				int total = ((SchemeInteger)object.car()).i.intValue() * ((SchemeInteger)intermediate).i.intValue();
				finalResult = new SchemeInteger(total);
			}
			
			else {
				double total = ((SchemeNumber)object.car()).doubleValue() * intermediate.doubleValue();
				finalResult = new SchemeDouble(total);
			}
		}
		
		return finalResult;
	}
	
	/**********************************************************************************/
	// EQUALITY FUNCTIONS
	/**********************************************************************************/
	private static boolean isEqualityOperation (SchemeSymbol symbol) {
		return symbol.toString().equals("<") || symbol.toString().equals("equal?") || symbol.toString().equals("=");
	}
	
	private static SchemeObject computeEqualityOperation (SchemeObject object) {
		SchemeObject result;
		if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("<")) {
			result = computeLessThanOperation(object.cdr());
			return result;
		}
		
		else {
			result = computeEqualToOperation(object.cdr());
			return result;
		}
	}
	
	private static SchemeObject computeLessThanOperation (SchemeObject object) throws RuntimeException {
		boolean result = true;
		if (object instanceof SchemeNull) {
			throw new RuntimeException("Not a valid input!");
		}
		
		if (object.cdr() instanceof SchemeNull) {
			throw new RuntimeException("Not a valid input!");
		}
		
		// If our original value is a scheme double.
		SchemeObject prev = object;
		SchemeObject current = object.car();
		SchemeObject next = object.cdr();
		while (!(next instanceof SchemeNull) && result) {
			double currentVal = 0;
			double nextVal = 0;
			
			if (current instanceof SchemePair) {
				SchemeNumber temp = (SchemeNumber)computeArithmeticOperation(current);
				currentVal = temp.doubleValue();
			}
			
			else if (current instanceof SchemeSymbol) {
				SchemeObject temp = checkSymbols(prev);
				if (temp instanceof SchemeNumber) {
					currentVal = ((SchemeNumber)temp).doubleValue();
				}
				
				else {
					throw new RuntimeException("Not a valid input!");
				}
			}
			
			else if (current instanceof SchemeNumber) {
				currentVal = ((SchemeNumber)current).doubleValue();
			}
			
			else {
				throw new RuntimeException("Not a valid input!");
			}

			if (next.car() instanceof SchemePair) {
				SchemeNumber temp = (SchemeNumber)computeArithmeticOperation(next.car());
				nextVal = temp.doubleValue();
			}
			
			else if (next.car() instanceof SchemeNumber) {
				nextVal = ((SchemeNumber)next.car()).doubleValue();
			}
			
			else if (next.car() instanceof SchemeSymbol) {
				SchemeObject temp = checkSymbols(next);
				if (temp instanceof SchemeNumber) {
					nextVal = ((SchemeNumber)temp).doubleValue();
				}
				
				else {
					throw new RuntimeException("Not a valid input!");
				}
			}
			
			else {
				throw new RuntimeException("Not a valid input!");
			}

			result = currentVal < nextVal;
			if (!result) {
				break;
			}
			prev = next;
			current = next.car();
			next = next.cdr();
		}
		
		if (result) {
			return SchemeBoolean.TRUE;
		}
		
		else {
			return SchemeBoolean.FALSE;
		}
	}
	
	private static SchemeObject computeEqualToOperation (SchemeObject object) throws RuntimeException {
		boolean result = true;
		if (object instanceof SchemeNull) {
			throw new RuntimeException("Not a valid input! Expected at least 2 operands!");
		}
		
		if (object.cdr() instanceof SchemeNull) {
			throw new RuntimeException("Not a valid input! Expected at least 2 operands!");
		}
		
		// If our original value is a scheme double.
		try {
			SchemeObject prev = object;
			SchemeObject current = object.car();
			SchemeObject next = object.cdr();
			if (current instanceof SchemePair && !current.car().toString().equals("list")) {
				current = checkSymbols(current);
			}
			
			if (current instanceof SchemeNumber || current instanceof SchemeSymbol) {
				while (!(next instanceof SchemeNull) && result) {
					double currentVal = 0;
					double nextVal = 0;

					if (current instanceof SchemePair) {
						SchemeNumber temp = (SchemeNumber)computeArithmeticOperation(current);
						currentVal = temp.doubleValue();
					}

					else if (current instanceof SchemeSymbol) {
						SchemeObject temp = checkSymbols(prev);
						if (temp instanceof SchemeNumber) {
							currentVal = ((SchemeNumber)temp).doubleValue();
						}

						else if (temp instanceof SchemeString) {
							result = false;
							break;
						}

						else {
							throw new RuntimeException("Not a valid input!");
						}
					}

					else if (current instanceof SchemeNumber) {
						currentVal = ((SchemeNumber)current).doubleValue();
					}

					else if (current instanceof SchemeString) {
						result = false;
						break;
					}

					else {
						throw new RuntimeException("Not a valid input!");
					}

					if (next.car() instanceof SchemePair) {
						SchemeNumber temp = (SchemeNumber)computeArithmeticOperation(next.car());
						nextVal = temp.doubleValue();
					}

					else if (next.car() instanceof SchemeNumber) {
						nextVal = ((SchemeNumber)next.car()).doubleValue();
					}

					else if (next.car() instanceof SchemeString) {
						result = false;
						break;
					}

					else if (next.car() instanceof SchemeSymbol) {
						SchemeObject temp = checkSymbols(next);
						if (temp instanceof SchemeNumber) {
							nextVal = ((SchemeNumber)temp).doubleValue();
						}

						else {
							throw new RuntimeException("Not a valid input!");
						}
					}

					else {
						throw new RuntimeException("Not a valid input!");
					}

					result = currentVal == nextVal;
					if (!result) {
						break;
					}
					prev = next;
					current = next.car();
					next = next.cdr();
				}
			}

			else if (current instanceof SchemeString  || current instanceof SchemeSymbol) {
				while (!(next instanceof SchemeNull) && result) {
					String currentVal = "";
					String nextVal = "";

					if (current instanceof SchemeSymbol) {
						SchemeObject temp = checkSymbols(prev);
						if (temp instanceof SchemeNumber) {
							result = false;
							break;
						}

						else if (temp instanceof SchemeString) {
							currentVal = ((SchemeString)temp).value;
						}

						else {
							throw new RuntimeException("Not a valid input!");
						}
					}

					else if (current instanceof SchemeNumber) {
						result = false;
						break;
					}

					else if (current instanceof SchemeString) {
						currentVal = ((SchemeString)current).value;
					}

					else {
						throw new RuntimeException("Not a valid input!");
					}

					if (next.car() instanceof SchemeNumber) {
						result = false;
						break;
					}

					else if (next.car() instanceof SchemeString) {
						nextVal = ((SchemeString)next.car()).value;
					}

					else if (next.car() instanceof SchemeSymbol) {
						SchemeObject temp = checkSymbols(next);
						if (temp instanceof SchemeNumber) {
							result = false;
							break;
						}

						else if (temp instanceof SchemeString) {
							nextVal = ((SchemeString)temp).value;
						}

						else {
							throw new RuntimeException("Not a valid input!");
						}
					}

					else {
						throw new RuntimeException("Not a valid input!");
					}

					result = currentVal.equals(nextVal);
					if (!result) {
						break;
					}
					prev = next;
					current = next.car();
					next = next.cdr();
				}
			}

			else {
				if (current instanceof SchemePair && current.car().toString().equals("list")) {
					while (!(next instanceof SchemeNull) && result) {
						String currentVal;
						String nextVal;
						if (current instanceof SchemePair) {
							currentVal = createList(current).toString();
						}

						else {
							result = false;
							break;
						}

						if (next instanceof SchemePair) {
							nextVal = createList(next.car()).toString();
						}

						else {
							result = false;
							break;
						}

						result = currentVal.equals(nextVal);
						if (!result) {
							break;
						}
						current = next.car();
						next = next.cdr();
					}
				}

				else {
					throw new RuntimeException("Not a valid input!");
				}
			}
		} catch (ClassCastException e) {
			result = false;
		}
		
		if (result) {
			return SchemeBoolean.TRUE;
		}
		
		else {
			return SchemeBoolean.FALSE;
		}
	}
	
	/**********************************************************************************/
	// LIST FUNCTIONS
	/**********************************************************************************/
	private static boolean isListOperation(SchemeSymbol symbol) {
		return symbol.toString().equals("car") || symbol.toString().equals("cdr") || symbol.toString().equals("set-car!") || symbol.toString().equals("set-cdr!") || symbol.toString().equals("cons") || symbol.toString().equals("list");
	}
	
	private static SchemeObject performListOperation(SchemeObject object) throws RuntimeException {
		SchemeObject result;
		if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("car")) {
			result = performCarOperation(object.cdr());
		}
		
		else if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("cdr")) {
			result = performCdrOperation(object.cdr());
		}
		
		else if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("set-car!")) {
			result = performSetCarOperation(object.cdr());
		}
		
		else if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("set-cdr!")) {
			result = performSetCdrOperation(object.cdr());
		}
		
		else if (SchemeSymbol.getSymbol(object.car().toString()).toString().equals("list")) {
			result = createList(object.cdr());
		}
		
		else {
			result = performConsOperation(object.cdr(), object.cdr().cdr());
		}
		
		return result;
	}
	
	private static SchemeObject createList(SchemeObject object) throws RuntimeException {
		SchemePair list = new SchemePair(SchemeSymbol.getSymbol("list"), flattenListTail(object));
		return list;
	}
	
	private static SchemeObject performCarOperation(SchemeObject object) throws RuntimeException {
		if (object instanceof SchemeNull) {
			return SchemeNull.getNull();
		}
		
		else if (object instanceof SchemePair && object.car() instanceof SchemePair) {
			if (object.car().car() instanceof SchemeSymbol) {
				SchemeObject list = checkSymbols(object.car());
				return list.cdr().car();
			}
		}
		
		throw new RuntimeException("Invalid input detected!");
	}
	
	private static SchemeObject performCdrOperation(SchemeObject object) throws RuntimeException {
		if (object instanceof SchemeNull) {
			return SchemeNull.getNull();
		}
		
		else if (object instanceof SchemePair && object.car() instanceof SchemePair) {
			if (object.car().car() instanceof SchemeSymbol) {
				SchemeObject list = checkSymbols(object.car());
				return new SchemePair(SchemeSymbol.getSymbol("list"), list.cdr().cdr());
			}
		}
		
		throw new RuntimeException("Invalid input detected!");
	}
	
	private static SchemeObject performSetCarOperation(SchemeObject object) throws RuntimeException {
		SchemeObject car;
		SchemeObject cdr;
		
		if (object.cdr().car() instanceof SchemeSymbol) {
			car = checkSymbols(object.cdr());
		}
		
		else {
			car = object.cdr().car();
		}
		
		// See if variable was passed
		if (object.car() instanceof SchemeSymbol) {
			cdr = checkSymbols(object).cdr().cdr();
			SchemePair list = new SchemePair(SchemeSymbol.getSymbol("list"), new SchemePair(car, cdr));
			SchemePair newVar = new SchemePair(object.car(), new SchemePair (list, SchemeNull.getNull()));
			setOperation(newVar);
			return SchemeBoolean.getTrue();
		}
		
		return SchemeBoolean.getFalse();
	}
	
	private static SchemeObject performSetCdrOperation(SchemeObject object) throws RuntimeException {
		SchemeObject car;
		SchemeObject cdr;
		
		if (object.cdr().car() instanceof SchemeSymbol) {
			cdr = new SchemePair(checkSymbols(object.cdr()), SchemeNull.getNull());
		}
		
		else {
			cdr = object.cdr();
		}
		
		// See if variable was passed
		if (object.car() instanceof SchemeSymbol) {
			car = checkSymbols(object).cdr().car();
			SchemePair list = new SchemePair(SchemeSymbol.getSymbol("list"), new SchemePair(car, cdr));
			SchemePair newVar = new SchemePair(object.car(), new SchemePair (list, SchemeNull.getNull()));
			setOperation(newVar);
			return SchemeBoolean.getTrue();
		}
		
		return SchemeBoolean.getFalse();
	}
	
	private static SchemeObject performConsOperation(SchemeObject object, SchemeObject tail) throws RuntimeException {
		SchemeObject head = object.car();
		if (head instanceof SchemeSymbol) {
			head = checkSymbols(object);
		}
		
		if (head instanceof SchemePair) {
			head = checkSymbols(head);
		}
		
		SchemeObject filteredTail;
		if (tail.car() instanceof SchemeSymbol) {
			tail = checkSymbols(tail);
			filteredTail = tail.cdr();
		}
		
		else {
			filteredTail = tail.car().cdr();
		}
		SchemePair list = new SchemePair(SchemeSymbol.getSymbol("list"), new SchemePair(head, flattenListTail(filteredTail)));
		return list;
	}

	private static SchemePair flattenListTail(SchemeObject object) throws RuntimeException {
		SchemeObject head = object.car();
		SchemeObject tail = object.cdr();
		
		if (head instanceof SchemeSymbol) {
			head = checkSymbols(object);
		}
		
		if (head instanceof SchemePair) {
			head = checkSymbols(head);
		}
		
		if (tail instanceof SchemeNull) {
			return new SchemePair(head, SchemeNull.getNull());
		}
		
		return new SchemePair(head, flattenListTail(tail));
	}

	/**********************************************************************************/
	// Symbol checking
	/**********************************************************************************/
	private static SchemeObject checkSymbols(SchemeObject context) {
		SchemeSymbol symbol = SchemeSymbol.getSymbol(context.car().toString());
		if (isArithmeticFunction(symbol)) {
			return computeArithmeticOperation(context);
		}

		else if (isEqualityOperation(symbol)) {
			return computeEqualityOperation(context);
		}

		else if (isListOperation(symbol)) {
			return performListOperation(context);
		}
		
		else if (isLetExpression(symbol)) {
			return computeLetExpression(context);
		}
		
		else if (isQuoteExpression(symbol)) {
			return quoteOperation(context);
		}
		
		else if (isIfStatement(symbol)) {
			return evaluateIfStatement(context);
		}
		
		else if (isConditionalStatement(symbol)) {
			return evaluateConditionalStatement(context);
		}
		
		else if (doesVariableExist(symbol)) {
			SchemeObject s = accessSchemeSymbol(symbol);
			if (s instanceof SchemePair && !s.car().toString().equals("list")) {
				SchemeObject lambda = s;
				SchemeObject expression = lambda.cdr().car();
				SchemeObject paramSymbols = lambda.car();
				SchemeObject paramVals = context.cdr();
				return computeLambdaExpression(expression, paramSymbols, paramVals);	
			}
			
			else {
				return s;
			}
		}
		
		throw new RuntimeException("Incorrect symbol!");
	}
	
	/**********************************************************************************/
	// ENTRY POINT
	/**********************************************************************************/
	public static void main(String[] args) {
		System.out.println("Welcome to CammaRacket, version 0.8b");
		
		environmentFrames.add(new HashMap<SchemeSymbol, SchemeObject>());
		boolean running = true;
		while(running == true) {
			
			// Purge the stack
			currentDepth = 0;
			if (environmentFrames.size() > 1) {
				for (int i = environmentFrames.size() - 1; i >= 0; i--) {
					if (i > 0) {
						environmentFrames.remove(i);
					}
				}
			}
			
			try {
				System.out.print("> ");
				SchemeObject context = SchemeObject.read(System.in);
				
				if (context.toString().equals("(exit)")) {
					running = false;
				}
				
				else {
					if (!(context instanceof SchemePair)) {
						SchemeSymbol variable = SchemeSymbol.getSymbol(context.toString());
						if (doesVariableExist(variable)) {
							SchemeObject value = accessSchemeSymbol(variable);
							if (value != null) {
								System.out.println(value.toString());
							}
						}
						
						else {
							if (context instanceof SchemeString || context instanceof SchemeNumber) {
								System.out.println(context.toString());
							}
							
							else {
								System.err.println(context.toString() + ": This variable is not defined.");
							}
						}
					}

					else {
						SchemeSymbol symbol = SchemeSymbol.getSymbol(context.car().toString());
						if (isArithmeticFunction(symbol)) {
							SchemeObject output = computeArithmeticOperation(context);
							System.out.println(output.toString());
						}

						else if (isEqualityOperation(symbol)) {
							SchemeObject output = computeEqualityOperation(context);
							System.out.println(output.toString());
						}

						else if (isListOperation(symbol)) {
							SchemeObject output = performListOperation(context);
							System.out.println(output.toString());
						}
						
						else if (isLetExpression(symbol)) {
							SchemeObject output = computeLetExpression(context);
							System.out.println(output.toString());
						}
						
						else if (isQuoteExpression(symbol)) {
							SchemeObject output = quoteOperation(context);
							System.out.println(output.toString());
						}
						
						else if (isSymbolDefinition(symbol)) {
							defineSchemeSymbol(context.cdr());
						}
						
						else if (isSymbolRedefinition(symbol)) {
							setOperation(context.cdr());
							System.out.println("(void)");
						}
						
						else if (isIfStatement(symbol)) {
							SchemeObject output = evaluateIfStatement(context);	
							System.out.println(output.toString());
						}
						
						else if (isConditionalStatement(symbol)) {
							SchemeObject output = evaluateConditionalStatement(context);
							System.out.println(output);
						}
						
						else if (doesVariableExist(symbol)) {
							if (context.cdr() instanceof SchemeNull) {
								throw new RuntimeException ("Incorrect input detected!");
							}
							
							else {
								SchemeObject lambda = accessSchemeSymbol(symbol);
								SchemeObject expression = lambda.cdr().car();
								SchemeObject paramSymbols = lambda.car();
								SchemeObject paramVals = context.cdr();
								SchemeObject output = computeLambdaExpression(expression, paramSymbols, paramVals);
								System.out.println(output.toString());
							}
						}
						
						else if (symbol.toString().equals("sh-quote")) {
							printShindlerQuote();
						}
						
						else {
							throw new RuntimeException ("Incorrect input detected!");
						}
					}
				}
			} catch (RuntimeException | IOException | EndOfSchemeListException e) {
				System.err.println(e.getMessage());
			}
		}
		System.out.println("Interactions Disabled.");
	}
}
