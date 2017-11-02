package com.xceptance.xlt.nocoding.util.variableResolver;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.xceptance.xlt.api.data.GeneralDataProvider;
import com.xceptance.xlt.nocoding.util.Context;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Tries to resolve variables. A variable is specified as "${(.)*}". Resolves values from the inside to the outside by
 * first looking into the dataStorage, then tries resolving it via beanshell and lastly it looks into the property
 * files.
 * 
 * @author ckeiner
 */
public class VariableResolver
{
    /**
     * The pattern for finding variables
     */
    private final static Pattern PARAMETER_PATTERN = Pattern.compile("\\$\\{[^\\{\\}]*\\}");

    // The maximum amount you can re-resolve a value (so if variables reference another variable, we only resolve this many
    // times)
    private final int resolveDepth = 2;

    public Interpreter interpreter;

    public VariableResolver(final GeneralDataProvider dataProvider)
    {
        interpreter = new Interpreter();
        try
        {
            interpreter.set("NOW", new ParameterInterpreterNow());
            interpreter.set("RANDOM", new ParameterInterpreterRandom());
            interpreter.set("DATE", new Date());
            interpreter.set("DATA", dataProvider);
        }
        catch (final EvalError e)
        {
            e.printStackTrace();
        }
    }

    public VariableResolver()
    {
        this(GeneralDataProvider.getInstance());
    }

    public String resolveString(final String toResolve, final Context context)
    {
        return resolveStringTakeThree(toResolve, context);
        // return resolveStringInAGoodWayHopefully(toResolve, context);
        // return resolveStringNew(toResolve, propertyManager, 0, new Stack<Integer>());
    }

    /**
     * Resolves string recursively from the inside to the outside
     * 
     * @param toResolve
     *            The String that you want to resolve
     * @param context
     *            The propertyManager with the global data storage inside
     * @return String - The resolved String
     * @throws EvalError
     */
    public String resolveStringOld(final String toResolve, final Context context)
    {
        // Set replacement to our toResolve string
        String replacement = toResolve;
        final Matcher matcher = PARAMETER_PATTERN.matcher(toResolve);

        while (matcher.find())
        {
            final String foundVariable = matcher.group();
            // Remove ${ and }
            final String resolvedVariable = foundVariable.substring(2, foundVariable.length() - 1);
            // Search in the storage for the variable
            String resolvedValue = context.getDataStorage().getVariableByKey(resolvedVariable);
            // if we didn't find it, let beanshell handle the variable
            if (resolvedValue == null)
            {
                try
                {
                    final Object beanShellEval = interpreter.eval(resolvedVariable);
                    // if beanshell found something, we save it as a string
                    if (beanShellEval != null)
                    {
                        resolvedValue = beanShellEval.toString();
                    }
                }
                catch (final EvalError e)
                {
                    throw new RuntimeException("Evaluation Error: ", e);
                }
            }

            // Replace the resolved value
            if (foundVariable != null && resolvedValue != null)
            {
                replacement = toResolve.replace(foundVariable, resolvedValue);
                // Finally resolve other placeholders
                replacement = resolveString(replacement, context);
            }
        }

        return replacement;
    }

    public String resolveStringInAGoodWayHopefully(final String toResolve, final Context context)
    {
        /**
         * When true, this ignores everything, until another ' apppears
         */
        Boolean ignore = false;
        String output = "";
        // iterate over the whole String
        for (int i = 0; i < toResolve.length(); i++)
        {
            // Save the current character
            final char current = toResolve.charAt(i);

            if (current == '\'')
            {
                // invert the value of ignore
                ignore = Boolean.logicalXor(ignore, false);
            }
            else if (!ignore && current == '$')
            {
                // check for variable if there is another symbol
                if (toResolve.length() > i + 1 && toResolve.charAt(i + 1) == '{')
                {
                    // VARIABLE HANDLER
                    // Raise i by two ( +1 -> {, +2 character after { )
                    i = i + 2;
                    // Search for variable in the new string and resolve it
                    final Pair<String, Integer> resolvedPair = doRecursion(toResolve.substring(i), context);

                    // TODO Start recursiveness here
                    if (resolvedPair.getLeft().startsWith("$"))
                    {
                        Pair<String, Integer> twiceResolvedPair = doRecursion(resolvedPair.getLeft(), context);
                        int numberRetries = 1;
                        while (resolvedPair.getLeft().startsWith("$") && numberRetries <= this.resolveDepth)
                        {
                            twiceResolvedPair = doRecursion(resolvedPair.getLeft(), context);
                            numberRetries++;
                        }
                        output += twiceResolvedPair.getLeft().substring(2);

                    }
                    else
                    {
                        // Add the resolved variable to output
                        output += resolvedPair.getLeft();
                    }
                    // And raise i by the length of the variable name
                    i += resolvedPair.getRight();

                }
                // happy path
                else
                {
                    output += current;
                }
            }
            else
            {
                output += current;
            }
        }
        return output;
    }

    private Pair<String, Integer> doRecursion(final String toResolve, final Context context)
    {
        /**
         * When true, this ignores everything, until another ' apppears
         */
        Boolean ignore = false;
        String output = "";
        int length = 0;
        // iterate over the whole String
        for (int i = 0; i < toResolve.length(); i++)
        {
            // Save the current character
            final char current = toResolve.charAt(i);

            if (current == '\'')
            {
                // invert the value of ignore
                ignore = !ignore;
            }
            else if (!ignore && current == '$')
            {
                // check for variable if there is another symbol
                if (toResolve.length() > i + 1 && toResolve.charAt(i + 1) == '{')
                {
                    // Raise i by two ( +1 -> {, +2 character after { )
                    i = i + 2;
                    final Pair<String, Integer> resolvedPair = doRecursion(toResolve.substring(i), context);
                    // Add the resolved variable to output
                    output += resolvedPair.getLeft();
                    // And raise i by the length of the variable name
                    i += resolvedPair.getRight();
                    // test if we are at the end
                    if (i >= toResolve.length() - 1)
                    {
                        // We are at the end but still expect a variable
                        output = "${" + output;
                    }
                }
                // happy path
                else
                {
                    output += current;
                }
            }
            // We found a possible end
            else if (!ignore && current == '}')
            {
                // Since we are in this function, we did find a variable sign,
                length = i;
                String resolvedValue = context.getDataStorage().getVariableByKey(output);
                // if we didn't find it, let beanshell handle the variable
                if (resolvedValue == null && !output.equals("{") && !output.equals("}"))
                {
                    try
                    {
                        final Object beanShellEval = interpreter.eval(output);
                        // if beanshell found something, we save it as a string
                        if (beanShellEval != null)
                        {
                            resolvedValue = beanShellEval.toString();
                            // TODO added
                            // if we define a variable as ${RANDOM.String(8)} we only want to resolve it once. Thus we need to save
                            // variables in our dataStorage afterwards
                            context.getDataStorage().storeVariable(toResolve.substring(0, toResolve.length() - 1), resolvedValue);

                        }
                        // BeanSheall doesn't know the variable, therefore we want the plain text
                        else
                        {
                            // Try to find it in the properties
                            resolvedValue = context.getPropertyByKey(output);
                            // If it still cannot be found, it isn't resolvable anymore
                            if (resolvedValue == null)
                            {
                                // So we simply add ${ and } again.
                                resolvedValue = "${" + output + "}";
                            }
                        }
                    }
                    catch (final EvalError e)
                    {
                        // throw new RuntimeException("Evaluation Error: ", e);
                        // We couldn't resolve it, so it was probably some function parameter
                        output += current;
                        continue;
                    }
                }
                // This fixes Text${'{'}
                else if (resolvedValue == null)
                {
                    resolvedValue = output;
                }

                output = resolvedValue;
                // we found a variable, therefore we are done
                break;
            }
            else if (i >= toResolve.length() - 1)
            {
                // We are at the end and haven't found anything
                output = "${" + output + current;
            }
            else
            {
                output += current;
            }
            length = i;
        }

        // handle missing ending curly brace aka }
        if (output.equals(toResolve))
        {
            output = "${" + output;
        }

        final Pair<String, Integer> resolvedPair = new ImmutablePair<String, Integer>(output, length);
        return resolvedPair;
    }

    public String resolveStringTakeThree(final String toResolve, final Context context)
    {
        String output = "";
        char current;

        // Main iteration over the string
        for (int index = 0; index < toResolve.length(); index++)
        {
            current = toResolve.charAt(index);

            switch (current)
            {
                // Add next char
                case '\\':
                    output += toResolve.charAt(++index);
                    break;
                // Check for variable definition
                case '$':
                    // If we have a full definition of a variable
                    if (toResolve.charAt(index + 1) == '{' && toResolve.substring(index).contains("}"))
                    {
                        // doRecursion
                        final String variableName = resolveVariableName(toResolve.substring(index + 2), context);
                        // TODO resolve the variable, then fix the index, so the length of variableName+3 is added.
                        // but what about the original String and multiple resolutions?
                    }
                    // Otherwise, simply add the current char
                    else
                    {
                        output += current;
                    }
                    break;
                // Add current char
                default:
                    output += current;
                    break;
            }

        }

        return output;
    }

    /**
     * Resolves the variable name when encountering a variable definiton
     * 
     * @param toResolve
     * @param context
     * @return
     */
    private String resolveVariableName(final String toResolve, final Context context)
    {
        String output = "";
        char current;
        boolean ignoreNextChars = false;

        // Main iteration over the string
        for (int index = 0; index < toResolve.length(); index++)
        {
            current = toResolve.charAt(index);
            // Ignore next char
            if (current == '\\')
            {
                output += toResolve.charAt(++index);
            }
            // Ignore every character that is following
            else if (current == '\'' && toResolve.substring(index).contains("\'"))
            {
                ignoreNextChars = true;
            }
            // Stop ignoring every character that is following
            else if (current == '\'' && ignoreNextChars)
            {
                ignoreNextChars = false;
            }
            // We are at the end of our variable definition
            else if (current == '}' && !ignoreNextChars)
            {
                break;
            }
            // We found another variable
            else if (current == '$')
            {
                // If we have a full definition of a variable
                if (toResolve.charAt(index + 1) == '{' && toResolve.substring(index).contains("}"))
                {
                    // doRecursion
                    final String variableName = resolveVariableName(toResolve.substring(index + 2), context);
                    // TODO resolve the variable, then fix the index, so the length of variableName+3 is added.
                    // but what about the original String and multiple resolutions?
                }
                // Otherwise, simply add the current char
                else
                {
                    output += current;
                }
            }

            else
            {
                output += current;
            }
        }

        return output;
    }

    /**
     * Asks dataStorage etc for the value of the variable
     * 
     * @param variableName
     * @param context
     * @return
     */
    private String resolveVariable(final String variableName, final Context context)
    {
        // Try to resolve it in the dataStorage
        String resolvedValue = context.getDataStorage().getVariableByKey(variableName);
        // If we didn't find it, let beanshell handle the variable
        if (resolvedValue == null && !variableName.equals("{") && !variableName.equals("}"))
        {
            try
            {
                final Object beanShellEval = interpreter.eval(variableName);
                // if beanshell found something, we save it as a string
                if (beanShellEval != null)
                {
                    resolvedValue = beanShellEval.toString();
                    // TODO added
                    // if we define a variable as ${RANDOM.String(8)} we only want to resolve it once. Thus we need to save
                    // variables in our dataStorage afterwards
                    context.getDataStorage().storeVariable(variableName, resolvedValue);

                }
                // BeanSheall doesn't know the variable, therefore we want the plain text
                else
                {
                    // Try to find it in the properties
                    resolvedValue = context.getPropertyByKey(variableName);
                    // If it still cannot be found, it isn't resolvable anymore
                    if (resolvedValue == null)
                    {
                        // So we simply add ${ and } again.
                        resolvedValue = "${" + variableName + "}";
                    }
                }
            }
            catch (final EvalError e)
            {
                // throw new RuntimeException("Evaluation Error: ", e);
                // We couldn't resolve it, therefore we simply can ignore it
            }
        }
        // This fixes Text${'{'}
        else if (resolvedValue == null)
        {
            resolvedValue = variableName;
        }

        // we found a variable, therefore we are done
        return resolvedValue;
    }

}
