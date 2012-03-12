
package org.mule.munit.config;

import org.mule.api.MuleException;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.config.MuleManifest;
import org.mule.munit.DBServerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>DBServerModuleLifecycleAdapter</code> is a wrapper around {@link DBServerModule } that adds lifecycle methods to the pojo.
 * 
 */
public class DBServerModuleLifecycleAdapter
    extends DBServerModuleCapabilitiesAdapter
    implements Disposable, Initialisable, Startable, Stoppable
{


    public void start()
        throws MuleException
    {
    }

    public void stop()
        throws MuleException
    {
    }

    public void initialise() {
        Logger log = LoggerFactory.getLogger(DBServerModuleLifecycleAdapter.class);
        String runtimeVersion = MuleManifest.getProductVersion();
        if (runtimeVersion.equals("Unknown")) {
            log.warn("Unknown Mule runtime version. This module may not work properly!");
        } else {
            String[] expectedMinVersion = "3.2".split("\\.");
            if (runtimeVersion.contains("-")) {
                runtimeVersion = runtimeVersion.split("-")[ 0 ];
            }
            String[] currentRuntimeVersion = runtimeVersion.split("\\.");
            for (int i = 0; (i<expectedMinVersion.length); i ++) {
                try {
                    if (Integer.parseInt(currentRuntimeVersion[i])<Integer.parseInt(expectedMinVersion[i])) {
                        throw new RuntimeException("This module is only valid for Mule 3.2");
                    }
                } catch (NumberFormatException nfe) {
                    log.warn("Error parsing Mule version, cannot validate current Mule version");
                }
            }
        }
    }

    public void dispose() {
    }

}
