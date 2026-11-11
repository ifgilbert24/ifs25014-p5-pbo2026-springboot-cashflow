package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTransactionRepository implements ITransactionRepository {

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        LocalDateTime now = LocalDateTime.now();
        String sql = "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, description);
            ps.setLong(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, null);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.executeUpdate();
            int id = readGeneratedId(ps);
            return new Transaction(id, userId, description, amount, type, null, now);
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan transaksi baru", e);
        }
    }

    @Override
    public Transaction save(Transaction entity) {
        Optional<Transaction> existing = findById(entity.getId());
        if (existing.isPresent()) {
            update(entity);
            return entity;
        }
        return insertWithId(entity);
    }

    private void update(Transaction entity) {
        String sql = "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getDescription());
            ps.setLong(2, entity.getAmount());
            ps.setString(3, entity.getType().name());
            ps.setString(4, entity.getProofPath());
            ps.setInt(5, entity.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal memperbarui transaksi", e);
        }
    }

    private Transaction insertWithId(Transaction entity) {
        String sql = "INSERT INTO transactions (id, user_id, description, amount, type, proof_path, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getId());
            ps.setInt(2, entity.getUserId());
            ps.setString(3, entity.getDescription());
            ps.setLong(4, entity.getAmount());
            ps.setString(5, entity.getType().name());
            ps.setString(6, entity.getProofPath());
            ps.setTimestamp(7, Timestamp.valueOf(entity.getCreatedAt()));
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyisipkan transaksi", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus transaksi", e);
        }
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        String sql = "SELECT id, user_id, description, amount, type, proof_path, created_at FROM transactions WHERE id = ?";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal mencari transaksi", e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(connection);
        }
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT id, user_id, description, amount, type, proof_path, created_at FROM transactions ORDER BY id";
        List<Transaction> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal mengambil semua transaksi", e);
        }
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        String sql = "SELECT id, user_id, description, amount, type, proof_path, created_at FROM transactions WHERE user_id = ? ORDER BY id";
        List<Transaction> result = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal mengambil transaksi pengguna", e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(connection);
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

    static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception ignored) {
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("description"),
                rs.getLong("amount"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("proof_path"),
                createdTs.toLocalDateTime()
        );
    }
}

