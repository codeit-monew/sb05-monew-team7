package com.spring.monew.interest.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.service.InterestService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InterestController.class)
@AutoConfigureMockMvc(addFilters = false) // Security Filter 비활성화
class InterestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private InterestService interestService;

  UUID userId = UUID.randomUUID();
  UUID interestId = UUID.randomUUID();

  @Test
  @DisplayName("관심사 등록 성공")
  void interestAdd_success() throws Exception {
    // given
    InterestRegisterRequest request = new InterestRegisterRequest("테스트", List.of("야구", "축구"));
    InterestDto response = new InterestDto(
        interestId, "테스트", List.of("야구", "축구"), 0L, false, Instant.now()
    );
    given(interestService.addInterest(any())).willReturn(response);

    // when & then
    mockMvc.perform(post("/api/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("테스트"))
        .andExpect(jsonPath("$.keywords[0]").value("야구"));
  }

  @Test
  @DisplayName("관심사 목록 조회 성공")
  void interestList_success() throws Exception {
    // given
    InterestDto dto = new InterestDto(interestId, "테스트", List.of("야구", "농구"), 3L, true,
        Instant.now());
    CursorPageResponseInterestDto response =
        new CursorPageResponseInterestDto(List.of(dto), "nextCursorValue", Instant.now(), 1, 1L,
            false);

    given(interestService.getInterests(any(), any(), any(), any(), any(), anyInt(), any()))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/interests")
            .header("Monew-Request-User-ID", userId)
            .param("keyword", "테스트")
            .param("orderBy", "name")
            .param("direction", "ASC")
            .param("limit", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("테스트"))
        .andExpect(jsonPath("$.content[0].subscribedByMe").value(true));
  }

  @Test
  @DisplayName("관심사 수정 성공")
  void interestModify_success() throws Exception {
    // given
    InterestUpdateRequest updateRequest = new InterestUpdateRequest(List.of("테니스", "골프"));
    InterestDto response = new InterestDto(
        interestId, "수정 테스트", List.of("테니스", "골프"), 5, false
        , Instant.now());

    given(interestService.modifyInterest(eq(interestId), any())).willReturn(response);

    // when & then
    mockMvc.perform(patch("/api/interests/{id}", interestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keywords[0]").value("테니스"));
  }

  @Test
  @DisplayName("관심사 삭제 성공")
  void interestRemove_success() throws Exception {
    // given
    willDoNothing().given(interestService).removeInterest(interestId);

    // when & then
    mockMvc.perform(delete("/api/interests/{id}", interestId))
        .andExpect(status().isOk());
  }
}
