/**
 * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
 * @version $Id: ComponentLifecycle.java 4986 2006-08-04 09:54:38Z novotny $
 */
package org.gridsphere.layout;

import org.gridsphere.portletcontainer.GridSphereEvent;

import javax.portlet.PortletRequest;
import java.util.List;

/**
 * The <code>ComponentLifecycle</code> represents the lifecycle methods required by any
 * PortletComponent.
 */
public interface ComponentLifecycle extends ComponentRender {

    /**
     * Initializes the portlet component. Since the components are isolated
     * after Castor unmarshalls from XML, the ordering is determined by a
     * passed in List containing the previous portlet components in the tree.
     *
     * @param list a list of component identifiers
     * @return a list of updated component identifiers
     * @see ComponentIdentifier
     */
    public List init(PortletRequest req, List list);

    /**
     * Performs an action on this portlet component
     *
     * @param event a gridsphere event
     */
    public void actionPerformed(GridSphereEvent event);

    /**
     * Destroys this portlet component
     */
    public void destroy();

    /**
     * Returns the associated portlet component id
     *
     * @return the portlet component id
     */
    public int getComponentID();

    /**
     * Returns the associated portlet component id variable
     *
     * @param the portlet request
     * @return the portlet component id variable
     */
    public String getComponentIDVar(PortletRequest req);
}