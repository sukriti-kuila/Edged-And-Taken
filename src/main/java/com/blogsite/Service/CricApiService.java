//package com.blogsite.Service;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.HttpClients;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse.BodyHandlers;
//
//@Service
//public class CricApiService {
//    public String getCricScore() throws IOException, InterruptedException {
//        var url = "https://api.cricapi.com/v1/cricScore?apikey=9a9687f7-b84c-4a7e-8d49-65c89250d971";
//        var request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
//        var client = HttpClient.newBuilder().build();
//        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
//        return response.body();
//    }
//}
