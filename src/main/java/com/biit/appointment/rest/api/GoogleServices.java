package com.biit.appointment.rest.api;

import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.google.client.GoogleCalendarController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external-calendar-credentials/google")
public class GoogleServices {

    private final GoogleCalendarController googleCalendarController;

    public GoogleServices(GoogleCalendarController googleCalendarController) {
        this.googleCalendarController = googleCalendarController;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets the credentials from a user on a specific provider.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/code/{code}/state/{state}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ExternalCalendarCredentialsDTO getGoogleAuth(@Parameter(description = "Google Auth code.", required = true)
                                                        @PathVariable(name = "code") String code,
                                                        @Parameter(description = "State that matches the code of the requester.", required = true)
                                                        @PathVariable(name = "state") String state,
                                                        Authentication authentication,
                                                        HttpServletRequest request) {
        return googleCalendarController.exchangeCodeForToken(authentication.getName(), code, state);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets the credentials from a user on a specific provider.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/code", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ExternalCalendarCredentialsDTO getGoogleAuthByRequestParams(@RequestParam(name = "code") String code,
                                                        @RequestParam(name = "state") String state,
                                                        Authentication authentication,
                                                        HttpServletRequest request) {
        return googleCalendarController.exchangeCodeForToken(authentication.getName(), code, state);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Deletes the credencial from the current logged in user.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(value = "/code", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteGoogleAuth(Authentication authentication, HttpServletRequest request) {
        googleCalendarController.deleteToken(authentication.getName());
    }
}
