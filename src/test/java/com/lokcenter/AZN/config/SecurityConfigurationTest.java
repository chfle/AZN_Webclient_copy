package com.lokcenter.AZN.config;

import lombok.With;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.matchers.JUnitMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Access Control Tests
 *
 * @version 1.01 - 02-07-2022
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigurationTest {
    @Autowired
    private MockMvc mvc;

    // /dayplan (GET)
    @Test
    @DisplayName("/dayplan (get) - User should get 403 without role")
    @WithMockUser
    void dayplan_get_without_role() throws Exception {
        mvc.perform(get("/dayplan")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/dayplan (get) - User should get 200 with User role")
    @WithMockUser(roles = {"User"})
    void dayplan_get_with_user_role() throws Exception {
        mvc.perform(get("/dayplan")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("/dayplan (get) - User should get 403 with Admin role")
    @WithMockUser(roles = {"Admin"})
    void dayplan_get_with_admin_role() throws Exception {
        mvc.perform(get("/dayplan")).andExpect(status().isForbidden());
    }


}