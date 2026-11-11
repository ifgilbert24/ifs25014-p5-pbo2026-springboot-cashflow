package org.delcom.ifs25014springbootcashflow;

import org.delcom.ifs25014springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private FinanceUseCase financeUseCase;

    @Test
    void contextLoads() {
        assertNotNull(financeUseCase);
    }

    @Test
    void mainInvokesSpringApplicationRun() {
        String[] args = new String[]{"--spring.main.web-application-type=none"};
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(App.class, args))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            App.main(args);

            mocked.verify(() -> SpringApplication.run(App.class, args));
        }
    }
}

