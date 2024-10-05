package com.nheatyon.searchoffersbot.closeable.amazon;

import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoriesMapper {

    private static CategoriesMapper instance;
    @Getter private final Map<String, String> categories = new HashMap<>();
    @Getter private final List<String> translations = new ArrayList<>();

    public static CategoriesMapper getInstance() {
        if (instance == null) {
            instance = new CategoriesMapper();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getCategories(ConfigurationManager<?, String> config) {
        if (categories.isEmpty()) {
            JSONObject categoriesObject = config.read("categories");
            categoriesObject.forEach((k, v) -> categories.put(k.toString(), v.toString()));
        }
        return categories;
    }

    public List<String> getCategoriesTranslations(ConfigurationManager<?, String> config) {
        if (translations.isEmpty()) {
            getCategories(config).forEach((k, v) -> translations.add(v));
        }
        return translations;
    }
}
