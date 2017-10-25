package com.xceptance.xlt.nocoding.parser.yamlParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.nocoding.parser.Parser;
import com.xceptance.xlt.nocoding.parser.yamlParser.scriptItems.AbstractScriptItemParser;
import com.xceptance.xlt.nocoding.parser.yamlParser.scriptItems.ActionItemParser;
import com.xceptance.xlt.nocoding.parser.yamlParser.scriptItems.DefaultItemParser;
import com.xceptance.xlt.nocoding.parser.yamlParser.scriptItems.StoreItemParser;
import com.xceptance.xlt.nocoding.scriptItem.ScriptItem;
import com.xceptance.xlt.nocoding.util.Constants;

/**
 * Reads a yaml file, provided per constructor, and generates a testsuite out of the yaml file.
 * 
 * @author ckeiner
 */
public class YamlParser implements Parser
{

    /**
     * The yaml file
     */
    final File file;

    public YamlParser(final String pathToFile)
    {
        this.file = new File(pathToFile);
    }

    /**
     * Parses the file and returns a list of ScriptItem
     */
    @Override
    public List<ScriptItem> parse() throws Exception
    {
        final List<ScriptItem> scriptItems = new ArrayList<ScriptItem>();
        // Build the parser
        final YAMLFactory factory = new YAMLFactory();
        final JsonParser parser = factory.createParser(file);
        // Allow comments in the parser, so we have the correct line numbers
        parser.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);

        int numberObject = 0;

        // Iterate over all tokens
        while (parser.nextToken() != null)
        {
            // Check if we have a permitted list item
            if (Constants.isPermittedListItem(parser.getText()))
            {
                numberObject++;
                XltLogger.runTimeLogger.info(numberObject + ".th ScriptItem: " + parser.getText());

                try
                {
                    // Differentiate between Store, Action and default definition
                    AbstractScriptItemParser scriptItemParser = null;
                    if (parser.getText().equals(Constants.STORE))
                    {
                        // Set parser to StoreItemParser
                        scriptItemParser = new StoreItemParser();
                    }
                    else if (parser.getText().equals(Constants.ACTION))
                    {
                        // Set parser to ActionItemParser
                        scriptItemParser = new ActionItemParser();
                    }
                    else
                    {
                        // Set parser to DefaultItemParser
                        scriptItemParser = new DefaultItemParser();
                    }

                    scriptItems.addAll(scriptItemParser.parse(parser));

                }
                // Catch any exception while parsing, so we can print the current line/column number with the error
                catch (final Exception e)
                {
                    throw new JsonParseException(e.getMessage(), parser.getCurrentLocation(), e);
                }
            }
            // If we don't have a list item, check if it is really a field name. If it is, throw an error
            else if (parser.getCurrentToken() != null && parser.getCurrentToken().equals(JsonToken.FIELD_NAME))
            {
                // XltLogger.runTimeLogger.warn("No permitted list item: " + parser.getText() + logLineColumn(parser));
                // throw new com.google.gson.JsonParseException("No permitted list item: " + parser.getText() + logLineColumn(parser));

                // TODO the other methods dont work - NoSuchMethodError -> Dependency Issue but Pom says we use jackson-core 2.9.0
                throw new JsonParseException("No permitted list item: " + parser.getText(), parser.getCurrentLocation());
            }
            // If we don't have a list item and it's not a field name, we have found null or an Array Entry/Exit or an Object
            // Entry/Exit.

        }

        // Finally return all scriptItems
        return scriptItems;
    }

}
