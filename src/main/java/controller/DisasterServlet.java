package controller;

import dao.DisasterDAO;
import model.Disaster;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/disaster")
@MultipartConfig
public class DisasterServlet extends HttpServlet {

    private DisasterDAO disasterDAO = new DisasterDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<Disaster> list = disasterDAO.getAllDisasters();
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < list.size(); i++) {

            Disaster d = list.get(i);

            json.append("{")
                    .append("\"id\":").append(d.getDisasterId()).append(",")
                    .append("\"type\":\"").append(escapeJson(d.getType())).append("\",")
                    .append("\"location\":\"").append(escapeJson(d.getLocation())).append("\",")
                    .append("\"severity\":\"").append(escapeJson(d.getSeverity())).append("\",")
                    .append("\"status\":\"").append(escapeJson(d.getStatus())).append("\",")
                    .append("\"description\":\"").append(escapeJson(d.getDescription())).append("\",")
                    .append("\"latitude\":").append(d.getLatitude()).append(",")
                    .append("\"longitude\":").append(d.getLongitude()).append(",")
                    .append("\"imagePath\":\"").append(escapeJson(d.getImagePath())).append("\",")
                    .append("\"imagePath2\":\"").append(escapeJson(d.getImagePath2())).append("\"")
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
            out.print("{\"status\":\"error\",\"message\":\"Unauthorized\"}");
            return;
        }

        User u = (User) session.getAttribute("user");

        if (!"Admin".equals(u.getRole())) {
            out.print("{\"status\":\"error\",\"message\":\"Forbidden\"}");
            return;
        }

        String action = request.getParameter("action");

        try {

            /* ================= ADD DISASTER ================= */

            if ("add".equals(action)) {

                Disaster d = new Disaster();

                d.setType(request.getParameter("type"));
                d.setLocation(request.getParameter("location"));
                d.setSeverity(request.getParameter("severity"));
                d.setStatus("Reported");
                d.setDescription(request.getParameter("description"));

                /* Safe coordinate parsing */

                String latStr = request.getParameter("latitude");
                String lonStr = request.getParameter("longitude");

                if (latStr != null && lonStr != null) {
                    d.setLatitude(Double.parseDouble(latStr));
                    d.setLongitude(Double.parseDouble(lonStr));
                }

                /* Image upload */

                Part image1 = request.getPart("image1");
                Part image2 = request.getPart("image2");

                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                File uploadDir = new File(uploadPath);

                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }

                if (image1 != null && image1.getSize() > 0) {

                    String fileName1 = System.currentTimeMillis() + "_1_" + image1.getSubmittedFileName();

                    image1.write(uploadPath + File.separator + fileName1);

                    d.setImagePath("uploads/" + fileName1);
                }

                if (image2 != null && image2.getSize() > 0) {

                    String fileName2 = System.currentTimeMillis() + "_2_" + image2.getSubmittedFileName();

                    image2.write(uploadPath + File.separator + fileName2);

                    d.setImagePath2("uploads/" + fileName2);
                }

                boolean ok = disasterDAO.addDisaster(d);

                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");
            }

            /* ================= UPDATE DISASTER ================= */

            else if ("update".equals(action)) {

                Disaster d = new Disaster();

                d.setDisasterId(Integer.parseInt(request.getParameter("id")));
                d.setType(request.getParameter("type"));
                d.setLocation(request.getParameter("location"));
                d.setSeverity(request.getParameter("severity"));
                d.setDescription(request.getParameter("description"));

                String status = request.getParameter("status");

                if (status == null || status.trim().isEmpty()) {
                    status = "Reported";
                }

                if (!isValidStatus(status)) {
                    out.print("{\"status\":\"error\",\"message\":\"Invalid status\"}");
                    return;
                }

                d.setStatus(status);

                /* Coordinates */

                String latStr = request.getParameter("latitude");
                String lonStr = request.getParameter("longitude");

                if (latStr != null && lonStr != null) {
                    d.setLatitude(Double.parseDouble(latStr));
                    d.setLongitude(Double.parseDouble(lonStr));
                }

                /* Preserve images */

                Disaster existing = disasterDAO.getDisasterById(d.getDisasterId());

                if (existing != null) {
                    d.setImagePath(existing.getImagePath());
                    d.setImagePath2(existing.getImagePath2());
                }

                boolean ok = disasterDAO.updateDisaster(d);

                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");
            }

            /* ================= UPDATE STATUS ================= */

            else if ("update_status".equals(action)) {

                int id = Integer.parseInt(request.getParameter("id"));
                String status = request.getParameter("status");

                if (!isValidStatus(status)) {
                    out.print("{\"status\":\"error\",\"message\":\"Invalid status\"}");
                    return;
                }

                Disaster d = disasterDAO.getDisasterById(id);

                if (d == null) {
                    out.print("{\"status\":\"error\",\"message\":\"Disaster not found\"}");
                    return;
                }

                d.setStatus(status);

                boolean ok = disasterDAO.updateDisaster(d);

                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");
            }

            /* ================= DELETE ================= */

            else if ("delete".equals(action)) {

                int id = Integer.parseInt(request.getParameter("id"));

                boolean ok = disasterDAO.deleteDisaster(id);

                out.print(ok ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}");
            }

            else {

                out.print("{\"status\":\"error\",\"message\":\"Invalid action\"}");
            }

        }

        catch (Exception e) {

            e.printStackTrace();

            out.print("{\"status\":\"error\",\"message\":\"Server Error\"}");
        }
    }

    /* ================= VALIDATE STATUS ================= */

    private boolean isValidStatus(String status) {

        return "Reported".equals(status)
                || "In Progress".equals(status)
                || "Resolved".equals(status);
    }

    /* ================= JSON ESCAPE ================= */

    private String escapeJson(String str) {

        if (str == null)
            return "";

        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}