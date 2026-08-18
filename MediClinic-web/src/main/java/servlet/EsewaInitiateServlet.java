/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import com.mycompany.LoggedInUser;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import service.AuthException;
import service.BillingService;
import util.EsewaConfig;

/**
 *
 * @author acefonfo
 */
@WebServlet("/payment/esewa/initiate/*")
public class EsewaInitiateServlet extends HttpServlet {
    
    @EJB
    private BillingService billingService;

    /**
     * Handles GET requests sent to this servlet.
     * 
     * The main purpose of this method is to start the eSewa payment process 
     * for a specific invoice.
     * 
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long invoiceId = parseId(req.getPathInfo());
        if (invoiceId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpSession session = req.getSession(false);
        LoggedInUser loggedInUser = (session != null) ? (LoggedInUser) session.getAttribute("loggedInUser") : null;
        User actor = (loggedInUser != null) ? loggedInUser.getUser() : null;
        if (actor == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();

        Map<String, String> fields;
        try {
            fields = billingService.buildEsewaFormFields(actor, invoiceId, baseUrl);
        } catch (AuthException e) {
            resp.sendRedirect(req.getContextPath() + "/patient/billing.xhtml?esewaStatus=failed");
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html><head><title>Redirecting to eSewa...</title></head>");
            out.println("<body onload=\"document.forms[0].submit()\">");
            out.println("<p>Redirecting to eSewa, please wait...</p>");
            out.println("<form method=\"POST\" action=\"" + EsewaConfig.FORM_URL + "\">");
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                out.println("<input type=\"hidden\" name=\"" + escape(entry.getKey()) + "\" value=\"" + escape(entry.getValue()) + "\"/>");
            }
            out.println("</form></body></html>");
        }
    }

    /**
     * Escapes special HTML characters.
     * 
     * This is important when inserting values into HTML attributes.
     * Without escaping, characters such as &, ", <, and > could
     * cause invalid HTML or potentially create an HTML injection
     * vulnerability.
     * 
     * @param s
     * @return 
     */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Extracts the invoice Id from the servlet path.
     * 
     * Example:
     * pathInfo = "/123"
     *
     * The method removes the leading "/" and converts
     * "123" into a Long value.
     * 
     * @param pathInfo
     * @return 
     */
    private Long parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.length() < 2) return null;
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}