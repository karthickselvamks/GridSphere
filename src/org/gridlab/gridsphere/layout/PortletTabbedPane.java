/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.layout;

import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.PortletResponse;
import org.gridlab.gridsphere.portlet.PortletRequest;
import org.gridlab.gridsphere.portlet.PortletContext;

import java.util.Vector;
import java.util.List;
import java.io.PrintWriter;
import java.io.IOException;

public class PortletTabbedPane extends BasePortletComponent {

    private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(PortletTabbedPane.class);

    private Vector tabs = new Vector();

    public PortletTabbedPane() {}

    public void addTab(String title, PortletPanel panel) {
        PortletTab tab = new PortletTab(this, title, panel);
        tabs.add(tab);
    }

    public String getBackgroundAt(int index)  {
        PortletTab tab = (PortletTab)tabs.get(index);
        return tab.getBackground();
    }

    public PortletPanel getPortletPanelAt(int index)  {
        PortletTab tab = (PortletTab)tabs.get(index);
        return tab.getPortletPanel();
    }

    public String getForegroundAt(int index) {
        PortletTab tab = (PortletTab)tabs.get(index);
        return tab.getForeground();
    }

    public PortletPanel getSelectedPortletPanel() {
        for (int i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getSelected())
                return tab.getPortletPanel();
        }
        return null;
    }

    public int getSelectedIndex() {
        for (int i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getSelected())
                return i;
        }
        return -1;
    }

    public PortletTab getSelectedTab() {
        for (int i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getSelected())
                return tab;
        }
        return null;
    }

    public int getTabCount() {
        return tabs.size();
    }

    public String getTitleAt(int index) {
        PortletTab tab = (PortletTab)tabs.get(index);
        return tab.getTitle();
    }

    public int indexOfPortletPanel(PortletPanel panel) {
        boolean found = false;
        int i;
        for (i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getPortletPanel().equals(panel)) {
                found = true;
                break;
            }
        }
        if (found)
            return i;
        return -1;
    }

    public int indexOfTab(String title) {
        boolean found = false;
        int i;
        for (i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getTitle().equals(title)) {
                found = true;
                break;
            }
        }
        if (found)
            return i;
        return -1;
    }

    public void insertTab(String title, PortletPanel panel, int index) {
        PortletTab tab = new PortletTab(this, title, panel);
        tabs.add(index, tab);
    }

    public void remove(PortletPanel panel) {
        for (int i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getPortletPanel().equals(panel)) {
                tabs.remove(tab);
            }
        }
    }

    public void remove(int index) {
        tabs.remove(index);
    }

    public void removeAll() {
        int i;
        for (i = 0; i < tabs.size(); i++) {
            tabs.remove(i);
        }
    }

    public void removeTabAt(int index) {
        tabs.remove(index);
    }

    public void setBackgroundAt(int index, String background) {
        PortletTab tab = (PortletTab)tabs.get(index);
        tab.setBackground(background);
    }

    public void setPortletPanelAt(int index, PortletPanel panel) {
        PortletTab tab = (PortletTab)tabs.get(index);
        tab.setPortletPanel(panel);
    }

    public void setForegroundAt(int index, String foreground) {
        PortletTab tab = (PortletTab)tabs.get(index);
        tab.setForeground(foreground);
    }

    public void setSelectedPortletPanel(PortletPanel c) {
        int i;
        unselectLastTab();
        for (i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getPortletPanel().equals(c)) {
                tab.setSelected(true);
                break;
            }
        }
    }

    public void setSelectedIndex(int index) {
        unselectLastTab();
        PortletTab tab = (PortletTab)tabs.get(index);
        tab.setSelected(true);
    }

    public void setSelectedTab(PortletTab tab) {
        unselectLastTab();
        for (int i = 0; i < tabs.size(); i++) {
            PortletTab t = (PortletTab)tabs.get(i);
            if (t.equals(tab)) {
                t.setSelected(true);
                break;
            }
        }
    }

    public void setTitleAt(int index, String title) {
        PortletTab tab = (PortletTab)tabs.get(index);
        tab.setTitle(title);
    }

    private void unselectLastTab() {
        for (int i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab)tabs.get(i);
            if (tab.getSelected())
                tab.setSelected(false);
        }
    }

    public void setPortletTabs(Vector tabs) {
        this.tabs = tabs;
    }

    public List getPortletTabs() {
        return tabs;
    }

    public void doRender(PortletContext ctx, PortletRequest req, PortletResponse res) throws PortletLayoutException, IOException {
        log.debug("in doRender()");

        PrintWriter out = res.getWriter();

        // First render tabs titles
        out.println("<table width=\"100%\">");
        for (int i = 0; i < tabs.size(); i++) {
            String title = getTitleAt(i);
            out.println("<th><b>" + title + "</b></th>");
        }
        out.println("</table>");
        PortletPanel panel = getSelectedPortletPanel();

        out.println("<table width=\"100%\">");
        out.println("<tr><td>");
        panel.doRender(ctx, req, res);
        out.println("</td></tr></table>");
    }

    public void doRenderFirst(PortletContext ctx, PortletRequest req, PortletResponse res) throws PortletLayoutException, IOException {
        doRender(ctx, req, res);
    }

    public void doRenderLast(PortletContext ctx, PortletRequest req, PortletResponse res) throws PortletLayoutException, IOException {}


}
