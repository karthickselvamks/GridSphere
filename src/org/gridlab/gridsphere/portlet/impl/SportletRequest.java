/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlet.impl;

import org.gridlab.gridsphere.portlet.*;

/**
 * A <code>SportletRequest</code> turns the immutable <code>PortletRequest</code>
 * into a stateful mutable object with all the additional set methods not defined
 * by the <code>PortletRequest</code> interface.
 */
public interface SportletRequest extends PortletRequest {

    /**
     * Sets the client device that the user connects to the portal with.
     *
     * @param client the client device
     */
    public void setClient(Client client);

    /**
     * Sets the data of the concrete portlet instance
     * If the portlet is run in <code>CONFIGURE</code> mode, the portlet data
     * is not accessible and this method will return <code>null</code>
     *
     * @param data the portlet data the <code>PortletData</code>
     */
    public void setData(PortletData data);

    /**
     * Sets the roles this user has in the associated portlet
     *
     * @param role the user's <code>PortletRole</code>
     */
    public void setRole(PortletRole role);

    /**
     * Sets the group associated with this portlet request
     *
     * @param group the user's <code>PortletGroup</code>
     */
    public void setGroup(PortletGroup group);

    /**
     * Sets the PortletSettings object of the concrete portlet.
     *
     * @param settings the portlet settings
     */
    public void setPortletSettings(PortletSettings settings);

    /**
     * Sets the mode that the portlet is running in.
     *
     * @param mode the portlet mode
     */
    public void setMode(Portlet.Mode mode);

    /**
     * Sets the previos portlet mode.
     *
     * @param previousMode the previous portlet mode
     */
    public void setPreviousMode(Portlet.Mode previousMode);

    /**
     * Returns the window state that the portlet is running in.
     *
     * @param state the portlet window state
     */
    public void setWindowState(PortletWindow.State state);

}
