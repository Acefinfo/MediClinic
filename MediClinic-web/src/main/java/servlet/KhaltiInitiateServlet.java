/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import com.mycompany.LoggedInUser;
import entity.User;
import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import service.AuthException;
import service.BillingService;

/**
 *
 * @author acefonfo
 */
@WebServlet("/payment/khalti/initiate/*")
public class KhaltiInitiateServlet extends HttpServlet {

    @EJB
    private BillingService billingService;

    /**
     * Handles GET requests made to this servlet.
     *
     * The method: 1. Gets the invoice ID from the URL. 2. Checks whether a user
     * is logged in. 3. Builds the application's base URL. 4. Requests a Khalti
     * payment URL from BillingService. 5. Redirects the user to Khalti.
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

        try {
            String paymentUrl = billingService.initiateKhaltiPayment(actor, invoiceId, baseUrl);
            resp.sendRedirect(paymentUrl);
        } catch (AuthException e) {
            System.out.println("KHALTI INIT FAILED: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/patient/billing.xhtml?khaltiStatus=failed");
        }
    }

    /**
     * Extracts and converts the invoice ID from the URL path.
     *
     * Example: If the request URL is: /payment/khalti/initiate/123
     *
     * getPathInfo() returns: /123
     *
     * This method removes the "/" and converts "123" into a Long value.
     *
     * @param pathInfo
     * @return
     */
    private Long parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.length() < 2) {
            return null;
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
