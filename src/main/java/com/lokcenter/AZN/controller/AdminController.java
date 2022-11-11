package com.lokcenter.AZN.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lokcenter.AZN.helper.ds.Search;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

/**
 * Admin Controller
 *
 * @version 1.14 2022-11-11
 */
@Controller
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final WebClient webClient;

    /**
     * Check if user is admin
     * @param roles roles collection
     * @return true if admin
     * @throws Exception if user is not an admin
     */
    private boolean isAdmin(Collection<? extends GrantedAuthority> roles) throws Exception {
        if (roles.isEmpty()) {
            throw new Exception("No Role");

        } else if (Search.binarySearch(roles.stream()
                .toList().stream().map(Objects::toString).toList(), "ROLE_Admin") == -1) {
            throw new Exception("Not Authorized");
        }

        return true;
    }

    /**
     * Return Admin Panel for administration
     *
     * @param model The model to give our frond-end data
     * @return a html page
     */
    @GetMapping
    String getAdminPanel(Model model, @RegisteredOAuth2AuthorizedClient("userwebapp")
    OAuth2AuthorizedClient authorizedClient, Authentication authentication) throws Exception {
        var roles = authentication.getAuthorities();

        if (roles.isEmpty()) {
            throw new Exception("No Role");
        }

        String role = roles.toArray()[0].toString();


        // make get request and get data
        Mono<String> body = webClient.method(HttpMethod.GET)
                .uri("/admin")
                .body(Mono.just(Map.of("role", role)), Map.class)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve().bodyToMono(String.class);

        if (body.block() != null) {
            JsonNode jsonData = null;
            // get data and try to convert

            try {
                jsonData = new ObjectMapper().readTree(body.block());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            model.addAttribute("data", jsonData);
            System.out.println(jsonData);
        }

        return "adminPanel";
    }

    /**
     * Request Year Plan Data from userId
     *
     * @param userId requested userId
     *
     * @return Json Data
     */
    @GetMapping("/yearplan")
    @ResponseBody
    String getYearPlanOfUser(@RegisteredOAuth2AuthorizedClient("userwebapp")
                             OAuth2AuthorizedClient authorizedClient, Authentication authentication,
                             @RequestParam(name = "userId") String userId) throws Exception {

        if (isAdmin(authentication.getAuthorities())) {
            Mono<String> res = webClient.get().uri(String.format("/admin/years?userid=%s", userId)).
                    attributes(oauth2AuthorizedClient(authorizedClient)).retrieve().bodyToMono(String.class);

            // check if there is any data
            if (res.block() != null) {
                return new ObjectMapper().readTree(res.block()).toPrettyString();
            }
        }

        return "";
    }

    /**
     * Request Request Data from userId
     *
     * @return Json Data
     */
    @GetMapping("/requests")
    @ResponseBody
    String getRequestsOfUser(@RegisteredOAuth2AuthorizedClient("userwebapp")
                             OAuth2AuthorizedClient authorizedClient, Authentication authentication,
                             @RequestParam(name = "userId") String userId) throws Exception {

        if (isAdmin(authentication.getAuthorities())) {
            Mono<String> res = webClient.get().uri(String.format("/admin/requests?userId=%s", userId)).
                    attributes(oauth2AuthorizedClient(authorizedClient)).retrieve().bodyToMono(String.class);

            // check if there is any data
            if (res.block() != null) {
                return new ObjectMapper().readTree(res.block()).toPrettyString();
            }
        }

        return "";
    }

    /**
     * Delete request from user by request id
     * @param requestId id from request
     *
     * @return true or false
     */
    @PutMapping("/requests/delete")
    @ResponseBody
    Boolean deleteRequestFromUser(@RegisteredOAuth2AuthorizedClient("userwebapp")
                                OAuth2AuthorizedClient authorizedClient, Authentication authentication,
                                @RequestParam(name = "requestId") String requestId){

        // TODO: ...

        return true;
    }

    /**
     * Accept request from user by request id
     * @param requestId id from request to accept
     *
     * @return true or false
     */
    @PutMapping("/requests/accept")
    @ResponseBody
    Boolean AcceptRequestFromUser(@RegisteredOAuth2AuthorizedClient("userwebapp")
                                OAuth2AuthorizedClient authorizedClient, Authentication authentication,
                                @RequestParam(name = "requestId") String requestId){

        // TODO: ...
        return true;
    }

    @GetMapping("/monthplan")
    String getMonthPlan(Model model, @RegisteredOAuth2AuthorizedClient("userwebapp")
                        OAuth2AuthorizedClient authorizedClient, Authentication authentication) {
        return "adminMonthPlan";
    }
}