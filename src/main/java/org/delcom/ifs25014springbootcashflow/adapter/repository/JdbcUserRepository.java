package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Role;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements IUserRepository {

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User saveNew(String username, String passwordHash, String fullName, Role role) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, photo_path) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            ps.setString(5, null);
            ps.executeUpdate();
            int id = readGeneratedId(ps);
            return new User(id, username, passwordHash, fullName, role, null);
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan pengguna baru", e);
        }
    }

    @Override
    public User save(User entity) {
        String sql = "UPDATE users SET full_name = ?, password_hash = ?, photo_path = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getFullName());
            ps.setString(2, entity.getPasswordHash());
            ps.setString(3, entity.getPhotoPath());
            ps.setInt(4, entity.getId());
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui pengguna", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus pengguna", e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT id, username, password_hash, full_name, role, photo_path FROM users WHERE id = ?";
        return queryOptional(sql, ps -> ps.setInt(1, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, full_name, role, photo_path FROM users WHERE LOWER(username) = LOWER(?)";
        return queryOptional(sql, ps -> ps.setString(1, username));
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, password_hash, full_name, role, photo_path FROM users ORDER BY id";
        List<User> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal mengambil semua pengguna", e);
        }
    }

    private Optional<User> queryOptional(String sql, StatementBinder binder) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            binder.bind(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal mencari pengguna", e);
        } finally {
            JdbcTransactionRepository.closeQuietly(rs);
            JdbcTransactionRepository.closeQuietly(ps);
            JdbcTransactionRepository.closeQuietly(connection);
        }
    }

    static int readGeneratedId(PreparedStatement ps) throws SQLException {
        ResultSet keys = ps.getGeneratedKeys();
        try {
            if (!keys.next()) {
                throw new SQLException("ID baru tidak dikembalikan database");
            }
            return keys.getInt(1);
        } finally {
            keys.close();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                rs.getString("photo_path")
        );
    }

    @FunctionalInterface
    interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}

