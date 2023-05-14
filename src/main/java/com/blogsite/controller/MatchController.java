//package com.blogsite.controller;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.ui.Model;
//
//import com.blogsite.Service.CricApiService;
//
//import java.io.IOException;
//
//@Controller
//public class MatchController {
//
//    private final CricApiService cricApiService;
//
//    @Autowired
//    public MatchController(CricApiService cricApiService) {
//        this.cricApiService = cricApiService;
//    }
//
//    @GetMapping("/matches")
//    public String getMatches(Model model) throws IOException {
//        String matchesJson = cricApiService.getCurrentMatches();
//        
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode matchesNode = objectMapper.readTree(matchesJson);
//        System.out.println("Hello");
//        System.out.println("Matches "+matchesJson);
//        model.addAttribute("matches", matchesNode);
//        return "matches";
//    }
//
//}
