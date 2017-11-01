package com.xceptance.xlt.nocoding.parser.yamlParser.actionItems.subrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.nocoding.parser.yamlParser.actionItems.request.RequestParser;
import com.xceptance.xlt.nocoding.parser.yamlParser.actionItems.response.ResponseParser;
import com.xceptance.xlt.nocoding.scriptItem.action.AbstractActionItem;
import com.xceptance.xlt.nocoding.scriptItem.action.Request;
import com.xceptance.xlt.nocoding.scriptItem.action.subrequest.AbstractSubrequest;
import com.xceptance.xlt.nocoding.scriptItem.action.subrequest.XHRSubrequest;
import com.xceptance.xlt.nocoding.util.Constants;
import com.xceptance.xlt.nocoding.util.ParserUtils;

public class XhrSubrequestParser
{

    /**
     * Parses a XhrSubrequest to a XhrSubrequest Object
     * 
     * @param node
     *            The node the item starts at
     * @return The XhrSubrequest
     * @throws IOException
     */
    public AbstractSubrequest parse(final JsonNode node) throws IOException
    {
        // Initialize variables
        String name = null;
        final List<AbstractActionItem> actionItems = new ArrayList<AbstractActionItem>();

        // Extract all fieldNames of the node
        final Iterator<String> fieldNames = node.fieldNames();

        // Iterate over the fieldNames
        while (fieldNames.hasNext())
        {
            // Get the next fieldName
            final String fieldName = fieldNames.next();
            AbstractActionItem actionItem = null;

            switch (fieldName)
            {
                case Constants.NAME:
                    name = ParserUtils.readValue(node, fieldName);
                    XltLogger.runTimeLogger.debug("Actionname: " + name);
                    break;

                case Constants.REQUEST:
                    // Log the Request block with the unparsed content
                    XltLogger.runTimeLogger.debug("Request: " + node.get(fieldName).toString());
                    // Create a new request parser and get the first element, so we can set Xhr to true
                    actionItem = new RequestParser().parse(node.get(fieldName)).get(0);
                    if (actionItem instanceof Request)
                    {
                        ((Request) actionItem).setXhr("true");
                    }
                    else
                    {
                        throw new IOException("Could not convert Request Item to Request Object: " + fieldName);
                    }
                    break;

                case Constants.RESPONSE:
                    // Log the Response block with the unparsed content
                    XltLogger.runTimeLogger.debug("Response: " + node.get(fieldName).toString());
                    // Create a new ResponseParser and parse the response
                    actionItem = new ResponseParser().parse(node.get(fieldName)).get(0);
                    break;

                case Constants.SUBREQUESTS:
                    XltLogger.runTimeLogger.debug("Subrequests: " + node.get(fieldName).toString());
                    // Create a new SubrequestParser and parse the subrequest
                    actionItem = new SubrequestParser().parse(node.get(fieldName)).get(0);
                    break;

                default:
                    throw new IOException("No permitted xhr subrequest item: " + fieldName);
            }
            actionItems.add(actionItem);
        }

        // Create a new Subrequest
        final AbstractSubrequest subrequest = new XHRSubrequest(name, actionItems);
        // Return the subrequest
        return subrequest;
    }

}
