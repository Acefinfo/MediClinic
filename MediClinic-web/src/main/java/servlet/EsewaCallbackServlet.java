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
@WebServlet("/payment/esewa/callback")
public class EsewaCallbackServlet extends HttpServlet {

    @EJB
    private BillingService billingService;

    /**
     * Handles the FET request sent to the eSewa callback URL.
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

        String data = req.getParameter("data");
        try {
            billingService.completeEsewaPayment(actor, data);
            resp.sendRedirect(req.getContextPath() + "/patient/billing.xhtml?esewaStatus=success");
        } catch (AuthException e) {
            resp.sendRedirect(req.getContextPath() + "/patient/billing.xhtml?esewaStatus=failed");
        }
    }
}
