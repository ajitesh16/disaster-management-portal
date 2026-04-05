package controller;

import dao.HelpRequestDAO;
import model.HelpRequest;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/help-request")
public class HelpRequestServlet extends HttpServlet {
    private HelpRequestDAO hrDAO = new HelpRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            out.print("[]");
            return;
        }

        User u = (User) session.getAttribute("user");
        List<HelpRequest> list;

        if ("Citizen".equals(u.getRole())) {
            list = hrDAO.getRequestsByCitizen(u.getUserId());
        } else {
            // Admin or Rescue Team
            list = hrDAO.getAllRequests();
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            HelpRequest hr = list.get(i);

            json.append("{")
                    .append("\"id\":").append(hr.getRequestId()).append(",")
                    .append("\"citizenName\":\"").append(escapeJson(hr.getCitizenName())).append("\",")
                    .append("\"disasterType\":\"").append(escapeJson(hr.getDisasterType())).append("\",")
                    .append("\"location\":\"").append(escapeJson(hr.getLocation())).append("\",")
                    .append("\"description\":\"").append(escapeJson(hr.getDescription())).append("\",")
                    .append("\"status\":\"").append(escapeJson(hr.getStatus())).append("\",")
                    .append("\"assignedTeam\":\"").append(escapeJson(hr.getAssignedTeam())).append("\"")
                    .append("}");

            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        out.print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            out.print("{\"status\":\"error\", \"message\":\"Unauthorized\"}");
            return;
        }

        User u = (User) session.getAttribute("user");
        String action = request.getParameter("action");

        try {
            if ("raise".equals(action) && "Citizen".equals(u.getRole())) {
                HelpRequest hr = new HelpRequest();
                hr.setCitizenId(u.getUserId());
                hr.setDisasterId(Integer.parseInt(request.getParameter("disasterId")));
                hr.setDescription(request.getParameter("description"));
                hr.setLocation(request.getParameter("location"));
                hr.setAssignedTeam(null);
                hr.setStatus("Pending");
                hr.setCitizenName(u.getName());
                hr.setDisasterType(request.getParameter("disasterType"));

                boolean ok = hrDAO.createRequest(hr);
                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");

            } else if ("update_status".equals(action)
                    && ("Admin".equals(u.getRole()) || "RescueTeam".equals(u.getRole()))) {

                int id = Integer.parseInt(request.getParameter("id"));
                String status = request.getParameter("status");
                String assignedTeam = request.getParameter("assignedTeam");

                if (assignedTeam != null && assignedTeam.trim().isEmpty()) {
                    assignedTeam = null;
                }

                boolean ok = hrDAO.updateHelpRequestStatus(id, status, assignedTeam);
                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");

            } else {
                out.print("{\"status\":\"error\", \"message\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"Server Error\"}");
        }
    }

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}