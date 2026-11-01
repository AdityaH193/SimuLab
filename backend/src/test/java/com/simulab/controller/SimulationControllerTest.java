package com.simulab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulab.dto.ExecuteAdhocSimulationRequest;
import com.simulab.engine.model.ComponentType;
import com.simulab.engine.model.SimEdge;
import com.simulab.engine.model.SimNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return pre-seeded architecture templates")
    void testGetTemplates() throws Exception {
        mockMvc.perform(get("/api/architectures/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @DisplayName("Should execute ad-hoc simulation via REST endpoint")
    void testExecuteAdhocSimulation() throws Exception {
        ExecuteAdhocSimulationRequest req = new ExecuteAdhocSimulationRequest();
        req.setName("Adhoc Web Test");
        req.setDurationSec(2.0);
        req.setRandomSeed(123L);

        SimNode client = new SimNode("c-1", "Client", ComponentType.CLIENT);
        client.setWorkloadRps(15.0);

        SimNode api = new SimNode("api-1", "API", ComponentType.API_SERVER);
        SimEdge edge = new SimEdge("e-1", "c-1", "api-1");

        req.setNodes(Arrays.asList(client, api));
        req.setEdges(Collections.singletonList(edge));

        mockMvc.perform(post("/api/simulations/execute-adhoc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").isNotEmpty())
                .andExpect(jsonPath("$.totalRequestsCompleted").isNumber())
                .andExpect(jsonPath("$.throughputRps").isNumber())
                .andExpect(jsonPath("$.bottleneckReport").exists());
    }
}
