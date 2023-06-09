package com.lokcenter.AZN.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lokcenter.AZN.helper.ControllerHelper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
@AllArgsConstructor
@RequestMapping("/monthplan")
public class MonthPlanController {
    private final WebClient webClient;
    @GetMapping()
    String getMonthPlan(Model model,
                        @RequestParam(name = "month", required = false) String month,
                        @RequestParam(name = "year", required = false) String year,
                        @RegisteredOAuth2AuthorizedClient("userwebapp")
                        OAuth2AuthorizedClient authorizedClient, Authentication authentication) throws Exception {

        String month2;
        // generate date if no date
        if (year == null || month == null) {
            Calendar calendar = Calendar.getInstance();

            month = String.valueOf(calendar.get(Calendar.MONTH));
            month2 = month;
            year = String.valueOf(calendar.get(Calendar.YEAR));
        } else {
            month2 = month;
        }

        month =  String.valueOf(Integer.parseInt(month) +1);


        String role = ControllerHelper.getUserOrAdminRole(authentication);

        // make get request and get data
        String finalMonth = month;
        String finalYear = year;
        Mono<String> res = ControllerHelper.makeRequest(() ->
                webClient.get().uri(String.format("/monthplan?month=%s&year=%s&role=%s", finalMonth, finalYear, role)).
                attributes(oauth2AuthorizedClient(authorizedClient)).retrieve().bodyToMono(String.class)).get();

        Mono<String> resStatus = ControllerHelper.makeRequest(() ->webClient.method(HttpMethod.GET).uri("monthplan/status").
                attributes(oauth2AuthorizedClient(authorizedClient))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // send
                .body(Mono.just(Map.of("month", finalMonth, "year", finalYear)), Map.class)
                .retrieve().bodyToMono(String.class)).get();

        // get soll time
        Mono<String> resSoll = ControllerHelper.makeRequest(()
                -> webClient.get().uri(String.format("/worktime/sollMonth?role=%s&month=%s&year=%s",
                        role, month2, finalYear)).
                attributes(oauth2AuthorizedClient(authorizedClient)).retrieve().bodyToMono(String.class)).get();

        // check if there is any data
        if (res.block() != null && resStatus.block() != null && resSoll.block() != null) {
            JsonNode jsonData = new ObjectMapper().readTree(res.block());
            JsonNode jsonDataStatus = new ObjectMapper().readTree(resStatus.block());
            JsonNode jsonSoll = new ObjectMapper().readTree(resSoll.block());

            model.addAttribute("data", jsonData);
            model.addAttribute("status", jsonDataStatus);
            model.addAttribute("title", "Monatsübersicht");
            model.addAttribute("dataSoll", jsonSoll);

            return "monthPlan";
        }

        throw new Exception("Bad request");
    }

    /**
     * Submit Monthplan
     */
    @ResponseBody
    @PutMapping("/submit")
    Boolean SubmitMonthPlan(@RequestBody Map<String, Object> payload, @RegisteredOAuth2AuthorizedClient("userwebapp")
    OAuth2AuthorizedClient authorizedClient) {
        return Boolean.TRUE.equals(this.webClient
                .put()
                .uri("/monthplan/submit")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                // send
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // send
                .body(Mono.just(payload), Map.class)
                .retrieve()
                // res type
                .bodyToMono(Boolean.class)
                .block());
    }

    /**
     * get all messages by user
     * @param month
     * @throws JsonProcessingException
     */
    @ResponseBody
    @GetMapping("/messages")
    String getMessage(@RequestParam(name = "month", required = true) String month,
                       @RequestParam(name = "year", required = true) String year,
                       @RequestParam(name = "type", required = true) String type,
                      @RegisteredOAuth2AuthorizedClient("userwebapp")
                      OAuth2AuthorizedClient authorizedClient ) throws JsonProcessingException {

        Mono<String> res = webClient.method(HttpMethod.GET).uri("/monthplan/messages").
                attributes(oauth2AuthorizedClient(authorizedClient))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // send
                .body(Mono.just(Map.of("year", year, "month", month, "type", type)), Map.class)
                .retrieve().bodyToMono(String.class);

        // check if there is any data
        if (res.block() != null) {
            return new ObjectMapper().readTree(res.block()).toPrettyString();
        }

        return "";
    }

    @PutMapping("/message")
    @ResponseBody
    Boolean deleteMessageByMessageId(@RequestParam(name = "messageId") String messageId,
                                     @RegisteredOAuth2AuthorizedClient("userwebapp") OAuth2AuthorizedClient authorizedClient  ) {
        return Boolean.TRUE.equals(this.webClient
                .delete()
                .uri(String.format("/monthplan/message?messageId=%s", messageId))
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // send
                .retrieve()
                // res type
                .bodyToMono(Boolean.class)
                .block());
    }

    /**
     * Delete all messages by user, month, year
     * @return true or false
     */
    @PutMapping("/messages/delete")
    @ResponseBody
    Boolean deleteAllMessagesByMonthAndYear(@RequestBody Map<String, String> payload,
                                     @RegisteredOAuth2AuthorizedClient("userwebapp") OAuth2AuthorizedClient authorizedClient  ) {

        System.out.println(payload);
        return Boolean.TRUE.equals(this.webClient
                .method(HttpMethod.DELETE)
                .uri("/monthplan/messages/delete")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(payload), payload.getClass())
                // send
                .retrieve()
                // res type
                .bodyToMono(Boolean.class)
                .block());
    }
}
