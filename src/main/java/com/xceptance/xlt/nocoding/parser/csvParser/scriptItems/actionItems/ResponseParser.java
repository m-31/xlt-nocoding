package com.xceptance.xlt.nocoding.parser.csvParser.scriptItems.actionItems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.xceptance.xlt.nocoding.parser.csvParser.CsvConstants;
import com.xceptance.xlt.nocoding.scriptItem.action.response.AbstractResponseItem;
import com.xceptance.xlt.nocoding.scriptItem.action.response.HttpcodeValidator;
import com.xceptance.xlt.nocoding.scriptItem.action.response.Response;
import com.xceptance.xlt.nocoding.scriptItem.action.response.Validator;
import com.xceptance.xlt.nocoding.scriptItem.action.response.extractor.AbstractExtractor;
import com.xceptance.xlt.nocoding.scriptItem.action.response.extractor.RegexpExtractor;
import com.xceptance.xlt.nocoding.scriptItem.action.response.extractor.xpathExtractor.XpathExtractor;
import com.xceptance.xlt.nocoding.scriptItem.action.response.store.AbstractResponseStore;
import com.xceptance.xlt.nocoding.scriptItem.action.response.store.ResponseStore;
import com.xceptance.xlt.nocoding.scriptItem.action.response.validationMethod.AbstractValidationMethod;
import com.xceptance.xlt.nocoding.scriptItem.action.response.validationMethod.MatchesValidator;

/**
 * Extracts the information of a {@link Response} from a {@link CSVRecord}.
 * 
 * @author ckeiner
 */
public class ResponseParser implements AbstractActionItemParser
{
    /**
     * Extracts the information needed for a {@link Response} from the {@link CSVRecord}
     * 
     * @param record
     *            The {@link CSVRecord} with the action item
     * @return The response defined by the CSVRecord
     */
    public Response parse(final CSVRecord record)
    {
        // Initialize all needed variables for the response
        final List<AbstractResponseItem> responseItems = new ArrayList<AbstractResponseItem>();
        String responsecode = null;
        AbstractExtractor extractor = null;
        AbstractValidationMethod validationMethod = null;

        final List<AbstractResponseStore> responseStores = new ArrayList<AbstractResponseStore>();
        boolean hasXpath = false;
        boolean hasRegexp = false;

        /*
         * Read values
         */

        // Build an iterator over the headers
        final Iterator<String> headerIterator = record.toMap().keySet().iterator();
        // While there are headers
        while (headerIterator.hasNext())
        {
            // Get the next header
            final String header = headerIterator.next();
            // Get the value
            final String value = record.get(header);
            // If the value is null or empty, discard it
            if (value == null || value.isEmpty())
            {
                continue;
            }

            // If the fieldName contains either REGEXP_GETTER_PREFIX and XPath isn't used
            if (header.contains(CsvConstants.REGEXP_GETTER_PREFIX))
            {
                responseStores.add(handleStore(header, value));
                hasRegexp = true;
            }
            // If the fieldName contains either XPATH_GETTER_PREFIX and Regexp isn't used
            else if (header.contains(CsvConstants.XPATH_GETTER_PREFIX))
            {
                responseStores.add(handleStore(header, value));
                hasXpath = true;
            }
        }

        if (record.isMapped(CsvConstants.RESPONSECODE))
        {
            responsecode = record.get(CsvConstants.RESPONSECODE);
        }
        if (record.isMapped(CsvConstants.XPATH))
        {
            extractor = new XpathExtractor(record.get(CsvConstants.XPATH));
            hasXpath = true;
        }
        if (record.isMapped(CsvConstants.REGEXP))
        {
            extractor = new RegexpExtractor(record.get(CsvConstants.REGEXP));
            hasRegexp = true;
        }
        if (record.isMapped(CsvConstants.TEXT))
        {
            validationMethod = new MatchesValidator(record.get(CsvConstants.TEXT));
        }

        /*
         * Verify that values with prerequisites have fulfilled prerequisites
         */

        // Verify Xpath and Regexp aren't combined in any way
        if (hasXpath && hasRegexp)
        {
            throw new IllegalArgumentException("Cannot map Xpath Validations/Stores and Regexp Validations/Stores together");
        }
        // Verify that either both CsvConstants.TEXT and the extractor have values, or only the extractor
        if (validationMethod != null && (extractor == null))
        {
            throw new IllegalArgumentException("Cannot map " + CsvConstants.TEXT + " without extractor");
        }

        /*
         * Build the response
         */

        // HttpcodeValidator
        if (responsecode != null)
        {
            responseItems.add(new HttpcodeValidator(responsecode));
        }
        // Validator
        if (extractor != null)
        {
            // TODO better name for the validation
            final String validationName = "Validate " + extractor.getExtractionExpression();
            final Validator validator = new Validator(validationName, extractor, validationMethod);
            responseItems.add(validator);
        }
        // ResponseStore
        if (responseStores != null && !responseStores.isEmpty())
        {
            responseItems.addAll(responseStores);
        }

        // Return the response
        return new Response(responseItems);
    }

    /**
     * Creates an {@link AbstractResponseStore} depending on the fieldName
     * 
     * @param fieldName
     *            The name of the field
     * @param value
     *            The value of the field
     * @return An {@link AbstractResponseStore} with the variableName of fieldName and the proper {@link AbstractExtractor}
     *         with the extractionExpression of value
     */
    private AbstractResponseStore handleStore(final String fieldName, final String value)
    {
        // Create an empty AbstractExtractor
        AbstractExtractor storeExtractor = null;
        // If the fieldName contains CsvConstants.REGEXP_GETTER_PREFIX
        if (fieldName.contains(CsvConstants.REGEXP_GETTER_PREFIX))
        {
            // And the rest of the string is numbers
            if (fieldName.substring(CsvConstants.REGEXP_GETTER_PREFIX.length()).matches("[0-9]+"))
            {
                // Create an AbstractExtractor
                storeExtractor = new RegexpExtractor(value);
            }
            else
            {
                // Throw an error
                throw new IllegalArgumentException(fieldName + " must be " + CsvConstants.REGEXP_GETTER_PREFIX + " a number");
            }
        }
        // If the fieldName contains CsvConstants.XPATH_GETTER_PREFIX
        else if (fieldName.contains(CsvConstants.XPATH_GETTER_PREFIX))
        {
            // And the rest of the string is numbers
            if (fieldName.substring(CsvConstants.XPATH_GETTER_PREFIX.length()).matches("[0-9]+"))
            {
                // Create an AbstractExtractor
                storeExtractor = new XpathExtractor(value);
            }
            else
            {
                // Throw an error
                throw new IllegalArgumentException(fieldName + " must be " + CsvConstants.XPATH_GETTER_PREFIX + " a number");
            }
        }

        // Return the new ResponseStore
        return new ResponseStore(fieldName, storeExtractor);
    }
}
