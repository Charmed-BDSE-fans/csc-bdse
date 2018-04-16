package ru.csc.bdse.kv.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.*;
import java.util.function.Function;

/**
 * Http client for storage unit.
 *
 * @author semkagtn
 */
public class KeyValueApiHttpClient implements InternalKeyValueApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueApiHttpClient.class);

    private final List<String> baseUrls;
    private final RestTemplate rest = new RestTemplate();

    private static final ParameterizedTypeReference<byte[]> BYTES_ARRAY_TYPE =
            new ParameterizedTypeReference<byte[]>() { };
    private static final ParameterizedTypeReference<Set<String>> STRING_SET_TYPE =
            new ParameterizedTypeReference<Set<String>>() { };
    private static final ParameterizedTypeReference<Set<NodeInfo>> NODEINFO_SET_TYPE =
            new ParameterizedTypeReference<Set<NodeInfo>>() { };

    public KeyValueApiHttpClient(final String baseUrl) {
        this(Collections.singletonList(baseUrl));
    }

    public KeyValueApiHttpClient(List<String> baseUrls) {
        this.baseUrls = baseUrls;
    }

    private <T> T tryUrls(Function<String, T> block) {
        for (String baseUrl: baseUrls) {
            try {
                return block.apply(baseUrl);
            } catch (RequestFailedException e) {
                LOGGER.warn("Failed to request url %s", e);
            }
        }
        throw new RuntimeException("Failed to process request, every url failed");
    }

    @Override
    public void put(String key, byte[] value) {
        Require.nonNull(key, "null key");
        Require.nonNull(value, "null value");

        tryUrls(baseUrl -> {
            final String url = baseUrl + "/key-value/" + key;
            final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.PUT, value, BYTES_ARRAY_TYPE);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RequestFailedException("Response error: " + responseEntity);
            }
            return null;
        });
    }

    @Override
    public Optional<byte[]> get(String key) {
        Require.nonNull(key, "null key");

        return tryUrls(baseUrl -> {
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
                    throw new RequestFailedException("Response error: " + responseEntity);
            }
        });
    }

    @Override
    public Set<String> getKeys(String prefix) {
        Require.nonNull(prefix, "null prefix");

        return tryUrls(baseUrl -> {
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
                throw new RequestFailedException("Response error: " + responseEntity);
            }
        });
    }

    @Override
    public void delete(String key) {
        Require.nonNull(key, "null key");

        tryUrls(baseUrl -> {
            final String url = baseUrl + "/key-value/" + key;
            final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, Constants.EMPTY_BYTE_ARRAY);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RequestFailedException("Response error: " + responseEntity);
            }
            return null;
        });
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return tryUrls(baseUrl -> {
            final String url = baseUrl + "/info";
            final ResponseEntity<Set<NodeInfo>> responseEntity = request(
                    url, HttpMethod.GET,
                    Constants.EMPTY_BYTE_ARRAY,
                    NODEINFO_SET_TYPE
            );
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else {
                throw new RequestFailedException("Response error: " + responseEntity);
            }
        });
    }

    @Override
    public void action(String node, NodeAction action) {
        Require.nonNull(node, "null node name");

        tryUrls(baseUrl -> {
            final String url = baseUrl + "/action/" + node + "/" + action.toString();
            final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.POST, Constants.EMPTY_BYTE_ARRAY);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RequestFailedException("Response error: " + responseEntity);
            }
            return null;
        });
    }

    @Override
    public void deleteAll() {
        tryUrls(baseUrl -> {
            final String url = baseUrl + "/deleteAll";
            final ResponseEntity<byte[]> responseEntity = request(url, HttpMethod.DELETE, Constants.EMPTY_BYTE_ARRAY);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Response error: " + responseEntity);
            }
            return null;
        });
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

    private static class RequestFailedException extends RuntimeException {
        public RequestFailedException(String message) {
            super(message);
        }
    }
}
