package com.cloudvandana.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = {
        "http://localhost:5173",
        "https://cloud-vandana-one.vercel.app"
    }
)
@RequiredArgsConstructor
public class AuthController {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${salesforce.client-id}")
    private String clientId;

    @Value("${salesforce.client-secret}")
    private String clientSecret;

    @Value("${salesforce.redirect-uri}")
    private String redirectUri;

    @Value("${salesforce.login-url}")
    private String loginUrl;

    private static String accessToken = "";
    private static String instanceUrl = "";

    private static final String CODE_VERIFIER = "CloudVandanaASEProjectHandshakeChallengeSecureString1234567890";
    private static final String CODE_CHALLENGE = generateCodeChallenge(CODE_VERIFIER);

    @GetMapping("/login")
    public RedirectView loginToSalesforce() {
        String authUrl = loginUrl + "/services/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&code_challenge=" + CODE_CHALLENGE
                + "&code_challenge_method=S256";
        return new RedirectView(authUrl);
    }

    @GetMapping("/callback")
    public RedirectView handleCallback(@RequestParam("code") String code) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = loginUrl + "/services/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code_verifier", CODE_VERIFIER); // Verifies the payload signature matching the challenge hash

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                accessToken = (String) body.get("access_token");
                instanceUrl = (String) body.get("instance_url");
                
                return new RedirectView(frontendUrl + "/dashboard?auth=success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RedirectView(frontendUrl + "/?auth=failed");
    }

    private static String generateCodeChallenge(String verifier) {
        try {
            byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            return verifier;
        }
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static String getInstanceUrl() {
        return instanceUrl;
    }
}