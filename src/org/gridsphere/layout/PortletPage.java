/*
 * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
 * @version $Id: PortletPage.java 5032 2006-08-17 18:15:06Z novotny $
 */
package org.gridsphere.layout;

import org.gridsphere.layout.view.Render;
import org.gridsphere.portlet.impl.SportletProperties;
import org.gridsphere.portlet.service.PortletServiceException;
import org.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridsphere.portletcontainer.GridSphereEvent;
import org.gridsphere.portletcontainer.impl.PortletInvoker;
import org.gridsphere.services.core.cache.CacheService;
import org.gridsphere.services.core.persistence.PersistenceManagerException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * The <code>PortletPage</code> is the generic container for a collection of
 * concrete portlet components and provides lifecycle methods for traversing
 * the tree of components and handling actions and performing rendering.
 */
public class PortletPage extends BasePortletComponent implements Serializable, Cloneable {

    protected transient CacheService cacheService = null;

    protected transient PortletInvoker portletInvoker = null;

    protected PortletContainer footerContainer = null;
    protected PortletContainer headerContainer = null;
    //protected PortletTabbedPane tabbedPane = null;
    protected PortletComponent component = null;

    // The component ID's of each of the layout components
    protected List componentIdentifiers = new Vector();

    protected String keywords = "";
    protected String title = "";
    protected String icon = "images/favicon.ico";
    protected int refresh = 0;
    protected boolean editable = true;

    private String layoutDescriptor = null;

    private Hashtable labelsHash = new Hashtable();
    private Hashtable portletHash = new Hashtable();

    private transient Render pageView = null;

    private String renderKit = "brush";

    /**
     * Constructs an instance of PortletPage
     */
    public PortletPage() {
    }

    public void setLayoutDescriptor(String layoutDescriptor) {
        this.layoutDescriptor = layoutDescriptor;
    }

    public String getLayoutDescriptor() {
        return layoutDescriptor;
    }

    /**
     * Sets the portlet container title
     *
     * @param title the portlet container title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the portlet container title
     *
     * @return the portlet container title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Boolean flag to determine if this layout can be customized
     *
     * @param editable flag to determine if this layout can be customized
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Boolean flag to determine if this layout can be customized
     *
     * @return true if this layout can be customized
     */
    public boolean getEditable() {
        return editable;
    }

    /**
     * Returns the favicon for the page
     *
     * @return the favicon for the page
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the favicon for the page
     *
     * @param icon the favicon for the page
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Returns the page refresh rate
     *
     * @return the page refresh rate
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Sets the page refresh rate
     *
     * @param refresh the page refresh rate
     */
    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    /**
     * Returns the keywords used in rendering output
     *
     * @return keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Sets the keywords used in rendering output
     *
     * @param keywords used in rendering output
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Returns the render kit  'standard' or 'classic'
     *
     * @return the render kit  'standard' or 'classic'
     */
    public String getRenderKit() {
        return renderKit;
    }

    /**
     * Sets the render kit  'standard' or 'classic'
     *
     * @param renderKit the render kit
     */
    public void setRenderKit(String renderKit) {
        this.renderKit = renderKit;
    }

    /**
     * Sets the page header
     *
     * @param headerContainer a portlet container with header components
     */
    public void setPortletHeader(PortletContainer headerContainer) {
        this.headerContainer = headerContainer;
    }

    /**
     * Returns the page header
     *
     * @return a portlet container with header components
     */
    public PortletContainer getPortletHeader() {
        return headerContainer;
    }

    /**
     * Sets the page footer
     *
     * @param footerContainer a portlet container with footer components
     */
    public void setPortletFooter(PortletContainer footerContainer) {
        this.footerContainer = footerContainer;
    }

    /**
     * Returns the page footer
     *
     * @return a portlet container with footer components
     */
    public PortletContainer getPortletFooter() {
        return footerContainer;
    }

    public void setPortletComponent(PortletComponent component) {
        this.component = component;
    }


    public PortletComponent getPortletComponent() {
        return component;
    }

    /**
     * Returns the list of portlet component identifiers
     *
     * @return the list of portlet component identifiers
     * @see ComponentIdentifier
     */
    public List getComponentIdentifierList() {
        return componentIdentifiers;
    }

    /**
     * Sets the list of portlet component identifiers
     *
     * @param componentIdentifiers a list of portlet component identifiers
     * @see ComponentIdentifier
     */
    public void setComponentIdentifierList(List componentIdentifiers) {
        this.componentIdentifiers = componentIdentifiers;
    }

    /**
     * Returns the associated portlet component id
     *
     * @return the portlet component id
     */
    public int getComponentID() {
        return COMPONENT_ID;
    }

    /**
     * Initializes the portlet component. Since the components are isolated
     * after Castor unmarshalls from XML, the ordering is determined by a
     * passed in List containing the previous portlet components in the tree.
     *
     * @param list a list of component identifiers
     * @return a list of updated component identifiers
     * @see ComponentIdentifier
     */
    public List init(PortletRequest req, List list) {

        try {
            cacheService = (CacheService) PortletServiceFactory.createPortletService(CacheService.class, true);
        } catch (PortletServiceException e) {
            System.err.println("Unable to init Cache service! " + e.getMessage());
        }
        portletInvoker = new PortletInvoker();
        list = super.init(req, list);
        if (renderKit == null) renderKit = "standard";
        req.getPortletSession().setAttribute(SportletProperties.LAYOUT_RENDERKIT, renderKit, PortletSession.APPLICATION_SCOPE);


        pageView = (Render)getRenderClass(req, "Page");

        if (headerContainer != null) {
            list = headerContainer.init(req, list);
        }

        if (component != null) {
            list = component.init(req, list);
        }


        if (footerContainer != null) {
            list = footerContainer.init(req, list);
        }

        componentIdentifiers = list;

        // Now go thru and create a labels hash

        Iterator it = componentIdentifiers.iterator();
        while (it.hasNext()) {
            ComponentIdentifier cid = (ComponentIdentifier) it.next();
            String compLabel = cid.getComponentLabel();
            if (cid.hasPortlet()) {
                String portletClass = cid.getPortletClass();
                portletHash.put(portletClass, new Integer(cid.getComponentID()));
            }
            if (!compLabel.equals("")) {
                // create a labels to integer component id mapping
                labelsHash.put(compLabel, new Integer(cid.getComponentID()));
            }
        }

        return componentIdentifiers;
    }

    /**
     * Destroys this portlet container
     */
    public void destroy() {
        if (headerContainer != null) headerContainer.destroy();
        if (component != null) component.destroy();
        if (footerContainer != null) footerContainer.destroy();
    }

    /**
     * Performs an action by performing an action on the appropriate portlet component
     * contained by this PortletPage
     *
     * @param event a gridsphere event
     */
    public void actionPerformed(GridSphereEvent event) {
        // if there is a layout action do it!
        PortletRequest req = event.getActionRequest();
        String cid = event.getComponentID();
        if (cid != null) {
            PortletComponent comp = getActiveComponent(cid);
            if (comp != null) {
                System.err.println("Calling action performed on " + comp.getClass().getName() + ":" + comp.getName() + " label=" + comp.getLabel());
                String reqRole = comp.getRequiredRole();
                if (reqRole.equals("") || req.isUserInRole(reqRole)) comp.actionPerformed(event);
            }
        }
    }

    public PortletComponent getActiveComponent(String cid) {
        // the component id determines where in the list the portlet component is
        // first check the hash

        /*
        List compList = getComponentIdentifierList();
        for (int i = 0; i < compList.size(); i++)  {
            ComponentIdentifier compid = (ComponentIdentifier)compList.get(i);
            System.err.println(compid.toString());
        }
        */

        ComponentIdentifier compId = null;
        int compIntId;
        if (labelsHash.containsKey(cid)) {
            Integer cint = (Integer) labelsHash.get(cid);
            compIntId = cint.intValue();
            compId = (ComponentIdentifier) componentIdentifiers.get(compIntId);
        } else {
            // try converting to integer
            try {
                compIntId = Integer.parseInt(cid);
                // number can't exceed available components
                if (compIntId < componentIdentifiers.size()) {
                    compId = (ComponentIdentifier) componentIdentifiers.get(compIntId);
                }
            } catch (NumberFormatException e) {
                System.err.println("unable to convert cid=" + cid);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("unable to convert cid=" + cid);
            }
        }
        return (compId != null) ? compId.getPortletComponent() : null;
    }


    /**
     * Renders the portlet cotainer by performing doRender on all portlet components
     *
     * @param event a gridsphere event
     */
    public void doRender(GridSphereEvent event) {
        // handle any client logic to determin which markup to display

        super.doRender(event);
        RenderRequest req = event.getRenderRequest();

        boolean floating = false;
        PortletFrame f = null;
        // In case the "floating" portlet state has been selected:
        String wstate = req.getParameter(SportletProperties.PORTLET_WINDOW);
        if ((wstate != null) && (wstate.equalsIgnoreCase("FLOATING"))) {
            String cid = event.getComponentID();
            PortletComponent comp = getActiveComponent(cid);

            PortletComponent pc = comp.getParentComponent();
            if (comp instanceof PortletFrame) {
                f = (PortletFrame) comp;
            } else if (pc != null) {
                if (pc instanceof PortletFrame) {
                    f = (PortletFrame) pc;
                }
            }

            if (f != null) {
                // render portlet frame in pop-up without titlebar
                f.setTransparent(true);
                req.setAttribute(CacheService.NO_CACHE, CacheService.NO_CACHE);
                req.setAttribute(SportletProperties.FLOAT_STATE, "true");

                String reqRole = f.getRequiredRole();

                if (req.isUserInRole(reqRole) || (reqRole.equals(""))) {
                    f.doRender(event);
                }


                f.setTransparent(false);
                floating = true;
                //writer.println(f.getBufferedOutput(req));
            }
        } else {


            // A Portal page in 3 lines -- voila!
            //  -------- header ---------
            if (headerContainer != null) {
                headerContainer.setStyle(PortletContainer.STYLE_HEADER);
                headerContainer.doRender(event);
                //writer.println(headerContainer.getBufferedOutput(req));
            }

            // ..| tabs | here |....
            if (component != null) {

                component.doRender(event);
                //writer.println(tabbedPane.getBufferedOutput(req));
            }
            //.... the footer ..........
            if (footerContainer != null) {
                footerContainer.setStyle(PortletContainer.STYLE_FOOTER);
                footerContainer.doRender(event);
                //writer.println(footerContainer.getBufferedOutput(req));
            }

        }

        StringBuffer page = new StringBuffer();
        page.append(pageView.doStart(event, this));
        if (floating) page.append(f.getBufferedOutput(req));
        if (headerContainer != null) page.append(headerContainer.getBufferedOutput(req));
        if (component != null) page.append(component.getBufferedOutput(req));
        if (footerContainer != null) page.append(footerContainer.getBufferedOutput(req));
        page.append(pageView.doEnd(event, this));

        setBufferedOutput(req, page);

    }

    public Object clone() throws CloneNotSupportedException {
        int i;
        PortletPage c = (PortletPage) super.clone();
        c.COMPONENT_ID = this.COMPONENT_ID;
        c.renderKit = this.renderKit;
        List compList = new Vector(this.componentIdentifiers.size());
        for (i = 0; i < this.componentIdentifiers.size(); i++) {
            ComponentIdentifier cid = (ComponentIdentifier) this.componentIdentifiers.get(i);
            compList.add(new ComponentIdentifier(cid));
        }
        c.componentIdentifiers = compList;
        c.title = title;
        c.headerContainer = (this.headerContainer == null) ? null : (PortletContainer) this.headerContainer.clone();
        c.footerContainer = (this.footerContainer == null) ? null : (PortletContainer) this.footerContainer.clone();
        c.component = (this.component == null) ? null : (PortletTabbedPane) this.component.clone();
        return c;
    }

    public void save(ServletContext ctx) throws IOException {
        if (component instanceof PortletTabbedPane) {
            PortletTabbedPane tabbedPane = (PortletTabbedPane)component;
            try {
                // save user tab
                PortletTabbedPane myPane = new PortletTabbedPane();
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    PortletTab tab = tabbedPane.getPortletTabAt(i);
                    if (tab.getCanModify()) {
                        myPane.addTab(tab);
                    }
                }
                if (myPane.getTabCount() > 0) {
                    PortletLayoutDescriptor.savePortletTabbedPane(myPane, layoutDescriptor, LAYOUT_MAPPING_PATH);
                }
            } catch (PersistenceManagerException e) {
                throw new IOException("Unable to save user's tabbed pane: " + e.getMessage());
            }
        }
    }

    /**
     * Processes a message. The message is directed at a concrete portlet with
     * a given concrete portlet ID. If the target ID is "*" the message is delivered
     * to every portlet in the PortletPage.
     *
     * @param concPortletID The target concrete portlet's ID
     * @param msg           The message to deliver
     * @param event         The GridsphereEvent associated with the message delivery
     */
    /*
    public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {

        // support for broadcast messages
        if (concPortletID.equals("*")) {
            Iterator entryIter = portletHash.keySet().iterator();
            while (entryIter.hasNext()) {
                Map.Entry entry = (Map.Entry) entryIter.next();
                Integer cint = (Integer) entry.getValue();
                String portletID = (String) entry.getKey();

                int compIntId = cint.intValue();
                ComponentIdentifier compId = (ComponentIdentifier) componentIdentifiers.get(compIntId);

                if (compId != null) {
                    PortletComponent comp = compId.getPortletComponent();

                    // perform an action if the component is non null
                    if (comp == null) {
                        //log.warn("Event has invalid component id associated with it!");
                    } else {
                        //log.debug("Calling action performed on " + comp.getClass().getName() + ":" + comp.getName());
                        comp.messageEvent(portletID, msg, event);
                    }
                }
            }
            return;
        }

        // the component id determines where in the list the portlet component is
        // first check the hash
        ComponentIdentifier compId = null;

        int compIntId;
        if (portletHash.containsKey(concPortletID)) {
            Integer cint = (Integer) portletHash.get(concPortletID);
            compIntId = cint.intValue();
            compId = (ComponentIdentifier) componentIdentifiers.get(compIntId);
        } else {
            log.debug("Delivery of the message " + msg.toString() + " failed: " + concPortletID + " not found");
        }

        if (compId != null) {
            PortletComponent comp = compId.getPortletComponent();
            // perform an action if the component is non null
            if (comp == null) {
                //log.warn("Event has invalid component id associated with it!");
            } else {
                //log.debug("Calling action performed on " + comp.getClass().getName() + ":" + comp.getName());
                comp.messageEvent(concPortletID, msg, event);
            }
        }
    }
    */
}
