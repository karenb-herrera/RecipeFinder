package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class RecipeFinder implements RequestHandler<Map<String, String>, String>
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleRequest(Map<String, String> event, Context context)
    {
        String ingredient1 = event.get("ingredient1");
        String ingredient2 = event.get("ingredient2");
        String ingredientsTotal = ingredient1 + " " + ingredient2;

        //Response
        Map<String, Object> response = Map.of(
            "statusCode", 200,
            "body", "Hello from Lambda," + ingredientsTotal
        );

        //Convert response to JSON
        try{
            return objectMapper.writeValueAsString(response);
        } catch (Exception e){
            return "{\"statusCode\":500, \"body\":\"Error\"}";
        }
    }
}
