package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.HelpRequest;
import util.DBConnection;

public class HelpRequestDAO {

    public boolean createRequest(HelpRequest hr) {
        String sql = "INSERT INTO help_requests (citizen_id, disaster_id, location, description, status, assigned_team) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hr.getCitizenId());
            stmt.setInt(2, hr.getDisasterId());
            stmt.setString(3, hr.getLocation());
            stmt.setString(4, hr.getDescription());
            stmt.setString(5, hr.getStatus());
            stmt.setString(6, hr.getAssignedTeam());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HelpRequest> getRequestsByCitizen(int citizenId) {
        List<HelpRequest> list = new ArrayList<>();
        String sql = "SELECT hr.*, d.type AS d_type, u.name AS u_name " +
                "FROM help_requests hr " +
                "JOIN disasters d ON hr.disaster_id = d.disaster_id " +
                "JOIN users u ON hr.citizen_id = u.user_id " +
                "WHERE hr.citizen_id = ? " +
                "ORDER BY hr.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, citizenId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HelpRequest hr = extractHelpRequest(rs);
                hr.setCitizenName(rs.getString("u_name"));
                list.add(hr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<HelpRequest> getAllRequests() {
        List<HelpRequest> list = new ArrayList<>();
        String sql = "SELECT hr.*, d.type AS d_type, u.name AS u_name " +
                "FROM help_requests hr " +
                "JOIN disasters d ON hr.disaster_id = d.disaster_id " +
                "JOIN users u ON hr.citizen_id = u.user_id " +
                "ORDER BY hr.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                HelpRequest hr = extractHelpRequest(rs);
                hr.setCitizenName(rs.getString("u_name"));
                list.add(hr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateHelpRequestStatus(int id, String status, String assignedTeam) {
        String sql = "UPDATE help_requests SET status = ?, assigned_team = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, assignedTeam);
            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private HelpRequest extractHelpRequest(ResultSet rs) throws SQLException {
        HelpRequest hr = new HelpRequest();

        hr.setRequestId(rs.getInt("request_id"));
        hr.setCitizenId(rs.getInt("citizen_id"));
        hr.setDisasterId(rs.getInt("disaster_id"));
        hr.setDescription(rs.getString("description"));
        hr.setLocation(rs.getString("location"));
        hr.setStatus(rs.getString("status"));
        hr.setCreatedAt(rs.getTimestamp("created_at"));
        hr.setAssignedTeam(rs.getString("assigned_team"));
        hr.setDisasterType(rs.getString("d_type"));

        return hr;
    }
}