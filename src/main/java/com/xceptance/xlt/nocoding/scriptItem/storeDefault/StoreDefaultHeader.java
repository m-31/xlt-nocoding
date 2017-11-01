package com.xceptance.xlt.nocoding.scriptItem.storeDefault;

import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.nocoding.util.Constants;
import com.xceptance.xlt.nocoding.util.Context;

/**
 * Stores a new default header
 * 
 * @author ckeiner
 */
public class StoreDefaultHeader extends StoreDefault
{

    /**
     * @param variableName
     *            The name of the header
     * @param value
     */
    public StoreDefaultHeader(final String variableName, final String value)
    {
        super(variableName, value);
    }

    @Override
    public void execute(final Context context) throws Throwable
    {
        // If the value is not "delete"
        if (!value.equals("delete"))
        {
            // Store the header
            context.storeDefaultHeader(variableName, value);
            XltLogger.runTimeLogger.debug("Added " + variableName + "=" + value + " to default header storage");
        }
        else
        {
            // If the variableName is Constants.HEADERS, then we delete all default headers
            if (variableName.equals(Constants.HEADERS))
            {
                context.deleteDefaultHeader();
            }
            // else we simply delete the specified header
            else
            {
                context.deleteDefaultHeader(variableName);
            }
            XltLogger.runTimeLogger.debug("Removed " + variableName + " from default header storage");
        }
    }

}
