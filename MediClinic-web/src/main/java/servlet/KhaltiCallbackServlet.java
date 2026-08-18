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
@WebServlet("/payment/khalti/callback")
public class KhaltiCallbackServlet extends HttpServlet {

    @EJB
    private BillingService billingService;

    /**
     * Handels the HTTP GET requests.
     * 
     * This method is called when Khalti redirects the user back to the application after payment.
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        LoggedInUser loggedInUser = (session != null) ? (LoggedInUser) session.getAttribute("loggedInUser") : null;
        User actor = (loggedInUser != null) ? loggedInUser.getUser() : null;
        if (actor == null) {
            resp.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        String pidx = req.getParameter("pidx");
        String purchaseOrderId = req.getParameter("purchase_order_id");

        try {
            billingService.completeKhaltiPayment(actor, pidx, purchaseOrderId);
            resp.sendRedirect(req.getContextPath() + "/patient/billing.xhtml?khaltiStatus=success");
        } catch (AuthException e) {
            resp.sendRedirect(req.getContextPath() + "/patient/billing.xhtml?khaltiStatus=failed");
        }
    }
}
