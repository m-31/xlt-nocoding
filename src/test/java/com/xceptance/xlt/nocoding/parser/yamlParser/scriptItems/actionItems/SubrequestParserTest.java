package com.xceptance.xlt.nocoding.parser.yamlParser.scriptItems.actionItems;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.xceptance.xlt.nocoding.parser.Parser;
import com.xceptance.xlt.nocoding.parser.ParserTest;
import com.xceptance.xlt.nocoding.parser.yamlParser.YamlParser;

/**
 * Tests for parsing the "Subrequests" tag
 * 
 * @author ckeiner
 */
public class SubrequestParserTest extends ParserTest
{
    protected final String path = super.path + "actionItems/subrequests/";

    protected final String fileXhrSubrequests = path + "xhrSubrequests.yml";

    protected final String fileStaticSubrequests = path + "staticSubrequests.yml";

    protected final String fileSyntaxErrorSubrequests = path + "syntaxErrorSubrequests.yml";

    protected final String fileSyntaxErrorSubrequestsObjectNotArray = path + "syntaxErrorSubrequestsObjectNotArray.yml";

    protected final String fileSyntaxErrorSubrequestsStaticItemObjectNotArray = path + "syntaxErrorSubrequestsStaticItemObjectNotArray.yml";

    protected final String fileSyntaxErrorSubrequestsXhrItemArrayNotObject = path + "syntaxErrorSubrequestsXhrItemArrayNotObject.yml";

    protected final String fileSyntaxErrorXhr = path + "syntaxErrorXhr.yml";

    protected final String fileSyntaxErrorStatic = path + "syntaxErrorStatic.yml";

    /**
     * Verifies a XhrSubrequest can be parsed
     * 
     * @throws Exception
     */
    @Test
    public void testXhrSubrequestsParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileXhrSubrequests);
        parser.parse();
    }

    /**
     * Verifies a static subrequest can be parsed
     * 
     * @throws Exception
     */
    @Test
    public void testStaticSubrequestsParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileStaticSubrequests);
        parser.parse();
    }

    /**
     * Verifies an error happens when "Subrequests" has an invalid tag
     * 
     * @throws Exception
     */
    @Test(expected = JsonParseException.class)
    public void testSyntaxErrorSubrequestsParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileSyntaxErrorSubrequests);
        parser.parse();
    }

    /**
     * Verifies an error happens when "Subrequests" has objects beneath it and not arrays
     * 
     * @throws Exception
     */
    @Test(expected = JsonParseException.class)
    public void testSyntaxErrorSubrequestsObjectNotArrayParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileSyntaxErrorSubrequestsObjectNotArray);
        parser.parse();
    }

    /**
     * Verifies an error happens when "Static" beneath "Subrequests" has objects beneath it and not arrays
     * 
     * @throws Exception
     */
    @Test(expected = JsonParseException.class)
    public void testSyntaxErrorSubrequestsStaticItemObjectNotArrayParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileSyntaxErrorSubrequestsStaticItemObjectNotArray);
        parser.parse();
    }

    /**
     * Verifies an error happens when "Xhr" beneath "Subrequests" has an array beneath it and not objects
     * 
     * @throws Exception
     */
    @Test(expected = JsonParseException.class)
    public void testSyntaxErrorSubrequestsXhrItemArrayNotObjectParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileSyntaxErrorSubrequestsXhrItemArrayNotObject);
        parser.parse();
    }

    /**
     * Verifies an error happens when "Xhr" beneath "Subrequests" has an invalid tag
     * 
     * @throws Exception
     */
    @Test(expected = JsonParseException.class)
    public void testSyntaxErrorXhrParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileSyntaxErrorXhr);
        parser.parse();
    }

    /**
     * Verifies an error happens when "Static" beneath "Subrequests" has a single value
     * 
     * @throws Exception
     */
    @Test(expected = JsonParseException.class)
    public void testSyntaxErrorStaticParsing() throws Exception
    {
        final Parser parser = new YamlParser(fileSyntaxErrorStatic);
        parser.parse();
    }

}
