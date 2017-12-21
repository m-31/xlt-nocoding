package com.xceptance.xlt.nocoding.util.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.LightWeightPageImpl;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.nocoding.util.dataStorage.DataStorage;

public class LightWeightContext extends Context
{
    /**
     * Cache of the SgmlPage
     */
    protected final Map<WebResponse, SgmlPage> sgmlPages = new HashMap<>();

    public LightWeightContext(final Context context)
    {
        super(context);
    }

    public LightWeightContext(final XltProperties xltProperties, final DataStorage dataStorage)
    {
        super(xltProperties, dataStorage);
    }

    public LightWeightContext(final XltProperties xltProperties)
    {
        super(xltProperties);
    }

    /**
     * @return The {@link Map} that maps {@link WebResponse} to {@link SgmlPage}
     */
    public Map<WebResponse, SgmlPage> getSgmlPages()
    {
        return sgmlPages;
    }

    /**
     * Returns <code>sgmlPages.get(webResponse)</code> if it is not <code>null</code>, else builds a {@link SgmlPage}
     * 
     * @return The {@link SgmlPage} corresponding to the {@link #webResponse}.
     */
    public SgmlPage getSgmlPage(final WebResponse webResponse)
    {
        SgmlPage sgmlPage = sgmlPages.get(webResponse);
        if (sgmlPage == null)
        {
            XltLogger.runTimeLogger.debug("Generating new SgmlPage...");
            try
            {
                final Page page = getWebClient().loadWebResponseInto(getWebResponse(), getWebClient().getCurrentWindow());
                if (page instanceof SgmlPage)
                {
                    sgmlPage = (SgmlPage) page;
                    XltLogger.runTimeLogger.debug("SgmlPage built");
                    sgmlPages.put(webResponse, sgmlPage);
                }
            }
            catch (FailingHttpStatusCodeException | IOException e)
            {
                e.printStackTrace();
            }
        }
        return sgmlPage;
    }

    @Override
    public LightWeightPage getLightWeightPage()
    {
        return lightWeightPage;
    }

    @Override
    public void setLightWeightPage(final LightWeightPage lightWeightPage)
    {
        this.lightWeightPage = lightWeightPage;
    }

    /**
     * Sets {@link #webResponse}
     * 
     * @param webResponse
     * @throws IOException
     * @throws FailingHttpStatusCodeException
     */
    @Override
    public void loadWebResponse(final WebRequest webRequest) throws FailingHttpStatusCodeException, IOException
    {
        if (!webRequest.isXHR())
        {
            this.setLightWeightPage(this.getWebClient().getLightWeightPage(webRequest));
            webResponse = this.getLightWeightPage().getWebResponse();
        }
        else
        {
            webResponse = this.getWebClient().loadWebResponse(webRequest);
        }
    }

    @Override
    public SgmlPage getSgmlPage()
    {
        throw new IllegalStateException("Cannot get SgmlPage in LightWeightMode");
    }

    @Override
    public void setSgmlPage(final SgmlPage sgmlPage)
    {
        throw new IllegalStateException("Cannot set SgmlPage in LightWeightMode");
    }

    @Override
    public void appendToResultBrowser() throws Exception
    {
        final String name = getWebClient().getTimerName();
        if (getLightWeightPage() != null)
        {
            ((SessionImpl) Session.getCurrent()).getRequestHistory().add(getLightWeightPage());
        }
        else
        {
            ((SessionImpl) Session.getCurrent()).getRequestHistory().add(new LightWeightPageImpl(getWebResponse(), name, getWebClient()));

        }
    }

    @Override
    public Context buildNewContext()
    {
        return new LightWeightContext(this);
    }

}
