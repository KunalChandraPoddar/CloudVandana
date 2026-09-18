package com.cloudvandana.assignment.controller;

import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/salesforce")
@CrossOrigin(origins = "http://localhost:5173")
public class SalesforceCrudController {

    private final RestTemplate restTemplate;

    public SalesforceCrudController() {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        this.restTemplate = new RestTemplate(factory);
    }

    private String getFieldsForObject(String objectName) {
        return switch (objectName.toLowerCase()) {
            case "account" -> "Id,Name,Phone,Industry,Website,Type";
            case "opportunity" -> "Id,Name,StageName,Amount,CloseDate,Type";
            case "lead" -> "Id,Name,Company,Status,Email,Phone";
            case "contact" -> "Id,FirstName,LastName,Email,Phone,Department";
            case "case" -> "Id,CaseNumber,Subject,Status,Priority,Origin";
            default -> throw new IllegalArgumentException("Unsupported Salesforce target schema entity: " + objectName);
        };
    }

    @GetMapping("/{objectName}")
    public ResponseEntity<String> getRecords(
            @PathVariable String objectName,
            @RequestParam(defaultValue = "0") int offset) {

        String token = AuthController.getAccessToken();
        String instanceUrl = AuthController.getInstanceUrl();

        if (token.isEmpty() || instanceUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Active verification session absent.");
        }

        String fields = getFieldsForObject(objectName);
        String query = "SELECT " + fields + " FROM " + objectName + " LIMIT 10 OFFSET " + offset;
        String url = instanceUrl + "/services/data/v58.0/query/?q=" + query;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{objectName}")
    public ResponseEntity<String> createRecord(
            @PathVariable String objectName,
            @RequestBody String jsonPayload) {

        String token = AuthController.getAccessToken();
        String instanceUrl = AuthController.getInstanceUrl();

        if (token == null || token.isEmpty() ||
                instanceUrl == null || instanceUrl.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Salesforce authentication session is missing.");
        }

        String url = instanceUrl
                + "/services/data/v58.0/sobjects/"
                + objectName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpStatusCodeException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PatchMapping("/{objectName}/{id}")
    public ResponseEntity<String> updateRecord(
            @PathVariable String objectName,
            @PathVariable String id,
            @RequestBody String jsonPayload) {

        String token = AuthController.getAccessToken();
        String instanceUrl = AuthController.getInstanceUrl();

        if (token == null || token.isEmpty() ||
                instanceUrl == null || instanceUrl.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Salesforce authentication session is missing.\"}");
        }

        String url = instanceUrl
                + "/services/data/v58.0/sobjects/"
                + objectName
                + "/"
                + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    String.class);

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpStatusCodeException e) {

            System.out.println(
                    "Salesforce UPDATE error: "
                            + e.getResponseBodyAsString());

            return ResponseEntity
                    .status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\""
                            + e.getMessage()
                                    .replace("\"", "\\\"")
                            + "\"}");
        }
    }

    @DeleteMapping("/{objectName}/{id}")
    public ResponseEntity<String> deleteRecord(
            @PathVariable String objectName,
            @PathVariable String id) {

        String token = AuthController.getAccessToken();
        String instanceUrl = AuthController.getInstanceUrl();

        if (token == null || token.isEmpty() ||
                instanceUrl == null || instanceUrl.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Salesforce authentication session is missing.");
        }

        String url = instanceUrl
                + "/services/data/v58.0/sobjects/"
                + objectName
                + "/"
                + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String.class);

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpStatusCodeException e) {

            // Very important:
            // Salesforce normally returns useful JSON here.
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

}
