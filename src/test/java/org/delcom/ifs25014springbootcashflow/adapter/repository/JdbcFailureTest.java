package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Role;
import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcFailureTest {

    @Test
    void wrapsSqlExceptionsIntoIllegalState() throws Exception {
        DataSource broken = mock(DataSource.class);
        when(broken.getConnection()).thenThrow(new SQLException("boom"));

        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(broken));

        JdbcUserRepository userRepo = new JdbcUserRepository(broken);
        assertThrows(IllegalStateException.class, () -> userRepo.saveNew("a", "h", "A", Role.USER));
        assertThrows(IllegalStateException.class, () -> userRepo.save(new User(1, "a", "h", "A", Role.USER, null)));
        assertThrows(IllegalStateException.class, () -> userRepo.deleteById(1));
        assertThrows(IllegalStateException.class, () -> userRepo.findById(1));
        assertThrows(IllegalStateException.class, () -> userRepo.findByUsername("a"));
        assertThrows(IllegalStateException.class, userRepo::findAll);

        JdbcTransactionRepository txRepo = new JdbcTransactionRepository(broken);
        assertThrows(IllegalStateException.class, () -> txRepo.save(1, "T", 100L, TransactionType.INCOME));
        assertThrows(IllegalStateException.class, () -> txRepo.deleteById(1));
        assertThrows(IllegalStateException.class, () -> txRepo.findById(1));
        assertThrows(IllegalStateException.class, txRepo::findAll);
        assertThrows(IllegalStateException.class, () -> txRepo.findByUserId(1));
    }

    @Test
    void transactionSaveUpdateAndInsertWrapSqlExceptions() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection connFind = mock(Connection.class);
        PreparedStatement psFind = mock(PreparedStatement.class);
        ResultSet rsFind = mock(ResultSet.class);
        Connection connWrite = mock(Connection.class);

        when(connFind.prepareStatement(anyString())).thenReturn(psFind);
        when(psFind.executeQuery()).thenReturn(rsFind);
        when(rsFind.getInt("id")).thenReturn(1);
        when(rsFind.getInt("user_id")).thenReturn(1);
        when(rsFind.getString("description")).thenReturn("T");
        when(rsFind.getLong("amount")).thenReturn(100L);
        when(rsFind.getString("type")).thenReturn("INCOME");
        when(rsFind.getString("proof_path")).thenReturn(null);
        when(rsFind.getTimestamp("created_at")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.now()));

        when(connWrite.prepareStatement(anyString())).thenThrow(new SQLException("gagal tulis"));

        JdbcTransactionRepository txRepo = new JdbcTransactionRepository(ds);
        Transaction sample = new Transaction(1, 1, "T", 100L, TransactionType.INCOME);

        when(ds.getConnection()).thenReturn(connFind, connWrite);
        when(rsFind.next()).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> txRepo.save(sample));

        when(ds.getConnection()).thenReturn(connFind, connWrite);
        when(rsFind.next()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> txRepo.save(sample));
    }

    @Test
    void readGeneratedIdThrowsWhenNoKeyReturned() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet emptyKeys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(emptyKeys);
        when(emptyKeys.next()).thenReturn(false);

        assertThrows(SQLException.class, () -> JdbcTransactionRepository.readGeneratedId(ps));
        assertThrows(SQLException.class, () -> JdbcUserRepository.readGeneratedId(ps));

        JdbcTransactionRepository.closeQuietly(null);
        AutoCloseable failingClose = () -> {
            throw new SQLException("tutup gagal");
        };
        JdbcTransactionRepository.closeQuietly(failingClose);

        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement insertPs = mock(PreparedStatement.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(insertPs);
        when(insertPs.getGeneratedKeys()).thenReturn(emptyKeys);

        assertThrows(IllegalStateException.class,
                () -> new JdbcTransactionRepository(ds).save(1, "T", 100L, TransactionType.INCOME));
        assertThrows(IllegalStateException.class,
                () -> new JdbcUserRepository(ds).saveNew("u", "h", "U", Role.USER));
    }
}

