package ru.csc.bdse.kv.node;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.csc.bdse.util.Constants;
import ru.csc.bdse.util.Encoding;
import ru.csc.bdse.util.Require;

import java.util.Optional;
import java.util.Set;

/**
 * Http client for storage unit.
 *
 * @author semkagtn
 */
public class KeyValueApiHttpClient implements InternalKeyValueApi {
    private final String baseUrl;
    private final RestTemplate rest = new RestTemplate();

    private static final ParameterizedTypeReference<byte[]> BYTES_ARRAY_TYPE =
            new ParameterizedTypeReference<byte[]>() { };
    private static final ParameterizedTypeReference<Set<String>> STRING_SET_TYPE =
            new ParameterizedTypeReference<Set<String>>() { };
    private static final ParameterizedTypeReference<Set<NodeInfo>> NODEINFO_SET_TYPE =
            new ParameterizedTypeReference<Set<NodeInfo>>() { };

    public KeyValueApiHttpClient(final String baseUrl) {
        Require.nonEmpty(baseUrl, "empty base url");
        this.baseUrl = baseUrl;
    }

    @Override
    public void put(String key, byte[] value) {
        Require.nonNull(key, "null key");
        Require.nonNull(value, "null value");

        final String url = baseUrl + "/key-value/" + key;
        final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.PUT, value, BYTES_ARRAY_TYPE);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public Optional<byte[]> get(String key) {
        Require.nonNull(key, "null key");

        final String url = baseUrl + "/key-value/" + key;
        final ResponseEntity<byte[]> responseEntity = request(
                url, HttpMethod.GET,
                Constants.EMPTY_BYTE_ARRAY,
                BYTES_ARRAY_TYPE
        );
        switch (responseEntity.getStatusCode()) {
            case OK:
                return Optional.of(responseEntity.getBody());
            case NOT_FOUND:
                return Optional.empty();
            default:
                throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public Set<String> getKeys(String prefix) {
        Require.nonNull(prefix, "null prefix");

        final String url = baseUrl + "/key-value?prefix=" + Encoding.encodeUrl(prefix);
        final ResponseEntity<Set<String>> responseEntity = request(
                url,
                HttpMethod.GET,
                Constants.EMPTY_BYTE_ARRAY,
                STRING_SET_TYPE
        );
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public void delete(String key) {
        Require.nonNull(key, "null key");

        final String url = baseUrl + "/key-value/" + key;
        final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, Constants.EMPTY_BYTE_ARRAY);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public Set<NodeInfo> getInfo() {
        final String url = baseUrl + "/info";
        final ResponseEntity<Set<NodeInfo>> responseEntity = request(
                url, HttpMethod.GET,
                Constants.EMPTY_BYTE_ARRAY,
                NODEINFO_SET_TYPE
        );
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public void action(String node, NodeAction action) {
        Require.nonNull(node, "null node name");

        final String url = baseUrl + "/action/" + node + "/" + action.toString();
        final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.POST, Constants.EMPTY_BYTE_ARRAY);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    @Override
    public void deleteAll() {
        final String url = baseUrl + "/deleteAll";
        final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, Constants.EMPTY_BYTE_ARRAY);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response error: " + responseEntity);
        }
    }

    private <T> ResponseEntity<byte[]> request(String url, HttpMethod method, T body) {
        return request(url, method, body, BYTES_ARRAY_TYPE);
    }

    private <T, U> ResponseEntity<U> request(String url, HttpMethod method, T body, ParameterizedTypeReference<U> type) {
        try {
            return rest.exchange(url, method, new HttpEntity<>(body), type);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>((U) null, e.getStatusCode());
        }
    }
}
