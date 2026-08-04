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
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filters incoming requests to ensure that:
 * 
 *   Public pages remain accessible without login
 *   Protected pages require user authentication.
 *   Users can only access pages assigned to their role. 
 *
 * @author acefonfo
 */

@WebFilter("/*")
public class AuthFilter  implements Filter{

    /**
     * List of pages that can be accessed without authentication.
     */
    private static final String[] PUBLIC_PAGES = {
        "/register.xhtml",
        "/register-success.xhtml",
        "/login.xhtml",
        "/forgot-password.xhtml",
        "/reset-password.xhtml",
        "/verify.xhtml",
        "/index.html"
    };

    /**
     * Initializes the filter.
     *
     * @param filterConfig Filter configuration information.
     * @throws ServletException If initialization fails.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Intercepts every incoming request and checks whether
     * the user is authenticated and authorized to access
     * the requested resource.
     *
     * @param request The incoming servlet request.
     * @param response The outgoing servlet response.
     * @param chain The filter chain.
     * @throws IOException If an input/output error occurs.
     * @throws ServletException If a servlet error occurs.
     */
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        // Convert generic request/response objects to HTTP versions.
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Get the requested URI without the application context path.
        String uri = req.getRequestURI().substring(req.getContextPath().length());

        // Allow unrestricted access to public pages and static resources.
        if (isPublic(uri)
                || uri.contains("/javax.faces.resource")
                || uri.contains("/resources/")) {

            chain.doFilter(request, response);
            return;
        }

        // Retrieve the current session without creating a new one.
        HttpSession session = req.getSession(false);

        // Retrieve the logged-in user from the session.
        LoggedInUser loggedInUser = (session != null)
                ? (LoggedInUser) session.getAttribute("loggedInUser")
                : null;

        User user = (loggedInUser != null)
                ? loggedInUser.getUser()
                : null;

        // Redirect unauthenticated users to the login page.
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        // Retrieve the user's role.
        String role = (user.getRole() != null)
                ? user.getRole().getName()
                : "";

        // Restrict access to administrator pages.
        if (uri.startsWith("/admin/") && !"ADMIN".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Restrict access to doctor pages.
        if (uri.startsWith("/doctor/") && !"DOCTOR".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Restrict access to receptionist pages.
        if (uri.startsWith("/receptionist/") && !"RECEPTIONIST".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Restrict access to patient pages.
        if (uri.startsWith("/patient/") && !"PATIENT".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // User is authenticated and authorized.
        chain.doFilter(request, response);
    }

    /**
     * Cleans up any resources used by the filter.
     */
    @Override
    public void destroy() {
        // No cleanup required.
    }

    /**
     * Determines whether the requested page is publicly accessible.
     *
     * @param uri The requested URI.
     * @return true if the page is public; otherwise false.
     */
    private boolean isPublic(String uri) {

        // Check whether the requested page matches any public page.
        for (String page : PUBLIC_PAGES) {
            if (uri.equals(page)) {
                return true;
            }
        }

        return false;
    }
}