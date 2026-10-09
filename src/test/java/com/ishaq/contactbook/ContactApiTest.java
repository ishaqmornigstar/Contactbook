package com.ishaq.contactbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:contactbooktest;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ContactApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach void clearContacts() { jdbc.update("DELETE FROM contacts"); }

    @Test void createSearchUpdateDelete() throws Exception {
        String body = mvc.perform(post("/api/contacts").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":" Ishaq ","phone":"+91 98765 43210","email":"ishaq@example.com","address":"Chennai"}
                """))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Ishaq"))
            .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();
        mvc.perform(get("/api/contacts").param("search", "ISHAQ"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/contacts").param("search", "Chennai"))
            .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/contacts").param("search", "%"))
            .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(put("/api/contacts/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Mohamed","phone":"1234567890","email":"","address":""}
                """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Mohamed"));
        mvc.perform(get("/api/contacts/" + id)).andExpect(jsonPath("$.name").value("Mohamed"));
        mvc.perform(delete("/api/contacts/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/contacts/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/contacts/" + id)).andExpect(status().isNotFound());
    }

    @Test void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/contacts").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"  ","phone":"abc","email":"invalid"}
                """))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.phone").exists()).andExpect(jsonPath("$.errors.email").exists());
        mvc.perform(get("/api/contacts/not-a-number")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/contacts").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/contacts")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test void optionalFieldsAndMissingUpdate() throws Exception {
        String contact = "{\"name\":\"Test\",\"phone\":\"1234567\"}";
        mvc.perform(post("/api/contacts").contentType(MediaType.APPLICATION_JSON).content(contact))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value(""));
        mvc.perform(put("/api/contacts/999999").contentType(MediaType.APPLICATION_JSON).content(contact))
            .andExpect(status().isNotFound());
        mvc.perform(get("/index.html")).andExpect(status().isOk());
    }
}
