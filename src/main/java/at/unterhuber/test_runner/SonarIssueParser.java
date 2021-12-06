package at.unterhuber.test_runner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SonarIssueParser {
    private static final String URL = "https://sonarcloud.io/api/issues/search?";

    private final String projectName;

    public SonarIssueParser(String projectName) {
        this.projectName = projectName;
    }

    private List<SonarIssue> parse(JSONObject object) {
        List<SonarIssue> issues = new ArrayList<>();
        JSONArray issuesJson = (JSONArray) object.get("issues");
        for (Object issueObject : issuesJson) {
            JSONObject issue = (JSONObject) issueObject;
            String component = (String) issue.get("component");
            // remove project name from component because there is only 1 project
            component = component.split(":")[1];
            SonarIssue.Type type = SonarIssue.Type.valueOf((String) issue.get("type"));
            SonarIssue.Severity severity = SonarIssue.Severity.valueOf((String) issue.get("severity"));
            issues.add(new SonarIssue(component, type, severity));
        }
        return issues;
    }

    private String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    private JSONObject parseJson(String content) throws ParseException {
        JSONParser parser = new JSONParser();
        Object resultObject = parser.parse(content);
        return (JSONObject)resultObject;
    }

    public Map<String, List<SonarIssue>> getIssuesByClass() throws IOException, URISyntaxException, InterruptedException, ParseException, ExecutionException {
        int page = 0, totalPages = 1;
        List<CompletableFuture<HttpResponse<String>>> asyncResponses = new ArrayList<>();
        List<SonarIssue> issues = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        while (totalPages > page) {
            HttpRequest request = buildRequest(page);
            if (page == 0) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject obj = parseJson(response.body());
                totalPages = getTotalPages(obj);
                issues.addAll(parse(obj));
            } else {
                asyncResponses.add(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
            }
            page++;
        }

        for (CompletableFuture<HttpResponse<String>> asyncResponse : asyncResponses) {
            HttpResponse<String> response = asyncResponse.get();
            issues.addAll(parse(parseJson(response.body())));
        }
        Map<String, List<SonarIssue>> classIssues = new HashMap<>();
        issues.forEach(issue -> {
            classIssues.putIfAbsent(issue.component, new ArrayList<>());
            classIssues.get(issue.component).add(issue);
        });
        return classIssues;
    }

    private int getTotalPages(JSONObject obj) {
        int totalPages;
        JSONObject paging = (JSONObject) obj.get("paging");
        long total = (long) paging.get("total");
        long pageSize = (long) paging.get("pageSize");
        totalPages = (int) Math.ceil((double) total / pageSize);
        return totalPages;
    }

    private HttpRequest buildRequest(int page) throws MalformedURLException, URISyntaxException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("componentKeys", projectName);
        parameters.put("statuses", "OPEN");
        // parameters.put("ps", "2");
        parameters.put("p", String.valueOf(page + 1)); // 1-indexed
        URL url = new URL(URL + getParamsString(parameters));
        return HttpRequest.newBuilder().GET().uri(url.toURI()).build();
    }
}
