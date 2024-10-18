package com.example;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class RecipeFinder implements RequestHandler<Map<String, Object>, String>
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamoDB dynamoDB;
    private final Table table;

    public RecipeFinder(){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        dynamoDB = new DynamoDB(client);
        table = dynamoDB.getTable("RecipesTable");
    }

    @Override
    public String handleRequest(Map<String, Object> event, Context context)
    {
        String operation = (String) event.get("operation");

        if("addRecipe".equals(operation)){
            return addRecipe(event);
        } else if("getRecipes".equals(operation)){
            return getRecipes(event);
        }
        
        return "{\"statusCode\": 400, \"body\": \"Invalid operation\"}";
    }

    private String addRecipe(Map<String, Object> event){
        //Get elements
        String name = (String) event.get("name");
        List<String> ingredients = (List<String>) event.get("ingredientsList");
        String process = (String) event.get("process");

        //Store them in DynamoDB
        table.putItem(new Item()
            .withPrimaryKey("RecipeName", name)
            .withList("Ingredients", ingredients)
            .withString("Process", process));
        
        return createResponse(200, "Recipe has been added successfully: " + name);
    }

    private String getRecipes(Map<String, Object> event){
        String ingredient1 = (String) event.get("ingredient1");
        String ingredient2 = (String) event.get("ingredient2");
        
        List<Item> foundRecipes = new ArrayList<>();
        ItemCollection<ScanOutcome> items = table.scan();
        
        for (Item item : items){
            List<String> ingredients = item.getList("Ingredients");
            if (ingredients.contains(ingredient1) && ingredients.contains(ingredient2)) {
                foundRecipes.add(item);
            }
        }

        return createResponse(200, foundRecipes);
    }

    private String createResponse(int statusCode, Object body) {
        try {
            return objectMapper.writeValueAsString(new Response(statusCode, body));
        } catch (Exception e) {
            return "{\"statusCode\": 500, \"body\": \"Internal Server Error\"}";
        }
    }

    // Response class from JSON format
    static class Response{
        public int statusCode;
        public Object body;

        public Response(int statusCode, Object body){
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
