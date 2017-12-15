package com.xceptance.xlt.nocoding.util;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.xlt.api.data.GeneralDataProvider;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.nocoding.util.dataStorage.DataStorage;
import com.xceptance.xlt.nocoding.util.dataStorage.storageUnits.DuplicateStorage;
import com.xceptance.xlt.nocoding.util.dataStorage.storageUnits.SingleStorage;
import com.xceptance.xlt.nocoding.util.dataStorage.storageUnits.uniqueStorage.DefaultKeyValueStorage;
import com.xceptance.xlt.nocoding.util.dataStorage.storageUnits.uniqueStorage.UniqueStorage;
import com.xceptance.xlt.nocoding.util.variableResolver.VariableResolver;

/**
 * The context for the execution. Thus, the context has methods for handling the {@link DataStorage},
 * {@link XltWebClient}, resolving variables, storing/accessing the current {@link WebResponse} and accessing
 * properties.
 * 
 * @author ckeiner
 */
public class Context
{
    protected final DataStorage dataStorage;

    protected final XltWebClient webClient;

    protected final VariableResolver resolver;

    protected WebResponse webResponse;

    protected SgmlPage sgmlPage;

    protected NoCodingPropertyAdmin propertyAdmin;

    /**
     * Creates a new context, sets default Values in the dataStorage and configures the webClient according to the
     * xltProperties
     * 
     * @param xltProperties
     *            The properties to use - normally XltProperties.getInstance()
     */
    public Context(final XltProperties xltProperties)
    {
        this(xltProperties, new DataStorage());
    }

    /**
     * Creates a new context, sets default Values in the dataStorage and configures the webClient according to the
     * xltProperties
     * 
     * @param xltProperties
     *            The properties to use - normally XltProperties.getInstance()
     * @param dataStorage
     *            The {@link DataStorage} you want to use
     */
    public Context(final XltProperties xltProperties, final DataStorage dataStorage)
    {
        this.dataStorage = dataStorage;
        this.propertyAdmin = new NoCodingPropertyAdmin(xltProperties);
        this.webClient = new XltWebClient();
        this.resolver = new VariableResolver(GeneralDataProvider.getInstance());
        initialize();
    }

    /**
     * Creates a new context out of the old context
     * 
     * @param context
     */
    public Context(final Context context)
    {
        this.dataStorage = context.getDataStorage();
        this.webClient = context.getWebClient();
        this.resolver = context.getResolver();
        this.webResponse = context.getWebResponse();
        this.propertyAdmin = context.getPropertyAdmin();
    }

    /**
     * Initializes the {@link Context} by loading some default items and configuring the {@link XltWebClient}
     */
    public void initialize()
    {
        configureWebClient();
    }

    public DataStorage getDataStorage()
    {
        return dataStorage;
    }

    /**
     * @return The {@link XltWebClient} that is used in this context
     */
    public XltWebClient getWebClient()
    {
        return webClient;
    }

    /**
     * @return The {@link WebResponse} of the current request
     */
    public WebResponse getWebResponse()
    {
        return webResponse;
    }

    /**
     * Sets {@link #webResponse} to parameter and sets {@link #sgmlPage} to <code>null</code>
     * 
     * @param webResponse
     */
    public void setWebResponse(final WebResponse webResponse)
    {
        this.webResponse = webResponse;
        setSgmlPage(null);
    }

    /**
     * Builds a {@link SgmlPage} if {@link #sgmlPage} is null, else returns {@link #sgmlPage}
     * 
     * @return The {@link SgmlPage} corresponding to {@link #webResponse}.
     */
    public SgmlPage getSgmlPage()
    {
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
                }
            }
            catch (FailingHttpStatusCodeException | IOException e)
            {
                e.printStackTrace();
            }
        }
        return sgmlPage;
    }

    public void setSgmlPage(final SgmlPage sgmlPage)
    {
        this.sgmlPage = sgmlPage;
    }

    public VariableResolver getResolver()
    {
        return resolver;
    }

    /**
     * @return The {@link NoCodingPropertyAdmin} that handles property related issues.
     */
    public NoCodingPropertyAdmin getPropertyAdmin()
    {
        return propertyAdmin;
    }

    /*
     * Data Storage
     */

    public DuplicateStorage getDefaultCookies()
    {
        return getDataStorage().getDefaultCookies();
    }

    public DuplicateStorage getDefaultParameters()
    {
        return getDataStorage().getDefaultParameters();
    }

    public SingleStorage getDefaultStatics()
    {
        return getDataStorage().getDefaultStatics();
    }

    public UniqueStorage getDefaultHeaders()
    {
        return getDataStorage().getDefaultHeaders();
    }

    public UniqueStorage getVariables()
    {
        return getDataStorage().getVariables();
    }

    public DefaultKeyValueStorage getDefaultItems()
    {
        return getDataStorage().getDefaultItems();
    }

    /*
     * Resolver
     */

    /**
     * Resolves every variable of a specified string. If a variable is specified and no value for it can be found, the
     * variable doesn't get resolved
     * 
     * @param toResolve
     *            The string which might have variables
     * @return A string with the resolved variables
     */
    public String resolveString(final String toResolve)
    {
        String resolvedString = null;
        if (toResolve != null)
        {
            resolvedString = resolver.resolveString(toResolve, this);
        }
        return resolvedString;
    }

    /*
     * Property Methods
     */

    public String getPropertyByKey(final String key)
    {
        return getPropertyAdmin().getPropertyByKey(key);
    }

    public String getPropertyByKey(final String key, final String defaultValue)
    {
        return getPropertyAdmin().getPropertyByKey(key, defaultValue);
    }

    public int getPropertyByKey(final String key, final int defaultValue)
    {
        return getPropertyAdmin().getPropertyByKey(key, defaultValue);
    }

    public boolean getPropertyByKey(final String key, final boolean defaultValue)
    {
        return getPropertyAdmin().getPropertyByKey(key, defaultValue);
    }

    public long getPropertyByKey(final String key, final long defaultValue)
    {
        return getPropertyAdmin().getPropertyByKey(key, defaultValue);
    }

    /**
     * Configures the webClient as specified in the config, thus dis-/enabling javascript, css, etc.
     */
    private void configureWebClient()
    {
        getPropertyAdmin().configWebClient(webClient);
    }

}
