package com.xceptance.xlt.nocoding.parser.yamlParser.scriptItems.actionItems.request;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.xceptance.xlt.nocoding.util.ParserUtils;

/**
 * The class for parsing the header items to a map with a String key and a String value
 * 
 * @author ckeiner
 */
public class HeaderParser
{

    /**
     * Parses the header item to a map with a String key and a String value
     * 
     * @param headersNode
     *            The {@link JsonNode} with the headers in it
     * @return A map containing the parsed headers
     */
    public Map<String, String> parse(final JsonNode headersNode)
    {
        // Parse the JsonNode to a Map<String, String>
        final Map<String, String> headers = ParserUtils.getArrayNodeAsMap(headersNode);
        // Parse headers and return them
        return headers;
    }

}
