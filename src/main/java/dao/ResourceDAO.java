package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Resource;
import util.DBConnection;

public class ResourceDAO {

    public boolean addResource(Resource res) {
        String sql = "INSERT INTO resources (name, category, quantity, unit) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, res.getName());
            stmt.setString(2, res.getCategory());
            stmt.setInt(3, res.getQuantity());
            stmt.setString(4, res.getUnit());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Resource> getAllResources() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY category, name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Resource r = new Resource();
                r.setResourceId(rs.getInt("resource_id"));
                r.setName(rs.getString("name"));
                r.setCategory(rs.getString("category"));
                r.setQuantity(rs.getInt("quantity"));
                r.setUnit(rs.getString("unit"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateQuantity(int resourceId, int quantity) {
        String sql = "UPDATE resources SET quantity = ? WHERE resource_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, resourceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
