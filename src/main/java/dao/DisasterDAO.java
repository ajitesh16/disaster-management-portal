package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Disaster;
import util.DBConnection;

public class DisasterDAO {

    public boolean addDisaster(Disaster d) {
        String sql = "INSERT INTO disasters (type, location, severity, description, status, image_path, image_path_2, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getType());
            stmt.setString(2, d.getLocation());
            stmt.setString(3, d.getSeverity());
            stmt.setString(4, d.getDescription());
            stmt.setString(5, d.getStatus());
            stmt.setString(6, d.getImagePath());
            stmt.setString(7, d.getImagePath2());
            stmt.setDouble(8, d.getLatitude());
            stmt.setDouble(9, d.getLongitude());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Disaster> getAllDisasters() {
        List<Disaster> list = new ArrayList<>();
        String sql = "SELECT * FROM disasters ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Disaster d = new Disaster();
                d.setDisasterId(rs.getInt("disaster_id"));
                d.setType(rs.getString("type"));
                d.setLocation(rs.getString("location"));
                d.setSeverity(rs.getString("severity"));
                d.setStatus(rs.getString("status"));
                d.setDescription(rs.getString("description"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                d.setImagePath(rs.getString("image_path"));
                d.setImagePath2(rs.getString("image_path_2"));
                d.setLatitude(rs.getDouble("latitude"));
                d.setLongitude(rs.getDouble("longitude"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Disaster getDisasterById(int id) {
        String sql = "SELECT * FROM disasters WHERE disaster_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Disaster d = new Disaster();
                    d.setDisasterId(rs.getInt("disaster_id"));
                    d.setType(rs.getString("type"));
                    d.setLocation(rs.getString("location"));
                    d.setSeverity(rs.getString("severity"));
                    d.setStatus(rs.getString("status"));
                    d.setDescription(rs.getString("description"));
                    d.setCreatedAt(rs.getTimestamp("created_at"));
                    d.setImagePath(rs.getString("image_path"));
                    d.setImagePath2(rs.getString("image_path_2"));
                    d.setLatitude(rs.getDouble("latitude"));
                    d.setLongitude(rs.getDouble("longitude"));
                    return d;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateDisaster(Disaster d) {
        String sql = "UPDATE disasters SET type=?, location=?, severity=?, description=?, status=?, image_path=?, image_path_2=?, latitude=?, longitude=? WHERE disaster_id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getType());
            stmt.setString(2, d.getLocation());
            stmt.setString(3, d.getSeverity());
            stmt.setString(4, d.getDescription());
            stmt.setString(5, d.getStatus());
            stmt.setString(6, d.getImagePath());
            stmt.setString(7, d.getImagePath2());
            stmt.setDouble(8, d.getLatitude());
            stmt.setDouble(9, d.getLongitude());
            stmt.setInt(10, d.getDisasterId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDisaster(int id) {
        String sql = "DELETE FROM disasters WHERE disaster_id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}