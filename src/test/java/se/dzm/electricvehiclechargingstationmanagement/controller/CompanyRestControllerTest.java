package se.dzm.electricvehiclechargingstationmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.dzm.electricvehiclechargingstationmanagement.model.CompanyModel;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompanyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(roles="ADMIN")
    @Test
    @Order(1)
    @Commit
    void save_shouldSaveCompanyModelToDatabase() throws Exception {
        CompanyModel model = new CompanyModel() {{
            setName("test company");
            setParent(new CompanyModel(){{setId(3L);}});
        }};
        String json = objectMapper.writeValueAsString(model);

        mockMvc.perform(post("/api/v1/company/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.parent.id").value(3))
        ;
    }

    @WithMockUser(roles="ADMIN")
    @Test
    @Order(2)
    void deleteById_shouldDeleteByIdFromDatabase() throws Exception {
        mockMvc.perform(delete("/api/v1/company/deleteById/{id}",4L))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(roles="ADMIN")
    @Test
    void save_shouldThrowBadRequestError() throws Exception {
        mockMvc.perform(post("/api/v1/company/save").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    @WithMockUser(roles="ADMIN")
    @Test
    void findById_shouldReturnCompanyModel() throws Exception {
        mockMvc.perform(get("/api/v1/company/findById/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("company A"));
    }

    @WithMockUser(roles="ADMIN")
    @Test
    void findById_shouldThrowNotFoundError() throws Exception {
        mockMvc.perform(get("/api/v1/company/findById/{id}", 44L))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(roles="ADMIN")
    @Test
    void findAll_shouldReturnPageableCompanyModels() throws Exception {
        String filter = objectMapper.writeValueAsString(new CompanyModel(){{setName("company");}});

        mockMvc.perform(get("/api/v1/company/findAll").param("model",filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", Matchers.hasSize(3)));
    }

    @WithMockUser(roles="ADMIN")
    @Test
    void countAll_shouldReturnTotalNumberOfCompanies() throws Exception {
        String filter = objectMapper.writeValueAsString(new CompanyModel(){{setName("company");}});

        mockMvc.perform(get("/api/v1/company/countAll").param("model",filter))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @WithMockUser(roles="ADMIN")
    @Test
    void findAllSelect_shouldReturnPageableSelect2Models() throws Exception {
        String filter = objectMapper.writeValueAsString(new CompanyModel(){{setName("company A");}});

        mockMvc.perform(get("/api/v1/company/findAllSelect")
                        .param("model",filter)
                        .param("page","0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect((jsonPath("$.content", Matchers.hasSize(1))))
                .andExpect(jsonPath("$.content.[0].text").value("company A"))
        ;
    }

    @WithMockUser(roles="ADMIN")
    @Test
    @Transactional
    void findAllByParentId_shouldReturnPageableCompanyModels() throws Exception {
        mockMvc.perform(get("/api/v1/company/findAllByParentId/{parentId}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect((jsonPath("$.content[*].name").value(containsInAnyOrder("company B"))));
    }
}
