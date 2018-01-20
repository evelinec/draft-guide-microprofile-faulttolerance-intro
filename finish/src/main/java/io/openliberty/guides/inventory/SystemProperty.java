package io.openliberty.guides.inventory;

import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SystemProperty {
    @Inject
    @ConfigProperty(name="io.openliberty.guides.inventory.fallback", 
                   defaultValue="false")
    private boolean fallbackBoolean;

    public  boolean getFallbackBoolean() {
      return fallbackBoolean;
    }
}
