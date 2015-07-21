package no.finansportalen.freecalc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.junit.Before;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public abstract class FreeCalcTest<P, R> {
    
    protected ArrayList<P> products;
    private TypeToken<ArrayList<P>> productArrayTypeToken;
    private TypeToken<ArrayList<R>> resultArrayTypeToken;
    private String calcPackageName;
    private String productFileName;

    
    
    
    protected FreeCalcTest(TypeToken<ArrayList<P>> productArrayTypeToken, TypeToken<ArrayList<R>> resultArrayTypeToken, String calcPackageName, String productFileName) {
        this.productArrayTypeToken = productArrayTypeToken;
        this.resultArrayTypeToken = resultArrayTypeToken;
        this.productFileName = productFileName;
        this.calcPackageName = calcPackageName;
    }

    @Before
    public void setup() {
        products = getProducts();
    }

    protected String getFromFile(String file) {
        
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder(1000000);
        
        try {
            
            String line;
            
            reader = new BufferedReader(new InputStreamReader(FreeCalcTest.class.getResourceAsStream("/" + file), "UTF-8"));
 
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
 
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return stringBuilder.toString();
    }
    

    protected ArrayList<P> getProducts() {
        
        String productJSON = getFromFile(calcPackageName + "/" + productFileName);
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Integer.class, new FreeCalcTest.IntegerDeserializer());
        gsonBuilder.registerTypeAdapter(Double.class, new FreeCalcTest.DoubleDeserializer());
        Gson gson = gsonBuilder.create();
        
        Type type = productArrayTypeToken.getType();
        
        return gson.fromJson(productJSON, type);
    }
    
    
    protected ArrayList<R> getExpectedResults(String resultFileName) {
        
        String productJSON = getFromFile(calcPackageName + "/" + resultFileName);
        
        Gson gson = new Gson();
        
        Type type = resultArrayTypeToken.getType();
        
        return gson.fromJson(productJSON, type);
    }
    
    
    private static class DoubleDeserializer implements JsonDeserializer<Double> {
        
        @Override
        public Double deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            try {
                String text = json.getAsString();
    
                text = text.replace(" ", "").replace(",", ".");
                
                return Double.valueOf(text);
            } catch (Exception e) {
                return null;
            }
        }
        
    }


    private static class IntegerDeserializer implements JsonDeserializer<Integer> {
    
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            try {
                String text = json.getAsString();
    
                text = text.replace(" ", "");
    
                return Integer.valueOf(text);
            } catch (Exception e) {
                return null;
            }
        }
        
    }
}
