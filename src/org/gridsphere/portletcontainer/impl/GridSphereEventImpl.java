/*
 * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
 * @version $Id: GridSphereEventImpl.java 5032 2006-08-17 18:15:06Z novotny $
 */
package org.gridsphere.portletcontainer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gridsphere.layout.event.PortletComponentEvent;
import org.gridsphere.portletcontainer.Client;
import org.gridsphere.portletcontainer.DefaultPortletAction;
import org.gridsphere.portlet.impl.*;
import org.gridsphere.portletcontainer.GridSphereEvent;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * The <code>GridSphereEventImpl</code> is an implementation of the <code>GridSphereEvent</code> interface.
 * <p/>
 * A <code>GridSphereEvent</code> represents a general portlet container
 * event. The <code>GridSphereEvent</code> is passed into components that
 * need to access the <code>PortletRequest</code>
 * <code>PortletResponse</code> objects, such as the layout components.
 */
public class GridSphereEventImpl implements GridSphereEvent {

    protected Log log = LogFactory.getLog(GridSphereEventImpl.class);

    protected HttpServletRequest req;
    protected HttpServletResponse res;

    protected PortletContext portletContext;

    protected RenderRequest renderRequest;
    protected RenderResponse renderResponse;
    protected ActionRequest actionRequest;
    protected ActionResponse actionResponse;

    protected String componentID = null;
    protected String layoutID = null;

    protected DefaultPortletAction action = null;

    protected Stack events = null;

    public GridSphereEventImpl(PortletContext ctx, HttpServletRequest req, HttpServletResponse res) {

        this.req = req;
        this.res = res;
        this.portletContext = ctx;

        this.renderRequest = new RenderRequestImpl(req, ctx);
        this.renderResponse = new RenderResponseImpl(req, res);
        this.actionRequest = new ActionRequestImpl(req, ctx);
        this.actionResponse = new ActionResponseImpl(req, res);

        renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
        renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);

        events = new Stack();

        String compVar = (String)req.getAttribute(SportletProperties.COMPONENT_ID_VAR);
        if (compVar == null) compVar = SportletProperties.COMPONENT_ID;
        componentID = req.getParameter(compVar);
        if (componentID == null) {
            log.debug("Received a null component ID");
            componentID = "";
        } else {
            log.debug("Received cid= " + componentID);
        }
        //req.setAttribute(SportletProperties.COMPONENT_ID, componentID);

        layoutID = req.getParameter(SportletProperties.LAYOUT_PAGE_PARAM);
        if (layoutID == null) {
            log.debug("Received a null layout ID");
            layoutID = "";
        } else {
            log.debug("Received layout id= " + layoutID);
            req.setAttribute(SportletProperties.LAYOUT_PAGE, layoutID);
        }


        action = createAction(req);

        //log.debug("Received action=" + action);
        /* This is where a DefaultPortletMessage gets put together if one exists */
        /*
        String messageStr = portletRequest.getParameter(SportletProperties.DEFAULT_PORTLET_MESSAGE);
        if (messageStr != null) {
            log.debug("Received message: " + messageStr);
            message = new DefaultPortletMessage(messageStr);
        }
        */
    }

    public static DefaultPortletAction createAction(HttpServletRequest request) {
        /* This is where a DefaultPortletAction gets put together if one exists */
        DefaultPortletAction myaction = null;
        Enumeration e = null;
        String name, newname, value;
        String actionStr = null;

        actionStr = request.getParameter(SportletProperties.DEFAULT_PORTLET_ACTION);


        if (actionStr != null) {
            myaction = new DefaultPortletAction(actionStr);
            String prefix = null;

            prefix = request.getParameter(SportletProperties.PREFIX);
            e =  request.getParameterNames();

            if ((prefix != null) && (e != null)) {
                while (e.hasMoreElements()) {
                    name = ((String) e.nextElement());
                    if (name.startsWith(prefix)) {
                        newname = name.substring(prefix.length() + 1);

                        value =  request.getParameter(name);


                        //newname = decodeUTF8(newname);
                        //value = decodeUTF8(newname);
                        myaction.addParameter(newname, value);
                    }
                }
            }
        } else {

            e =  request.getParameterNames();


            if (e != null) {

                /// Check to see if action is of form action_name generated by submit button
                while (e.hasMoreElements()) {
                    name = (String) e.nextElement();
                    if (name.startsWith(SportletProperties.DEFAULT_PORTLET_ACTION)) {
                        // check for parameter names and values

                        name = name.substring(SportletProperties.DEFAULT_PORTLET_ACTION.length() + 1);

                        StringTokenizer st = new StringTokenizer(name, "&");
                        if (st.hasMoreTokens()) {
                            newname = st.nextToken();
                        } else {
                            newname = "";
                        }
                        myaction = new DefaultPortletAction(newname);
                        //log.debug("Received " + myaction);
                        String paramName;
                        String paramVal = "";
                        Map tmpParams = new HashMap();
                        String prefix = "";
                        while (st.hasMoreTokens()) {
                            // now check for "=" separating name and value
                            String namevalue = st.nextToken();
                            int hasvalue = namevalue.indexOf("=");
                            if (hasvalue > 0) {
                                paramName = namevalue.substring(0, hasvalue);
                                paramVal = namevalue.substring(hasvalue + 1);
                                if (paramName.equals(SportletProperties.PREFIX)) {
                                    prefix = paramVal;
                                } else {
                                    tmpParams.put(paramName, paramVal);
                                }
                            } else {
                                tmpParams.put(namevalue, paramVal);
                            }
                        }
                        // put unprefixed params in action
                        for (Object o : tmpParams.keySet()) {
                            String n = (String) o;
                            String v = (String) tmpParams.get(n);
                            if (!prefix.equals("")) {
                                n = n.substring(prefix.length() + 1);
                            }

                            myaction.addParameter(n, v);
                        }
                    }
                }
            }
        }
        return myaction;
    }

    /**
     * Returns an object representing the client device that the user connects to the portal with.
     *
     * @return the client device
     */
    public Client getClient() {
        Client client = (Client) this.getHttpServletRequest().getSession().getAttribute(SportletProperties.CLIENT);
        if (client == null) {
            client = new ClientImpl(this.getHttpServletRequest());
            this.getHttpServletRequest().getSession().setAttribute(SportletProperties.CLIENT, client);
        }
        return client;
    }

    public HttpServletRequest getHttpServletRequest() {
        return req;
    }

    public HttpServletResponse getHttpServletResponse() {
        return res;
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public RenderResponse getRenderResponse() {
        return renderResponse;
    }

    public ActionRequest getActionRequest() {
        return actionRequest;
    }

    public ActionResponse getActionResponse() {
        return actionResponse;
    }

    public PortletContext getPortletContext() {
        return portletContext;
    }

    public DefaultPortletAction getAction() {
        return action;
    }

    public boolean hasAction() {
        return (action != null);
    }

    public boolean hasMessage() {
        return false;
    }

    /*
    public PortletMessage getMessage() {
        return message;
    }
    */

    public String getComponentID() {
        return componentID;
    }

    public String getLayoutID() {
        return layoutID;
    }

    public void addNewRenderEvent(PortletComponentEvent evt) {
        if (evt != null) events.push(evt);
    }

    public PortletComponentEvent getLastRenderEvent() {
        return (events.isEmpty() ? null : (PortletComponentEvent) events.pop());
    }
}