package controller;

import dao.ResourceDAO;
import model.Resource;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/resource")
public class ResourceServlet extends HttpServlet {
    private ResourceDAO resourceDAO = new ResourceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<Resource> list = resourceDAO.getAllResources();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Resource r = list.get(i);
            json.append("{")
                    .append("\"id\":").append(r.getResourceId()).append(",")
                    .append("\"name\":\"").append(escapeJson(r.getName())).append("\",")
                    .append("\"category\":\"").append(escapeJson(r.getCategory())).append("\",")
                    .append("\"quantity\":").append(r.getQuantity()).append(",")
                    .append("\"unit\":\"").append(escapeJson(r.getUnit())).append("\"")
                    .append("}");
            if (i < list.size() - 1)
                json.append(",");
        }
        json.append("]");
        out.print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            out.print("{\"status\":\"error\", \"message\":\"Unauthorized\"}");
            return;
        }

        User u = (User) session.getAttribute("user");
        if (!"Admin".equals(u.getRole())) {
            out.print("{\"status\":\"error\", \"message\":\"Forbidden\"}");
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                Resource r = new Resource();
                r.setName(request.getParameter("name"));
                r.setCategory(request.getParameter("category"));
                r.setQuantity(Integer.parseInt(request.getParameter("quantity")));
                r.setUnit(request.getParameter("unit"));

                boolean ok = resourceDAO.addResource(r);
                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");
            } else if ("update_quantity".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));

                boolean ok = resourceDAO.updateQuantity(id, quantity);
                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"Server Error\"}");
        }
    }

    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
