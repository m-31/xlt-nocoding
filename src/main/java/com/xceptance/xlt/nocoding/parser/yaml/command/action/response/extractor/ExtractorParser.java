package com.xceptance.xlt.nocoding.parser.yaml.command.action.response.extractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.yaml.snakeyaml.nodes.NodeTuple;

import com.fasterxml.jackson.databind.JsonNode;
import com.xceptance.xlt.nocoding.command.action.response.extractor.AbstractExtractor;
import com.xceptance.xlt.nocoding.command.action.response.extractor.CookieExtractor;
import com.xceptance.xlt.nocoding.command.action.response.extractor.HeaderExtractor;
import com.xceptance.xlt.nocoding.command.action.response.extractor.RegexpExtractor;
import com.xceptance.xlt.nocoding.command.action.response.extractor.xpath.XpathExtractor;
import com.xceptance.xlt.nocoding.parser.yaml.YamlParserUtils;
import com.xceptance.xlt.nocoding.util.Constants;

/**
 * Takes an identifier (which is an element of {@link JsonNode#fieldNames()}) of a <code>JsonNode</code> with the
 * extraction item in it and parses it to an {@link AbstractExtractor}.
 *
 * @author ckeiner
 */
public class ExtractorParser
{
    /**
     * The identifier of the extraction item
     */
    final String identifier;

    public ExtractorParser(final String identifier)
    {
        this.identifier = identifier;
    }

    /**
     * Parses the extraction item in the {@link JsonNode} to an {@link AbstractExtractor}. Also checks if
     * {@link Constants#GROUP} is specified in the item at the <code>JsonNode</code> and if <code>Constants#GROUP</code>
     * is specified, verifies the <code>AbstractExtractor</code> is a {@link RegexpExtractor}.
     *
     * @param node
     *            The <code>NodeTuple</code> with the extraction items
     * @return The <code>AbstractExtractor</code> corresponding to the identifier. <br>
     *         For example, {@link Constants#REGEXP} is parsed to a <code>RegexpExtractor</code>.
     */
    public AbstractExtractor parse(final List<NodeTuple> nodeTupels)
    {
        // Transform the NodeTuples to a map of strings
        final Map<String, String> map = new HashMap<String, String>();
        nodeTupels.forEach(node -> {
            final String key = YamlParserUtils.transformScalarNodeToString(node.getKeyNode());
            if (key.equals(identifier) || key.equals(Constants.GROUP))
            {
                final String value = YamlParserUtils.transformScalarNodeToString(node.getValueNode());
                map.put(key, value);
            }
        });

        final boolean hasGroup = map.containsKey(Constants.GROUP);

        AbstractExtractor extractor = null;
        // Get the associated value
        final String extractorExpression = map.get(identifier);
        // Build a extractor depending on the name of the selector
        switch (identifier)
        {
            case Constants.XPATH:
                extractor = new XpathExtractor(extractorExpression);
                break;

            case Constants.REGEXP:
                if (hasGroup)
                {
                    extractor = new RegexpExtractor(extractorExpression, map.get(Constants.GROUP));
                }
                else
                {
                    extractor = new RegexpExtractor(extractorExpression);
                }
                break;

            case Constants.HEADER:
                extractor = new HeaderExtractor(extractorExpression);
                break;

            case Constants.COOKIE:
                extractor = new CookieExtractor(extractorExpression);
                break;

            default:
                throw new NotImplementedException("Permitted Extraction but no parsing specified: " + identifier);
        }

        // Throw an Exception when Constants.GROUP is specified, but the extractor is not a RegexpExtractor
        if (hasGroup && !(extractor instanceof RegexpExtractor))
        {
            throw new IllegalArgumentException(Constants.GROUP + " only allowed with RegexpExtractor, but is "
                                               + extractor.getClass().getSimpleName());
        }

        // Return the extractor
        return extractor;
    }

}
