package com.blogsite.controller;


import com.blogsite.Model.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
//Date formatter
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Controller
public class LiveScoreController {
    private final String API_URL = "YOUR_API_URl";

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/live-score")
    public String getLiveMatches(Model model) {
        // Make API request and retrieve JSON response
//    	RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(API_URL, String.class);
        System.out.println(response);
        // Extract necessary data and pass it to the template
        List<Match> liveMatches = extractLiveMatches(response);
        Collections.sort(liveMatches, (match1, match2) -> match2.getTeam2().compareTo(match1.getTeam2()));

        model.addAttribute("liveMatches", liveMatches);

        return "matches";
    }

    private List<Match> extractLiveMatches(String jsonResponse) {
        List<Match> liveMatches = new ArrayList<>();

        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse))) {
            JsonObject jsonObject = jsonReader.readObject();

            // Check if the "matches" array is present
            if (jsonObject.containsKey("data")) {
                JsonArray matchesArray = jsonObject.getJsonArray("data");

                for (int i = 0; i < matchesArray.size(); i++) {
                    JsonObject matchObject = matchesArray.getJsonObject(i);

                    // Extract relevant fields from the JSON object
//                  String matchId = matchObject.getString("id");
                    /* String team2 = matchObject.getString("series_id"); */
                    String date = matchObject.getString("date");
                    String matchName = matchObject.getString("name");
                    String venue = matchObject.getString("venue");
                    String matchStatus = matchObject.getString("status");
                    
                    String [] matchNameFilter = matchName.split(",");
                    String [] venueFilter = venue.split(",");
                    
                    LocalDate newDate = LocalDate.parse(date);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    String formattedDate = newDate.format(outputFormatter);

                    Match match = new Match(matchNameFilter[0], formattedDate, venueFilter[venueFilter.length-1], matchStatus);
                    liveMatches.add(match);
                }
            } else {
                System.out.println("No live matches found.");
            }
        } catch (JsonException e) {
            // Handle any JSON parsing exceptions
            System.out.println("Error parsing JSON: " + e.getMessage());
        }

        return liveMatches;
    }

   }



