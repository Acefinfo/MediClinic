/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filter;

import com.mycompany.LoggedInUser;
import entity.User;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author acefonfo
 */
public class AuthFilter  implements Filter{
    
    private static final String[] PUBLIC_PAGES ={
        "/register.xhtml", 
        "/register-success.xhtml", 
        "/login.xhtml",
        "/forgot-password.xhtml", 
        "/reset-password.xhtml", 
        "/verify.xhtml",
        "/index.html"
    };
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        
        if (isPublic(uri) || uri.contains("/javax.faces.resource") || uri.contains("/resources/")) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpSession session = req.getSession(false);
        LoggedInUser loggedInUser = (session != null) ? (LoggedInUser) session.getAttribute("loggedInUser") : null;
        User user = (loggedInUser != null) ? loggedInUser.getUser() : null;
        
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }
        
        
        String role = (user.getRole() != null) ? user.getRole().getName() : "";
        
        if (uri.startsWith("/admin/") && !"ADMIN".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (uri.startsWith("/doctor/") && !"DOCTOR".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (uri.startsWith("/receptionist/") && !"RECEPTIONIST".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (uri.startsWith("/patient/") && !"PATIENT".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy(){}
    
    private boolean isPublic(String uri) {
        for (String page : PUBLIC_PAGES) {
            if (uri.equals(page)) {
                return true;
            }
        }
        return false;
    }
}
