package com.gym.management;

import com.gym.management.dto.RegistrationDto;
import com.gym.management.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationAndRoleTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Public routes (home, login, register, classes, access-denied) return HTTP 200 for anonymous users")
    void publicRoutesAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/classes"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin login succeeds and redirects directly to /dashboard")
    void adminLoginSucceedsAndRedirectsToDashboard() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("admin@apexfit.com")
                        .password("password123"))
                .andExpect(authenticated().withUsername("admin@apexfit.com").withRoles("ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("All coaches (Elena, Alex, Marcus, Priya) authenticate successfully with ROLE_TRAINER and reach /trainer/dashboard")
    void allTrainersCanAuthenticate() throws Exception {
        String[] coachEmails = {
                "elena.rostova@apexfit.com",
                "alex.hunter@apexfit.com",
                "marcus.chen@apexfit.com",
                "priya.sharma@apexfit.com"
        };

        for (String email : coachEmails) {
            mockMvc.perform(formLogin("/login")
                            .user(email)
                            .password("password123"))
                    .andExpect(authenticated().withUsername(email).withRoles("TRAINER"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/trainer/dashboard"));
        }
    }

    @Test
    @DisplayName("Member login succeeds and redirects directly to /member/portal")
    void memberLoginSucceedsAndRedirectsToMemberPortal() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("sophia.miller@example.com")
                        .password("password123"))
                .andExpect(authenticated().withUsername("sophia.miller@example.com").withRoles("MEMBER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/portal"));
    }

    @Test
    @DisplayName("Login with mixed-case and whitespace username succeeds")
    void loginWithCaseInsensitiveUsername() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("  ADMIN@APEXFIT.COM  ")
                        .password("password123"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Invalid credentials fail and redirect to /login?error=true")
    void invalidCredentialsFail() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("admin@apexfit.com")
                        .password("wrongpassword"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("Unauthenticated access to protected member portal redirects to /login")
    void unauthenticatedAccessToMemberPortalRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/member/portal"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "sophia.miller@example.com", roles = {"MEMBER"})
    @DisplayName("Member role accessing Admin dashboard is forbidden with /access-denied")
    void memberCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "sophia.miller@example.com", roles = {"MEMBER"})
    @DisplayName("Member role accessing Trainer dashboard is forbidden with /access-denied")
    void memberCannotAccessTrainerDashboard() throws Exception {
        mockMvc.perform(get("/trainer/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "admin@apexfit.com", roles = {"ADMIN"})
    @DisplayName("Admin role accessing /dashboard succeeds with 200 OK")
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "elena.rostova@apexfit.com", roles = {"TRAINER"})
    @DisplayName("Trainer role accessing /trainer/dashboard succeeds with 200 OK")
    void trainerCanAccessTrainerDashboard() throws Exception {
        mockMvc.perform(get("/trainer/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("New Athlete self-registration provisions account and enables immediate login")
    void selfRegistrationAndImmediateLogin() throws Exception {
        String testEmail = "chathura.perera@example.com";

        mockMvc.perform(post("/register")
                        .param("firstName", "Chathura")
                        .param("lastName", "Perera")
                        .param("email", testEmail)
                        .param("phone", "+94 77 555 9999")
                        .param("gender", "Male")
                        .param("password", "newpassword123")
                        .param("confirmPassword", "newpassword123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        assertTrue(userService.existsByUsername(testEmail));

        // Now test logging in with the newly registered user
        mockMvc.perform(formLogin("/login")
                        .user(testEmail)
                        .password("newpassword123"))
                .andExpect(authenticated().withUsername(testEmail).withRoles("MEMBER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/portal"));
    }

    @Test
    @DisplayName("Attempting to register with an existing Gmail/email (case-insensitive) is rejected with validation error")
    void duplicateEmailRegistrationIsBlocked() throws Exception {
        // Sophia Miller is already seeded with sophia.miller@example.com
        mockMvc.perform(post("/register")
                        .param("firstName", "Imposter")
                        .param("lastName", "User")
                        .param("email", "SOPHIA.MILLER@EXAMPLE.COM") // uppercase check
                        .param("phone", "+94 77 111 2222")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .with(csrf()))
                .andExpect(status().isOk()) // form redisplayed with validation error
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registration", "email"));
    }

    @Test
    @WithMockUser(username = "admin@apexfit.com", roles = {"ADMIN"})
    @DisplayName("Admin attempting to save member with duplicate email is rejected with error")
    void adminCannotAddMemberWithDuplicateEmail() throws Exception {
        mockMvc.perform(post("/members/save")
                        .param("firstName", "Duplicate")
                        .param("lastName", "Member")
                        .param("email", "james.anderson@example.com") // Already exists
                        .param("phone", "+1-555-9999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().attributeHasFieldErrors("member", "email"));
    }

    @Test
    @WithMockUser(username = "sophia.miller@example.com", roles = {"MEMBER"})
    @DisplayName("Member is blocked with 403 Forbidden when trying to view class attendee roster (/classes/view/**)")
    void memberCannotAccessClassRoster() throws Exception {
        mockMvc.perform(get("/classes/view/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "elena.rostova@apexfit.com", roles = {"TRAINER"})
    @DisplayName("Trainer can view class attendee roster (/classes/view/**)")
    void trainerCanAccessClassRoster() throws Exception {
        mockMvc.perform(get("/classes/view/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("classes/view"));
    }

    @Test
    @WithMockUser(username = "sophia.miller@example.com", roles = {"MEMBER"})
    @DisplayName("Member cannot cancel another member's class booking (IDOR protection)")
    void memberCannotCancelOtherMemberClassBooking() throws Exception {
        // In DataInitializer, booking 3 belongs to James Anderson (m1), not Sophia Miller (m2)
        mockMvc.perform(post("/classes/cancel-booking/3")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", org.hamcrest.Matchers.containsString("Unauthorized")));
    }

    @Test
    @WithMockUser(username = "sophia.miller@example.com", roles = {"MEMBER"})
    @DisplayName("Member cannot cancel another member's equipment reservation (IDOR protection)")
    void memberCannotCancelOtherMemberResourceBooking() throws Exception {
        // Sophia Miller tries to cancel booking 2 which belongs to James Anderson (m1)
        mockMvc.perform(post("/member/resources/cancel/2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", org.hamcrest.Matchers.containsString("Unauthorized")));
    }

    @Test
    @WithMockUser(username = "sophia.miller@example.com", roles = {"MEMBER"})
    @DisplayName("Member can cancel their own equipment reservation")
    void memberCanCancelOwnResourceBooking() throws Exception {
        // Sophia Miller cancels booking 1 which belongs to her (m2)
        mockMvc.perform(post("/member/resources/cancel/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", org.hamcrest.Matchers.containsString("cancelled successfully")));
    }
}
