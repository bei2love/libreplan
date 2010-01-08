package org.zkoss.ganttz.print;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.servlets.CallbackServlet;
import org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler;
import org.zkoss.zk.ui.Executions;

public class CutyPrint {

    private static final Log LOG = LogFactory.getLog(CutyPrint.class);

    private static final String CUTYCAPT_COMMAND = "/usr/bin/CutyCapt ";

    // Calculate dynamically width and delay parameters
    private static final String CUTYCAPT_PARAMETERS = " --min-width=2500 --delay=1000 ";

    public static void print() {

        HttpServletRequest request = (HttpServletRequest) Executions
                .getCurrent().getNativeRequest();

        String url = CallbackServlet.registerAndCreateURLFor(request,
                new IServletRequestHandler() {

                    @Override
                    public void handle(HttpServletRequest request,
                            HttpServletResponse response)
                            throws ServletException, IOException {

                        String forwardURL = "/planner/index.zul";
                        String orderId = request.getParameter("order");

                        if ((orderId != null) && !(orderId.equals(""))) {
                            forwardURL += ";order=" + orderId;
                        }

                        // Pending to forward and process additional parameters
                        // as show labels or resources
                        request.getRequestDispatcher(forwardURL).forward(
                                request, response);
                    }
                });

        String CUTYCAPT_URL = "--url=http://" + request.getLocalName() + ":"
                + request.getLocalPort() + url;
        String captureString = CUTYCAPT_COMMAND + CUTYCAPT_URL;

        captureString += CUTYCAPT_PARAMETERS;

        String absolutePath = request.getSession().getServletContext()
                .getRealPath("/");
        String filename = "/print/"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + ".pdf";
        captureString += "--out=" + absolutePath + filename;

        try {
            // CutyCapt command execution
            // If there is a not real X server environment, use framebuffer
            LOG.debug(captureString);
            Process print;
            Process server = null;
            if ((System.getenv("DISPLAY") == null)
                    || (System.getenv("DISPLAY").equals(""))) {
                String[] serverEnvironment = { "PATH=$PATH" };
                server = Runtime.getRuntime().exec("env - Xvfb :99",
                        serverEnvironment);
                String[] environment = { "DISPLAY=:99.0" };
                print = Runtime.getRuntime().exec(captureString, environment);
            } else {
                print = Runtime.getRuntime().exec(captureString);
            }
            try {
                print.waitFor();
                print.destroy();
                if ((System.getenv("DISPLAY") == null)
                        || (System.getenv("DISPLAY").equals(""))) {
                    server.destroy();
                }
                Executions.getCurrent().sendRedirect(filename, "_blank");
            } catch (InterruptedException e) {
                LOG.error(_("Could open generated PDF"), e);
            }

        } catch (IOException e) {
            LOG.error(_("Could not execute print command"), e);
        }
    }

}
