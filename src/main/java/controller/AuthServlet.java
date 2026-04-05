package controller;

import dao.UserDAO;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if ("register".equals(action)) {

                User u = new User();
                u.setName(request.getParameter("name"));
                u.setEmail(request.getParameter("email"));
                u.setPasswordHash(request.getParameter("password")); // hashed in DAO
                u.setRole("Citizen"); // fixed role for all registrations

                boolean success = userDAO.registerUser(u);

                if (success) {
                    out.print("{\"status\":\"success\", \"message\":\"Registration successful!\"}");
                } else {
                    out.print("{\"status\":\"error\", \"message\":\"Registration failed! Email might be taken.\"}");
                }

            } else if ("login".equals(action)) {

                String email = request.getParameter("email");
                String password = request.getParameter("password");

                User user = userDAO.loginUser(email, password);

                if (user != null && "Citizen".equalsIgnoreCase(user.getRole())) {
                    HttpSession session = request.getSession();
                    session.setAttribute("user", user);

                    out.print("{\"status\":\"success\", \"redirect\":\"citizen_dashboard.html\"}");
                } else if (user != null && "Admin".equalsIgnoreCase(user.getRole())) {
                    out.print(
                            "{\"status\":\"error\", \"message\":\"Admins must log in through the Admin Login page.\"}");
                } else {
                    out.print("{\"status\":\"error\", \"message\":\"Invalid credentials!\"}");
                }

            } else if ("adminLogin".equals(action)) {

                String email = request.getParameter("email");
                String password = request.getParameter("password");

                User user = userDAO.loginUser(email, password);

                if (user != null && "Admin".equalsIgnoreCase(user.getRole())) {
                    HttpSession session = request.getSession();
                    session.setAttribute("user", user);

                    out.print("{\"status\":\"success\", \"redirect\":\"admin_dashboard.html\"}");
                } else {
                    out.print("{\"status\":\"error\", \"message\":\"Invalid admin credentials!\"}");
                }

            } else if ("logout".equals(action)) {

                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                out.print("{\"status\":\"success\", \"redirect\":\"login.html\"}");

            } else {
                out.print("{\"status\":\"error\", \"message\":\"Invalid action!\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"Server error!\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (session != null && session.getAttribute("user") != null) {
            User u = (User) session.getAttribute("user");
            out.print("{\"status\":\"success\", \"role\":\"" + u.getRole() + "\", \"name\":\"" + u.getName() + "\"}");
        } else {
            out.print("{\"status\":\"error\"}");
        }
    }
}