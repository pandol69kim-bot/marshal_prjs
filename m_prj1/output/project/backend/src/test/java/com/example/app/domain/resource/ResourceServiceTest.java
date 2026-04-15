package com.example.app.domain.resource;

import com.example.app.common.exception.BusinessException;
import com.example.app.common.exception.ErrorCode;
import com.example.app.domain.auth.entity.User;
import com.example.app.domain.auth.repository.UserRepository;
import com.example.app.domain.resource.dto.CreateResourceRequest;
import com.example.app.domain.resource.dto.ResourceDto;
import com.example.app.domain.resource.entity.Resource;
import com.example.app.domain.resource.repository.ResourceRepository;
import com.example.app.domain.resource.service.ResourceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("존재하지 않는 리소스 조회 시 BusinessException 발생")
    void findById_not_found_throws_exception() {
        // Arrange
        given(resourceRepository.findById(any())).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.findById(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("유효한 데이터로 리소스 생성 성공")
    void create_success() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User mockUser = User.createLocal("owner@example.com", "encoded", "소유자");
        Resource mockResource = Resource.create("테스트 리소스", "설명", mockUser);

        given(userRepository.findById(any())).willReturn(Optional.of(mockUser));
        given(resourceRepository.save(any())).willReturn(mockResource);

        CreateResourceRequest request = new CreateResourceRequest("테스트 리소스", "설명");

        // Act
        ResourceDto result = resourceService.create(request, userId);

        // Assert
        assertThat(result.name()).isEqualTo("테스트 리소스");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }
}
