package com.xceptance.xlt.nocoding.util.context;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.LightWeightPageImpl;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.nocoding.util.dataStorage.DataStorage;

/**
 * The {@link Context} used in the LightWeight mode of the execution. Therefore, it extends
 * <code>Context&lt;LightWeightPage&gt;</code>
 * 
 * @author ckeiner
 */
public class LightWeightContext extends Context<LightWeightPage>
{
    /**
     * The SgmlPage
     */
    protected SgmlPage sgmlPage;

    /**
     * Creates a new {@link LightWeightContext} out of the old {@link LightWeightContext}
     * 
     * @param context
     */
    public LightWeightContext(final Context<LightWeightPage> context)
    {
        super(context);
    }

    /**
     * Creates a new {@link LightWeightContext}, with the provided {@link DataStorage} and configures the
     * {@link XltWebClient} according to the {@link XltProperties}
     * 
     * @param xltProperties
     *            The properties to use - normally {@link XltProperties#getInstance()}
     * @param dataStorage
     *            The {@link DataStorage} you want to use
     */
    public LightWeightContext(final XltProperties xltProperties, final DataStorage dataStorage)
    {
        super(xltProperties, dataStorage);
    }

    /**
     * Creates a new {@link LightWeightContext}, with a new {@link DataStorage} and configures the {@link XltWebClient}
     * according to the {@link XltProperties}
     * 
     * @param xltProperties
     *            The properties to use - normally {@link XltProperties#getInstance()}
     */
    public LightWeightContext(final XltProperties xltProperties)
    {
        super(xltProperties);
    }

    /**
     * Gets the {@link SgmlPage} if it isn't <code>null</code>. Else, it builds the SgmlPage from the {@link WebResponse}.
     * 
     * @return
     */
    public SgmlPage getSgmlPage()
    {
        // If the sgmlPage is null and therefore wasn't loaded already
        if (sgmlPage == null)
        {
            // Generate a new sgmlPage
            XltLogger.runTimeLogger.debug("Generating new SgmlPage...");
            Page page;
            try
            {
                // Load the WebResponse into the window of the web client
                page = getWebClient().loadWebResponseInto(getWebResponse(), getWebClient().getCurrentWindow());
                // If the built page is an instance of SgmlPage
                if (page instanceof SgmlPage)
                {
                    if (page instanceof XmlPage && ((XmlPage) page).getXmlDocument() == null)
                    {
                        throw new IllegalStateException("Faulty WebResponse, the page doesn't have child nodes.");
                    }
                    // Set the sgmlPage
                    setSgmlPage((SgmlPage) page);
                    XltLogger.runTimeLogger.debug("SgmlPage built");
                }
            }
            catch (FailingHttpStatusCodeException | IOException e)
            {
                throw new IllegalStateException("Cannot convert WebResponse to SgmlPage.");
            }
        }
        // Return the sgmlPage
        return sgmlPage;
    }

    public void setSgmlPage(final SgmlPage sgmlPage)
    {
        this.sgmlPage = sgmlPage;
    }

    /**
     * @return The current {@link LightWeightPage}
     */
    @Override
    public LightWeightPage getPage()
    {
        return page;
    }

    /**
     * @param lightWeightPage
     *            {@link LightWeightPage}
     */
    @Override
    public void setPage(final LightWeightPage lightWeightPage)
    {
        this.page = lightWeightPage;
    }

    /**
     * Loads the {@link WebResponse} corresponding to the {@link WebRequest} and sets {@link #sgmlPage} to
     * <code>null</code>. <br>
     * If {@link WebRequest#isXHR()} is <code>false</code>, it loads the {@link LightWeightPage} and sets the
     * {@link WebResponse}.<br>
     * If <code>WebRequest.isXHR()</code> is <code>true</code>, it only loads the WebResponse.
     * 
     * @param webResponse
     * @throws IOException
     * @throws FailingHttpStatusCodeException
     */
    @Override
    public void loadWebResponse(final WebRequest webRequest) throws FailingHttpStatusCodeException, IOException
    {
        // Reset the sgmlPage
        setSgmlPage(null);
        // If the webRequest is not a Xhr
        if (!webRequest.isXHR())
        {
            // Load and set page
            this.setPage(this.getWebClient().getLightWeightPage(webRequest));
            // Set webResponse
            setWebResponse(this.getPage().getWebResponse());
        }
        // If the webRequest is a Xhr
        else
        {
            // Load and set the WebResponse
            setWebResponse(this.getWebClient().loadWebResponse(webRequest));
        }
    }

    /**
     * Appends the {@link #getPage()} to the result browser, if it is not <code>null</code>.<br>
     * Otherwise, it creates a new {@link LightWeightPage}.
     */
    @Override
    public void appendToResultBrowser() throws Exception
    {
        final String name = getWebClient().getTimerName();
        if (getPage() != null)
        {
            ((SessionImpl) Session.getCurrent()).getRequestHistory().add(getPage());
        }
        else
        {
            ((SessionImpl) Session.getCurrent()).getRequestHistory().add(new LightWeightPageImpl(getWebResponse(), name, getWebClient()));

        }
    }

    /**
     * @return {@link #LightWeightContext(Context)}
     */
    @Override
    public Context<LightWeightPage> buildNewContext()
    {
        return new LightWeightContext(this);
    }

}
