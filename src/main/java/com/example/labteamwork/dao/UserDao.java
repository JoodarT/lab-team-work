package com.example.labteamwork.dao;

import com.example.labteamwork.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .password(rs.getString("password"))
            .email(rs.getString("email"))
            .enabled(rs.getBoolean("enabled"))
            .roles(new HashSet<>())
            .build();

    public User save(User user) {
        String sql = "INSERT INTO users (username, password, email, enabled) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setBoolean(4, user.getEnabled() != null ? user.getEnabled() : true);
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    public void addRoleToUser(Long userId, String roleName) {
        String getRoleIdSql = "SELECT id FROM roles WHERE name = ?";
        Long roleId = jdbcTemplate.queryForObject(getRoleIdSql, Long.class, roleName);

        String insertUserRoleSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        jdbcTemplate.update(insertUserRoleSql, userId, roleId);
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Optional<User> user = jdbcTemplate.query(sql, userRowMapper, username).stream().findFirst();

        user.ifPresent(this::loadUserRoles);
        return user;
    }

    public java.util.List<User> findAll() {
        String sql = "SELECT * FROM users";
        java.util.List<User> users = jdbcTemplate.query(sql, userRowMapper);
        users.forEach(this::loadUserRoles);
        return users;
    }


    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Optional<User> user = jdbcTemplate.query(sql, userRowMapper, email).stream().findFirst();

        user.ifPresent(this::loadUserRoles);
        return user;
    }


    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Optional<User> user = jdbcTemplate.query(sql, userRowMapper, id).stream().findFirst();

        user.ifPresent(this::loadUserRoles);
        return user;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    private void loadUserRoles(User user) {
        String sql = "SELECT r.name FROM roles r " +
                "JOIN user_roles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ?";
        user.setRoles(new HashSet<>(jdbcTemplate.queryForList(sql, String.class, user.getId())));
    }
}