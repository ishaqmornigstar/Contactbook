package com.ishaq.contactbook;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ContactRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Contact> MAPPER = (rs, row) -> new Contact(
        rs.getLong("id"), rs.getString("name"), rs.getString("phone"),
        rs.getString("email"), rs.getString("address"));

    public ContactRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Contact> findAll(String search) {
        // Escape LIKE wildcards so %, _ and ! are searched as ordinary text.
        String term = "%" + search.toLowerCase(Locale.ROOT)
            .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return jdbc.query("""
            SELECT * FROM contacts
            WHERE LOWER(name) LIKE ? ESCAPE '!' OR LOWER(phone) LIKE ? ESCAPE '!'
               OR LOWER(email) LIKE ? ESCAPE '!' OR LOWER(address) LIKE ? ESCAPE '!'
            ORDER BY LOWER(name), id
            """, MAPPER, term, term, term, term);
    }

    public Optional<Contact> findById(long id) {
        return jdbc.query("SELECT * FROM contacts WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public Contact create(ContactRequest request) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO contacts(name, phone, email, address) VALUES (?, ?, ?, ?)", new String[]{"id"});
            statement.setString(1, request.name());
            statement.setString(2, request.phone());
            statement.setString(3, request.email());
            statement.setString(4, request.address());
            return statement;
        }, keys);
        Number id = keys.getKey();
        if (id == null) throw new IllegalStateException("Contact ID was not generated");
        return new Contact(id.longValue(), request.name(), request.phone(), request.email(), request.address());
    }

    public boolean update(long id, ContactRequest request) {
        return jdbc.update("UPDATE contacts SET name = ?, phone = ?, email = ?, address = ? WHERE id = ?",
            request.name(), request.phone(), request.email(), request.address(), id) == 1;
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM contacts WHERE id = ?", id) == 1;
    }
}
