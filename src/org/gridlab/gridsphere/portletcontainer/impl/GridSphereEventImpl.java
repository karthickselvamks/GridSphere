/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portletcontainer.impl;

import org.gridlab.gridsphere.layout.event.PortletComponentEvent;
import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.impl.*;
import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * The <code>GridSphereEventImpl</code> is an implementation of the <code>GridSphereEvent</code> interface.
 * <p>
 * A <code>GridSphereEvent</code> represents a general portlet container
 * event. The <code>GridSphereEvent</code> is passed into components that
 * need to access the <code>PortletRequest</code>
 * <code>PortletResponse</code> objects, such as the layout components.
 */
public class GridSphereEventImpl implements GridSphereEvent {

    protected transient PortletLog log = SportletLog.getInstance(GridSphereEventImpl.class);
    protected SportletRequest portletRequest;
    protected SportletResponse portletResponse;
    protected PortletContext portletContext;

    protected String portletComponentID = null;
    protected DefaultPortletAction action = null;
    protected DefaultPortletMessage message = null;

    protected Stack events = null;

    public GridSphereEventImpl(AccessControlManagerService  aclService, PortletContext ctx, HttpServletRequest req, HttpServletResponse res) {

        portletRequest = new SportletRequestImpl(req);
        portletResponse = new SportletResponse(res, portletRequest);
        portletContext = ctx;

        events = new Stack();

        portletComponentID = req.getParameter(SportletProperties.COMPONENT_ID);
        if (portletComponentID == null) {
            log.debug("Received a null component ID");
            portletComponentID = "";
        }
        /*
        try {
           portletComponentID = new Integer(cidStr).intValue();
        } catch (NumberFormatException e) {
           portletComponentID = -1;
           log.debug("Received a non-number portlet component ID: " + cidStr);
        }
        */

        /* This is where a DefaultPortletAction gets put together if one exists */

        Enumeration enum;
        String name, newname, value;
        String actionStr = portletRequest.getParameter(SportletProperties.DEFAULT_PORTLET_ACTION);
        if (actionStr != null) {
            log.debug("Received action=" + actionStr);

            action = new DefaultPortletAction(actionStr);
            String prefix = portletRequest.getParameter(SportletProperties.PREFIX);
            if (prefix != null) {
                enum = portletRequest.getParameterNames();

                while (enum.hasMoreElements()) {
                    name = (String) enum.nextElement();
                    if (name.startsWith(prefix)) {
                        newname = name.substring(prefix.length() + 1);
                        value = portletRequest.getParameter(name);
                        action.addParameter(newname, value);
                    }
                }
            }
        } else {

            /// Check to see if action is of form action_name generated by submit button
            enum = portletRequest.getParameterNames();
            while (enum.hasMoreElements()) {
                name = (String) enum.nextElement();
                if (name.startsWith(SportletProperties.DEFAULT_PORTLET_ACTION)) {
                    // check for parameter names and values

                    name = name.substring(SportletProperties.DEFAULT_PORTLET_ACTION.length() + 1);

                    StringTokenizer st = new StringTokenizer(name, "&");
                    newname = st.nextToken();
                    action = new DefaultPortletAction(newname);
                    log.debug("Received " + action);
                    String paramName = "";
                    String paramVal = "";
                    while (st.hasMoreTokens()) {
                        // now check for "=" separating name and value
                        String namevalue = st.nextToken();
                        int hasvalue = namevalue.indexOf("=");
                        if (hasvalue > 0) {
                            paramName = namevalue.substring(0, hasvalue);
                            paramVal = namevalue.substring(hasvalue+1);
                            action.addParameter(paramName, paramVal);
                        } else {
                            action.addParameter(namevalue, paramVal);
                        }
                    }
                }

            }
        }

        /* This is where a DefaultPortletMessage gets put together if one exists */
        String messageStr = portletRequest.getParameter(SportletProperties.DEFAULT_PORTLET_MESSAGE);
        if (messageStr != null) {
            log.debug("Received message: " + messageStr);
            message = new DefaultPortletMessage(messageStr);
        }

        /* This is where we get ACL info and update sportlet request */
        /*
        User user = portletRequest.getUser();
        if (! (user instanceof GuestUser) ) {
            log.debug("Role information for user: " + user.getUserID());
            List groups = aclService.getGroups(user);
            Iterator git = groups.iterator();
            PortletGroup group = null;
            while (git.hasNext()) {
                group = (PortletGroup)git.next();
                PortletRole role = aclService.getRoleInGroup(portletRequest.getUser(), group);
                portletRequest.setRole(group, role);
                log.debug("Group: " + group.toString() + " Role: " + role.toString());
            }
        }
        */
    }

    public PortletRequest getPortletRequest() {
        return portletRequest;
    }

    public PortletResponse getPortletResponse() {
        return portletResponse;
    }

    public PortletContext getPortletContext() {
        return portletContext;
    }

    public DefaultPortletAction getAction() {
        return action;
    }

    public boolean hasAction() {
        return (action != null) ? true : false;
    }

    public boolean hasMessage() {
        return false;
    }

    public DefaultPortletMessage getMessage() {
        return message;
    }

    public String getPortletComponentID() {
        return portletComponentID;
    }

    public void addNewRenderEvent(PortletComponentEvent evt) {
        if (evt != null) events.push(evt);
    }


    public PortletComponentEvent getLastRenderEvent() {
        return (events.isEmpty() ? null : (PortletComponentEvent)events.pop());
    }
}
