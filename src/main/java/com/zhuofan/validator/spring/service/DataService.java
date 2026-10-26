package com.zhuofan.validator.spring.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuofan.validator.spring.model.SubstationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Component
public class DataService {

    @Value("${data.json.substation}")
    private String substationJsonPath;

    /**
     * {@link ResourceLoader::getResource}
     */
    private final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

    public List<SubstationModel> findAllSubstation() {
        Gson gson = new Gson();
        try {
            Resource resource = pathMatchingResourcePatternResolver.getResource(substationJsonPath);
            return gson.fromJson(new InputStreamReader(resource.getInputStream()), new TypeToken<List<SubstationModel>>(){}.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
